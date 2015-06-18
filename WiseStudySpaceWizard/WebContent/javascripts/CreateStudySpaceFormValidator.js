function validateForm()
{
	
var formElementName = ["studySpaceName","serverURL","serverAppName","sharedFiles_linkName","dirName","dbuser","dbpass","dbname","projectTitle","dbCryptKey","emailSendTime"];

for(var i=0;i<formElementName.length;i++){
	var x=document.forms["createStudySpaceForm"][formElementName[i]].value;
	if (x==null || x=="")
	  {
	  alert(formElementName[i]+" must be filled out");
	  return false;
	  }
}


}