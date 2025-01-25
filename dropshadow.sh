#!/bin/bash

for Y in `/bin/ls` ; do convert $Y -shave 1x1 -bordercolor black -border 1 \( +clone -background gray -shadow 80x3+5+5 \) +swap -background none -layers merge +repage ${Y}2.png
done

