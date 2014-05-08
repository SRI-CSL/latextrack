#!/usr/bin/emacs --script

;;;
;;
;; LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
;;
;; Copyright (C) 2009 - 2014 SRI International
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
;;;

;(message "Hello")

(progn
  (add-to-list 'load-path "${project.basedir}/src/main/resources")
  (byte-compile-file "${project.basedir}/src/main/resources/xml-rpc.el")
  (byte-compile-file "${project.basedir}/src/main/resources/ltc-mode.el"))
