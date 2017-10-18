<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
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
-->

<xsl:template match="/">
  <html>
    <head>
      <style type="text/css">
        body { bgcolor: #222222; color: #BBBBBB; }
        table {
          empty-cells: show;
          border-collapse: collapse;
        }
        thead {
          background-color: black;
          color: white;
          text-align: center;
        }
        td {
          border: 1px solid #444444;
          padding: 2px 4px;
        }
      </style>
    </head>
    <body>
      <xsl:apply-templates />
    </body>
  </html>
</xsl:template>

<xsl:template match="DAY">
  <h1>Accu-Chek Smart Pix Data of <xsl:value-of select="@Dt" /></h1>

  <h3>Bg Measurements</h3> 
  <table>
    <thead><tr>
	    <td>Time</td>
	    <td width="50">Bg Value</td>
	    <td>Flags</td>
	    <td>Insulin 1</td>
	    <td>Insulin 2</td>
	    <td>Insulin 3</td>
	    <td>Carbs</td>
	    <td>Event</td>
    </tr></thead>
 
 	<xsl:for-each select="BG">
 	<xsl:sort select="@Tm"/>
 	<xsl:choose>
   	<xsl:when test = "@Ctrl>0 or @Ctrl='?'" ></xsl:when>
   	<xsl:otherwise>
   	<tr> 
   		<td><xsl:if test="not(@Tm)">&#160;</xsl:if><xsl:value-of select="@Tm" /></td>
   		<td align="right"><b><xsl:value-of select="@Val" /></b></td>
   		<td><xsl:if test="not(@Flg)">&#160;</xsl:if><xsl:value-of select="@Flg" /></td>
   		<td align="right"><xsl:if test="not(@Ins1)">&#160;</xsl:if><xsl:value-of select="@Ins1" /></td>
		  <td align="right"><xsl:if test="not(@Ins2)">&#160;</xsl:if><xsl:value-of select="@Ins2" /></td>
		  <td align="right"><xsl:if test="not(@Ins3)">&#160;</xsl:if><xsl:value-of select="@Ins3" /></td>
		  <td align="right"><xsl:if test="not(@Carb)">&#160;</xsl:if><xsl:value-of select="@Carb" /></td>
		  <td><xsl:if test="not(@Evt)">&#160;</xsl:if><xsl:value-of select="@Evt" /></td>
	  </tr>
	  </xsl:otherwise>
	</xsl:choose>
 	</xsl:for-each>
  </table>

  <h3>Bolus</h3> 
  <table>
    <thead><tr>
 		  <td>Time</td>
 		  <td>Bolus U</td>
 		  <td>Type</td>
 		  <td>Comment</td>
   	</tr></thead>

 	<xsl:for-each select="BOLUS">
 	<xsl:sort select="@Tm"/>
 	<xsl:choose> 
   	<xsl:when test = "@remark='Bolus+Basal Total'" >
   	<tr>
   		<td align="right"><b>&#160;</b></td>
   		<td align="right"><b><xsl:value-of select="@amount"/></b></td>
   		<td><b>&#160;</b></td>
	    <td><b><xsl:value-of select="@remark" /></b></td>
    </tr>
   	</xsl:when>
	  <xsl:when test = "@remark='Bolus Total'" >
   	<tr>
   		<td align="right">&#160;</td>
   		<td align="right"><b><xsl:value-of select="@amount"/></b></td>
   		<td>&#160;</td>
	    <td><b><xsl:value-of select="@remark" /></b></td>
    </tr>
   	</xsl:when>
	  <xsl:otherwise>
   	<tr> 
   		<td align="right"> <xsl:value-of select="@Tm" /></td>
   		<td align="right"><xsl:value-of select="@amount" /></td>
   		<td><xsl:value-of select="@type"/></td>
	    <td>
        <xsl:if test="not(@remark)">
            <xsl:if test="not(@cmd)">&#160;</xsl:if>
            <xsl:if test="@cmd='1'">Adv</xsl:if>
            <xsl:if test="@cmd='2'">Rmt</xsl:if>
        </xsl:if> 
        <xsl:if test="(@remark)">
            <xsl:value-of select="@remark" />
            <xsl:if test="@cmd='1'">, Adv</xsl:if>
            <xsl:if test="@cmd='2'">, Rmt</xsl:if>
        </xsl:if> 
      </td>
    </tr>
	  </xsl:otherwise>
	</xsl:choose>
 	</xsl:for-each>

  </table>
 
  <h3>Basal</h3> 
  <table>
    <thead><tr>
   		<td>Time</td>
   		<td>Basal Rate U/h</td>
   		<td>Profile</td>
   		<td>Inc</td>
   		<td>Dec</td>
   		<td>Comment</td>
    </tr></thead>

 	<xsl:for-each select="BASAL">
  <xsl:sort select="@Tm"/>
 		<tr> 
   		<td align="right"><xsl:value-of select="@Tm" /></td>
   		<td align="right"><xsl:value-of select="@cbrf" /></td>
   		<td><xsl:if test="not(@profile)">&#160;</xsl:if><xsl:value-of select="@profile" /></td>
   		<td><xsl:if test="not(@TBRinc)">&#160;</xsl:if><xsl:value-of select="@TBRinc" /></td>
   		<td><xsl:if test="not(@TBRdec)">&#160;</xsl:if><xsl:value-of select="@TBRdec" /></td>
		  <td>
        <xsl:if test="not(@remark)">
            <xsl:if test="not(@cmd)">&#160;</xsl:if>
            <xsl:if test="@cmd='1'"> Adv</xsl:if>
            <xsl:if test="@cmd='2'"> Rmt</xsl:if>
        </xsl:if> 
        <xsl:if test="(@remark)">
            <xsl:value-of select="@remark" />
            <xsl:if test="@cmd='1'">, Adv</xsl:if>
            <xsl:if test="@cmd='2'">, Rmt</xsl:if>
        </xsl:if> 
      </td>
	  </tr>
	</xsl:for-each>

  </table>

  <br/>

  <h3>Events</h3> 
  <table>
    <thead><tr>
      <td>Time</td>
      <td>Event</td>
      <td>Description</td>
    </tr></thead>
 
 	<xsl:for-each select="EVENT">
  <xsl:sort select="@Tm"/>
 	 	<tr> 
   		<td align="right"><xsl:value-of select="@Tm" /></td>
   		<td align="right"><xsl:if test="not(@shortinfo)">&#160;</xsl:if><xsl:value-of select="@shortinfo" /></td>
   		<td align="right"><xsl:if test="not(@description)">&#160;</xsl:if><xsl:value-of select="@description" /></td>
	  </tr>
	</xsl:for-each>

  </table>
 
</xsl:template>

</xsl:stylesheet>
