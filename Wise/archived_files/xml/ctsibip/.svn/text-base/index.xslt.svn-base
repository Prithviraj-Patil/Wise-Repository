<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:template match="/">
		<html>
			<head>
				<title>Web-based Interactive Survey Environment (WISE) : Welcome</title>
				<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
			</head>
			<body bgcolor="#FFFFCC" text="#000000">
				<center>
					<table width="100%" border="0" cellspacing="2" cellpadding="3">
						<tr>
							<td width="15%" align="left" valign="top">
								<img src="imageRender?img={/body/logoImage}" width="75" height="75">
								</img>
							</td>
							<td align="center" valign="middle">
								<img src="imageRender?img={/body/titleImage}" width="400" height="50"></img>
							</td>
						</tr>
						<tr>
							<xsl:value-of select="body/desc" />
						</tr>
						<tr>
							<a href="{/body/link}">
								<xsl:value-of select="body/hlink" />
							</a>
						</tr>
					</table>
				</center>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>