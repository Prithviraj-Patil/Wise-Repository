//Version 1.31.2013.1
$(document).ready(function(){

	//------------------------------------------------------------------------------//
	//Brief description of current implementation of form elements
	//1. Page consists of page_items. Some page_items are of the "repeating_set" type
	//2. Page input items not in the "repeating" set are handled as before.
	//3. On a given page, find all the "repeating set" items, and get the values inside them using json
	//4. User can add more items to these in two steps. First user enters name and presses "add".
	//	 Then user fills details and says save. Ajax call adds it to the backend, returns the id for the row.
	//	 The newly returned id is taken and a div is created for the particular saved item
	//5. To edit any item here, change values and click save. This gets to the database.

	//for tooltips
	//$(function(){$('a[rel=tooltip').tooltip();});
	//----------------------------------------------------------------------------//
	//Get JSON from server for each repeating item set and display its instances


	setTimeout(function() {
		$("div.repeating_item_instance_details").hide('fast');
	}, 1000);

	$(".repeating_item_set").each(function(){

		$("body").addClass("loading");

		var item_set_name = $(this).find(".repeating_question").last().attr("name");
//		console.log("Repeating item set name is "+item_set_name);

		//pass milliseconds to prevent IE caching issues with Ajax
		var milliseconds = new Date().getTime();
		//get the JSON object
		$.getJSON("./repeating_item_io?repeat_table_name="+item_set_name+"&"+"time="+milliseconds, function(json_response) {
		
			var instance_number = 0;
			for(instance in json_response){
				instance_number++;
			}

			if(instance_number > 0){
				changePlaceholderValue(item_set_name);
			}

			add_repeating_set_instances(json_response, item_set_name);

			$("body").removeClass("loading");
		});

		function add_repeating_set_instances(json_response, item_set_name){
			for(instance in json_response)
			{
				var instance_name = json_response[instance].instanceName;
				var instance_id = instance;

//				console.log(instance_name);
				if(instance_name!="")
				{
					var add_html = "<div class='repeating_item_instance' style='background-color:#e5e5e5;'>";
					add_html  += "<a class='delete_repeating_instance' href='#' style='display:inline;margin-left:5px;margin-right:200px;letter-spacing:0;float:right;'>Delete</a>";
					add_html += "<a class='edit_repeating_instance' href='#' style='display:inline;letter-spacing:0;float:right;'>Edit/Review</a>";
					add_html +="<h3 style='font-size:1.5em;font-weight:bold;margin-bottom:0'>"+instance_name+"</h3>";
					add_html += "</div>";

					var div_id_for_instance = item_set_name+"_"+instance_id;
					
					//Get contents of this div as html
					//create a string which has a div, with div id, and then put all the content inside that div
					add_html += "<div id='"+ div_id_for_instance +"' name="+instance_name+" class='repeating_item_instance_details' style='display:none'>";		
					$("#repeating_set_with_id_"+item_set_name).find(".add_item_to_repeating_set").children("div").not(".wrapper_for_add_cancel").not("div[style*='display:none']").each(function(){

						var html_in_add_repeat_set = $(this).html();
						var modified_html = $("<div>"+html_in_add_repeat_set+"</div>");

						//change the ids of the input elements

						$(modified_html).find(":input").each(function() {
							//						var input_element_id = $(this).attr("id");
							var input_element_name = $(this).attr("name");

//							if($.browser.msie === true){
//							$(this).replaceWith(
//							document.createElement(
//							this.outerHTML.replace(/name=\w+/ig, 'name=repeat_'+input_element_name)
//							));
//							}
//							else{
							$(this).attr("name","repeat"+"_"+input_element_name);
//							}
						}); 
						add_html += "<div>";					
						add_html += $(modified_html).html();
						add_html += "</div>";
					});
					add_html += "<a href='#' class='save_repeating_item_instance btn-primary btn-small'> Save Changes </a>";
					add_html += "</div>";

//					console.log(add_html);
					var modified_add_html = $(add_html);

					$(modified_add_html).clone().prependTo("div#repeating_set_with_id_"+item_set_name+" > .repeating_question[name=\""+item_set_name+"\"]");

					//code to handle filling the newly appended instance with values
					var instance_answers = json_response[instance_id].answers;

					for(j in instance_answers)
					{
//						console.log("Value of j is "+j);
						
						var questionId = j;
						var answer = instance_answers[j].answer;
						var answerType = instance_answers[j].type;
						
						var input_selector = ":input[name='"+questionId+"']";
//								console.log("Input selector is: "+ input_selector);
						var input_item_value = answer;

								$("#repeating_set_with_id_"+item_set_name+" > .repeating_question > div#"+div_id_for_instance).find(input_selector).each(function(){
									set_input_element_value($(this), input_item_value);
								});

					}
				}

			}
		}
	});

	function set_input_element_value(input_selector,instance_item_value)
	{

		input_selector.each(function(){
			switch(this.type){
			case "password":
			case "select-multiple":
			case "select-one":
			case "text":
			case "textarea":
				$(this).val(instance_item_value);
				break;
			case "checkbox":
				$(this).attr("checked",true);
				break;
			case "radio":
				if($(this).attr("value")==instance_item_value) $(this).attr("checked",true);
				break;
			}

		});
	}

	function get_input_element_value(input_selector){
		var input_value = "null";

		input_selector.each(function(){

			var input_type = this.type;

			switch(input_type){
			case "password":
			case "select-multiple":
			case "select-one":
			case "text":
			case "textarea":
				input_value = $(this).val();
				break;
			case "checkbox":
			case "radio":
				if($(this).is(":checked")){input_value =  $(this).attr("value");}
				else{input_value = "unchecked";}
				break;
			}


		});

		return input_value;

	}
	//===================================================================================================/

	//---------------------------------------------------------------------------------------------------/
	//Add the repeating instance that user has entered
	$(".add_repeat_instance_name_button").click(function(){

		isARepeatingInstanceAddedGlobal = true;
		var instance_name = $(this).parent().children(".repeat_item_name").val();
		$(this).parent().children(".repeat_item_name").val('');

		instance_name = instance_name.replace(/[^a-z0-9\s]/gi, '');

		if(jQuery.trim(instance_name).length > 0){

			var instance_name_header = "<div class='instance_name'><h3 style='font-weight:bold'>"+instance_name+"</h3></div>";
			$(this).parent().parent().children(".add_item_to_repeating_set").find('.instance_name').remove();
			$(this).parent().parent().children(".add_item_to_repeating_set").prepend(instance_name_header);
			$(this).parent().parent().children(".add_item_to_repeating_set").find('.add_repeat_item_save_button').addClass('unsaved');
			$(this).parent().parent().children(".add_item_to_repeating_set").show("slow");
		}else{
			alert("Please enter a valid value in the text box");
		}

		return false;
	});
	//===================================================================================================/

	//---------------------------------------------------------------------------------------------------//
	//Repeat item save is clicked in a particular instance of repeating item
	$(".add_repeat_item_save_button").click(function () {

		$(this).removeClass("unsaved");

		//Clear the add repeat item save box
		$(this).parent().parent().siblings().find(".repeat_item_name").val('');

		var item_set_name= $(this).parent().parent().parent().attr("id").replace("repeating_set_with_id_","");

		//set the global variable to keep track of whether user has saved
		isARepeatingInstanceAddedGlobal = false;
		//Get the project instance name, all will have the same name
		var project_name = $(this).parent().siblings(".instance_name").text();


		// get the database table name
		var data_table_name = $(this).parent().parent().siblings(".repeating_question").attr("name");
//		console.log("Table name is "+data_table_name);

		// get the instance id
		var instance_id;//not initializing

		var selector = $(this).parent().parent();
		var generatedKey = -1;

		
		//console.log("Starting the when function");
		$.when(create_send_json(selector,data_table_name, instance_id,project_name, true),selector).then(function(genKey,div_selector){
			generatedKey = String(genKey[0]);
		
			//console.log("done with the when function with genKey "+genKey[0]+"!");
			
			var project_name_as_id = data_table_name+"_"+generatedKey;

		
			if(generatedKey == -1){
				alert("Data could not be saved due to network issues, please try again.");
			}

			//Get contents of this div as html
			//create a string which has a div, with div id, and then put all the content inside that div
			var add_html = "<div class='repeating_item_instance' style='background-color:#e5e5e5;'>";
			add_html  += "<a class='delete_repeating_instance' href='#' style='display:inline;margin-left:5px;margin-right:200px;letter-spacing:0;float:right;'>Delete</a>";
			add_html += "<a class='edit_repeating_instance' href='#' style='display:inline;letter-spacing:0;float:right;'>Edit/Review</a>";
			add_html +="<h3 style='font-size:1.5em;font-weight:bold;margin-bottom:0'>"+project_name+"</h3>";
			add_html += "</div>";

			add_html += "<div id='"+project_name_as_id+"' class='repeating_item_instance_details'>";		


			$(div_selector[0]).children("div").not(".wrapper_for_add_cancel").not("div[style*='display:none']").not(".instance_name").each(function(){

				var html_in_div = $(this).html();

				var modified_html_in_div = $("<div>"+html_in_div+"</div>");

				//Make sure input values are set in the input elements

				//change the ids of the input elements

				$(modified_html_in_div).find(":input").each(function() {
					var input_element_name = $(this).attr("name");

//					if($.browser.msie === true){
//					$(this).replaceWith(
//					document.createElement(
//					this.outerHTML.replace(/name=\w+/ig, 'name=repeat_'+input_element_name)
//					));
//					}
//					else{
					$(this).attr("name","repeat"+"_"+input_element_name);
//					}
//					$(this).val($(this).attr("value"));
				});


				add_html += "<div>";		
				add_html += $(modified_html_in_div).html();
				add_html += "</div>";
			});
			add_html += "<a href='#' class='save_repeating_item_instance'> Save Changes </a>";
			add_html += "</div>";


			//append this div to the repeating questions parent div
			$("#repeating_set_with_id_"+item_set_name+" > div.repeating_question").prepend(add_html);

			var input_values = {};
			$(div_selector[0]).find(":input").each(function(){

				var input_name = $(this).attr("Name");
				var input_value = get_input_element_value($(this));

				//	console.log("Setting: Input name is "+input_name+" value is "+input_value);

				if(input_value != "null" && input_value !="unchecked"){
					input_values[input_name]=input_value;
				}


			});

			for(var input_name_to_find in input_values){
				//	console.log("div#" + project_name_as_id+" "+":input[name='"+"repeat_"+input_name_to_find+"']");
				$("#" + project_name_as_id).find(":input[name='"+"repeat_"+input_name_to_find+"']").each(function(){
					//		console.log("Getting: Input name is "+input_name_to_find+" value is "+input_values[input_name_to_find]);
					set_input_element_value($(this),input_values[input_name_to_find]);
				});
			}





//		if($.browser.msie === true){
//		location.reload();
//		}else{
		clearAddItemToRepeatingSetDiv();
		hide_repeating_instances();
//		}

		});
		return false;
	});	
	//=================================================================================================/
	//Cancel the adding of a repeating item instance
	$(".add_repeat_item_cancel_button").click(function(){
		$(this).parent().parent().hide('fast');
	});
	//minimize the repeating item_instance
	function hide_repeating_instances()
	{
		$("div.repeating_item_instance_details").each(function(){
			$(this).hide("fast");
		});

		$(".add_item_to_repeating_set").hide("fast");
	}
	//toggle the repeating item instance
	$(".edit_repeating_instance").live('click', function(){
		$(this).parent().next("div").toggle("slow");
		return false;
	});

	$(".delete_repeating_instance").live('click', function(){

		//there should be some sort of alert asking if sure

		var confirmDelete = confirm("Are you sure you want to delete this item?");

		if(! (confirmDelete==true)){
			return;
		}

		//if its a currently added div?

		//get the id of the row, and send a delete command
		var nameOfTable = "repeat_set_"+$(this).parent().parent().attr('name');
		var instanceNameToDelete = $(this).parent().find("h3").text();

		var jquerySelectorForParentDiv = $(this).parent();

		if(typeof nameOfTable === 'undefined' || typeof instanceNameToDelete === 'undefined'){
			alert("Sorry, cound not delete the entry");
		}

		else{
			$.ajax({
				url : "./repeating_set_control",
				data: "request_type=DELETE&table_name="+nameOfTable+"&instance_name="+instanceNameToDelete+"&invitee_id="+userId,
				type: "GET",
				responseType: "text"
			}).done(function(html){
				//	console.log("Delete results: "+html);
				jquerySelectorForParentDiv.hide();
				jquerySelectorForParentDiv.next().hide();
				jquerySelectorForParentDiv.remove();
			}).fail(function(){
				alert("The data could not be deleted. Please make sure the data entered is correct and try again");
			});	
		}

	});

	//get the name of the repeating item set. the database can be queried with this
	var array_of_repeating_item_names = [];
	$("div.repeating_question").each(function(){

		array_of_repeating_item_names.push($(this).attr("Name"));

	});

	for(var i=0; i< array_of_repeating_item_names.length;i++)
	{
//		alert(array_of_repeating_item_names);
	}

	//-------------------------------------------------------------------------------//
	//Handle the saving functionality for only an instance of the repeating item set

	// user clicks the save button within a repeating item set
	$(".save_repeating_item_instance").live('click', function(){
		//	console.log("saving repeating item instance");

		// get the database table name
		var data_table_name = $(this).parent().parent().attr("name");

		//get the row name
		var instance_id= ""+$(this).parent().attr("id");
		
		var instance_id_split = instance_id.split('_');
		
		if(instance_id_split === "undefined"){
			alert("Snap, something went wrong. Please refresh.");
			instance_id_split[1] == "null";
		}

		// get the instance id
		var instance_name = $(this).parent().attr("name");

		var selector = $(this).parent();
		create_send_json(selector,data_table_name, instance_id_split[1], instance_name,  false);
		
		hide_repeating_instances();
	});
	function create_send_json(selector,data_table_name, instance_id, instance_name, is_prefix_required)
	{
		var instance_values = {};

		instance_values["repeat_table_name"] = "repeat_set_"+data_table_name;
		instance_values["repeat_table_row"] = instance_id;
		instance_values["repeat_table_row_name"]=instance_name;

//		console.log(selector);
		// get the instance values as an array
		//...First read all checkboxes and radio buttons
		selector.find(":input").each(function(){

			var input_name = $(this).attr("Name");

			var input_type = this.type;

			if(is_prefix_required)
			{
				input_name = "repeat_"+input_name;
			}

			var input_value = get_input_element_value($(this));

			if(input_value!="unchecked"){
				instance_values[input_name] = input_type+":::"+input_value;
			}
			else{
				//for radio buttons if none is selected
				if (typeof instance_values[input_name] === 'undefined' && instance_values[input_name] === null && instance_values[input_name]==''){
					instance_values[input_name] = input_type+":::null";
				}
			}




		});
		//	console.log(instance_values);
		
		
		// send it to the backend with an ajax call
		return $.ajax({
			url : "./repeating_set_read_input",
			data: instance_values,
			type: "POST"
		});
		
		/*
		.then(function(result){

			//		console.log("done");
			changePlaceholderValue(data_table_name);
			return result;
		},function(){
			alert("The data could not be saved. Please make sure the data entered is correct and try again");
			return -1;
		});
		*/
	}
	//------------------------------------------------------------------------------//
	//Function to change the placeholder string for repeating instance input box
	function changePlaceholderValue(repeating_item_title){
		$(".repeat_item_name").attr("placeholder","Enter short name for another "+repeating_item_title);
	}
	//Function to clear add_item_to_repeating_set_div
	function clearAddItemToRepeatingSetDiv(){
		$(".add_item_to_repeating_set").each(function(){
			$(this).find(":input").each(function(){
				switch(this.type){
				case "password":
				case "select-multiple":
				case "select-one":
				case "text":
				case "textarea":
					$(this).val("");
					break;
				case "checkbox":
				case "radio":
					this.checked = false;
				}
			});
		});
	}
	//==============================================================================//
	//------------------------------------------------------------------------------//
	$("body").on({
		// When ajaxStart is fired, add 'loading' to body class
		ajaxStart: function() { 
			$(this).addClass("loading"); 
		},
		// When ajaxStop is fired, remove 'loading' from body class
		ajaxStop: function() { 
			$(this).removeClass("loading"); 
		}    
	});
	//==============================================================================//

	$('.repeat_item_name').keyup(function(e){
		if(e.keyCode == 13)
		{
		  e.preventDefault();
		  $(this).trigger("enterKey");
		}
		return false;
		});
	
	$('.repeat_item_name').bind("enterKey",function(e){
		$(this).parent().find('.add_repeat_instance_name_button').click();
		return false;
	});
	
	function checkEnter(e){
		 e = e || event;
		 var txtArea = /textarea/i.test((e.target || e.srcElement).tagName);
		 return txtArea || (e.keyCode || e.which || e.charCode || 0) !== 13;
		}
	
	document.querySelector('form').onkeypress = checkEnter;
	
	
	//==============================================================================//
});//end of document.ready function
