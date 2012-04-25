#!/bin/bash
# install LTC in given directory and optionally extract Emacs lisp files

################################################################################
# variables with default values

declare DOWNLOAD_DIR=.
declare JAR_FILE=${ueber.jar}.jar
declare EMACS_DIR=""

################################################################################
# helper functions:

usage ()  # print error message if given as argument and then general usage
{
    if [ $# -gt 0 ]; then
	echo " *** ERROR: $1"
    fi
    cat << EOF

usage: $0 <OPTIONS> <JAVA DIR> [<EMACS DIR>]

Install $JAR_FILE into given <JAVA DIR>.  Specify via option -d the download
directory if not the default (see below).  Optionally give an Emacs directory,
where to extract the Emacs lisp files.

OPTIONS:
   -h        Show this message
   -d <DIR>  Directory where JAR was downloaded (default: ${DOWNLOAD_DIR})
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

# test Emacs directory (if given)
if [ $# -gt 1 ]; then
    EMACS_DIR=$2
    testdir "$EMACS_DIR" "Emacs" "w"
    type -P jar &>/dev/null
    if [ $? -gt 0 ]; then
	usage "Cannot find executable of 'jar' to extract Emacs Lisp files"; exit 1
    fi
fi

# test download directory
testdir "$DOWNLOAD_DIR" "Download" "r"
if [ ! -e "$DOWNLOAD_DIR/$JAR_FILE" ]; then
    usage "Cannot find $JAR_FILE in Download directory."; exit 1
fi
# make DOWNLOAD_DIR absolute
case ${DOWNLOAD_DIR:0:1} in
    /|~) ;; # keep
    *) 
	DOWNLOAD_DIR=`pwd`/$DOWNLOAD_DIR
	;;
esac

################################################################################
# copy JAR file

cp -pv "$DOWNLOAD_DIR/$JAR_FILE" "$JAVA_DIR"

################################################################################
# (optionally) extract Emacs Lisp files

if [ -n "$EMACS_DIR" ]; then
    DIR=`pwd`
    cd "$EMACS_DIR"
    echo "inflating Emacs Lisp files in ${EMACS_DIR}:"
    jar xvf "$DOWNLOAD_DIR/$JAR_FILE" xml-rpc.el versions.el ltc-mode.el
    if [ $? -gt 0 ]; then
	usage "Something went wrong when extracting Emacs Lisp files"; exit 3
    fi    
    cd "$DIR"
fi

################################################################################
# message

echo "Done with installing LTC in ${JAVA_DIR}"
printf "To start LTC server with default options, use the following command:\n  java -jar %s\n\n" $JAVA_DIR/$JAR_FILE

