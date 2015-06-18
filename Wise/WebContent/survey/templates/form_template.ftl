<form name='mainform' method='post' action='readform'>
	<!--The action field is set  to indicate info of interrupt/abort/done/interview etc. -->
	<input type='hidden' name='action' value=''>
	<input type='hidden' name='nextPage' value='${nextPage}'>
	${items}
	<center>
		<a href="javascript:document.forms['mainform'].action.value='interrupt';document.forms['mainform'].submit();">
			<img src='imageRender?img=save.gif' alt='save' style='margin:1ex'>
		</a>
		<a href='javascript:check_and_submit();'>
			<img src='imageRender?img=${submitImage}.gif' alt='${submitImage}' style='margin:1ex'>
		</a>
	</center>
</form>