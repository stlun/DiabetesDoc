# DiabetesDoc

DiabetesDoc is a software to save and transform the data of your diabetes therapy which are read by a Accu-Chek
SmartPix device. This software can automatically save the data from the SmartPix device on your hard drive in a
format which is also compatible for use with Unix/Linux. Additional Output of the data as diary in .PDF-format 
is possible.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites

For this software, you need to have a *Java Runtime* version 8 or newer as well as *bash*,
*make* and *maven* installed. *convert* is recommended, but not necessary.
Additionally you need an Accu-Chek SmartPix device.

To install the necessary software under Ubuntu, you can run the following command in the bash:
```
sudo apt-get install make maven java-common imagemagick
```

### Installing

To install the software in a local directory, you need to clone or download the git project and run `make install`
in the main directory:

* [download it as zip-archive](https://github.com/stlun/DiabetesDoc/archive/master.zip)
* clone it from GitHub by running the following bash command
  ```
  git clone https://github.com/stlun/DiabetesDoc.git
  ```

To uninstall it, you can run `make uninstall` in the main directory or just remove the complete directory.

## Usage

After you have read your devices with the Accu-Chek SmartPix device, you have to run the java application
`java -jar DiabetesDoc.jar`.

You can use it to copy the data to the *reports* directory and to set up the internal data storage in the
*xml* directory or to to create a *diary* in .PDF-format. For more information see the [help](help.html).

## Built With

This software uses the following third party packages:

* [Pdfbox](http://pdfbox.apache.org) - Used to generate the PDF output
* [JDOM](http://www.jdom.org) - Used to read and write XML
* [Maven](https://maven.apache.org/) - Dependency Management (to include Pdfbox and JDOM)

## Author

* **Stephan Lunowa** - *Copyright 2011-2017*

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
along with DiabetesDoc in the file [LICENSE](LICENSE). If not,
see <http://www.gnu.org/licenses/>.

