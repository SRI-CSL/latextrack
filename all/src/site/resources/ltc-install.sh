#!/bin/bash

###
# #%L
# LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
# %%
# Copyright (C) 2009 - 2012 SRI International
# %%
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 3 of the 
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public 
# License along with this program.  If not, see
# <http://www.gnu.org/licenses/gpl-3.0.html>.
# #L%
###
# install LTC in given directory and optionally extract Emacs lisp files

################################################################################
# variables with default values

declare JAR_FILE=${ueber.jar}.jar
declare DOWNLOAD_URL=${temp.url}/downloads
declare DOWNLOAD_DIR=""
declare EMACS_DIR=""
declare FETCH_TYPE=-1  # indicates nothng has been set

################################################################################
# helper functions:

usage ()  # print error message if given as argument and then general usage
{
    if [ $# -gt 0 ]; then
	echo " *** ERROR: $1"
    fi
    cat << EOF

usage: $0 <OPTIONS> <JAVA DIR> [<EMACS DIR>]

Install $JAR_FILE into given <JAVA DIR>.  
If you specify via option -d a directory, the script will look there for a file 
with the recent version to be copied.  Otherwise, it will try to download the JAR
from the web using 'wget' or 'curl'.

If successfully downloaded or copied, the script creates a symbolic link pointing 
to the latest version in
  <JAVA DIR>/LTC.jar

Optionally give an Emacs directory where to extract the Emacs lisp files.

OPTIONS:
   -h        Show this message
   -d <DIR>  Directory where JAR was downloaded instead of fetching online
EOF
}

testdir () # test existence of given DIR ($1) and use NAME ($2) for error messages
{
    if [ ! -d "$1" ]; then
	usage "$2 directory is not a directory"; exit 1
    fi
    case $3 in
	r) 
	    if [ ! -r "$1" ]; then
		usage "$2 directory is not readable"; exit 1
	    fi
	    ;;
	w)
	    if [ ! -w "$1" ]; then
		usage "$2 directory is not writable"; exit 1
	    fi
	    ;;
	*)
	    usage "Cannot test directory for unknown ability"; exit 2
	    ;;
    esac
}

################################################################################
# test options and arguments

while getopts “hd:” OPTION
do
    case $OPTION in
        h|\?)
            usage; exit 1
            ;;
	d)
	    DOWNLOAD_DIR=$OPTARG
	    ;;
    esac
    shift $((OPTIND-1)); OPTIND=1 
done

# now $@ has remaining arguments:
if [ $# -lt 1 ]; then
    usage "need at least 1 argument"; exit 1
fi

# test java directory
JAVA_DIR=$1
testdir "$JAVA_DIR" "Java" "w"
# make JAVA_DIR absolute if it doesn't start with '/' or '~'
case ${JAVA_DIR:0:1} in
    /|~) ;; # keep
    *) 
	JAVA_DIR=`pwd`/$JAVA_DIR
	;;
esac

# test Emacs directory (if given)
if [ $# -gt 1 ]; then
    EMACS_DIR=$2
    testdir "$EMACS_DIR" "Emacs" "w"
    type -P jar &>/dev/null
    if [ $? -gt 0 ]; then
	usage "Cannot find executable of 'jar' to extract Emacs Lisp files"; exit 1
    fi
fi

# test download directory (if given) or build URL for download
if [ -n "$DOWNLOAD_DIR" ]; then
    testdir "$DOWNLOAD_DIR" "Download" "r"
    if [ ! -e "$DOWNLOAD_DIR/$JAR_FILE" ]; then
	usage "Cannot find $JAR_FILE in Download directory."; exit 1
    fi
    # make DOWNLOAD_DIR absolute if it doesn't start with '/' or '~'
    case ${DOWNLOAD_DIR:0:1} in
	/|~) ;; # keep
	*) 
	    DOWNLOAD_DIR=`pwd`/$DOWNLOAD_DIR
	    ;;
    esac
    FETCH_TYPE=0  # indicates using download directory
else
    which wget &>/dev/null
    if [ $? -eq 0 ]; then
	FETCH_TYPE=1  # indicates wget
    else
	which curl &>/dev/null
	if [ $? -eq 0 ]; then
	    FETCH_TYPE=2  # indicates curl
	fi
    fi
    if [ $FETCH_TYPE -lt 0 ]; then
	usage "Cannot find executable of 'wget' or 'curl' to download JAR file"; exit 1
    fi
    URL=$DOWNLOAD_URL/$JAR_FILE
fi

################################################################################
# copy or download JAR file

case $FETCH_TYPE in
    0)
	cp -pv "$DOWNLOAD_DIR/$JAR_FILE" "$JAVA_DIR"
	;;
    1)
	printf "downloading %s via wget:\n" $JAR_FILE
	rm -f $JAVA_DIR/$JAR_FILE
	wget -P $JAVA_DIR $URL
	if [ $? -gt 0 ]; then
	    usage "Something went wrong during downloading with 'wget' -- exiting."; exit 5
	fi
	;;
    2) 
	printf "downloading %s via curl:\n" $JAR_FILE
	curl $URL > $JAVA_DIR/$JAR_FILE
	if [ $? -gt 0 ]; then
	    usage "Something went wrong during downloading with 'curl' -- exiting."; exit 5
	fi
	;;
esac

################################################################################
# (optionally) extract Emacs Lisp files

if [ -n "$EMACS_DIR" ]; then
    DIR=`pwd`
    cd "$EMACS_DIR"
    echo "inflating Emacs Lisp files in ${EMACS_DIR}:"
    jar xvf "$JAVA_DIR/$JAR_FILE" xml-rpc.el versions.el ltc-mode.el
    if [ $? -gt 0 ]; then
	usage "Something went wrong when extracting Emacs Lisp files -- exiting."; exit 3
    fi    
    cd "$DIR"
fi

################################################################################
# removing and creating new softlink

rm -f $JAVA_DIR/LTC.jar
ln -v -s $JAR_FILE $JAVA_DIR/LTC.jar

################################################################################
# message

echo "Done with installing LTC in ${JAVA_DIR}"
printf "To start LTC server with default options, use the following command:\n\n  java -jar %s/LTC.jar\n\n" $JAVA_DIR

