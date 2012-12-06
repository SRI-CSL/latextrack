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

# generate examples for tech report 2012
for i in 1 2 3 4 5
do
    echo " *** looking at diff between version $i and $(( $i + 1 ))"
    java -cp target/${ueber.jar}.jar com.sri.ltc.latexdiff.LatexDiff src/site/resources/examples/independence-version$i.tex src/site/resources/examples/independence-version$(( $i + 1 )).tex > src/tex/techreport2012/examples/diff.$i.$(( $i + 1 )).unix
    java -cp target/${ueber.jar}.jar com.sri.ltc.latexdiff.LatexDiff -x src/site/resources/examples/independence-version$i.tex src/site/resources/examples/independence-version$(( $i + 1 )).tex > src/tex/techreport2012/examples/diff.$i.$(( $i + 1 )).cml
done