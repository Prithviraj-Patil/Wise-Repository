var sid, jid, jstatus;
//sid - survey's internal ID
//jid - survey ID
//jstatus - D: clean up the survey data (developing mode)
//          R: remove the survey data table (developing mode)
//          P: archive the survey data (production mode)

//manipulate the survey data according to the situation selected (jstatus)
function remove_confirm() {
	var msg;
	if (jstatus.toUpperCase() == "R") {
		msg = "\nThis operation will remove the survey and permanently delete all data collected."
			+ "\n(Note this operation is not available for surveys in Production mode.) \nAre you sure you want to continue?\n";
	} else if (jstatus.toUpperCase() == "P") {
		msg = "\nThis operation will remove the survey from the available list and will archive any data collected.\n"
			+ "Are you sure you want to continue?\n";
	} else {
		msg = "\nThis operation will clear all submitted data and associated tracking data for this survey."
			+ "\n(Note this operation is not available for surveys in Production mode.)\nAre you sure you want to continue?\n";
	}
	var url = "dropSurvey?s=" + jid + "&t=" + jstatus;
	if (confirm(msg))
		location.replace(url);
	else
		return;
}

//change the survey mode from developing to production
function change_mode() {
	var msg = "\nYou are about to change the survey mode from development to production.\n"
		+ "Are you sure to continue this operation?\n";
	var url = "dev2prod?s=" + sid;
	if (confirm(msg))
		location.replace(url);
	else
		return;
}
