
//test the "abort" if user closed the window by click the "x" button on top-right corner
function win_close()
{		
	//alert("Window close event")
	if(parent.frames.mainFrame.form.mainform!=null) //for abort from quiz page
	{
		if(parent.frames.mainFrame.form.mainform.action)
		{
			//alert("Form found post-close")
			parent.frames.mainFrame.form.mainform.action.value="abort"
			parent.frames.mainFrame.form.mainform.submit()
			window.parent.opener.location.replace('http://localhost:8080/WISE/survey/abort.htm')	 
		}
	}
	else
	{
		return	
	}
}

function init2() {

	if (parseInt(navigator.appVersion)>3)
	{
		 if (navigator.appName=="Netscape")
		 {
			if(parseInt(navigator.appVersion)>=5)
				document.oncontextmenu = disableRightClick
			else
				document.captureEvents(Event.MOUSEDOWN);
		 }
		 else
		 {
			document.onmousedown = mouseDown;
			document.oncontextmenu = disableRightClick
		 }
		  
	}

}

function no_right_click() {

	if (parseInt(navigator.appVersion)>3)
	{
		 if (navigator.appName=="Netscape")
		 {
			if(parseInt(navigator.appVersion)>=5)
				parent.frames.mainFrame.document.oncontextmenu = disableRightClick
			else
				parent.frames.mainFrame.document.captureEvents(Event.MOUSEDOWN);
		 }
		 else
		 {
			parent.frames.mainFrame.document.onmousedown = mouseDown;
			parent.frames.mainFrame.document.oncontextmenu = disableRightClick
		 }
		  
	}

}


//test the "abort" if user closed the window by click the "x" button on top-right corner
function win_close_disabled()
{
	if(parent.frames.mainFrame.form.mainform!=null)
	{
		parent.frames.mainFrame.form.mainform.action.value="ABORT";
		parent.frames.mainFrame.form.mainform.submit();
		window.parent.opener.location.replace('http://localhost:8080/WISE/survey/abort.htm')
	 }
}

//to disable the mouse right click
function mouseDown(e)
{
	 var clickType=1
	 if (parseInt(navigator.appVersion)>3)
	 {
		  if (navigator.appName=="Netscape")
		  	clickType=e.which
		  else
		  	clickType=event.button
	 }
	 if (clickType!=1)
	 {
		  return false
	 }
	 else
	 {
	 	 return true
	 }
}

function disableRightClick()
{
  return false;
}
