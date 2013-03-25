# updating images based on their file endings

TIFF_FILES = $(sort $(wildcard *.tiff))
PNG_FROM_TIFF_FILES = $(TIFF_FILES:.tiff=.png)
PDF_FILES = $(sort $(wildcard manual-figure*.pdf))
PNG_FROM_PDF_FILES = $(PDF_FILES:.pdf=.png)

.PHONY = all

all : $(PNG_FROM_TIFF_FILES) $(PNG_FROM_PDF_FILES)

$(PNG_FROM_TIFF_FILES) : %.png : %.tiff
	@printf "Converting to PNG from %s... " "$*.tiff"
	@convert $*.tiff $*.png
	@printf "done\n"

$(PNG_FROM_PDF_FILES) : %.png : %.pdf
	@printf "Converting (with density 150) to PNG from %s... " "$*.pdf"
	@convert -density 150 $*.pdf $*.png
	@printf "done\n"

clean :
	@for FILE in $(TIFF_FILES) ; do \
		base=`basename $$FILE .tiff`;\
		rm -fv $$base.png ; \
	done
	@for FILE in $(PDF_FILES) ; do \
		base=`basename $$FILE .pdf`;\
		rm -fv $$base.png ; \
	done
