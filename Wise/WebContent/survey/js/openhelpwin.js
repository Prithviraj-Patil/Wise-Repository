function open_helpwin(){
 var helpwin=window.open('/WISE/admin/result_help.htm', 'help_win', 'height=500, width=500, scrollbars=yes, toolbar=no')
 if (helpwin.opener==null)
   helpwin.opener = self;
}