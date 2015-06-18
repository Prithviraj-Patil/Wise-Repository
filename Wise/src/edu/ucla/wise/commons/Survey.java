/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.commons;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.commons.InviteeMetadata.Values;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * This class is a survey object and contains information about a specific
 * survey.
 */
public class Survey {
	public static final Logger LOGGER = Logger.getLogger(Survey.class);

	/** Instance Variables */
	// public String file_loc;
	private final String id;
	private final String title;
	private final String projectName;
	private final String fromString;
	private final String fromEmail;
	private final String interruptMessage;
	private final String doneMessage;
	private final String reviewMessage;
	private String version = "0";
	private boolean allowGoback;
	private final int minCompleters;
	private final String forwardUrl;
	private final String eduModule;
	private final String logoName;
	private final String inviteeFields[];

	private final Hashtable<String, ResponseSet> responseSets;
	private final Hashtable<String, SubjectSet> subjectSets;
	private final Hashtable<String, TranslationItem> translationItems;

	private final InviteeMetadata inviteeMetadata;
	private final Page[] pages;
	private final StudySpace studySpace;
	private int totalItemCount;

	/**
	 * @param id
	 * @param title
	 * @param projectName
	 * @param fromString
	 * @param fromEmail
	 * @param interruptMessage
	 * @param doneMessage
	 * @param reviewMessage
	 * @param version
	 * @param allowGoback
	 * @param minCompleters
	 * @param forwardUrl
	 * @param eduModule
	 * @param logoName
	 * @param inviteeFields
	 * @param responseSets
	 * @param subjectSets
	 * @param translationItems
	 * @param inviteeMetadata
	 * @param pages
	 * @param studySpace
	 * @param totalItemCount
	 */
	public Survey(String id, String title, String projectName, String fromString, String fromEmail,
			String interruptMessage, String doneMessage, String reviewMessage, String version,
			boolean allowGoback, int minCompleters, String forwardUrl, String eduModule,
			String logoName, String[] inviteeFields, Hashtable<String, ResponseSet> responseSets,
			Hashtable<String, SubjectSet> subjectSets,
			Hashtable<String, TranslationItem> translationItems, InviteeMetadata inviteeMetadata,
			Page[] pages, StudySpace studySpace, int totalItemCount) {
		super();
		this.id = id;
		this.title = title;
		this.projectName = projectName;
		this.fromString = fromString;
		this.fromEmail = fromEmail;
		this.interruptMessage = interruptMessage;
		this.doneMessage = doneMessage;
		this.reviewMessage = reviewMessage;
		this.version = version;
		this.allowGoback = allowGoback;
		this.minCompleters = minCompleters;
		this.forwardUrl = forwardUrl;
		this.eduModule = eduModule;
		this.logoName = logoName;
		this.inviteeFields = inviteeFields;
		this.responseSets = responseSets;
		this.subjectSets = subjectSets;
		this.translationItems = translationItems;
		this.inviteeMetadata = inviteeMetadata;
		this.pages = pages;
		this.studySpace = studySpace;
		this.totalItemCount = totalItemCount;
	}

	public static Survey getDemoSurvey() {

		String[] inviteeFields = { "firstname", "lastname" };
		Hashtable<String, ResponseSet> responseSets = new Hashtable<>();
		Hashtable<String, SubjectSet> subjectSets = new Hashtable<>();
		Hashtable<String, TranslationItem> translationItems = new Hashtable<>();
		InviteeMetadata inviteeMetadata = new InviteeMetadata();
		Page[] pages;

		return new Survey("id", "title", "projectName", "fromString", "fromEmail",
				"interruptMessage", "doneMessage", "reviewMessage", "version", true, 1000,
				"forwardUrl", "eduModule", "logoName", inviteeFields, responseSets, subjectSets,
				translationItems, inviteeMetadata, null, null, 10);

	}

	/**
	 * Constructor - setup a survey by parsing the file
	 * 
	 * @param xmlDoc
	 *            Survey Xml that contains all the questions.
	 * @param ss
	 *            Study space to which this survey belongs to.
	 */
	public Survey(Document xmlDoc, StudySpace ss) throws DOMException {
		int numbPages = 0;
		this.totalItemCount = 0;
		this.studySpace = ss;

		/* create a parser and an XML document */
		xmlDoc.getDocumentElement().normalize();

		/* parse out the data from survey xml file */
		NodeList nodelist = xmlDoc.getElementsByTagName("Survey");
		if (nodelist.getLength() < 1) {
			throw new IllegalStateException(
					"The survey node is not present in the xml document provided");
		}
		Node node = nodelist.item(0);

		/* parse out the survey attributes survey ID & title */
		this.id = node.getAttributes().getNamedItem("ID").getNodeValue();
		this.title = node.getAttributes().getNamedItem("Title").getNodeValue();

		// names who send the email
		Node node2 = node.getAttributes().getNamedItem("From_String");
		if (node2 != null) {
			this.fromString = node2.getNodeValue();
		} else {
			this.fromString = "";
		}

		/*
		 * FROM email address - fake one the actual FROM email will use the one
		 * in SMTP email account
		 */
		node2 = node.getAttributes().getNamedItem("From_Email");
		if (node2 != null) {
			this.fromEmail = node2.getNodeValue();
		} else {
			this.fromEmail = "";
		}

		/* survey version */
		node2 = node.getAttributes().getNamedItem("Version");
		if (node2 != null) {
			this.version = node2.getNodeValue();
		} else {
			this.version = "";
		}

		// get the name of user-defined data table
		// the default is the invitee table
		// node2 = node.getAttributes().getNamedItem("user_data_page");
		// if (node2 != null)
		// user_data_page = node2.getNodeValue();
		// else
		// user_data_page = "Invitee";

		/* messsage ID for survey-interruption message */
		node2 = node.getAttributes().getNamedItem("Interrupt_Msg");
		if (node2 != null) {
			this.interruptMessage = node2.getNodeValue();
		} else {
			this.interruptMessage = "";
		}

		/* messsage ID for survey-completion message */
		node2 = node.getAttributes().getNamedItem("Done_Msg");
		if (node2 != null) {
			this.doneMessage = node2.getNodeValue();
		} else {
			this.doneMessage = "";
		}

		/* messsage ID for survey-review message */
		node2 = node.getAttributes().getNamedItem("Review_Msg");
		if (node2 != null) {
			this.reviewMessage = node2.getNodeValue();
		} else {
			this.reviewMessage = "";
		}

		/*
		 * allow user to go back to review those survey pages have been past
		 * over
		 */
		node2 = node.getAttributes().getNamedItem("Allow_Goback");
		if (node2 != null) {
			this.allowGoback = new Boolean(node2.getNodeValue()).booleanValue();
		} else {
			this.allowGoback = false;
		}

		/*
		 * user won't allow to review the survey results, until the number of
		 * completers reach up to this number.
		 */
		node2 = node.getAttributes().getNamedItem("View_Result_After_N");
		if (node2 != null) {
			this.minCompleters = Integer.parseInt(node2.getNodeValue());
		} else {
			this.minCompleters = -1;
		}

		/*
		 * forwarding URL after the survey process normally it is the URL link
		 * to the quiz
		 */
		node2 = node.getAttributes().getNamedItem("Forward_On");
		if (node2 != null) {
			this.forwardUrl = node2.getNodeValue();
		} else {
			this.forwardUrl = "";
		}

		/* the module ID for the quiz process */
		node2 = node.getAttributes().getNamedItem("Edu_Module");
		if (node2 != null) {
			this.eduModule = node2.getNodeValue();
		} else {
			this.eduModule = "";
		}

		/* the logo image's name for the survey */
		node2 = node.getAttributes().getNamedItem("Logo_Name");
		if (node2 != null) {
			this.logoName = node2.getNodeValue();
		} else {
			this.logoName = "proj_logo.gif";
		}
		node2 = node.getAttributes().getNamedItem("Project_Name");
		if (node2 != null) {
			this.projectName = node2.getNodeValue();
		} else {
			this.projectName = "UNKOWN";
		}

		/* count the number of pages */
		NodeList nodelistChildren = node.getChildNodes();
		for (int j = 0; j < nodelistChildren.getLength(); j++) {
			if (nodelistChildren.item(j).getNodeName().equalsIgnoreCase("Survey_Page")) {
				numbPages++;
			}
		}

		/* parse out the response sets */
		this.responseSets = new Hashtable<String, ResponseSet>();
		nodelist = xmlDoc.getElementsByTagName("Response_Set");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node childNode = nodelist.item(i);
			ResponseSet r = new ResponseSet(childNode, this);
			this.responseSets.put(r.id, r);
		}

		/* parse out the invitee fields */
		nodelist = xmlDoc.getElementsByTagName("Invitee_Fields");
		if (nodelist.getLength() < 1) {
			throw new IllegalStateException(
					"The invitee fields are not present in the xml provided");
		}
		Node inviteeNode = nodelist.item(0);
		this.inviteeMetadata = new InviteeMetadata(inviteeNode, this);
		if (this.getInviteeMetadata() == null) {
			throw new IllegalStateException("Please provide the invitee metadata in the survey xml");
		}
		this.inviteeFields = new String[this.getInviteeMetadata().getFieldMap().size()];
		int cnt = 0;
		for (Map.Entry<String, Values> map : this.getInviteeMetadata().getFieldMap().entrySet()) {
			this.getInviteeFields()[cnt++] = map.getKey();
		}
		this.getStudySpace().db.syncInviteeTable(this.getInviteeMetadata());

		/* parse out the translation sets */
		this.translationItems = new Hashtable<String, TranslationItem>();
		// nodelist = doc.getElementsByTagName("Translation");
		nodelist = xmlDoc.getElementsByTagName("TranslationType");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node childNode = nodelist.item(i);
			TranslationItem t = new TranslationItem(childNode, this);
			this.translationItems.put(t.id, t);
		}

		/* parse out the subject sets */
		this.subjectSets = new Hashtable<String, SubjectSet>();
		nodelist = xmlDoc.getElementsByTagName("Subject_Set");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node n = nodelist.item(i);
			SubjectSet subject_set = new SubjectSet(n, this);
			this.subjectSets.put(subject_set.id, subject_set);
		}

		/* create the pages */
		this.pages = new Page[numbPages];
		nodelist = xmlDoc.getElementsByTagName("Survey");

		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i).getNodeName().equalsIgnoreCase("Survey")) {
				NodeList nodelist1 = nodelist.item(i).getChildNodes();
				for (int j = 0, k = 0; j < nodelist1.getLength(); j++) {
					if (nodelist1.item(j).getNodeName().equalsIgnoreCase("Survey_Page")) {
						this.getPages()[k] = new Page(nodelist1.item(j), this);
						this.totalItemCount += this.getPages()[k].getItemCount();
						k++;
					}
				}
			}
		}
	}

	/**
	 * Returns a the DataBank object for the Survey's StudySpace.
	 * 
	 * @return DataBank.
	 */
	public DataBank getDB() {
		return this.getStudySpace().getDB();
	}

	/**
	 * Displays the sub-menu on the left side of frame to show the survey
	 * progress - the sub-menu won't let the user go back to review completed
	 * pages.
	 * 
	 * @param currentPage
	 *            Current page the user is on in the survey.
	 * @param completedPages
	 *            Hashtable of the completed pages in the survey.
	 * @return String HTML format of the survey progress on left side of the
	 *         survey page.
	 */
	public String printProgress(Page currentPage, Hashtable<String, String> completedPages) {
		String s = "";
		try {
			s += "<html><head>";
			s += "<STYLE>";
			s += "a:hover {color: #aa0000; text-decoration: underline}";
			s += "a:link {color: #003366; text-decoration: none}";
			s += "a:visited {color: #003366; text-decoration: none}";
			s += "</STYLE>";
			s += "</head>";
			s += "<LINK href='" + "styleRender?app=" + this.getStudySpace().studyName
					+ "&css=style.css" + "' type=text/css rel=stylesheet>";
			s += "<body>";
			// s += "<body text='#000000' bgcolor='#FFFFCC' >";
			s += "<font face='Verdana, Arial, Helvetica, sans-serif'>";
			s += "<table cellpadding=4 cellspacing=0>";
			s += "<tr><td align=center valign=top>";
			s += "<img src='" + "imageRender?app=" + this.getStudySpace().studyName + "&img="
					+ this.logoName + "' border=0></td></tr>";
			s += "<tr><td>&nbsp;</td></tr>";

			// display the interrupt link
			/*
			 * Pralav: get rid of save feature s += "<tr><td>"; s +=
			 * "<a href=\"javascript:top.mainFrame.form.document.mainform.action.value='interrupt';"
			 * ; s +=
			 * "top.mainFrame.form.document.mainform.submit();\" target=\"_top\">"
			 * ; s +=
			 * "<font size=\"-1\"><center>Click here to <B>SAVE</B> your answers "
			 * ; s += "if you need to pause</center></font></a>"; s +=
			 * "</td></tr>"; s +=
			 * "<tr><td><font size=\"-2\">&nbsp;</font></td></tr>"; s +=
			 * "<tr><td>";
			 */

			/* display the page name list */
			s += "<table width=100 height=200 border=0 cellpadding=5 cellspacing=5 bgcolor=#F0F0FF>";
			s += "<tr><td align=center>";
			s += "<font size=\"-2\"><u>Page Progress</u></font></td></tr>";

			for (int i = 0; i < this.getPages().length; i++) {
				s += "<tr>";
				String pageStatus = completedPages.get(this.getPages()[i].getId());
				if ((pageStatus != null) && pageStatus.equalsIgnoreCase("Completed")) {

					/* completed pages */
					s += "<td bgcolor='#99CCFF' align=left colspan=1>";
				} else if ((pageStatus != null) && pageStatus.equalsIgnoreCase("Current")) {
					/* current page - will be highlighted in yellow */
					s += "<td bgcolor='#FFFF00' align=left colspan=1>";
				} else {
					s += "<td>";
				}
				s += "<font size=\"-2\">";
				s += "<b>" + this.getPages()[i].getTitle() + "</b></font>";
				s += "</td></tr>";
			}
			s += "</table>";
			s += "</td></tr></table>";
		} catch (NullPointerException e) {
			LOGGER.error("WISE - SURVEY - PRINT PROGRESS WITHOUT LINK: " + e.toString() + " --> "
					+ currentPage.toString() + ", " + completedPages.toString(), null);
		}
		return s;
	}

	/**
	 * Displays the sub-menu on the left side of frame to show the survey
	 * progress - the sub-menu will have the links to let the user review the
	 * completed pages
	 * 
	 * @param currentPage
	 *            Current page the user is on in the survey.
	 * @return String HTML format of the survey progress on left side of the
	 *         survey page.
	 */
	public String printProgress(Page currentPage) {
		String s = "";
		try {
			s += "<table width=100% cellpadding=0 cellspacing=0>";
			s += "<tr><td align=center valign=top>";

			/* changing the path to WISE_Application.images_path */
			s += "<img src='" + "imageRender?app=" + this.getStudySpace().studyName + "&img="
					+ this.logoName + "' border=0></td></tr>";
			s += "<tr><td>&nbsp;</td></tr>";

			/*
			 * Pralav: remove save button s += "<tr><td>"; s +=
			 * "<a href=\"javascript:top.mainFrame.form.document.mainform.action.value='interrupt';"
			 * ; s +=
			 * "top.mainFrame.form.document.mainform.submit();\" target=\"_top\">"
			 * ; s +=
			 * "<font size=\"-1\"><center>Click here to <B>SAVE</B> your answers "
			 * ; s += "if you need to pause</center></font></a>"; s +=
			 * "</td></tr>";
			 */

			s += "<tr><td><font size=\"-2\">&nbsp;</font></td></tr>";
			s += "<tr><td>";
			s += "<table width=100 height=200 border=0 cellpadding=5 cellspacing=5 bgcolor=#F0F0FF>";
			s += "<tr><td align=center>";
			s += "<font size=\"-2\"><u>Survey Pages</u></font></td></tr>";

			int idx = this.getPageIndex(currentPage.getId());
			for (int i = 0; i < this.getPages().length; i++) {
				s += "<tr>";
				if (i != idx) {
					s += "<td bgcolor='#99CCFF' align=left colspan=1>";
					s += "<a href=\"javascript:document.mainform.action.value='linkpage';";
					s += "document.mainform.nextPage.value='" + this.getPages()[i].getId() + "';";
					s += "document.mainform.submit();\" target=\"_top\">";
					s += "<font size=-2><b>" + this.getPages()[i].getTitle() + "</b></font></a>";
				} else {
					s += "<td bgcolor='#FFFF00' align=left colspan=1>";
					s += "<font size=-2><b>" + this.getPages()[i].getTitle() + "</b></font>";
				}
				s += "</td></tr>";
			}
			s += "</table>";
			s += "</td></tr></table>";
		} catch (Exception e) {
			LOGGER.error("WISE - SURVEY - PRINT PROGRESS WITH LINKS: " + e.toString() + " --> "
					+ currentPage.toString(), null);
		}
		return s;
	}

	/**
	 * Search by ID, return a specific ResponseSet
	 * 
	 * @param id
	 *            Id of the specific response set needed.
	 * @return ResponseSet The required response set.
	 */
	public ResponseSet getResponseSet(String id) {
		ResponseSet a = this.responseSets.get(id);
		return a;
	}

	/**
	 * Search by ID, return a specific SubjectSet
	 * 
	 * @param id
	 *            Id of the specific subject set needed.
	 * @return SubjectSet The required subject set.
	 */
	public SubjectSet getSubjectSet(String id) {
		SubjectSet ss = this.subjectSets.get(id);
		return ss;
	}

	/**
	 * Search by ID, return a specific Translation
	 * 
	 * @param id
	 *            Id of the specific translation item needed.
	 * @return TranslationItem The required translation item.
	 */
	public TranslationItem getTranslationItem(String id) {
		TranslationItem t = this.translationItems.get(id);
		return t;
	}

	/**
	 * search by page ID, return the page index in the page array
	 * 
	 * @param id
	 *            Id of the specific page needed.
	 * @return int The index of given page in the array of pages contained by
	 *         this survey.
	 */
	public int getPageIndex(String id) {
		for (int i = 0; i < this.getPages().length; i++) {
			if (this.getPages()[i].getId().equalsIgnoreCase(id)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Search by page ID, return the page the page array.
	 * 
	 * @param id
	 *            Id of the specific page needed.
	 * @return Page The page from the array of pages contained by this survey.
	 */
	public Page getPage(String id) {
		int i = this.getPageIndex(id);
		if (i != -1) {
			return this.getPages()[i];
		} else {
			return null;
		}
	}

	/**
	 * Checks if the index given is same as the index of the last page in the
	 * array of page of this survey.
	 * 
	 * @param idx
	 *            index of the page to be checked.
	 * @return boolean True of the page is last else false.
	 */
	public boolean isLastPage(int idx) {
		int i = this.getPages().length - 1;
		if (idx == i) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFirstPage(String id) {

		if (this.getPageIndex(id) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Searches by page ID and checks if the page is the last page in the page
	 * array
	 * 
	 * @param id
	 *            Id of the page to be checked.
	 * @return boolean True of the page is last else false.
	 */
	public boolean isLastPage(String id) {
		return this.isLastPage(this.getPageIndex(id));
	}

	/**
	 * Searches by page ID, get the next page from the page array
	 * 
	 * @param id
	 *            Id of the page whose next page is needed.
	 * @return Page Next page in the survey, null in case of last page or error.
	 */
	public Page nextPage(String id) {
		int idx = this.getPageIndex(id);
		if (idx < 0) {
			return null;
		}
		idx++;
		if (idx >= this.getPages().length) {
			return null;
		} else {
			return this.getPages()[idx];
		}
	}

	/** search by page index, return the page ID from the page array object */
	/*
	 * public String get_page_id(int idx) { if (idx > pages.length) return null;
	 * else return pages[idx].id; }
	 */

	/** returns the previous page after the page index */
	/*
	 * public Page previous_page(int indx) { if(indx <= 0 || indx > pages.length
	 * ) return null; else return pages[indx-1]; }
	 */
	/** search by page ID, get the previous page from the page array */
	public Page previousPage(String id) {
		int idx = this.getPageIndex(id);
		if (idx < 0) {
			return null;
		}
		idx--;
		if (idx < 0) {
			return null;
		} else {
			return this.getPages()[idx];
		}
	}

	public Page lastPage() {

		return this.getPages()[this.getPages().length - 1];

	}

	public Page firstPage() {

		return this.getPages()[0];

	}

	/** returns the page for that page index */
	/*
	 * public Page get_page(int idx) { if (idx <= pages.length) return
	 * pages[idx]; else return null; }
	 */

	/**
	 * get table creation syntax for the OLD version of data table -- RETIRABLE
	 * public String get_table_syntax(Statement stmt) { String create_str="";
	 * try { //get the 2nd max internal id in the survey table with the same
	 * survey ID //it is the index for locating the old survey data table String
	 * sql = "select max(internal_id) from "+
	 * "(select * from surveys where id='"+id+"' and internal_id <> "+
	 * "(select max(internal_id) from surveys where id='"+id+"')) as a;";
	 * boolean dbtype = stmt.execute(sql); ResultSet rs = stmt.getResultSet();
	 * //get the value from the table column of create_syntax if(rs.next()) {
	 * String sqlc =
	 * "select create_syntax from surveys where internal_id="+rs.getString(1);
	 * boolean dbtypec = stmt.execute(sqlc); ResultSet rsc =
	 * stmt.getResultSet(); if(rsc.next())
	 * create_str=rsc.getString("create_syntax"); }
	 * 
	 * if(create_str==null) create_str=""; } catch (Exception e) {
	 * Study_Util.email_alert("SURVEY - GET DATA TABLE SYNTAX: "+e.toString());
	 * }
	 * 
	 * return create_str; }
	 */

	/**
	 * This function returns all Field Names in survey -- used by DataBank to
	 * setup database.
	 * 
	 * @return Array Array of all the field names in the survey.
	 */
	public String[] getFieldList() {
		String[] mainFields = new String[this.totalItemCount];
		int mainI = 0;
		for (int pageI = 0; pageI < this.getPages().length; pageI++) {
			String[] pageFields = this.getPages()[pageI].getFieldList();
			for (int fieldI = 0; fieldI < pageFields.length; fieldI++) {
				mainFields[mainI++] = pageFields[fieldI];
			}
		}
		return mainFields;
	}

	/**
	 * This function returns all value types of fields in survey -- used by
	 * DataBank to setup database.
	 * 
	 * @return Array Array of all the value types of the fields in the survey.
	 */
	public char[] getValueTypeList() {
		char[] mainFieldTypes = new char[this.totalItemCount];
		int mainI = 0;
		for (int pageI = 0; pageI < this.getPages().length; pageI++) {
			char[] pageTypes = this.getPages()[pageI].getValueTypeList();
			for (int fieldI = 0; fieldI < pageTypes.length; fieldI++) {
				mainFieldTypes[mainI++] = pageTypes[fieldI];
			}
		}
		return mainFieldTypes;
	}

	/**
	 * This following function copy the above two functions for the repeating
	 * sets.
	 * 
	 * @return ArrayList of repeating item set in the survey.
	 */
	public ArrayList<RepeatingItemSet> getRepeatingItemSets() {
		ArrayList<RepeatingItemSet> surveyRepeatingItemSets = new ArrayList<RepeatingItemSet>();
		for (int pageI = 0; pageI < this.getPages().length; pageI++) {
			ArrayList<RepeatingItemSet> pageRepeatingItemSets = this.getPages()[pageI]
					.getRepeatingItemSets();
			for (RepeatingItemSet pageRepeatSetInstance : pageRepeatingItemSets) {
				surveyRepeatingItemSets.add(pageRepeatSetInstance);
			}
		}
		return surveyRepeatingItemSets;
	}

	/**
	 * get table creation syntax for the new data table public String
	 * create_table_syntax() throws SQLException { String create_str=""; for
	 * (int i = 0; i < pages.length; i++) create_str += pages[i].create_table();
	 * return create_str; }
	 */

	/**
	 * Prints a brief dump of survey structure.
	 * 
	 */
	@Override
	public String toString() {
		String s = "<p><b>SURVEY</b><br>";
		s += "ID: " + this.id + "<br>";
		s += "Title: " + this.getTitle() + "<br>";
		// s += "user_data_page: "+ user_data_page +"<br>";

		for (int i = 0; i < this.getPages().length; i++) {
			s += this.getPages()[i].toString();
		}
		if (this.getInviteeFields() != null) {
			s += "Invitee Fields ref'd: ";
			for (int i = 0; i < this.getInviteeFields().length; i++) {
				s += this.getInviteeFields()[i] + "; ";
			}
		}
		s += "</p>";
		return s;
	}

	public String getId() {
		return this.id;
	}

	public boolean isAllowGoback() {
		return this.allowGoback;
	}

	public void setAllowGoback(boolean allowGoBack) {
		this.allowGoback = allowGoBack;
	}

	public String getForwardUrl() {
		return this.forwardUrl;
	}

	public String getEduModule() {
		return this.eduModule;
	}

	public StudySpace getStudySpace() {
		return this.studySpace;
	}

	public int getMinCompleters() {
		return this.minCompleters;
	}

	public Page[] getPages() {
		return this.pages;
	}

	public String getTitle() {
		return this.title;
	}

	public InviteeMetadata getInviteeMetadata() {
		return this.inviteeMetadata;
	}

	public String[] getInviteeFields() {
		return this.inviteeFields;
	}
}
