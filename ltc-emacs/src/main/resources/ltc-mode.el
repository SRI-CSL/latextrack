;;; ltc-mode.el --- user interface for LaTeX Track Changes (LTC) 
;;
;; Copyright (C) 2009 - 2012 SRI International
;;
;; Author: Linda Briesemeister <linda.briesemeister@sri.com>
;;    Sam Owre <sam.owre@sri.com>
;; Maintainer: Linda Briesemeister <linda.briesemeister@sri.com>
;; Created: 20 May 2010
;; URL: ${url}
;;
;; This file is not part of GNU Emacs.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;; Commentary:
;;
;; LaTeX Track Changes (LTC) allows collaborators on a version-controlled 
;; LaTeX writing project to view and query changes in the .tex documents.
;;
;; To install:
;;   copy or link to this file to a directory in Emacs' load-path (view with C-h v load-path)
;;   and add this line to your .emacs or preferences file:
;;     (autoload 'ltc-mode "ltc-mode" "" t)
;; To run:
;;   (make sure that LTC server is running)
;;   M-x ltc-mode to toggle
;;   M-x ltc-update to run update
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as
;; published by the Free Software Foundation, either version 3 of the 
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public 
;; License along with this program.  If not, see
;; <http://www.gnu.org/licenses/gpl-3.0.html>.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;; Code:

(require 'xml-rpc)
(require 'versions)
(require 'cl) ; for set operations
(defconst min-xml-rpc-version "1.6.8.3" "minimum version requirement for xml-rpc mode")

;;; ----------------------------------------------------------------------------
;;; constants
;;;

(defconst on_disk "on disk" "revision ID used for files with modifications on disk.")
(defconst modified "modified" "revision ID used for files with modifications in buffer.")
(defconst edit-insert "INSERT" "edit key for insertions")
(defconst edit-remove "REMOVE" "edit key for removals")
(defconst edit-delete "DELETE" "edit key for deletions")

;;; ----------------------------------------------------------------------------
;;; minor mode declarations
;;;

(defgroup ltc nil
  "Latex Track Changes mode."
  :version "${project.version}"
  :link '(url-link "${url}")
  :tag "LTC"
  :prefix "ltc-"
  :group 'tex)

(defcustom ltc-port 7777
  "Port on localhost used for communication with an LTC server."
  :type '(integer)
  :tag "Port"
  :group 'ltc)

(defcustom ltc-command-prefix (kbd "C-c '")
  "Prefix key to use for commands in LTC mode."
  :type 'string
  :group 'ltc)

(defface ltc-addition
  '((t (:underline t)))
  "Face used for marking up additions (foreground color will be set at run-time)."
  :group 'ltc)

(defface ltc-deletion
  '((t (:strike-through t :inverse-video t)))
  "Face used for marking up deletions (foreground color will be set at run-time)."
  :group 'ltc)

;;; keeping track of filtering settings

(defvar ltc-show-deletions t "Show deletions")
(defvar ltc-show-small t "Show small changes")
(defvar ltc-show-preamble nil "Show changes in preamble")
(defvar ltc-show-comments nil "Show changes in comments")
(defvar ltc-show-commands t "Show changes in commands")
(defvar ltc-condense-authors nil "Condense authors in file history")
(defvar ltc-allow-similar-colors nil "Allow similar colors for different authors")
(defconst show-map
  '((ltc-show-deletions . "DELETIONS")
    (ltc-show-small . "SMALL")
    (ltc-show-preamble . "PREAMBLE")
    (ltc-show-commands . "COMMANDS")
    (ltc-show-comments . "COMMENTS"))
  "define mappings from custom options to show/hide changes to the string used in API call.")
(defconst other-settings-map
  '((ltc-condense-authors . "COLLAPSE_AUTHORS")
    (ltc-allow-similar-colors . "ALLOW_SIMILAR_COLORS"))
  "define mapping from other boolean settings to the string used in API call.")
;;; the following filters are buffer-local:
(defvar ltc-limiting-authors nil "Set of authors to limit commit graph.")
(defvar ltc-limiting-date nil "Date to limit commit graph.")
(defvar ltc-limiting-rev nil "Revision to limit commit graph.")
(defconst limit-vars 
  '(ltc-limiting-authors ltc-limiting-date ltc-limiting-rev)
  "all variables that are affecting buffer-local limiting")
(mapc 'make-variable-buffer-local limit-vars)

(defvar session-id nil "current LTC session ID.")
(make-variable-buffer-local 'session-id)

(defvar commit-graph nil 
  "Commit graph structure containing one element per commit: 
(id date (author-name author email) message isActive (list-of-parents) (list-of-children))")
(make-variable-buffer-local 'commit-graph)

(defvar self nil "3-tuple of current author when session is initialized.")
(make-variable-buffer-local 'self)

(defvar orig-coding-sytem nil "Keep information on original coding system.")
(make-variable-buffer-local 'orig-coding-sytem)

;; Defining key maps with prefix
(defvar ltc-prefix-map nil "LTC mode prefix keymap.")
(define-prefix-command 'ltc-prefix-map)
(define-key ltc-prefix-map (kbd "u") 'ltc-update)
(define-key ltc-prefix-map (kbd "q") 'ltc-mode)
(define-key ltc-prefix-map (kbd "la") 'ltc-limit-authors)
(define-key ltc-prefix-map (kbd "ld") 'ltc-limit-date)
(define-key ltc-prefix-map (kbd "lr") 'ltc-limit-rev)
(define-key ltc-prefix-map (kbd "z") 'ltc-undo-change)
(define-key ltc-prefix-map (kbd ">") 'ltc-next-change)
(define-key ltc-prefix-map (kbd "<") 'ltc-prev-change)
(define-key ltc-prefix-map (kbd "c") 'ltc-set-color)
(define-key ltc-prefix-map (kbd "b") 'ltc-bug-report)
;; Bind command `ltc-prefix-map' to `ltc-command-prefix' in `ltc-mode-map':
(defvar ltc-mode-map (make-sparse-keymap) "LTC mode keymap.")
(define-key ltc-mode-map ltc-command-prefix 'ltc-prefix-map)

(define-minor-mode ltc-mode
  "Toggle LTC mode.

     With no argument, this command toggles the mode.
     Non-null prefix argument turns on the mode.
     Null prefix argument turns off the mode.
     
     When LTC (Latex Track Changes) mode is enabled, 
     it shows all changes in the history of the current
     buffer file as obtained from the underlying git repository.
     
     The following keys are bound in this minor mode:

\\{ltc-mode-map}"
  :init-value nil
  :lighter " LTC"
  :keymap ltc-mode-map
  :group 'ltc
  (if ltc-mode
      (progn
	;; Mode was turned on
	(ltc-mode-start))
    ;; Mode was turned off
    (ltc-mode-stop)
    )
  )

;; adding LTC to context menu of latex mode
(if (fboundp 'add-minor-mode)
    (progn
      (put 'ltc-mode :included '(eq major-mode 'latex-mode))
      (put 'ltc-mode :menu-tag "LTC Mode")
      (add-minor-mode 'ltc-mode
		      " LTC" 
		      ltc-mode-map
		      nil
		      'ltc-mode))
  (unless (assoc 'ltc-mode minor-mode-alist)
    (push '(ltc-mode " LTC") minor-mode-alist))
  (unless (assoc 'ltc-mode minor-mode-map-alist)
    (push (cons 'ltc-mode ltc-mode-map) minor-mode-map-alist)))

(easy-menu-define
  ltc-minor-mode-menu ltc-mode-map "Menu for LTC"
 '("LTC"
   ["Update buffer" ltc-update]
   "--"
   "FILTERING"
   ("Show/Hide" :filter (lambda (ignored)  
    			  (mapcar (lambda (show-var) (vector  
						      (documentation-property show-var 'variable-documentation) 
						      (list 'list
						       (list 'setq show-var (list 'not show-var)) 
						       (list 'ltc-method-call 
							     "set_bool_pref" 
							     (list 'cdr (list 'assoc (list 'quote show-var) 'show-map)) 
							     show-var) 
						       '(ltc-update)) 
						      :style 'toggle :selected show-var :key-sequence nil)
				    ) (mapcar 'car show-map))
    			  ))
   ("Limit by"
    ["Set of authors..." ltc-limit-authors
     :label (concat "Set of authors" 
		    (if ltc-limiting-authors
			;; show a list of initials
			(concat " ["
				(mapconcat (lambda (name) (mapconcat (lambda (word) (substring word 0 1)) 
								     (split-string (upcase name)) "")) 
					   (mapcar 'car ltc-limiting-authors) " ")
				"]...")
		      "..."))]
    ["Start at date..." ltc-limit-date
     :label (concat "Start at date" 
      		    (if (or (not ltc-limiting-date) (string= "" ltc-limiting-date))
      			"..."
      		      (concat " [" ltc-limiting-date "]...")))]
    ["Start at revision..." ltc-limit-rev 
     :label (concat "Start at revision" 
		    (if (or (not ltc-limiting-rev) (string= "" ltc-limiting-rev))
			"..."
		      (concat " [" (shorten 7 ltc-limiting-rev) "]...")))]
    )
   ("Other settings"
    ["Condense authors" 
     (list 
      (setq ltc-condense-authors (not ltc-condense-authors))
      (ltc-method-call "set_bool_pref" (cdr (assoc 'ltc-condense-authors other-settings-map)) ltc-condense-authors)
      (ltc-update)
      )
     :style toggle :selected ltc-condense-authors :key-sequence nil]
    ["Allow similar colors" 
     (list 
      (setq ltc-allow-similar-colors (not ltc-allow-similar-colors))
      (ltc-method-call "set_bool_pref" (cdr (assoc 'ltc-allow-similar-colors other-settings-map)) ltc-allow-similar-colors)
      (ltc-update)
      )
     :style toggle :selected ltc-allow-similar-colors :key-sequence nil]
    )
   "--"
   "CHANGES"
   ["Undo" ltc-undo-change]
   ["Move to previous" ltc-prev-change]
   ["Move to next" ltc-next-change]
   "--"
   ["Set author color..." ltc-set-color]
   "--"
   ["Bug report..." ltc-bug-report]
   "--"
   ["Turn LTC off" ltc-mode]
   "--"
   ))

;;; ----------------------------------------------------------------------------
;;; mode implementation
;;;

(defvar ltc-info-buffer "" "string for temp buffer with LTC info")
(make-variable-buffer-local 'ltc-info-buffer)

(defun ltc-mode-start ()
  "start LTC mode"
  (if (not (buffer-file-name))
      (message "Error while starting LTC: Buffer has no file name associated")
    ;; else-forms:
    (message "Starting LTC mode for file \"%s\"..." (buffer-file-name))
    (if (not (condition-case err 
		 (progn
		   ;; testing xml-rpc version
		   (message "Using `xml-rpc' package version: %s" xml-rpc-version)
		   (and (version< xml-rpc-version min-xml-rpc-version)
			(error "`ltc-mode' requires `xml-rpc' package v%s or later" min-xml-rpc-version))
		   ;; init session with file name
		   (setq session-id (ltc-method-call "init_session" (buffer-file-name))))
	       ;; handling any initialization errors
	       ('error 
		(message "Error while initializing session: %s" (error-message-string err))
		nil))) ; 
	(ltc-mode 0) ; an error occurred: toggle mode off again
      ;; else-forms: initialization of session was successful:
      (message "LTC session ID = %d" session-id)
      (setq ltc-info-buffer (concat "LTC info (session " (number-to-string session-id) ")"))
      ;; update boolean settings
      (mapc (lambda (show-var) 
	      (set show-var (ltc-method-call "get_bool_pref" (cdr (assoc show-var show-map))))) (mapcar 'car show-map))
      (setq ltc-condense-authors (ltc-method-call "get_bool_pref" (cdr (assoc 'ltc-condense-authors other-settings-map))))
      (setq ltc-allow-similar-colors (ltc-method-call "get_bool_pref" (cdr (assoc 'ltc-allow-similar-colors other-settings-map))))
      (mapc (lambda (var) 
	      (set var 'nil)) 
	    limit-vars)
      (setq orig-coding-sytem buffer-file-coding-system)
      (setq buffer-file-coding-system 'no-conversion)
      (font-lock-mode 0) ; turn-off latex font-lock mode
      (add-hook 'write-file-functions 'ltc-hook-before-save nil t) ; add (local) hook to intercept saving to file
      (add-hook 'kill-buffer-hook 'ltc-hook-before-kill nil t) ; add hook to intercept closing buffer
      ;; run first update
      (ltc-update)))
  ) ;ltc-mode-start

(defun ltc-mode-stop ()
  "stop LTC mode"  
  (setq commit-graph nil) ; reset commit graph
  (setq self nil) ; reset information about current author
  (ltc-remove-edit-hooks) ; remove (local) hooks to capture user's edits
  (remove-hook 'write-file-functions 'ltc-hook-before-save t) ; remove (local) hook to intercept saving to file
  (remove-hook 'kill-buffer-hook 'ltc-hook-before-kill t) ; remove hook to intercept closing buffer
  ;; close session and obtain text for buffer without track changes
  (if session-id
      (progn
	(message "Stopping LTC mode for file \"%s\"..." (buffer-file-name))
	(condition-case err 
	    (let ((map (ltc-method-call "close_session" session-id 
					(list :base64 (base64-encode-string (buffer-string) t))
					(compile-deletions)
					(1- (point))))
		  (old-buffer-modified-p (buffer-modified-p))) ; maintain modified flag
	      ;; replace text in buffer with return value from closing session
	      (erase-buffer)
	      (insert (base64-decode-string (nth 1 (cdr (assoc-string "text64" map))))) 
	      (goto-char (1+ (cdr (assoc-string "caret" map)))) ; Emacs starts counting from 1!
	      (set-buffer-modified-p old-buffer-modified-p))
	  ('error 
	   (message "Error while closing session (reverting to text from file): %s" (error-message-string err))
	   ;; replace buffer with text from file
	   (erase-buffer)
	   (insert-file-contents (buffer-file-name))
	   (set-buffer-modified-p nil)
	   nil))
	(setq session-id nil)))
  ;; close any open temp info buffer 
  (when (setq b (get-buffer ltc-info-buffer))
    (delete-windows-on b t) ; kill window containing temp buffer
    (kill-buffer b)) ; kill temp buffer
  (setq ltc-info-buffer "")
  (font-lock-mode 1) ; turn latex font-lock mode back on
  (setq buffer-file-coding-system orig-coding-sytem)
  (setq orig-coding-sytem nil)
  ) ;ltc-mode-stop

(defun ltc-update ()
  "updating changes for current session"
  (interactive)
  (message "Starting LTC update...") ; TODO: make cursor turn into wait symbol
  (if ltc-mode
      (let* ((map (ltc-method-call "get_changes" session-id 
				   (buffer-modified-p) 
				   (list :base64 (base64-encode-string (buffer-string) t))
				   (compile-deletions)
				   (1- (point))))
	     (old-buffer-modified-p (buffer-modified-p)) ; maintain modified flag
	     (newtext (base64-decode-string (nth 1 (cdr (assoc-string "text64" map)))))
	     ;; build color table for this update: int -> color name
	     (color-table (mapcar (lambda (four-tuple)
				    (cons (string-to-number (nth 0 four-tuple)) (nth 3 four-tuple)))
				  (cdr (assoc-string "authors" map))))
	     (styles (cdr (assoc-string "styles" map)))
	     (revisions (cdr (assoc-string "revs" map)))
	     (commits (ltc-method-call "get_commits" session-id)) ; list of 6-tuple strings
	     )
	(setq self (ltc-method-call "get_self" session-id)) ; get current author and color
	(message "LTC updates received") ; TODO: change cursor back (or later?)
	(ltc-remove-edit-hooks) ; remove (local) hooks to capture user's edits temporarily
	;; replace text in buffer and update cursor position
	(erase-buffer)
	(insert newtext)
	(goto-char (1+ (cdr (assoc-string "caret" map)))) ; Emacs starts counting from 1!
	;; apply styles to new buffer
	(if (and styles (car styles))  ; sometimes STYLES = '(nil)
	    (mapc (lambda (style)
		    (let* ((revision (nth (nth 4 style) revisions))
			   ;; now find revision in commits for extracting date:
			   (date (catch 'findID
				   (mapc (lambda (commit) 
					   (if (string= (car commit) revision) 
					       (throw 'findID (nth 4 commit)))) ; found ID, so stop loop
					 commits)
				   nil))) ; ID was not found
		      (set-text-properties (1+ (car style)) (1+ (nth 1 style)) ; Emacs starts counting from 1!
					   (list
					    'face 
					    (list 
					     (if (= '1 (nth 2 style)) 'ltc-addition 'ltc-deletion) 
					     (list
					      :foreground (cdr (assoc (nth 3 style) color-table))))
					    'help-echo
					    (concat "rev: " (shorten 8 revision) 
						    (if date (concat "\ndate: " date) nil))
					    'ltc-change-rev  ; only revision info for undoing changes
					    (shorten 8 revision)))
		      )) styles))
	(ltc-add-edit-hooks) ; add (local) hooks to capture user's edits
	;; update commit graph in temp info buffer
	(setq commit-graph (init-commit-graph commits revisions))
	(update-info-buffer)
	(set-buffer-modified-p old-buffer-modified-p) ; restore modification flag
	))
  ) ;ltc-update

;;; --- capture save, close, and TODO: save-as (set-visited-file-name) operations 

(defun ltc-hook-before-save ()
  "Let LTC base system save the correct text to file."
  (if (not ltc-mode)
      nil
;    (message "Before saving file %s for session %d" (buffer-file-name) session-id)
    (ltc-method-call "save_file" session-id
 		     (list :base64 (base64-encode-string (buffer-string) t))
		     (compile-deletions))
    (clear-visited-file-modtime) ; prevent Emacs from complaining about modtime diff's as we are writing file from Java
    (set-buffer-modified-p nil) ; reset modification flag
    ;; manipulate commit graph: if at least one entry replace first ID with "on disk"
    (when commit-graph 
      (let ((head (car commit-graph)))
	(when head
	  (setcar head on_disk)
	  (setcar commit-graph head)
	  (update-info-buffer))))
    t)) ; prevents actual saving in Emacs as we have already written the file

(defun ltc-hook-before-kill ()
  "Close session before killing."
;  (message "Before killing buffer in session %d for file %s" session-id (buffer-file-name))
  (if ltc-mode (ltc-mode 0)) ; turn LTC mode off (includes closing session)
  nil)

;;; --- limiting functions: authors, date, revision

(defun ltc-limit-authors (authors)
  "Set or reset limiting AUTHORS for commit graph.  If empty list, no limit is used.  Updates automatically unless user chooses to quit input."
  (interactive
   (if ltc-mode
       (let ((completion-list (cons "" (mapcar 'author-to-string (mapcar 'caddr commit-graph))))
	     (author-list nil)
	     (n 1)
	     (old-spc (lookup-key minibuffer-local-completion-map " "))
	     )
	 (setq completion-ignore-case t)
	 ;; allow space in input, so modify minibuffer-local-completion-map temporarily
	 (define-key minibuffer-local-completion-map " " nil)
	 (while 
	     (let ((author (completing-read (format "Enter author [%d] or empty to stop: " n) completion-list nil nil)))
		  (setq n (+ 1 n))
		  (if (string< "" author)
		      (setq author-list (cons author author-list)))))
	 ;; reset minibuffer-local-completion-map binding
	 (define-key minibuffer-local-completion-map " " old-spc)
	 (list (mapcar 'string-to-author (nreverse author-list))))
     '(nil))) ; sets authors = nil
  (when ltc-mode
    (setq ltc-limiting-authors (ltc-method-call "set_limited_authors" 
						session-id 
						(if authors (list :array authors) [])))
    (message "Limiting Authors: %S" ltc-limiting-authors)
    (ltc-update)))

(defun ltc-select-date (event)
  "Select date for limiting in mouse event.  Updates automatically."
  (interactive "e")
  (let* ((window (posn-window (event-end event)))
	 (pos (posn-point (event-end event)))
	 (date (get-text-property pos 'action)) ; obtain action parameters from text properties
	 )
    (select-window parent-window)
    (set-limiting-date date)))

(defun ltc-limit-date (date)
  "Set or reset limiting DATE for commit graph.  If empty string, no limit is used.  Updates automatically unless user chooses to quit input."
  (interactive
   (if ltc-mode
       (let ((dates (mapcar 'cadr (cdr commit-graph))))
	 (list (completing-read (concat "Limit by date (eg. \""
					(substring (car dates) 0 2)
					"\" and TAB completion; empty to reset): ") 
				(cons "" dates)
				nil nil)))
     '(nil))) ; sets date = nil
  (when date
    (set-limiting-date date)))

(defun set-limiting-date (date)   
  "Set limiting DATE and update."
  (when ltc-mode
    (setq ltc-limiting-date (ltc-method-call "set_limited_date" session-id date))
    (ltc-update)))

(defun ltc-select-rev (event)
  "Select revision for limiting in mouse event.  Updates automatically."
  (interactive "e")
  (let* ((window (posn-window (event-end event)))
	 (pos (posn-point (event-end event)))
	 (rev (get-text-property pos 'action)) ; obtain action parameters from text properties
	 )
    (select-window parent-window)
    (set-limiting-rev rev)))

(defun ltc-limit-rev (rev)
  "Set or reset limiting REV for commit graph.  If empty string, no limit is used.  Offers currently known IDs from commit graph for completion.  Updates automatically unless user chooses to quit input."
  (interactive
   (if ltc-mode
       (let ((revs (mapcar (apply-partially 'shorten 7) (mapcar 'car (cdr commit-graph)))))
	 (list (completing-read (concat "Limit by revision ("
					(if (> (length (car revs)) 1)
					    (concat "eg. \"" (substring (car revs) 0 2)	"\" and TAB completion; "))
					"empty to reset): ")
				(cons "" revs)
				nil nil)))
     '(nil))) ; sets rev = nil
  (when rev
    (set-limiting-rev rev)))

(defun set-limiting-rev (rev)   
  "Set limiting REV and update."
  (when ltc-mode
    (setq ltc-limiting-rev (ltc-method-call "set_limited_rev" session-id rev))
    (ltc-update)))

;;; --- jump to next or previous change

(defun ltc-next-change ()
  "Jump to beginning of next change (if any)."
  (interactive)
  (move-to-change (point) 1))

(defun ltc-prev-change ()
  "Jump to end of previous change (if any)."
  (interactive)
  (move-to-change (point) -1))

(defun move-to-change (index dir)
  "Move from INDEX to next or previous change indicated by DIR."
  (if (= dir 0)
      (error "Cannot move to change for DIR = 0"))
  (setq index (if (> dir 0) index (1- index))) ; if looking for previous change start with previous char
  (if (is-buf-border index dir)
      (message "No change until %s of document found" (if (> dir 0) "end" "beginning"))
    (setq currface (get-text-property index 'face))
    ;; repeat..until loop: go through all characters with the *same* face
    (while (progn
	     (setq index (+ index dir)) ; increment or decrement index
	     ;; the "end-test" is the last item in progn:
	     (and (not (is-buf-border index dir))
		  (equal currface
			 (get-text-property index 'face)))))
    (if (is-buf-border index dir)
	(message "No change until %s of document found" (if (> dir 0) "end" "beginning"))
      ;; else-form: now find the next char face with addition or deletion
      (while (and (not (is-buf-border index dir))
		  (and (not (member 'ltc-addition (get-text-property index 'face)))
		       (not (member 'ltc-deletion (get-text-property index 'face)))))
	(setq index (+ index dir))) ; increment or decrement index
      (if (is-buf-border index dir)
	  (message "No change until %s of document found" (if (> dir 0) "end" "beginning"))
	;; else-form: index denotes position of change
	(setq index (if (> dir 0) index (1+ index))) ; increment position if looking for end of previous change
	(message "%s change found @ %d" (if (> dir 0) "Next" "Previous") index)
	(goto-char index)))))

(defun is-buf-border (index dir)
  "Whether given INDEX is at the beginning or end of buffer determined by DIR."
  (if (> dir 0)
      (not (< index (point-max))) ; index >= (point-max)
    (< index (point-min))) ; index < (point-min)
  )

;;; --- create bug report

(defun ltc-bug-report (directory msg includeSrc)
  "Create a bug report with MSG and use DIRECTORY.  If successful, will print message in mini-buffer with the created file name."
  (interactive 
   (if ltc-mode
       (list
	(read-directory-name "Directory where to save bug report files (created if not exist): ")
	(read-string "Explanation: ")
	(y-or-n-p "Include repository? "))x1 
     '(nil nil nil))) ; sets directory = nil and msg = nil and includeSrc = nil
  (when directory
    (setq file (ltc-method-call "create_bug_report" session-id msg includeSrc (expand-file-name directory)))
    (message "Created bug report at %s (please email to lilalinda@users.sourceforge.net)" file)
    ))


;;; --- info buffer functions

(defun init-commit-graph (commits &optional ids)
  "Init commit graph from given COMMITS.  If a list of IDS is given (optional), those will be used to determine the activation state.  In this case, the first entry of the list is left untouched if it exists.  The author in the first entry in the list will be the current self in either case.

Each entry in the commit graph that is returned, contains a list with the following elements:
 (ID date (name email) message isActive color (column (incoming-columns) (outgoing-columns) (passing-columns)))
"
  (if ltc-mode
    ;; build commit graph 
    (let ((authors (mapcar (lambda (v) (cons (list (car v) (cadr v)) (nth 2 v))) 
			   (ltc-method-call "get_authors" session-id)))
	  (parents-alist nil)
	  (children-alist nil)
	  (circle-alist '((0 0))) ; index -> circle column, start with 0 -> 0
	  (incoming-alist nil)    ; index -> set of incoming columns
	  (outgoing-alist nil)    ; index -> set of outgoing columns
	  (passing-alist nil)     ; index -> set of passing columns
	  (current-columns nil)   ; set of current columns
	  )
      ;; NOTE: this is modeled after CommitTableModel.init() in LTC Editor code!
      ;; 1) build up map : ID -> index in commits
      (setq commit-map (make-hash-table :test 'equal :size (length commits)))
      (setq counter -1) ; keep track of index in list of commits
      (mapc (lambda (raw-commit)
	      (let ((id (car raw-commit))
		    (author (list (nth 2 raw-commit) (nth 3 raw-commit))))
		; side-effect: build up map
		(setq counter (+ 1 counter))
		(puthash (car raw-commit) counter commit-map) ; revision -> index in list of commits
		))
	    commits)
      ;; 2) calculate index -> parents indices and index -> children indices (in alists)
      (mapc (lambda (raw-commit)
       	      (let* ((id (car raw-commit))
       		     (index (gethash id commit-map))
		     (parents (nth 5 raw-commit))
       		     (parent-ids (if parents (split-string parents) nil))
       		     (parent-indices nil) ; will hold list of parent indices
       		     )
       		; go through list of parents and find indices; also update children
       		(setq parent-indices
       		      (mapcar (lambda (parent-id)
       				(let* ((parent-index (gethash parent-id commit-map))
				       (children-indices (cdr (assoc parent-index children-alist)))
       				       )
       				  ; side-effect: add index to list of children
				  (setq children-indices (cons index children-indices))
				  (setq children-alist
					(cons
					 (cons parent-index children-indices)
					 children-alist))
       				  parent-index))
       			      parent-ids))
		(setq parents-alist 
		      (cons 
		       (cons index parent-indices)
		       parents-alist))
       		))
       	    commits)
      ;; 3) calculate graph column locations:
      (mapc (lambda (raw-commit)
	      (let* ((id (car raw-commit))
       		     (index (gethash id commit-map))
		     (circle (cadr (assoc index circle-alist)))
		     (incoming (cdr (assoc index incoming-alist)))
		     (parent-indices (cdr (assoc index parents-alist)))
		     )
		; update current columns based on incoming set
		(setq current-columns (set-difference current-columns incoming)) ; remove incoming
		(setq current-columns (cons circle current-columns)) ; add circle column
		; passing columns = current columns \ {circle column}
		(setq passing-alist
		      (cons
		       (cons index (set-difference current-columns (list circle)))
		       passing-alist))
		; determine circle columns of parents:
		(mapc (lambda (parent-index)
			(let* ((outgoing (cdr (assoc index outgoing-alist)))
			       (passing (cdr (assoc index passing-alist)))
			       (union (union outgoing passing))
			       (lowest (get-lowest-not-in union))
			       (parent-circle (cadr (assoc parent-index circle-alist)))
			       )
			  (if parent-circle
			      ; parent circle was set: move to the left?
			      (when (< lowest parent-circle)
				(setq current-columns (delete parent-circle current-columns)) ; remove old parent circle
				(setq circle-alist
				      (cons
				       (list parent-index lowest)
				       circle-alist))) ; set parent circle to lowest
			    ; parent circle not yet set: use lowest
			    (setq circle-alist
				  (cons
				   (list parent-index lowest)
				   circle-alist)))
			  ; maintain current columns: 
			  ; add latest parent circle (as one-element list) to it
			  (setq parent-circle (cdr (assoc parent-index circle-alist)))
			  (setq current-columns (union parent-circle current-columns))
			  ; update incoming columns of parent:
			  ; add latest parent circle to current incoming
			  (setq incoming-alist
				(cons
				 (cons parent-index (union 
						     parent-circle 
						     (cdr (assoc parent-index incoming-alist))))
				 incoming-alist))
			  ; update outgoing columns of index:
			  ; add latest parent circle to current outgoing
			  (setq outgoing-alist
				(cons
				 (cons index (union parent-circle outgoing))
				 outgoing-alist))
			  ))
		      parent-indices)
		; maintain current columns if there was a merge:
		(if (not (member circle (cdr (assoc index outgoing-alist))))
		    (setq current-columns (delete circle current-columns)))
		))
	    commits)
      ;; 4) prepend first row and assemble graph structure in list as return value:
      ;; each row:
      ;;  (id date (name email) msg isActive color graph)
      ;;  where GRAPH is:
      ;;   (column incoming-columns outgoing-columns passing-columns)
      (cons 
       ;; first row is item for self:
       ;; - if optional IDS given, look at last item and keep it if MODIFIED or ON_DISK, else 
       ;; - if prior graph not empty, keep first ID, otherwise "" 
       (list
	;; determine ID of first item:
	(if ids
	    (concat "" (car (member (car (last ids)) (list modified on_disk))))
	  (if commit-graph (caar commit-graph) "")) ; keep first ID if prior graph exists
	;; set rest of first item to empty and self:
	"" (list (car self) (nth 1 self)) "" t (nth 2 self) '(0 nil nil nil))
       ;; assemble rest of graph from list of commits and other data structures above:
       (mapcar (lambda (raw-commit)
		 (let* ((id (car raw-commit))
			(author (list (nth 2 raw-commit) (nth 3 raw-commit)))
			(index (gethash id commit-map)))
		   (list 
		    id ; revision
		    (nth 4 raw-commit) ; date
		    author ; (author-name author-email)
		    ; limit message to first full stop, question or exclamation mark (followed by space) or newline (if any):
		    (car (split-string (nth 1 raw-commit) "\\([.?!][:space:]+\\)\\|\n" t)) ; shortened message 
		    (or (not ids) (not (not (member id ids)))) ; isActive: if no IDS, then t, otherwise look up 
		    (cdr (assoc author authors)) ; color of author
		    (list ; graph:
		     (cadr (assoc index circle-alist)) ; circle column
		     (cdr (assoc index incoming-alist)) ; incoming columns
		     (cdr (assoc index outgoing-alist)) ; outgoing columns
		     (cdr (assoc index passing-alist)) ; passing columns
		     ))))
	       commits)))
    nil) ; LTC session not valid: return NIL
  ) ;init-commit-graph

(defun get-lowest-not-in (s)
  "Get the lowest number that is not in sorted set S, starting from 0."
  (setq n 0)
  (while (member n s)
    (setq n (1+ n)))
  n)

(defun update-info-buffer ()
  "Update output in info buffer from current commit graph."
  (when (string< "" ltc-info-buffer)
    (let ((prior-height 7) ; default height of info buffer = 7
	  (prior-window-start 1) ; default start of window of info-buffer
	  (info-window (get-buffer-window ltc-info-buffer))
	  )
      (when info-window ; if prior info buffer existed, remember current height and start of window
	(setq prior-height (window-height info-window))
	(setq prior-window-start (window-start info-window))
	)
      (with-output-to-temp-buffer ltc-info-buffer
	(let* ((old-buffer (current-buffer))
	       (old-window (get-buffer-window (current-buffer)))
	       (temp-buffer (get-buffer-create ltc-info-buffer))
	       (temp-window (get-buffer-window temp-buffer))
	       (temp-output (pretty-print-commit-graph)))
	  (set-buffer temp-buffer)
	  (set (make-variable-buffer-local 'parent-window) old-window)
	  (insert temp-output)
	  (setq-default line-spacing nil) ; no extra height between lines in this buffer
	  (setq-default truncate-lines t) ; no line wrap
	  (setq truncate-partial-width-windows nil) ; no truncate for vertically-split windows
	  (set-buffer old-buffer)
	  ))
      ;; TODO: hide cursor (using Cursor Parameters)?
      (with-selected-window (get-buffer-window ltc-info-buffer)
	(shrink-window (- (window-height) prior-height)) ; adjust height of temp info buffer
	(set-window-start (selected-window) prior-window-start nil) ; adjust position visible of temp info buffer
	)
      )))

(defun draw-branches (front back circle columns max-circle passing 
			    left-circle-string left-column-string 
			    middle-circle-string middle-column-string
			    right-circle-string right-column-string)
  "Create string representation of branches in between commits.  

FRONT and BACK are strings to concat to the front and back of the resulting string with branches.  

CIRCLE denotes the current position of the commit node.  COLUMNS is either the set of incoming or outgoing columns of the node depending on whether we draw branches above or below the commit.  MAX-CIRCLE is the largest column found in the commit graph and denotes the width of the resulting branch string.  PASSING is the list of passing lines.

The remaining arguments *-STRING denote the string representation of the characters needed for the left, middle, and right columns in the span that are either incoming or outgoing in nature."
  (setq graph-fmt "") ; build-up string for graph format
  ; calculate span from union of circle column and columns:
  (setq left-col (apply 'min (union (list circle) columns)))
  (setq right-col (apply 'max (union (list circle) columns)))
  (dotimes (col (1+ max-circle)) ; go through all columns
    (setq c " ") ; default character for column is space
    (if (and (< col left-col)      ; left of any joints
	     (member col passing)) ; and passing column
	(setq c (string #x2502)))
    (when (= col left-col) ; at the left column of span?
      (if (= col circle) 
	  ; circle column
	  (setq c (if (member circle columns) (string #x251c) left-circle-string))
	; not circle column: must be in columns, though!
	(setq c (if (member col passing) (string #x251c) left-column-string))
	))
    (when (and (> col right-col) (< col right-col)) ; middle of span?
      (if (= col circle) 
	  ; circle column in middle
	  (setq c (if (member circle columns) (string #x253c) middle-circle-string))
	; column in middle but not circle:
	(setq c (string #x2500)) ; default is "-"
	(if (member col columns) (setq c middle-column-string)) 
	(if (member col passing) (setq c (string #x253c))) ; cross
	))
    (when (= col right-col) ; at the right column of span?
      (if (= col circle)
	  ; circle column
	  (setq c (if (member circle columns) (string #x2524) right-circle-string))
	; not circle column: must be in columns, though!
	(setq c (if (member col passing) (string #x2524) right-column-string))
	))
    (if (and (> col right-col)     ; right of any joints
	     (member col passing)) ; and passing column
	(setq c (string #x2502)))
    (setq graph-fmt (concat graph-fmt c)))
  (concat front graph-fmt back) ; add front and back around line with characters
  ) ;draw-branches

(defun pretty-print-commit-graph ()
  "Create string representation with text properties from current commit graph."
  (if commit-graph
      (let ((rev-map (make-sparse-keymap))
	    (date-map (make-sparse-keymap))
	    (author-map (make-sparse-keymap))
	    ; Unicode characters from box-drawing set:
	    (commit-top-bottom #x255e) ;#x251d)
	    (commit-top #x2558) ;#x2515)
	    (commit-bottom #x2552) ;#x250d)
	    )
	(define-key rev-map (kbd "<mouse-1>") 'ltc-select-rev)
	(define-key date-map (kbd "<mouse-1>") 'ltc-select-date)
	(define-key author-map (kbd "<mouse-1>") 'ltc-select-color)
	;; first loop: 
	;; calculate biggest circle column and longest author string for padding to next column
	(setq max-circle 0)
	(setq max-author 0)	      
	(mapc (lambda (commit)
		(let ((circle (caar (last commit)))
		      (author (length (author-to-string (nth 2 commit))))
		      )
		  ; column
		  (if (> circle max-circle) 
		      (setq max-circle circle))
		  ; length of author string
		  (if (> author max-author)
		      (setq max-author author))
		  ))
	      commit-graph)
	;; second loop: build return value
	(mapconcat (function (lambda (commit) 
			       (let* ((is-active (nth 4 commit))
				      (author (author-to-string (nth 2 commit)))
				      (disabledcolor "#7f7f7f")
				      (facevalue (if is-active nil (list :foreground disabledcolor)))
				      (id (shorten 8 (car commit)))
				      (graph (nth 6 commit))
				      (circle (car graph))
				      (incoming (nth 1 graph))
				      (outgoing (nth 2 graph))
				      (passing (nth 3 graph))
				      (passing-before (delq nil (mapcar (lambda (x) (if (< x circle) x)) passing)))
				      (passing-after (delq nil (mapcar (lambda (x) (if (> x circle) x)) passing)))
				      )
				 (setq before-seq (progn 
						    (setq n -1) 
						    (mapcar (lambda (x) (setq n (1+ n)) (+ n x)) (make-list circle 0))))
				 (setq after-seq (progn 
						    (setq n circle) 
						    (mapcar (lambda (x) (setq n (1+ n)) (+ n x)) (make-list (- max-circle circle) 0))))
				 (concat
				  (if (set-difference incoming (list circle)) ; extra line to draw incoming branches?
				      (draw-branches " " "\n" circle incoming max-circle passing
						     (string #x250c) (string #x2514)
						     (string #x252c) (string #x2534)
						     (string #x2510) (string #x2518)))
				  ; all graph characters in default foreground color:
				  ;  this line only contains the passing lines and the commit column (circle)
				  (apply 'format 
					 (concat
					  " "
					  (mapconcat (lambda (x) (if (member x passing-before) "%c" " ")) before-seq "")
					  "%c"
					  (mapconcat (lambda (x) (if (member x passing-after) "%c" " ")) after-seq "")
					  " ")
					 (append (make-list (length passing-before) #x2502) ; passing lines before
						 ; circle column: depends on incoming and outgoing:
						 (list (if (member circle incoming)
							   (if outgoing commit-top-bottom commit-top)
							 (if outgoing commit-bottom ?\s)))
						 (make-list (length passing-after) #x2502))) ; passing lines after
				  (propertize 
				   (format "%8s" id) ; short revision
				   'mouse-face 'highlight
				   'help-echo "mouse-1: limit by this start revision"
				   'keymap rev-map
				   'action id
				   'face facevalue)
				  "  "
				  (propertize
				   (format "%25s" (nth 1 commit)) ; date
				   'mouse-face 'highlight
				   'help-echo "mouse-1: limit by this start date"
				   'keymap date-map
				   'action (nth 1 commit)
				   'face facevalue)
				  "  "
				  (propertize 
				   (format (concat "%-" (number-to-string (+ 2 max-author)) "s") 
					   author)
				   'mouse-face 'highlight
				   'help-echo "mouse-1: set color"
				   'keymap author-map
				   'action (cons "textColor" (nth 2 commit))
				   'face (list :foreground (if is-active (nth 5 commit) disabledcolor)))
				  (propertize 
				   (format "%s" (nth 3 commit)) ; message
				   'face facevalue)
				  (if (set-difference outgoing (list circle)) ; extra line to draw outgoing branches?
				      (draw-branches "\n " "" circle outgoing max-circle passing
						     (string #x2514) (string #x250c) 
						     (string #x2534) (string #x252c)
						     (string #x2518) (string #x2510)))
				  ))))
		   commit-graph 
		   "\n")
	)
    "<commit graph is empty>")
  ) ;pretty-print-commit-graph

;;; --- set author color and name functions

(defun ltc-select-color (event)
  "Select color for indicated author in mouse event.  Updates automatically."
  (interactive "e")
  (let* ((window (posn-window (event-end event)))
	 (pos (posn-point (event-end event)))
	 (action (get-text-property pos 'action)) ; obtain action parameters from text properties
	 (old-color (car action))
	 )
    (select-window parent-window)
    (select-color (cadr action) (caddr action) old-color)))

(defun ltc-set-color (author) 
  "Select and set color for given AUTHOR.  Updates automatically unless user aborts by entering an empty name."
  (interactive
   (if ltc-mode
       (list (completing-read "Author, for whom to set color (abort with empty value or C-g): " 
			      (mapcar 'author-to-string (mapcar 'caddr commit-graph))
			      nil t))
     '(nil))) ; sets author = nil
  (if (and author (string< "" author))
      (let* ((author-list (string-to-author author))
	     (name (nth 0 author-list))
	     (email (nth 1 author-list)))
	(select-color name email (ltc-method-call "get_color" name email)))))

(defun select-color (name email old-color)
  "Select and set color for author with given NAME and EMAIL.  If given OLD-COLOR is not nil, 
it will only set the new, chosen color if it is different than the old one."
  (when ltc-mode
    ;; open color list and then prompt user for input
    (list-colors-display)
    (condition-case nil
	(let* ((new-color 
		(read-color (format "Color for %s (name or #RGB; abort with empty input): " 
				    (author-to-string (list name email)))
			    t))
	       (new-color-short 
		(concat "#" (substring new-color 1 3) (substring new-color 5 7) (substring new-color 9 11)))
	       )
	  (when (not (string= (downcase old-color) (downcase new-color-short)))
	    (ltc-method-call "set_color" name email new-color-short)
	    (ltc-update))
	  )	
      (error nil)) ; no handlers as we simply abort if empty color name given
    ;; remove *Colors* buffer if still visible
    (when (setq b (get-buffer "*Colors*"))
      (delete-windows-on b t)
      (kill-buffer b))
    (update-info-buffer)))

(defun ltc-set-self (author) 
  "Set current self to given AUTHOR.  Updates automatically unless user aborts by entering an empty name."
  (interactive
   (if ltc-mode
       (list (completing-read "New current self in format \"name [<email>]\" (abort with empty value or C-g): " 
			      (mapcar 'author-to-string (mapcar 'caddr commit-graph))
			      nil t))
     '(nil))) ; sets author = nil
  (if (and author (string< "" author))
      (let* ((author-list (string-to-author author))
	     (name (nth 0 author-list))
	     (email (nth 1 author-list)))
	(setq self (ltc-method-call "set_self" session-id name email))
	(ltc-update))))

;;; --- functions to handle online editing

(defun compile-deletions ()
  "Collect start and end position pairs of all deletion regions in current text."
  (setq deletions nil)
  (setq start 0)
  (setq index 1)
  (while (< index (point-max))
    (let ((delface (get-text-property index 'face))) ; face properties (if any)
      (if (member 'ltc-deletion delface)
	  ;; character is deleted
	  (if (= start 0) ; first deletion character?
	      (setq start index))
	;; else form: character is not deleted
	(if (> start 0) ; ending a deletion region?
	    (progn
	      (setq deletions (append deletions (list (list (1- start) (1- index))))) ; Emacs positions are +1
	      (setq start 0)))) ; reset start marker
      (setq index (1+ index)))) ; advance index
  (if (> start 0) ; we ended with a deletion region
      (setq deletions (append deletions (list (list (1- start) (1- index)))))) ; Emacs positions are +1
  ;(message "Compiled deletions are: %S" deletions)
  (if deletions
      (list :array deletions) ; return deletions as a labeled :array
    [])) ; return empty list

(defun ltc-add-edit-hooks ()
  "Add hooks to capture user's edits."
  (add-hook 'before-change-functions 'ltc-hook-before-change nil t)
  (add-hook 'after-change-functions 'ltc-hook-after-change nil t)  
  )

(defun ltc-remove-edit-hooks ()
  "Remove hooks to capture user's edits."
  (remove-hook 'before-change-functions 'ltc-hook-before-change t)
  (remove-hook 'after-change-functions 'ltc-hook-after-change t)
  )

(defun ltc-hook-after-change (beg end len)
  "Hook to capture user's insertions while LTC mode is running."
  (message " --- LTC: after change with beg=%d and end=%d and len=%d" beg end len)
  (when (and self (= 0 len))
    ;; color text and use addition face
    (add-text-properties beg end (list 'face 
				       (list 'ltc-addition (list :foreground (car (last self))))))
    ))

(defun ltc-hook-before-change (beg end)
  "Hook to capture user's deletions while LTC mode is running."
  (message " --- LTC: before change with beg=%d and end=%d" beg end)
  ;; if first change (buffer-modified-p == nil) then update commit graph
  (when (and (not (buffer-modified-p)) commit-graph)
    ;; manipulate commit graph: if at least one entry and the first element is "" or "on disk" then replace first ID with "modified"
    (let ((head (car commit-graph)))
      (when (and head (or (string= "" (car head)) (string= on_disk (car head))))
	(setcar head modified)
	(setcar commit-graph head)
	(update-info-buffer))))
  (when (and self (not (= beg end)))
    (setq self-color (caddr self))
    ;; use idiom to manipulate deletion string in temp buffer before returning to current buffer
    (setq delstring (buffer-substring beg end)) ; string /w text props about to be deleted
    (setq insstring  ; calculate insertion string
	  (with-temp-buffer
	    (insert delstring)
	    ;; prepare new text properties for characters that are inserted by other or not marked up:
	    (setq newface (list 'ltc-deletion (list :foreground self-color)))
	    ;; collect indices which need new text properties here:
	    (setq newindices nil)
	    ;; go through upcoming deletion's characters one-by-one
	    (setq index 1)
	    (while (< index (point-max))
	      (let ((delface (get-text-property index 'face))) ; face properties (if any)
		(if (member 'ltc-deletion delface)
		    ;; character already deleted: keep with same properties
		    (setq index (1+ index)) ; advance index
		  (if (and (member 'ltc-addition delface)
			   (equal (color-values (mapconcat (function (lambda (x) (plist-get x :foreground)))
							   delface "")) ; obtain foreground color (if any)
				  (color-values self-color))) ; text is addition with color for self
		      ;; text inserted by self: remove character
		      (delete-region index (1+ index)) ; delete character and don't advance index in this branch!
		    ;; text inserted by other or not marked up:
		    (put-text-property index (1+ index) 'face newface) ; replace with new mark-up 
		    (setq index (1+ index)) ; advance index
		    ))))
	    (buffer-string) ; return the contents of the temp buffer
	    ))
    (insert insstring) ; this moves point to end of insertion
    (if (eq 'backspace last-input-char) (goto-char beg)) ; if last key was BACKSPACE, move point to beginning
    ))

;;; --- undo change

(defun ltc-undo-change ()
  "Undo change at current pointer (if any)."
  (interactive)
  (setq faceid (car (get-text-property (point) 'face)))
  (setq revid (get-text-property (point) 'ltc-change-rev))
  (if (not faceid)
      (message "Cannot undo at %d as there is no change found." (point))
    ; else forms:
    ;; find left and right border of change:
    (setq borders (mapcar (lambda (dir)
			    (setq index (point))
			    ;; repeat..until loop: go through all characters with the *same* change ID
			    (while (progn
				     (setq index (+ index dir)) ; increment or decrement index
				     ;; the "end-test" is the last item in progn:
				     (and (not (is-buf-border index dir))
					  (equal faceid
						 (car (get-text-property index 'face)))
					  (equal revid
						 (get-text-property index 'ltc-change-rev)))))
			    ; final border value: dir = -1 -> index + 1 and dir = 1 -> index
			    (truncate (+ index (+ 0.5 (* dir -0.5)))))
			  '(-1 1)))
    ;; TODO: hook into edit system...
    (message "change at %s is %S with face=%s rev=%s" (point) borders faceid revid)
    (cond ((equal 'ltc-addition faceid) ; found addition: delete it
	   (message "delete change in [%d %d]" (nth 0 borders) (nth 1 borders))
	   ; move point to beginning of region:
	   ;(setq origpoint (point))
	   ;(goto-char (nth 0 borders))
	   (delete-region (nth 0 borders) (nth 1 borders))
	   ; move point back to old location in region (only if deleted text wasn't in color!!):
	   ;(goto-char origpoint)
	   )
	  ((equal 'ltc-deletion faceid) ; found deletion: add it
	   (message "add change"))
	  (t (message "Cannot undo change at %d as neither addition nor deletion found." (point))))
    ))

;;; --- accessing API of base system

(defvar ltc-server-address 
  (concat "http://localhost:" (number-to-string ltc-port) "/xmlrpc"))

(defun ltc-method (name)
  (concat "com.sri.ltc.server.LTCserverInterface." name))

(defun ltc-method-call (name &rest args)
  (apply 'xml-rpc-method-call ltc-server-address (ltc-method name) args))

;;; --- helper functions

(defun trim (str)
  "Trim leading and tailing whitespace from STR."
  (let ((s (if (symbolp str) (symbol-name str) str)))
    (replace-regexp-in-string "\\(^[[:space:]\n]*\\|[[:space:]\n]*$\\)" "" s)))

(defun shorten (max str)
  "Shorten STR to MAX characters if it is that long."
  (if (> (length str) max)
      (substring str 0 max)
    str))

(defun author-to-string (author)
  "Convert AUTHOR given as pair '(NAME EMAIL) into git representation of form \"NAME <EMAIL>\"."
  (let ((name (car author))
	(email (cadr author)))
    (if (or (not email) (string= "" email))
	name ; only name if email is NULL or empty
      (concat name " <" email ">"))))

(defun string-to-author (str)
  "Convert STR as an author's git representation of form \"NAME <EMAIL>\" or \"NAME\" into a pair of strings '(NAME EMAIL) with EMAIL potentially the empty string.  Returns nil if str cannot be parsed according to format above."
  (let ((author (trim str)))
    (if (string-match "^\\([^<>]*\\)\\(<\\(.*\\)>\\)?$" author)
	(list 
	 (trim (match-string 1 author)) ; name
	 (if (> (length (match-data)) 7)
	     (match-string 3 author) ; email
	   "")))))


(provide 'ltc-mode)
;;; ltc-mode.el ends here
