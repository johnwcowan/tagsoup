<!-- Generate Java code to be inserted into HTMLModels.java.  -->
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tssl="http://www.ccil.org/~cowan/XML/tagsoup/tssl"
	version="1.0">

  <xsl:output method="text"/>

  <xsl:strip-space elements="*"/>

  <!-- The main template.  We are going to generate Java constant
       definitions for the groups in the file.  -->
  <xsl:template match="tssl:schema">
    <xsl:apply-templates select="tssl:group">
      <xsl:sort select="@id"/>
    </xsl:apply-templates>
    <!-- Handle the special M_PCDATA group, which is not in the file.  -->
    <xsl:call-template name="tssl:group">
      <xsl:with-param name="id" select="'M_PCDATA'"/>
      <xsl:with-param name="number" select="count(tssl:group) + 1"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generate a declaration for a single group.  -->
  <xsl:template match="tssl:group" name="tssl:group">
    <xsl:param name="id" select="@id"/>
    <xsl:param name="number" select="position()"/>
    <xsl:text>&#x9;public static final int </xsl:text>
    <xsl:value-of select="$id"/>
    <xsl:text> = 1 &lt;&lt; </xsl:text>
    <xsl:value-of select="$number"/>
    <xsl:text>;&#xA;</xsl:text>
  </xsl:template>

</xsl:transform>
