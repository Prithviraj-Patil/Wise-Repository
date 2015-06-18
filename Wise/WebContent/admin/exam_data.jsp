<%@page import="org.apache.log4j.Logger"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*, 
java.sql.*, java.text.*, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, 
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%!//update the survey information in the database when uploading the survey xml file
final Logger LOGGER = Logger.getLogger(this.getClass());
private String process_survey_file(Document doc, JspWriter out, Statement stmt) throws SQLException
{
        NodeList nodelist;
        Node n, nodeOne;
        NamedNodeMap nnm;

        String id, title;
        String sql;
        boolean dbtype;
        String return_val;

    try
    {
        //parse the survey node
        nodelist = doc.getElementsByTagName("Survey");
        n = nodelist.item(0);
        nnm = n.getAttributes();
        //get the survey attributes
        id = nnm.getNamedItem("ID").getNodeValue();
        title = nnm.getNamedItem("Title").getNodeValue();
        nodeOne = nnm.getNamedItem("Version");
        if (nodeOne != null)
            title = title + " (v" + nodeOne.getNodeValue() + ")";
        //get the latest survey's internal ID from the table of surveys
        sql = "select max(internal_id) from surveys where id = '"+id+"'";
        dbtype = stmt.execute(sql);
        ResultSet rs = stmt.getResultSet();
        boolean isn = rs.next();
        String max_id = rs.getString(1);
        //initiate the survey status as "N"
        String status = "N";
        //display processing information
        out.println("<table border=0><tr><td align=center>Processing a SURVEY (ID = "+id+")</td></tr>");
        //get the latest survey's status
        if (max_id != null)
        {
            sql = "select status from surveys where internal_id = "+max_id;
            dbtype = stmt.execute(sql);
            rs = stmt.getResultSet();
            isn = rs.next();
            status = (rs.getString(1)).toUpperCase();
        }        
        //if the survey status is in Developing or Production mode
        if (status.equalsIgnoreCase("D")|| status.equalsIgnoreCase("P"))
        {
            //display the processing situation about the status        
            out.println("<tr><td align=center>Existing survey is in "+status+" mode. </td></tr>");
            //insert a new survey record
            sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('"+id+"',\""+title+"\", '"+status+"', 'current')";
            dbtype = stmt.execute(sql);
            //get the new inserted internal ID
            //sql = "SELECT LAST_INSERT_ID() from surveys";
            sql = "SELECT max(internal_id) from surveys";            
            dbtype = stmt.execute(sql);
            rs = stmt.getResultSet();
            rs.next();
            String new_id = rs.getString(1);
            //use the newly created internal ID to name the file
            String filename = "file"+new_id+".xml";
            //update the file name and uploading time in the table
            sql = "UPDATE surveys SET filename = '"+filename+"', uploaded = now() WHERE internal_id = "+new_id;
            dbtype = stmt.execute(sql);
            //display the processing information about the file name
            out.println("<tr><td align=center>New version becomes the one with internal ID = "+id+"</td></tr>");
            return_val= filename;
        }
        //if the survey status is in Removed or Closed mode. Or there is no such survey (keep the default status as N)
        //the survey will be treated as a brand new survey with the default Developing status 
        else if (status.equalsIgnoreCase("N") || status.equalsIgnoreCase("R") || status.equalsIgnoreCase("C") )
        {
            out.println("<tr><td align=center>This is a NEW Survey.  Adding a new survey into DEVELOPMENT mode...</td></tr>");
            //insert the new survey record
            sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('"+id+"',\""+title+"\",'D','current')";
            dbtype = stmt.execute(sql);
            //get the newly created internal ID
            //sql = "SELECT LAST_INSERT_ID()";
            sql = "SELECT max(internal_id) from surveys";
            dbtype = stmt.execute(sql);
            rs = stmt.getResultSet();
            rs.next();
            String new_id = rs.getString(1);
            String filename = "file"+new_id+".xml";
            //update the file name and uploading time
            sql = "UPDATE surveys SET filename = '"+filename+"', uploaded = now() WHERE internal_id = "+new_id;
            dbtype = stmt.execute(sql);
            out.println("<tr><td align=center>New version becomes the one with internal ID = "+id+"</td></tr>");
            return_val= filename;
        }
        else
        {
            out.println("<tr><td align=center>ERROR!  Unknown STATUS!</td></tr>");
            out.println("<tr><td align=center>status:"+status+"</td></tr>");
            return_val= "NONE";
        }
        out.println("</table>");
    }
    catch (Exception e)
		{
        LOGGER.error("WISE ADMIN - PROCESS SURVEY FILE:"+e.toString(), e);
        return_val="ERROR";
		}
    return return_val;
}%>
<%!//update the invitee information in the database when uploading the invitee csv file
public void process_invitees_csv_file(File f, JspWriter out, Statement stmt) throws SQLException
 {
        //declare the column array of string
        String[] col_val = new String[1000];
        try
        {
          //compose the sql query
          String sql = "insert into invitee(";
          //open the file reader to read the file line by line
          FileReader fr = new FileReader(f);
          BufferedReader br = new BufferedReader(fr);
          String oneline = new String();
          int col_numb=0, line_count=0;                         
          while ((oneline = br.readLine()) != null)
          {
            oneline = oneline.trim();
            if(oneline.length()!=0)
            {
              line_count++;
              //put the column names into the array
              String[] split_str = oneline.split(",");
              //at the 1st line, the size of the array is the total number of the columns
              if(line_count==1)
                col_numb = split_str.length;
              //assign the column values
              for(int i=0, j=0; i< split_str.length; i++, j++)
              {
                 col_val[j]= split_str[i];
                 //mark as the null string if the phrase is an empty string
                  if(split_str[i].length()==0)
                  {
                      col_val[j]="NULL";
                  }
                  //parse the phrase with the comma inside (has the double-quotation mark)
                  //this string is just part of the entire string, so append with the next one
                  else if(split_str[i].charAt(0)=='\"' && split_str[i].charAt(split_str[i].length()-1)!='\"')
                  {
                    do
                    {
                        i++;
                        col_val[j] += ","+split_str[i];
                    }
                    //remove the double-quotation mark at the beginnig and end of the string
                    while(i < split_str.length && split_str[i].charAt(split_str[i].length()-1)!='\"');
                      col_val[j] = col_val[j].substring(1, col_val[j].length()-1);                    
                  }
                  //there could be double-quotation mark(s) (doubled by csv format) inside this string
                  //keep only one double-quotation mark(s)
                  else if(split_str[i].charAt(0)=='\"' && split_str[i].charAt(split_str[i].length()-1)=='\"')
                  {
                    if(split_str[i].indexOf("\"\"")!=-1)
                      col_val[j]=col_val[j].replaceAll("\"\"", "\"");
                  }

                  //keep only one double-quotation mark(s) if there is any inside the string
                  if(split_str[i].indexOf("\"\"")!=-1)
                    col_val[j]=col_val[j].replaceAll("\"\"", "\"");

                  //compose the sql query with the column values
                  if(line_count==1 || col_val[j].equalsIgnoreCase("null"))
                    sql += col_val[j]+",";
                  else
                    sql += "\""+col_val[j]+"\",";
              }           
            } //end if for oneline!=null

            //compose the sql query
            if(line_count==1)
              sql= sql.substring(0, sql.length()-1)+") values (";
            else
              sql= sql.substring(0, sql.length()-1) +"),(";

          }//end while

          //delete the last "," and "("
          sql = sql.substring(0, sql.length()-2);          
          
          //insert into the database
          boolean dbtype = stmt.execute(sql);
          out.println("The data has been successfully uploaded and input into database");
       }
       catch (IOException err)
       { 
         //catch possible io errors from readLine()
         System.out.println("CVS parsing: IOException error!");
         err.printStackTrace();
       }
 }%>
<%
	//get the server path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Load</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
	</tr>
</table>
<%
	session = request.getSession(true);
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }

        //get the admin info obj
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        if(adminUserSession == null)
        {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }

        String file_loc = adminUserSession.getStudyXmlPath();
        //String xml_temp_loc = file_loc + "tmp/";
        String xml_temp_loc = file_loc;
        String image_path= adminUserSession.getStudyImagePath();
		System.out.println("xml loc:" + file_loc +"\n");
        try
      {
%>
<p>Got admin info...</p>
<%
	}
		catch (Exception e)
    {
            LOGGER.error("WISE - ADMIN LOAD: "+e.toString(), e);
            out.println("<h3>Invalid XML document submitted.  Please try again.</h3>");
            out.println("<p>Error: "+e.toString()+"</p>");
		}
%>
<p><a href="tool.jsp">Return to Administration Tools</a></p>
</center>
<p>AdminInfo dump:
<pre>
	    file_loc [adminUserSession.study_xml_path]: <%=file_loc%>
        image_path [adminUserSession.study_image_path]: <%=image_path%> 
		alert email: <%=WISEApplication.getInstance().getWiseProperties().getAlertEmail()%>
            <%=WISEApplication.getInstance().getWiseProperties().getEmailFrom()%>
            <%=WISEApplication.getInstance().getWiseProperties().getEmailUsername()%>
            <%=WISEApplication.getInstance().getWiseProperties().getEmailPassword()%>
	   </pre>
</p>
</body>
</html>
