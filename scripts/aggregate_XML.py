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

import io
from pathlib import Path
import xml.etree.ElementTree as ET

def find_XML_files( root ):
  "Returns the list of all *.xml files contained in root or a subdirectory."
  return Path(root).glob('**/*.xml')

def parse_device_XML_files( filenames ):
  "Parses the given list of *.xml files."

  days = {}
  for name in filenames:
    print('Import', name)

    tree = ET.parse(str(name))
    root = tree.getroot()

    ip = root.find('IP') # profiles from insulin pump
    if( ip is not None ):
      date = ip.get('Dt')
      ET.ElementTree(ip).write("../xml/ipprofiles/" + date + '.xml')
  
    for data in root.findall('IPDATA/*'): # data from insulin pump
      date = data.get('Dt')
      #data.attrib.pop('Dt', None) # remove repeated date
      if(date not in days.keys()):
        days[date] = []
      days[date].append(data)

    for data in root.findall("BGDATA/*"): # data from device
      date = data.get('Dt')
      #data.attrib.pop('Dt', None) # remove repeated date
      if(date not in days.keys()):
        days[date] = []
      days[date].append(data)
  return days

def write_day_xml( days ):
  # write to ../xml/YYYY-MM-DD.xml
  # TODO merge with written days
  # TODO remove duplicates
  for day, elems in days.items():
    root = ET.Element('DAY')
    root.set('Dt', day)
    root.extend(elems)
    try:
      f = open("../xml/" + day + '.xml', 'w')
      f.write('<?xml version="1.0" encoding="utf8"?>\n' \
            + '<?xml-stylesheet type="text/xsl" href="day.xsl"?>\n')
      ET.ElementTree(root).write(f, encoding="unicode")
      f.close()
    except IOException as e:
      print("Error: Could not write to file.", e.args)  

if ( __name__ == "__main__"):
  filenames = find_XML_files("../reports/")
  days = parse_device_XML_files(filenames)
  write_day_xml(days)

