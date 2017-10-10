#!/usr/bin/python3
################################################################################
#
# This file is part of DiabetesDoc.
#
#   Copyright 2017 Stephan Lunowa
#
# DiabetesDoc is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# DiabetesDoc is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with DiabetesDoc. If not, see <http://www.gnu.org/licenses/>.
#
################################################################################

# convert files and some contents to lowercase (by default in '.')
import os
import re
from pathlib import PurePath

def rename_lower(dirpath, name):
  upper_path = os.path.join(dirpath, name)
  lower_path = os.path.join(dirpath, name.lower())
  os.rename(upper_path, lower_path)
  return lower_path

def rename_lower_recursive( rootdir ):
  pattern = re.compile('"([\w/.]+?)\.([\w/.]+?)"')

  for dirpath, dirnames, filenames in os.walk(rootdir, topdown=False):
    # rename directories
    for name in dirnames:
      rename_lower(dirpath, name)

    # rename files and change contents
    for name in filenames:
      filename = rename_lower(dirpath, name)
      suffix = PurePath(name).suffix
      if( suffix == ".bmp" or suffix == ".gif" or suffix == ".png" ):
        continue
      try:
        f = open(filename, "r")
        text = f.read()
        f.close()
      except IOError as e:
        print("Error reading file", filename, ":", e.args)
      else:
        replaced = pattern.sub(lambda m: m.group(0).lower(), text)
        try:
          f = open(filename, "w")
          f.write(replaced)
          f.close()
        except IOError as e:
          print("Error writing file", filename, ":", e.args)

if ( __name__ == "__main__"):
  rename_lower_recursive(".")

