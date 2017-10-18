# DiabetesDoc

*Copyright 2013-2017 Stephan Lunowa*

###Content
* What is DiabetesDoc
* Installation and dependencies
* Usage
* License

## What is DiabetesDoc
DiabetesDoc is a software to save and transform the data of your diabetes therapy which are read by a Accu-Chek
SmartPix device. This software can automatically save the data from the SmartPix device on your hard drive in a
format which is also compatible for use with Unix/Linux. Additional Output of the data as diary in .PDF-format 
is possible.

## Installation and dependencies
For this software, you need to have a current *Java Runtime* as well as *bash*, *make* and *maven* installed.
*convert* is recommended, but not necessary. Additionally you need an Accu-Chek SmartPix device.

To install the software in a local directory, you need to clone or download the git project and run `make install`
in the main directory.

## Usage
After you have read your devices with the Accu-Chek SmartPix device, you have to start the java application
`DiabetesDoc.jar`.

You can use it to copy the data to the *reports* directory and to set up the internal data storage in the
*xml* directory or to to create a *diary* in .PDF-format.

## License

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

It includes the third party packages **jdom2** and **pdfbox** with the following licenses:
* JDOM (see www.jdom.org) with own license, following below
* Pdfbox (see pdfbox.apache.org) with Apache License Version 2.0 (see www.apache.org/licenses/).

