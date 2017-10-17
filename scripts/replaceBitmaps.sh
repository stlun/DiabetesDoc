#!/bin/bash

# Don't use bitmaps.
for f in `find -name "*.bmp"`; do g=${f/.bmp/.gif}; convert $f $g; rm $f; ln -s `basename $g` $f; done

