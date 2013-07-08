# updating images based on their file endings

TIFF_FILES = $(sort $(wildcard *.tiff))
PNG_FROM_TIFF_FILES = $(TIFF_FILES:.tiff=.png)

.PHONY = all

all : $(PNG_FROM_TIFF_FILES)

$(PNG_FROM_TIFF_FILES) : %.png : %.tiff
	@printf "Converting (with color-type 6) to PNG from %s... " "$*.tiff"
	@convert -define png:color-type=6 $*.tiff $*.png
	@printf "done\n"

clean :
	@for FILE in $(TIFF_FILES) ; do \
		base=`basename $$FILE .tiff`;\
		rm -fv $$base.png ; \
	done
