#!/bin/bash

# generate examples for tech report 2012
for i in 1 2 3 4 5
do
    echo " *** looking at diff between version $i and $(( $i + 1 ))"
    java -cp target/${ueber.jar}.jar com.sri.ltc.latexdiff.LatexDiff src/site/resources/examples/independence-version$i.tex src/site/resources/examples/independence-version$(( $i + 1 )).tex > src/tex/techreport2012/examples/diff.$i.$(( $i + 1 )).unix
    java -cp target/${ueber.jar}.jar com.sri.ltc.latexdiff.LatexDiff -x src/site/resources/examples/independence-version$i.tex src/site/resources/examples/independence-version$(( $i + 1 )).tex > src/tex/techreport2012/examples/diff.$i.$(( $i + 1 )).cml
done