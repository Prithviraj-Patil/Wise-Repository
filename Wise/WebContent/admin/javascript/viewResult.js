//remove the check of the option for all invitees
function remove_check_allusers() {
	if (document.form1.alluser.checked)
		document.form1.alluser.checked = false;
}

//remove the check of the option for all the single invitees 
function remove_check_oneuser() {
	for (i = 0; i < document.form1.length; i++) {
		if (document.form1.elements[i].type == "checkbox"
			&& document.form1.elements[i].name != "alluser")
			document.form1.elements[i].checked = false;
	}
}
