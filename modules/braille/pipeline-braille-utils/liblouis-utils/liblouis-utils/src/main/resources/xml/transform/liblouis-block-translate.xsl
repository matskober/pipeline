<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:louis="http://liblouis.org/liblouis"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xsl"/>
	
	<xsl:param name="text-transform"/>
	
	<xsl:template match="css:block" mode="#default before after">
		<xsl:variable name="text" as="text()*" select="//text()"/>
		<xsl:variable name="style" as="xs:string*">
			<xsl:apply-templates mode="style"/>
		</xsl:variable>
		<xsl:variable name="lang" select="ancestor-or-self::*[@xml:lang][1]/string(@xml:lang)"/>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<!--
			    FIXME: xml:lang is not used in query anymore because the appropriate way to use
			    sub-translators is to compute them prior to the conversion, and the given query
			    should be of the form (id:...). Also xml:lang may vary inside a block. The new way
			    to pass locale information will be through text attributes. (pf:text-transform)
			-->
			<xsl:with-param name="new-text-nodes" select="louis:translate($text-transform, $text, $style)"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="style" match="*" as="xs:string*">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="source-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="css:deep-parse-stylesheet(@style)[not(@selector)]/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates mode="#current">
			<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="style" match="text()" as="xs:string">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:sequence select="css:serialize-declaration-list($source-style[not(@name='word-spacing')
		                                                                   and not(@value=css:initial-value(@name))])"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='word-spacing']">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="translate-style"
	              match="css:property[@name=('letter-spacing',
	                                         'font-style',
	                                         'font-weight',
	                                         'text-decoration',
	                                         'color')]"/>
	
	<xsl:template mode="translate-style" match="css:property[@name='hyphens' and @value='auto']">
		<xsl:param name="result-style" as="element()*" tunnel="yes"/>
		<css:property name="hyphens" value="manual"/>
	</xsl:template>
	
</xsl:stylesheet>
