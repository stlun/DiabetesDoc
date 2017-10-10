# DiabetesDoc

*Copyright 2017 Stephan Lunowa*

Content
* What is DiabetesDoc
* Installation
* Usage
* Licence

## What is DiabetesDoc
DiabetesDoc is a software to save and transform the data of your diabetes therapy which are read by a Accu-Chek
SmartPix device. This software can automatically save the data from the SmartPix device on your hard drive in a
format which is also compatible for use with unix/linux. Additional Output of the data as diary in .PDF-format 
is possible.

## Installation
To install the software in a local directory, you need to clone or download the git project and run the `install.sh`
script in its main directory. Additionally you need a current install of *python3* and a Accu-Chek SmartPix device.

## Usage
After you have read your devices with the Accu-Chek SmartPix device, you have to start the script
`script/copy_report.py` to copy the data to the *reports* directory and to set up the internal data storage in the
*xml* directory.

If you want to create a *diary* in .PDF-format, run the script `script/diary.py` and choose the time period in the
dialog. The file will then be created in the *pdf* directory.
**This function is currently not supported but will be implemented soon.**

## Licence

DiabetesDoc is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DiabetesDoc is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DiabetesDoc. If not, see <http://www.gnu.org/licenses/>.

