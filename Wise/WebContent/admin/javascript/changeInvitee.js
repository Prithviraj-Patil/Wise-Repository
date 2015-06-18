  var c_n, c_t, c_d;
  //c_n - column name
  //c_t - column data type
  //c_d - column default value

  //assign the field values in the form when to update
  function put_update_value()
  {    
      document.form1.cname.value=c_n;
      document.form1.ctype.value=c_t;
      document.form1.cdefault.value=c_d;
      document.form1.cedit.value ="update";
      document.form1.coname.value = c_n;
  }

  //clean up the field values in the form when to delete
  function put_delete_value()
  {
      document.form1.cname.value="";
      document.form1.ctype.value="";
      document.form1.cdefault.value="null";
      document.form1.cedit.value ="delete";
      document.form1.coname.value = c_n;
  }

  //display the warning message before removing the column
  function check_submit()
  {
      var msg;
      if(document.form1.cedit.value == "delete")
      {
        msg = "the column "+ c_n +" will be dropped from the invitee table. Do you want to proceed?";
        if (confirm(msg))
          return true;
        else
          return false;
      }
      else
        return true;     
  }