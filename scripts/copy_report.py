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

# Copies the files from Smart Pix to ./reports/YYYY-MM-DD/ and renames the files to lowercase
import os
from pathlib import Path
import shutil
import datetime
import subprocess
import lowercase

def copy_report():
  if( not (Path("/media") / os.getlogin() / "SMART_PIX/REPORT").exists() ):
    print("Error: The Smart Pix Device is not connected!")
    os._exit(os.EX_UNAVAILABLE)

  # copy files to new folder
  folder = Path("../reports") / datetime.date.today()
  shutil.copytree("/media/${USER}/SMART_PIX/REPORT", folder)
  subprocess.run(["chmod", "-R +w " + folder], shell=True, check=True) # TODO: replace
  os.chdir(folder)

  # Rename file names and contents to lower case.
  lowercase.rename_lower_recursive()

  # Remove some superfluous stuff.
  subprocess.run(["rm", "-fr img/rd*.gif img/scanning.gif img/*.png"], shell=True) # TODO: replace

  # Don't use bitmaps.
  subprocess.run(['for f in $(find -name "*.bmp"); do g=${f/.bmp/.gif}; convert $f $g; rm $f; ln -s $(basename $g) $f; done'], shell=True) # TODO: replace

  # Add index file.
  os.symlink('_review.htm', 'index.html')

if ( __name__ == "__main__"):
  copy_report()

