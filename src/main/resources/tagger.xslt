<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:template match="token">
         <xsl:value-of select="@form" /> => <xsl:value-of select="@form" />, tag#<xsl:value-of select="@tag" />, ctag#<xsl:value-of select="@ctag" />, pos#<xsl:value-of select="@pos" />, lemma#<xsl:value-of select="@lemma" />
    </xsl:template>
</xsl:stylesheet>
