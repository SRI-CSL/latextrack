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
# install LTC from web or file in given directory and optionally extract Emacs lisp files

################################################################################
# variables with default values

declare JAR_FILE=
declare DOWNLOAD_URL=http://sourceforge.net/projects/latextrack/files/latest/download
declare DOWNLOAD_DIR=""
declare EMACS_DIR=""
declare ONLINE_TYPE=-1  # how to contact the web site for updates
declare FETCH_TYPE=-1  # indicates nothing has been set
declare SKIP_UPDATE=""

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

Optionally give an Emacs directory <EMACS DIR> where to extract the Emacs lisp files.

This script also checks by default whether it itself is in the latest version.  To skip 
this check, use option -s.

OPTIONS:
   -h        Show this message
   -d <DIR>  Directory where JAR was downloaded instead of fetching online
   -s        Skip the check for updates to this install script
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

makedir_absolute () # if DIR ($1) does not exist, ask whether to create dir or abort
                    # then make absolute and set ABS_DIR
{
    if [ ! -e "$1" ]; then
	while true; do
	    printf " *** WARNING: The specified directory %s does not exist.\n" $1
            printf "              Would you like to create it and continue (C) or abort the installation (A)?\n"
	    read -n1 -p "Enter 'c' or 'a': " yn
	    echo
	    case $yn in
		[Cc]* ) 
		    mkdir -pv $1
		    if [ $? -gt 0 ]; then
			echo "Couldn't create directory $1; giving up."; exit 1
		    fi 
		    break
		    ;;
		[Aa]* ) 
		    echo "Aborting..." 
		    exit 5
		    ;;
		* ) echo "Please enter 'c' or 'a'.";;
	    esac
	done	
    fi
    ABS_DIR=$( cd $1 ; pwd -P )
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

ARGUMENTS="$*"  # preserve in case we need to restart script after update
while getopts “hd:s” OPTION
do
    case $OPTION in
        h|\?)
            usage; exit 1
            ;;
	d)
	    DOWNLOAD_DIR=$( cd $OPTARG ; pwd -P )  # make absolute
	    ;;
	s)
	    SKIP_UPDATE="true"
	    ;;
    esac
    shift $((OPTIND-1)); OPTIND=1 
done

# now $@ has remaining arguments:
if [ $# -lt 1 ]; then
    usage "need at least 1 argument"; exit 1
fi

# make absolut and test java directory
makedir_absolute $1
JAVA_DIR=$ABS_DIR
testdir "$JAVA_DIR" "Java" "w"

# test Emacs directory (if given)
if [ $# -gt 1 ]; then
    makedir_absolute $2
    EMACS_DIR=$ABS_DIR
    testdir "$EMACS_DIR" "Emacs" "w"
    which unzip &>/dev/null
    if [ $? -gt 0 ]; then
	usage "Cannot find executable of 'unzip' to extract Emacs Lisp files"; exit 1
    fi
fi

################################################################################
# check for updates of this install script

# determine online access type: prefer wget over curl
which wget &>/dev/null
if [ $? -eq 0 ]; then
    ONLINE_TYPE=1  # indicates wget
else
    which curl &>/dev/null
    if [ $? -eq 0 ]; then
	ONLINE_TYPE=2  # indicates curl
    fi
fi

# check for updates of this install script online unless specified to skip
if [ -z "$SKIP_UPDATE" ]; then
    if [ $ONLINE_TYPE -lt 1 ]; then
	printf " *** ERROR: Cannot find executable of 'wget' or 'curl' to check for updates.\n"
        printf "            Please install one of these utilities or skip the update check with -s\n\n"
	exit 2
    fi
    # determine location of install script:
    makedir_absolute $( dirname $0 )
    SCRIPT_DIR=$ABS_DIR
    # download to same location:
    case $ONLINE_TYPE in
	1)
	    printf "\nChecking for updates of 'ltc-install.sh' via wget...\n"
	    wget --quiet -N -P $SCRIPT_DIR http://sourceforge.net/projects/latextrack/files/ltc-install.sh/download
	    if [ $? -gt 0 ]; then
		usage "Something went wrong during update check with 'wget' -- exiting."; exit 5
	    fi
	    ;;
	2) 
	    printf "\nChecking for updates of 'ltc-install.sh' via curl...\n"
	    curl --silent -L http://sourceforge.net/projects/latextrack/files/ltc-install.sh/download -o $SCRIPT_DIR/download -z $SCRIPT_DIR/ltc-install.sh
	    if [ $? -gt 0 ]; then
		usage "Something went wrong during update check with 'curl' -- exiting."; exit 5
	    fi
	    ;;
	*)
	    printf "Unknown ONLINE_TYPE -- exiting."; exit 8
	    ;;
    esac
    # test how new online version is:
    if [ $SCRIPT_DIR/download -nt $SCRIPT_DIR/ltc-install.sh ] ; then 
	printf " *** ATTENTION: Newer install script available!\n"
	while true; do
	    read -p "                Do you wish to update the install script and start over? " -n 1 yn
	    echo
	    case $yn in
		[Yy]*)
		    # Spawn update script
		    cat > updateScript.sh << EOF
#!/bin/bash
# Overwrite old file with new and restart new script
if mv $SCRIPT_DIR/download $SCRIPT_DIR/ltc-install.sh; then
  echo "done."
  printf " *** Restarting with: bash %s/ltc-install.sh %s\n" $SCRIPT_DIR "$ARGUMENTS"
  rm \$0
  exec bash $SCRIPT_DIR/ltc-install.sh $ARGUMENTS
else
  echo "Failed to update!"
  exit 1
fi
exit 0
EOF
		    echo -n "                Updating..."
		    exec bash updateScript.sh
		    break ;;
		[Nn]*) 
		    printf " *** Update not installed -- continue with old install script.\n"
		    break ;;
		*) echo "                Please answer y[es] or n[o]." ;;
	    esac
	done
    else
	printf "You are running the latest version of the install script.\n"
    fi
    rm -f $SCRIPT_DIR/download
else
    printf "\nSkipping check for updates of 'ltc-install.sh'\n"
fi

################################################################################
# copy or download JAR file

# test download directory (if given) or determine method for download
if [ -n "$DOWNLOAD_DIR" ]; then
    testdir "$DOWNLOAD_DIR" "Download" "r"
    # find latest LTC-<version>.jar file there: set JAR_FILE
    findlatest $DOWNLOAD_DIR 
    FETCH_TYPE=0  # indicates using download directory
else
    FETCH_TYPE=$ONLINE_TYPE
    if [ $FETCH_TYPE -lt 0 ]; then
	printf " *** ERROR: Cannot find executable of 'wget' or 'curl' to download JAR file\n"
        printf "            Please download manually from\n"
	printf "     %s\n"  $DOWNLOAD_URL
        printf "            and then use\n"
	printf "     %s -d <DOWNLOAD_DIR>\n\n" $0
	exit 2
    fi
fi

echo
case $FETCH_TYPE in
    0)
	cp -pv "$DOWNLOAD_DIR/$JAR_FILE" "$JAVA_DIR"
	;;
    1)
	printf "Downloading LTC-<version>.jar via wget:\n\n"
	wget --trust-server-names -N -P $JAVA_DIR $DOWNLOAD_URL
	if [ $? -gt 0 ]; then
	    usage "Something went wrong during downloading with 'wget' -- exiting."; exit 5
	fi
	findlatest $JAVA_DIR
	;;
    2) 
	printf "Downloading LTC-<version>.jar via curl:\n\n"
	JAR_FILE=`curl -LIs $DOWNLOAD_URL | grep -e "^Content-Disposition: attachment; filename=" | sed "s/^Content-Disposition: attachment; filename=\"\(LTC-.*\.jar\)\"/\1/"`
	if [ -z "$JAR_FILE" ]; then
	    usage "Couldn't obtain latest JAR_FILE name with 'curl' -- exiting."; exit 6
	fi
	curl -L $DOWNLOAD_URL -o $JAVA_DIR/$JAR_FILE
	if [ $? -gt 0 ]; then
	    usage "Something went wrong during downloading with 'curl' -- exiting."; exit 5
	fi
	;;
    *)
	printf "Unknown FETCH_TYPE -- exiting."; exit 7
	;;
esac

################################################################################
# (optionally) extract Emacs Lisp files

if [ -n "$EMACS_DIR" ]; then
    echo "Inflating Emacs Lisp files in ${EMACS_DIR}:"
    unzip -o "$JAVA_DIR/$JAR_FILE" '*.el' -d "$EMACS_DIR"
    if [ $? -gt 0 ]; then
	usage "Something went wrong when extracting Emacs Lisp files -- exiting."; exit 3
    fi
    [[ "$EMACS_DIR" =~ ^"$HOME"(/|$) ]] && EMACS_DIR="~${EMACS_DIR#$HOME}"
    printf "\nIf Emacs is running, you should now reload the new emacs file with the command:\n\n"
    printf "  \e[34mM-x load-file <RET> %s/ltc-mode.el\e[0m\n\n" $EMACS_DIR
fi

################################################################################
# removing and creating new softlink

rm -f $JAVA_DIR/LTC.jar
ln -v -s $JAR_FILE $JAVA_DIR/LTC.jar

################################################################################
# message

[[ "$JAVA_DIR" =~ ^"$HOME"(/|$) ]] && JAVA_DIR="~${JAVA_DIR#$HOME}"
echo "Done with installing LTC in ${JAVA_DIR}"
printf "To start LTC server with default options, use the following command:\n\n"
printf "  \e[31mjava -jar %s/LTC.jar\e[0m\n\n" $JAVA_DIR

