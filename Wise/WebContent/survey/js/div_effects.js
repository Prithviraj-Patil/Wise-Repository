$(document).ready(function() {
         $("input").click(function() {
          top.fieldVals[this.name] = this.value;
          check_preconditions();
         });
         $(".wise_radio").click(function () {
         $('input:radio', this).attr('checked', true);
         });
});
function update_values(a,b,c)
{
     	values_array['activities_1']=a;
     	values_array['activities_2']=b;
     	values_array['activities_3']=c;

}

page_function_array = {};

function check_preconditions()
{
for(index in page_function_array)
{
item_should_appear = page_function_array[index](top.fieldVals);

element_id="#"+index;

if (item_should_appear)
{
$(element_id).slideDown("slow");

}
else
{
$(element_id).slideUp("slow");
}
}
}
