    //check the browser version 
	function check_browser_version(){
		var vers
		var numb
		var lowest_numb
		var download_site
		var text
	
		var b_agt=navigator.userAgent.toLowerCase()
			
		var b_type = navigator.appName
		//var app_vers = parseInt(navigator.appVersion)
	
		var search_ie=b_agt.indexOf("msie")
		var search_nav=b_agt.indexOf("netscape")
		var search_safari=b_agt.indexOf("applewebkit")
		
		download_ie_site="http://www.microsoft.com/windows/ie/default.asp"
		download_nav_site="http://channels.netscape.com/ns/browsers/default.jsp"
	
		if(search_ie!=-1)
		{
			vers=b_agt.substr(search_ie, 8)
			numb=parseFloat(vers.substr(5))
			if(numb<5.0)
			{
				lowest_numb=5.0
				compose_win(b_type, lowest_numb)
			}
	
		}
		else if(search_nav!=-1)
		{
	
			vers=b_agt.substr(search_nav, 12)
			numb=parseFloat(vers.substr(9))
			if(numb<6.0)
			{
			  lowest_numb=6.0
			  compose_win(b_type, lowest_numb)
			}
	
		}
		
	
	}
	
	function compose_win(b_type, lowest_numb)
	{
		text = "<html>\n<head>\n<title>Version Checking Message</title>\n<body bgcolor=\"#FFFFF5\">\n"
		text += "<p><center><font face=\"Verdana, Arial, Helvetica\" size=3><b>- Alert -</b></font></center></p>"
		text += "<p><font face=\"Verdana, Arial, Helvetica\" size=2>"
		text += "You are using a version of "+b_type+" lower than "+lowest_numb+
		". As a result, the tutorial might not run as designed.</font></p>"
		text += "<p><font face=\"Verdana, Arial, Helvetica\" size=2>"+
		"Please download a newer version of <a href='"+download_ie_site+"' target='_blank'>Microsoft Internet Explorer</a> or <a href='"+download_nav_site+"' target='_blank'>Netscape</a>.</font></p>"
		text += "<p><center><font face=\"Verdana, Arial, Helvetica\" size=2><button onClick='javascript: window.close()'>Close</button></center></font></p>"
		text += "</body>\n</html>\n"
	
		msgWindow = window.open('','newWin','width=340, height=240, screenX=0,screenY=0, left=320, top=240,toolbar=no')
		if(msgWindow!=null)
			msgWindow.document.write(text)
		else
			window.location="http://cme.arc2.ucla.edu/wise_product/file_product/error_browser.htm"
	
	}
	
	function compose_forbidden_win(b_type)
	{
		text = "<html>\n<head>\n<title>Version Checking Message</title>\n<body bgcolor=\"#FFFFF5\">\n"
		text += "<p><center><font face=\"Verdana, Arial, Helvetica\" size=3><b>- Alert -</b></font></center></p>"
		text += "<p><font face=\"Verdana, Arial, Helvetica\" size=2>"
		text += "You are using a version of "+b_type+
		". As a result, the tutorial might not run as designed.</font></p>"
		text += "<p><font face=\"Verdana, Arial, Helvetica\" size=2>"+
		"Please download a newer version of <a href='"+download_ie_site+"' target='_blank'>Microsoft Internet Explorer</a> or <a href='"+download_nav_site+"' target='_blank'>Netscape</a>.</font></p>"
		text += "<p><center><font face=\"Verdana, Arial, Helvetica\" size=2><button onClick='javascript: window.close()'>Close</button></center></font></p>"
		text += "</body>\n</html>\n"
		
		popWindow = window.open('','popWin','width=340, height=240, screenX=0,screenY=0, left=320, top=240,toolbar=no')
		if(popWindow!=null)
			popWindow.document.write(text)
		else
			window.location="http://cme.arc2.ucla.edu/wise_product/file_product/error_browser.htm"
	}
	
// Check whether cookies enabled
function check_cookie(){
   document.cookie = "Enabled=true";
   var cookieValid = document.cookie;
   
   // if retrieving the VALUE we just set actually works 
   // then we know cookies enabled
   if (cookieValid.indexOf("Enabled=true") == -1)
   {
      alert("Please turn the cookie ON for your browser!");
   }
}