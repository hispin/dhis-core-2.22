<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:d="http://dhis2.org/schema/dxf/2.0"
  >

  <xsl:template match="d:sqlView">
    <div class="sqlView">
      <h2>
        <xsl:value-of select="@name" />
      </h2>
      <table border="1">
        <tr>
          <td>ID</td>
          <td> <xsl:value-of select="@id" /> </td>
        </tr>
        <tr>
          <td>Last Updated</td>
          <td> <xsl:value-of select="@lastUpdated" /> </td>
        </tr>
        <tr>
          <td>Description</td>
          <td> <xsl:value-of select="d:description" /> </td>
        </tr>
        <tr>
          <td>SqlQuery</td>
          <td> <xsl:value-of select="d:sqlQuery" /> </td>
        </tr>
      </table>
    </div>
  </xsl:template>

</xsl:stylesheet>
