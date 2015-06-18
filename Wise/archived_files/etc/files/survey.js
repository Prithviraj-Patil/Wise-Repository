var startDate
var startSecs //the starting time of clock
var start_time //the starting time to count losing mousemove event

var timerID = null
var timerRunning = false

var nowSecs  //the current time in seconds
var elapsedSecs //total time
var timeValue;

var hours
var minutes
var seconds

var run_once=0


//to start the clock
function startclock()
{
	startDate = new Date();
	startSecs = (startDate.getHours()*60*60)+(startDate.getMinutes()*60)+startDate.getSeconds();
	start_time=startSecs;
	stopclock();
	
	//running the clock
	showtime();
}

//to stop the clock
function stopclock()
{
	if(timerRunning)
		clearTimeout(timerID);
	timerRunning = false;
}

//show the running time
function showtime()
{
	var now = new Date();
	nowSecs = (now.getHours()*60*60) + (now.getMinutes()*60) + now.getSeconds();
	elapsedSecs = nowSecs - startSecs;
	//if(elapsedSecs<1*60)
	if(elapsedSecs<28*60)
	{
		//check the tolerance as 25 mints
		if(elapsedSecs>=25*60 && run_once==0)
		//if(elapsedSecs>=1*10 && run_once==0)
		{
			//set run once flag
			run_once = 1;
			
			//get the current time
			hours = Math.floor(elapsedSecs / 3600);
			elapsedSecs = elapsedSecs - (hours*3600);
			minutes = Math.floor(elapsedSecs / 60);
			elapsedSecs = elapsedSecs - (minutes*60);
			seconds = elapsedSecs;

			//var timeValue = "" + hours
			timeValue = "";
			if(minutes > 0)
				timeValue  += minutes + " minute" + ((minutes < 2) ? "" : "s");
			if(minutes > 0 && seconds > 0)
				timeValue  += " and ";
			if(seconds > 0)
				timeValue  += seconds + " second" + ((seconds < 2) ? "" : "s");
				
			//popup warning window
			pop_win();
		
		}
		
		//continue to run the clock
		timerID = setTimeout("showtime()",1000);
		timerRunning = true;
	}
	else
	{
		stopclock();
		//alert("You spent more than 28 min on this page. Your session is put into end.");
		//after 5 seconds to wait for the alert window to popup, expire the session and forward to timeout page
		setTimeout("go_win()", 5000);
	}
}


//pop up the window for reminding doing the quiz first
function pop_win()
{
    	text =  "<html>\n<head>\n<title>Goback to Survey</title>\n<body bgcolor=\"#FFFFF5\">\n"
	text += "<p><font face=\"Verdana, Arial, Helvetica\" size=1>"
	text += "You have spent " + timeValue + " on this survey page.<br>"
	text += "Please close this pop-up window and finish this page within the next three minutes.<br>"
	text += "Otherwise your session will be loggoed off, but your data will be saved."
	text += "</font></p><p><center><button onClick='javascript: opener.focus(); opener.back_flag=1; window.close()'>Close</button></center></p>"
	text += "</body>\n</html>\n"
	newWindow = window.open('','newWin','width=300, height=145, screenX=0,screenY=0, left=0,top=0,toolbar=no')
	newWindow.document.write(text)
	if (newWindow.opener == null) 
		newWindow.opener = self;
}

function go_win()
{
	
	//parent.frames.mainFrame.form.mainform.action.value="timeout";
	//parent.frames.mainFrame.form.mainform.submit();
	document.mainform.action.value = "timeout";
	check_and_submit();	
}


//document.write("<h2>"+top.popupflag+"</h2>");
function RangeCheck(target, lower, upper) 
{
	if (isNaN(target.value) || target.value < lower || target.value > upper)
	{
		alert(target.value +" is outside the valid range for "+ target.name
			+" ("+ lower +" to "+ upper +")");
		target.value = "";
		target.focus();
	}
}

function fill_in(fieldName)
{
	document.write(top.userVals[fieldName]);
}

function SizeCheck(target, size) 
{
	if (target.value.length > size)
	{
		alert("The text you entered is too large.  It must be less than "+size+" characters.");
		target.focus();
	}
}

function setFields() 
{
	var flag = 0;
	if (top.fieldVals) 
	{
		for (var fieldName in top.fieldVals)
		{
			elementResult = document.mainform.elements[fieldName];
			if (elementResult != null)
			{
				if (elementResult.type == "text" || elementResult.type == "textarea" ) 
					elementResult.value = top.fieldVals[fieldName];
				else if (elementResult.type == "checkbox") 
					elementResult.checked = true;
				else
				{
					flag = 0;
					for (j=0; j<elementResult.length; j++)
					{
						if ( (elementResult[j].value == top.fieldVals[fieldName]) && (elementResult[j].type == "radio") )
						{
							elementResult[j].checked = true;
							flag = 1;
						}
					}
					if (!flag)
					{
						for (j=0; j<elementResult.length; j++) 
							if (elementResult[j].type == "text")
								elementResult[j].value = top.fieldVals[fieldName];
					}
					else
					{
						for (j=0; j<elementResult.length; j++) 
							if (elementResult[j].type == "text")
								elementResult[j].value = '';
					}
				}
			}
		}
	}
	startclock()
}

function isblank(s)
{
	for (var i = 0; i < s.length; i++)
	{
		var c = s.charAt(i);
		if ((c != ' ')&&(c != '\n') && (c != '\t')) return false
	}
	return true;
}

function check_and_submit() 
{
	var flag = 0;
	if (top.requiredFields) 
	{
		for (var fieldName in top.requiredFields)
		{
			var elementResult = document.mainform.elements[fieldName];

			if (elementResult.type == "text") 
			{
				if (isblank(elementResult.value))
				{
					flag = 1;
				}
			}
			else if (elementResult.type == "checkbox") 
				flag = 0;
			else
			{
				var check_flag = 0;
				for (j=0; j<elementResult.length; j++)
				{
					if ( (elementResult[j].checked == true) )
						check_flag = 1;
				}
				if (check_flag == 0)
				{
					flag = 1;
				}
			}

		}
	}

	if (flag == 0)
	{
		document.mainform.submit();
	}
	else
	{
		alert("This page has required questions which you have not answered.  Please answer all questions marked \"required\" before continuing.");
	}
}

function PageSkip(targPg)
{
	document.mainform.nextPage.value = targPg;
	check_and_submit();
}

function putDaytime() 
{
	today = new Date();
	if (today.getHours() < 12) 
		document.write('morning');
	else if (today.getHours() < 17) 
		document.write('afternoon');
	else 
		document.write('evening');
}

function clearButtons(keyArray) 
{
	for (i=0; i<keyArray.length; i++) 
	{
		elementResult = document.mainform.elements[keyArray[i]];
		if (elementResult.type == "text") 
			elementResult.value = "";
		else if (elementResult.type == "checkbox") 
			elementResult.checked = false;
		else
		{
			for (j=0; j<elementResult.length; j++) 
				if (elementResult[j].type == "text") 
					elementResult[j].value = "";
				else 
					elementResult[j].checked = false;
		} 
	}
}
