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

declare JAR_FILE=
declare DOWNLOAD_URL=http://sourceforge.net/projects/latextrack/files/latest/download
declare DOWNLOAD_DIR=""
declare EMACS_DIR=""
declare FETCH_TYPE=-1  # indicates nothng has been set

################################################################################
# helper functions:

usage ()  # print error message if given as argument and then general usage
{
    if [ $# -gt 0 ]; then
	printf " *** ERROR: %s\n" "$1"
    fi
    cat << EOF

usage: $0 <OPTIONS> <JAVA DIR> [<EMACS DIR>]

Install the latest LTC.jar file into given <JAVA DIR>.

The script will try to download the latest release from the web using 'wget' or 'curl'.
If you have already downloaded the latest release manually, you can specify a directory 
via option -d, and the script will look there for the most recent file of the form 
"LTC-<version>.jar" to be copied.

If successfully downloaded or copied, the script creates a symbolic link pointing 
to the latest version in
  <JAVA DIR>/LTC.jar

Optionally give an Emacs directory where to extract the Emacs lisp files.

OPTIONS:
   -h        Show this message
   -d <DIR>  Directory where JAR was downloaded instead of fetching online
EOF
}

testdir () # test given DIR ($1) for PERMISSIONS ($3)
           # use NAME ($2) for error messages
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

findlatest () # find the latest LTC-<version>.jar in given DIR ($1) and set JAR_FILE
{
    if [ ! -d "$1" ]; then
	usage "$1 is not a directory (findlatest)"; exit 1
    fi
    LTC_CANDIDATES=`ls -t $1/LTC-*.jar`
    LTC_ARR=($LTC_CANDIDATES)
    JAR_FILE=`echo ${LTC_ARR[0]} | sed "s/.*\(LTC-.*\.jar\)/\1/"`
    if [ -z "$JAR_FILE" ]; then
	usage "Cannot find matching LTC-<version>.jar file."; exit 1
    fi
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

# test download directory (if given) or determine method for download
if [ -n "$DOWNLOAD_DIR" ]; then
    testdir "$DOWNLOAD_DIR" "Download" "r"
    # make DOWNLOAD_DIR absolute if it doesn't start with '/' or '~'
    case ${DOWNLOAD_DIR:0:1} in
	/|~) ;; # keep
	*) 
	    DOWNLOAD_DIR=`pwd`/$DOWNLOAD_DIR
	    ;;
    esac
    # find latest LTC-<version>.jar file there: set JAR_FILE
    findlatest $DOWNLOAD_DIR 
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
	printf " *** ERROR: Cannot find executable of 'wget' or 'curl' to download JAR file\n"
        printf "            Please download manually from\n"
	printf "     %s\n"  $DOWNLOAD_URL
        printf "            and then use\n"
	printf "     %s -d <DOWNLOAD_DIR>\n\n" $0
	exit 2
    fi
fi

################################################################################
# copy or download JAR file

case $FETCH_TYPE in
    0)
	cp -pv "$DOWNLOAD_DIR/$JAR_FILE" "$JAVA_DIR"
	;;
    1)
	printf "downloading LTC-<version>.jar via wget:\n"
	wget --trust-server-names -N -P $JAVA_DIR $DOWNLOAD_URL
	if [ $? -gt 0 ]; then
	    usage "Something went wrong during downloading with 'wget' -- exiting."; exit 5
	fi
	findlatest $JAVA_DIR
	;;
    2) 
	printf "downloading LTC-<version>.jar via curl:\n"
	JAR_FILE=`curl -LIs $DOWNLOAD_URL | grep -e "^Content-Disposition: attachment; filename=" | sed "s/^Content-Disposition: attachment; filename=\"\(LTC-.*\.jar\)\"/\1/"`
	if [ -z "$JAR_FILE" ]; then
	    usage "Couldn't obtain latest JAR_FILE name with 'curl' -- exiting."; exit 6
	fi
	curl -L $DOWNLOAD_URL -o $JAVA_DIR/$JAR_FILE
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

