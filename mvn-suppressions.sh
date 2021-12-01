function suppressSiteWarnings() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] Unable to find a URL to the parent project\\. The parent menu will NOT be added\\.$"
}

# JaCoCo Maven Plugin: The Maven Plugin is referencing a moved Maven artifact.
function suppressJaCoCoMavenPlugin() {
	cat < /dev/stdin \
	| grep --invert-match --perl-regexp "^\\[WARNING\\] The artifact xml-apis:xml-apis:jar:2\\.0\\.2 has been relocated to xml-apis:xml-apis:jar:1\\.0\\.b2$"
}

cat < /dev/stdin \
| suppressSiteWarnings \
| suppressJaCoCoMavenPlugin
