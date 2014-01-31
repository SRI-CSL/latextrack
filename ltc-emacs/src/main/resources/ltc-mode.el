;;; ltc-mode.el --- user interface for LaTeX Track Changes (LTC) as minor mode to LaTeX
;;
;; Copyright (C) 2009 - ${current.year} SRI International
;;
;; Author: Linda Briesemeister <linda.briesemeister@sri.com>
;;    Sam Owre <sam.owre@sri.com>
;; Maintainer: Linda Briesemeister <linda.briesemeister@sri.com>
;; Created: 20 May 2010
;; URL: ${url}
;; Version: ${project.version}
;; Package-Version: ${numeric.version}
;; Package-Requires: ((cl "1.0") (xml-rpc "1.6.10"))
;; Keywords: latex, track changes, git, svn, xmlrpc, java
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
;;; License:
;;
;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as
;; published by the Free Software Foundation, either version 3 of the 
;; License, or (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public 
;; License along with this program.  If not, see
;; <http://www.gnu.org/licenses/gpl-3.0.html>.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;; Code:

(require 'xml-rpc)
(require 'cl) ; for set operations
(defconst min-xml-rpc-version "1.6.10" "Minimum version requirement for xml-rpc mode")
(defconst ltc-mode-version "${project.version}" "Current version of ltc-mode.el")

(eval-after-load "ltc-mode"
  '(progn
     (message "Emacs version: %s" (emacs-version))
     (message "LTC mode version: %s" ltc-mode-version)))

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
  "Latex Track Changes minor mode."
  :version "23"
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
  :type '(string)
  :group 'ltc)

(defcustom ltc-debug nil
  "Whether to turn debug messages on."
  :type '(boolean)
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

;;; regular and debugging output
(defun ltc-log (formatstr &rest args)
  "Produce consistent output in *Messages* buffer using ORMATSTR with ARGS like the function `message`"
  (apply #'message (concat  "LTC: " formatstr) args))
(defun ltc-error (formatstr &rest args)
  "Produce consistent error messages in *Messages* buffer using FORMATSTR with ARGS like the function `message`"
  (apply #'message (concat  "LTC Error: " formatstr) args))
(defun ltc-log-debug (formatstr &rest args)
  "Produce consistent debug output in *Messages* buffer using FORMATSTR with ARGS like the function `message`"
  (if ltc-debug
      (apply #'message (concat  "LTC debug: " formatstr) args)))

(defvar session-id nil "current LTC session ID.")
(make-variable-buffer-local 'session-id)

(defvar commit-graph nil 
  "Commit graph structure containing one element per commit: 
(id date (author-name author email) message isActive isLast color (columns....)? OR: (list-of-parents) (list-of-children))")
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
(define-key ltc-prefix-map (kbd "t") 'ltc-undo-changes-same-author)
(define-key ltc-prefix-map (kbd "r") 'ltc-undo-changes-within-region)
(define-key ltc-prefix-map (kbd ">") 'ltc-next-change)
(define-key ltc-prefix-map (kbd "<") 'ltc-prev-change)
(define-key ltc-prefix-map (kbd "c") 'ltc-set-color)
(define-key ltc-prefix-map (kbd "b") 'ltc-bug-report)
;; Bind command `ltc-prefix-map' to `ltc-command-prefix' in `ltc-mode-map':
(defvar ltc-mode-map (make-sparse-keymap) "LTC mode keymap.")
(define-key ltc-mode-map ltc-command-prefix 'ltc-prefix-map)

;;;###autoload
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
   ["Undo change (same rev) at point" ltc-undo-change
    :help "Undo change (i.e., all surrounding characters of same revision) at current point."]
   ["Undo changes with same author" ltc-undo-changes-same-author
    :help "Undo all changes of same author and kind (i.e. addition or deletion) at current point."]
   ["Undo changes within region" ltc-undo-changes-within-region 
    :help "Undo all changes in currently marked region."]
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
  (unless 
      (condition-case err 
	  (progn 
	    ;; check whether buffer has a file name:
	    (if (not (buffer-file-name))
		(error "%s" "While starting LTC mode: Buffer has no file name associated")
	      (ltc-log "Starting LTC mode for file \"%s\"..." (buffer-file-name)))
	    ;; testing xml-rpc version:
	    (ltc-log "Using `xml-rpc' package version: %s" xml-rpc-version)
	    (and (version< xml-rpc-version min-xml-rpc-version)
		 (error "LTC requires `xml-rpc' package v%s or later" min-xml-rpc-version))
	    ;; test whether server reports version and if so, whether version (prefixes) match
	    (condition-case version-error
		(let* ((ltc-server-version (ltc-method-call "get_version")))
		  (ltc-log "Server version: %s" ltc-server-version)
		  (unless (version= 
			   (replace-regexp-in-string "-.*" "" ltc-mode-version)
			   (replace-regexp-in-string "-.*" "" ltc-server-version))
		    (unless
			(y-or-n-p 
			 (format "Warning: ltc-mode.el (%s) and LTC Server (%s) numeric version prefixes don't match. Continue anyways? " 
				 ltc-mode-version ltc-server-version))
		      (error "%s" "User aborted as versions of ltc-mode.el and LTC Server don't match!"))))
	      ('error
	       (cond ; ignore older versions of API that did not have this method:
		   ((string-match "No such handler" (error-message-string version-error))
		    (ltc-log "Warning: possibly outdated LTC server running!")) ; ignore but warn
		   ((string-match "Invalid version syntax" (error-message-string version-error))
		    (ltc-log "Warning: %s" (error-message-string version-error))) ; ignore but log output
		   (t ; another error occurred: propagate up
		    (error "While testing LTC server version: %s%s"
			   (error-message-string version-error)
			   (if (string= "Why? url-http-response-status is nil" (error-message-string version-error))
			       "\nPerhaps the LTC server is not running?"
			     ""))))))
	    ;; update boolean settings:
	    (mapc (lambda (show-var) 
		    (set show-var (ltc-method-call "get_bool_pref" (cdr (assoc show-var show-map)))))
		  (mapcar 'car show-map))
	    (setq ltc-condense-authors 
		  (ltc-method-call "get_bool_pref" (cdr (assoc 'ltc-condense-authors other-settings-map))))
	    (setq ltc-allow-similar-colors 
		  (ltc-method-call "get_bool_pref" (cdr (assoc 'ltc-allow-similar-colors other-settings-map))))
	    (mapc (lambda (var) 
		    (set var 'nil)) 
		  limit-vars)
	    ;; init session (and propagate any errors) and create info buffer:
	    (condition-case init-error
		(setq session-id (ltc-method-call "init_session" (buffer-file-name)))
	      ('error ; propagate up
	       (error "While initializing session: %s%s" 
		      (error-message-string init-error) 
		      ;; if SVNAuthenticationException then display FAQ entry!
		      (if (string-match "SVNAuthenticationException" (error-message-string init-error)) 
			  (concat "\n\nFor a possible work-around see our FAQ at\n"
				  "  http://latextrack.sourceforge.net/faq.html#svn-authentication")
			"")))) 
	    (ltc-log "Session ID = %d" session-id)
	    (setq ltc-info-buffer (concat "LTC info (session " (number-to-string session-id) ")"))
	    ;; buffer settings:
	    (setq orig-coding-sytem buffer-file-coding-system)
	    (setq buffer-file-coding-system 'no-conversion)
	    (font-lock-mode 0) ; turn-off latex font-lock mode
	    (add-hook 'write-file-functions 'ltc-hook-before-save nil t) ; add (local) hook to intercept saving to file
	    (add-hook 'kill-buffer-hook 'ltc-hook-before-kill nil t) ; add hook to intercept closing buffer
	    ;; run first update
	    (ltc-update)
	    t) ; success
	;; handle any error
	('error (ltc-error "%s" (error-message-string err)) nil)) ; return NIL to signal error
    (ltc-mode 0)) ; an error occurred: toggle mode off again
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
	(ltc-log "Stopping mode for file \"%s\"..." (buffer-file-name))
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
	   (ltc-error "While closing session (reverting to text from file): %s" (error-message-string err))
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
  (ltc-log "Starting update...") ; TODO: make cursor turn into wait symbol
  (if ltc-mode
      (condition-case err
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
		 (last (cdr (assoc-string "last_rev" map)))
		 (rev_indices (cdr (assoc-string "revision indices" map)))
		 (commits (ltc-method-call "get_commits" session-id)) ; list of 6-tuple strings
		 )
	    (setq self (ltc-method-call "get_self" session-id)) ; get current author and color
	    (ltc-log "Updates received") ; TODO: change cursor back (or later?)
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
	    ;; update commit graph in temp info buffer:
	    ;;   - first ID = if the last element in "revisions" exists and is "on disk" or "modified", otherwise ""
	    ;;   - active IDs = use "rev_indices" to index into "revisions"
	    (setq commit-graph (init-commit-graph commits 
						  (or (car (member (car (last revisions)) (list modified on_disk))) "")
						  last 
						  (mapcar (lambda (i) (nth i revisions)) rev_indices)))
	    (update-info-buffer)
	    (set-buffer-modified-p old-buffer-modified-p) ; restore modification flag
	    t)
	('error 
	 (ltc-error "While updating: %s" (error-message-string err))
	 nil))
    (ltc-log "Warning: cannot update because LTC mode not active")) ; TODO: change cursor back
  ) ;ltc-update

;;; --- capture save, close, and TODO: save-as (set-visited-file-name) operations 

(defun ltc-hook-before-save ()
  "Let LTC base system save the correct text to file."
  (if (not ltc-mode)
      nil
    (ltc-log-debug "Before saving file %s for session %d" (buffer-file-name) session-id)
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
  (ltc-log-debug "Before killing buffer in session %d for file %s" session-id (buffer-file-name))
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
    (ltc-log "Limiting authors: %S" ltc-limiting-authors)
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
      (ltc-log "No change until %s of document found" (if (> dir 0) "end" "beginning"))
    (setq currface (car (get-text-property index 'face)))      ; nil, 'ltc-addition, or 'ltc-deletion
    (setq currrevid (get-text-property index 'ltc-change-rev)) ; nil or revision ID
    ;; repeat..until loop: go through all characters with the *same* face and revision ID to skip the current change
    (while (progn
	     (setq index (+ index dir)) ; increment or decrement index
	     ;; the "end-test" is the last item in progn:
	     (and (not (is-buf-border index dir))
		  (equal currface
			 (car (get-text-property index 'face)))
		  (equal currrevid
			 (get-text-property index 'ltc-change-rev)))))
    (if (is-buf-border index dir)
	(ltc-log "No change until %s of document found" (if (> dir 0) "end" "beginning"))
      ;; else-form: now find the next char face with addition or deletion
      (while (and (not (is-buf-border index dir))
		  (and (not (member 'ltc-addition (get-text-property index 'face)))
		       (not (member 'ltc-deletion (get-text-property index 'face)))))
	(setq index (+ index dir))) ; increment or decrement index
      (if (is-buf-border index dir)
	  (ltc-log "No change until %s of document found" (if (> dir 0) "end" "beginning"))
	;; else-form: index denotes position of change
	(setq index (if (> dir 0) index (1+ index))) ; increment position if looking for end of previous change
	(ltc-log "%s change found @ %d" (if (> dir 0) "Next" "Previous") index)
	(goto-char index)))))

(defun is-buf-border (index dir)
  "Whether given INDEX is at the beginning or end of (possibly narrowed) buffer determined by DIR."
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
	(y-or-n-p "Include repository? "))
     '(nil nil nil))) ; sets directory = nil and msg = nil and includeSrc = nil
  (when directory
    ;; copy current contents of *Messages* buffer to a new file Messages.txt in the directory
    (let ((mbuf (get-buffer "*Messages*"))
	  (filename (concat directory "Messages.txt")))
      (if mbuf
	  (save-current-buffer
	    (set-buffer mbuf)
	    (if (file-exists-p filename)
		(delete-file filename))
	    (append-to-file (buffer-string) nil filename))))
    (let ((file (ltc-method-call "create_bug_report" session-id msg includeSrc (expand-file-name directory))))
      (ltc-log "Created bug report at %s (please email to lilalinda@users.sourceforge.net)" file))))

;;; --- info buffer functions

(defun init-commit-graph (commits &optional first last active_ids)
  "Init commit graph from given COMMITS.  

If FIRST is given, use this revision for the first entry in the commit graph.  Otherwise, use the entry from the prior graph (if it exists).  The author in the first entry in the list will be the current self in either case.

If LAST is given, use this revision to set 'isLast' of the respective entry to t.

IF ACTIVE_IDS is given, use this to determine 'isActive' status of each entry (except the first entry for self).

Each entry in the commit graph that is returned, contains a list with the following elements:
 (ID date (name email) message isActive isLast color (column (incoming-columns) (outgoing-columns) (passing-columns)))
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
      ;; NOTE: this is modeled after CommitTableModel.update() in LTC Editor code!
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
      ;;  (id date (name email) msg isActive isLast color graph)
      ;;  where GRAPH is:
      ;;   (column incoming-columns outgoing-columns passing-columns)
      (cons 
       ;; first row is item for self:
       ;; - if optional FIRST given, look at last item and keep it if MODIFIED or ON_DISK, else 
       ;; - if prior graph not empty, keep first ID, otherwise "" 
       (list
	;; determine ID of first item:
	(or first
;	    (concat first (car (member (car (last ids)) (list modified on_disk))))
	    (if commit-graph (caar commit-graph) "")) ; keep first ID if prior graph exists
	;; set rest of first item to empty and self:
	"" (list (car self) (nth 1 self)) "" t nil (nth 2 self) '(0 nil nil nil))
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
		    (or (not active_ids)  ; isActive: if no ACTIVE_IDS, then t,
			(not (not (member id active_ids))))  ; otherwise set to t (not the returned tail) if found
		    (if last  ; isLast: if no LAST, then nil, otherwise compare ID with LAST
			(string= last id))
		    (cdr (assoc author authors)) ; color of author
		    (list ; graph:
		     (cadr (assoc index circle-alist))  ; circle column
		     (cdr (assoc index incoming-alist)) ; incoming columns
		     (cdr (assoc index outgoing-alist)) ; outgoing columns
		     (cdr (assoc index passing-alist))  ; passing columns
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
	  (setq line-spacing nil) ; no extra height between lines in this buffer
	  (setq truncate-lines t) ; no line wrap
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
	    (commit-top-bottom-last #x256a)
	    (commit-top-last #x2567)
	    (commit-bottom-last #x2564)
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
				      (is-last (nth 5 commit))
				      (author (author-to-string (nth 2 commit)))
				      (disabledcolor "#7f7f7f")
				      (facevalue (append 
						  (if is-active nil (list :foreground disabledcolor))
						  (if is-last (list :slant 'italic) nil)))
				      (id (shorten 8 (car commit)))
				      (graph (nth 7 commit))
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
						 ; circle column: depends on incoming and outgoing, as well as whether last:
						 (list (if (member circle incoming)
							   (if outgoing 
							       (if is-last commit-top-bottom-last commit-top-bottom)
							     (if is-last commit-top-last commit-top))
							 (if outgoing 
							     (if is-last commit-bottom-last commit-bottom)
							   ?\s)))
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
				   'face (append 
					  (list :foreground (if is-active (nth 6 commit) disabledcolor))
					  (if is-last (list :slant 'italic) nil)))
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
      ('error nil)) ; no handlers as we simply abort if empty color name given
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
  (ltc-log-debug "Compiled deletions are: %S" (if (> (length deletions) 20) (format "<list has %d elements>" (length deletions)) deletions))
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
  (ltc-log-debug " --- after change with beg=%d and end=%d and len=%d" beg end len)
  (when (and self (= 0 len))
    ;; color text and use addition face
    (ltc-log-debug " ------ marking as added: \"%s\" between %d and %d" (buffer-substring beg end) beg end)
    (add-text-properties beg end (list 'face 
				       (list 'ltc-addition (list :foreground (car (last self))))
				       'help-echo "rev: modified"
				       'ltc-change-rev modified)))
  (when (and (> len 0)
	     (string< "" insstring))
    (ltc-log-debug " ------ inserting: \"%s\" at %d" insstring (point))
    (let ((inhibit-modification-hooks t)) ; temporarily disable modification hooks
      (insert insstring)) ; this moves point to end of insertion
    (ltc-log-debug " ------ last char: %S" last-input-event)
    (cond ((or (eq 'backspace last-input-event) ; if last key was BACKSPACE, move point to beginning
	       (eq 'M-backspace last-input-event))
	   (ltc-log-debug " -------- moving to beginning of deleted string")
	   (goto-char beg))
	  ((or (eq 'kp-delete last-input-event) ; if last key was FORWARD DELETE, move point to end of inserted string
	       (eq 134217828 last-input-event)  ; M-d (delete word forward)
	       (eq 4 last-input-event)          ; C-d (delete char forward)
	       (eq 11 last-input-event))        ; C-k (delete forward rest of line)
	   (ltc-log-debug " -------- moving to end of deleted string")
	   (goto-char (+ beg (length insstring))))
	  (t ; move point back to original location in region (possibly adjusted)
	   (ltc-log-debug " ------ now moving back from %d to %d " (point) origpoint)
	   (goto-char origpoint)) ; adjust original point
	  ))
  (ltc-log-debug " ------ point is %d " (point))
  )

(defun ltc-hook-before-change (beg end)
  "Hook to capture user's deletions while LTC mode is running."
  (ltc-log-debug " --- before change with beg=%d and end=%d at point %d" beg end (point))
  ;; if modified but first row does not show this then update commit graph
  (if (and (buffer-modified-p) commit-graph)
    ;; manipulate commit graph: if at least one entry and the first element is "" or "on disk" then replace first ID with "modified"
    (let ((head (car commit-graph)))
      (when (and head (or (string= "" (car head)) (string= on_disk (car head))))
	(setcar head modified)
	(setcar commit-graph head)
	(update-info-buffer))))
  (if (or (not self) (= beg end))
      (setq insstring "") ; no deletion or no information about self: nothing to insert after change
    ; else forms: we have a deletion by SELF
    (let ((offset (if (>= (point) beg) (1+ (- (point) beg)) 0)) ; calculate offset of point in temp buffer
	  (self-color (caddr self)) ; obtain color for upcoming change
	  (delstring (buffer-substring beg end)) ; string /w text props about to be deleted
	  )
      (ltc-log-debug " ------ offset is %d " offset)
      (ltc-log-debug " ------ deleting: \"%s\"" delstring)
      ;; calculate string (/w properties) to insert after change
      ;; use idiom to manipulate deletion string in temp buffer before returning to current buffer
      (setq insstring  ; calculate insertion string
	    (with-temp-buffer
	      (insert delstring)
	      (if (and (> offset 0)
		       (< offset (point-max)))
		  (goto-char offset))
	      (ltc-log-debug " ---temp--- point after inserting is: %d" (point))
	      ;; go through upcoming deletion's characters one-by-one
	      (let ((newface (list 'ltc-deletion (list :foreground self-color))) ; new face properties for characters that are inserted by other or not marked up
		    (newindices nil) ; collect indices which need new text properties here
		    )
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
			;; text inserted by other or not marked up: replace with new mark-up 
			(add-text-properties index (1+ index) (list 'face newface
								    'help-echo "rev: modified"
								    'ltc-change-rev modified))
			(setq index (1+ index)) ; advance index
			)))))
	      (ltc-log-debug " ---temp--- point after going through buffer is: %d" (point))
	      (setq deltapoint (1- (point))) ; calculate delta for real buffer
	      (buffer-string))) ; return the contents of the temp buffer
      ;; calculate original point for after-change
      (if (= offset 0)
	  (setq origpoint (point)) ; keep original point
	; else-forms:
	(if (> (point) end) 
	    ; point is past region to be deleted 
	    (setq origpoint (+ (+ beg deltapoint) (- (point) end)))
	  ; else-forms: point is inside region to be deleted (including borders)
	  (setq origpoint (+ beg deltapoint))
	  ))
      (ltc-log-debug " ------ orig point is %d " origpoint)
      )))

;;; --- undo change

(defun undo-change (start revid faceclr &optional from-undo-in-region)
  "Undo change at given location START.  If not nil, use the properties of REVID and/or FACECLR to 
compare with the revision id and the foreground color, respectively, to determine the extent of the 
change in both directions.  The optional argument FROM-UNDO-IN-REGION causes the pointer 
to move to the end of an addition change to maintain the location after turning it into a deletion. 
This function returns the end position after the change was undone."
  (let ((faceid (car (get-text-property start 'face)))) ; face ID is nil, 'ltc-addition or 'ltc-deletion
    ;; find left and right border of change:
    ;; compare with FACEID, and also REVID and FACECLR, if not nil
    (setq borders (mapcar (lambda (dir)
			    (setq index start)
			    ;; repeat..until loop: go through all characters with the *same* face ID and author color
			    (while (progn
				     (setq index (+ index dir)) ; increment, keep or decrement index
				     ;; the "end-test" for repeat..until loop is the last item in progn:
				     (and (not (= 0 dir))    ; stop walking if dir = 0
					  (not (is-buf-border index dir)) ; not the end of buffer
					  (equal faceid      ; test that FACEID matches
						 (car (get-text-property index 'face)))
					  (or (not revid)    ; skip test if REVID = nil
					      (equal revid
						     (get-text-property index 'ltc-change-rev)))
					  (or (not faceclr)  ; skip test if FACECLR = nil
					      (equal faceclr
						     (plist-get (nth 1 (get-text-property index 'face)) :foreground))))))
			    ;; final border value: 
			    ;;   dir = -1 -> index + 1
			    ;;   dir = 0 -> index 
			    ;;   dir = 1 -> index
			    (truncate (+ index (+ 0.5 (* dir -0.5)))))
			  (if from-undo-in-region
			      '(0 1)   ; only go forward when coming from undo in region
			    '(-1 1)))) ; go backward and forward if looking for change at point
    ;; now perform the switch:
    (ltc-add-edit-hooks) ; just in case there was a problem, re-enable the edit hooks as we are depending on them
    (ltc-log-debug " == change at %s is %S with face=%s rev=%s clr=%s" start borders faceid revid faceclr)
    (let ((origpoint start)) ; remember original start
      (cond ((equal 'ltc-addition faceid) ; found addition: delete it
	     (ltc-log-debug " == delete change: \"%s\"" (buffer-substring (nth 0 borders) (nth 1 borders)))
	     (save-excursion
	       (if from-undo-in-region
		   (goto-char (nth 1 borders))) ; calc return value as end of deletion
	       (delete-region (nth 0 borders) (nth 1 borders)) ; this DOES trigger modification hooks
	       (ltc-log-debug " == now at point %d " (point))
	       (point))) ; return current position
	    ((equal 'ltc-deletion faceid) ; found deletion: add it
	     ;; remove deletion without change hooks, then insert
	     (let ((delstring (buffer-substring (nth 0 borders) (nth 1 borders))))
	       (let ((inhibit-modification-hooks t)) ; temporarily disable modification hooks
		 (delete-region (nth 0 borders) (nth 1 borders))) ; this DOES NOT trigger modification hooks
	       (goto-char (nth 0 borders)) ; do insertion from beginning of region
	       (ltc-log-debug " == add change again: \"%s\" at %d" delstring (point))
	       (insert delstring)) ; this DOES trigger modification hooks
	     (goto-char origpoint)
	     (ltc-log-debug " == now back at point %d " (point))
	     (nth 1 borders)) ; return end of inserted string
	    (t (nth 1 borders)) ; return end of no change
	    ))))

(defun ltc-undo-change ()
  "Undo change (i.e., all surrounding characters of same revision) at current point."
  (interactive)
  (let ((faceid (car (get-text-property (point) 'face)))) ; face ID is nil, 'ltc-addition or 'ltc-deletion
    (if (not faceid)
	(ltc-log "Cannot undo change of same revision at %d as no change found." (point))
      (undo-change (point) (get-text-property (point) 'ltc-change-rev) nil) ; use only revision ID for matching
      )))

(defun ltc-undo-changes-same-author ()
  "Undo all changes of same author and kind (i.e. addition or deletion) at current point."
  (interactive)
  (let* ((face (get-text-property (point) 'face))
	 (faceid (car face))) ; face ID is nil, 'ltc-addition or 'ltc-deletion
    (if (not faceid)
	(ltc-log "Cannot undo changes of same author at %d as no change found." (point))
      (undo-change (point) nil (plist-get (nth 1 face) :foreground)) ; use only face color for matching
      )))

(defun ltc-undo-changes-within-region (start end)
  "Undo all changes in currently marked region between START and END."
  (interactive "r")
  ;; do we have a region marked?
  (if (= start end)
      (ltc-log "Cannot undo changes in region as the region's length is 0.")
    ; else-forms:
    (ltc-log-debug " ~~ region is [%d, %d] and point is %d" start end (point))
    ;; narrow to region and then go through changes from beginning to end and flip them:
    (save-restriction
      (narrow-to-region start end)
      (setq startindex start)
      (while (< startindex (point-max))
	(let ((nextindex (undo-change startindex nil nil t))) ; only compare by face id (possibly nil)
	  (setq startindex nextindex)))) ; advance index to next possible change
    (ltc-log-debug " ~~ now at point %d " (point))
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
