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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is a preface object and contains information about a specific
 * welcome page and consent form.
 */

public class Preface {

	private static Logger LOGGER = Logger.getLogger(Preface.class);

	/** Instance Variables */
	public String projectName = "";
	public String studySpaceName = "";
	public Hashtable<String, WelcomePage> welcomePages = new Hashtable<String, WelcomePage>();
	public Hashtable<String, ConsentForm> consentForms = new Hashtable<String, ConsentForm>();
	public Hashtable<String, IRBSet> irbSets = new Hashtable<String, IRBSet>();
	private final Hashtable<String, MessageSequence> allMessageSequences = new Hashtable<String, MessageSequence>();
	private final Hashtable<String, Message> allMessages = new Hashtable<String, Message>();
	private final Hashtable<String, MessageSequence> messageSequencesByMsgID = new Hashtable<String, MessageSequence>();
	public ThankyouPage thankyouPage;
	public StudySpace studySpace;

	/**
	 * Constructor - create a preface by parsing the xml file.
	 * 
	 * @param studySpace
	 * @param prefaceFileName
	 */
	public Preface(StudySpace studySpace, String prefaceFileName) {

		this.studySpaceName = studySpace.studyName;
		this.studySpace = studySpace;

		try {

			/* Directly read the preface file */
			/*
			 * Document doc = DocumentBuilderFactory.newInstance()
			 * .newDocumentBuilder()
			 * .parse(CommonUtils.loadResource(preface_file_name));
			 */
			LOGGER.info("Loading preface file " + prefaceFileName + " for " + this.studySpaceName);

			InputStream prefaceFileInputStream = studySpace.db
					.getXmlFileFromDatabase(prefaceFileName);

			if (prefaceFileInputStream == null) {
				throw new FileNotFoundException();
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(prefaceFileInputStream);

			NodeList rootNode = doc.getElementsByTagName("Preface");
			for (int k = 0; k < rootNode.getLength(); k++) {
				Node nd = rootNode.item(k).getAttributes().getNamedItem("Project_Name");
				if (nd != null) {
					this.projectName = nd.getNodeValue();
				}
			}

			/* parse out the welcome pages */
			NodeList nodelist = doc.getElementsByTagName("Welcome_Page");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				/* create the welcome page class */
				WelcomePage wp = new WelcomePage(node, this);
				this.welcomePages.put(wp.id, wp);
			}

			/* parse out the thankyou pages */
			nodelist = doc.getElementsByTagName("ThankYou_Page");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				/* create the thank you page class */
				this.thankyouPage = new ThankyouPage(node, this);
			}

			/* parse out the IRB entities */
			nodelist = doc.getElementsByTagName("IRB");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				/* create the welcome page class */
				IRBSet irb = new IRBSet(node, this);
				this.irbSets.put(irb.id, irb);
			}

			/* parse out the consent forms */
			nodelist = doc.getElementsByTagName("Consent_Form");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				/* create the consent form class */
				ConsentForm cf = new ConsentForm(node, this);
				this.consentForms.put(cf.id, cf);
			}

			/* parse out the message sequence */
			nodelist = doc.getElementsByTagName("Message_Sequence");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				/* create the consent form class */
				MessageSequence ms = new MessageSequence(node, this);
				this.allMessageSequences.put(ms.id, ms);
			}

			/*
			 * after reading in & creating all message objects, resolve the
			 * references among them
			 */
			Enumeration<Message> e = this.allMessages.elements();
			while (e.hasMoreElements()) {
				Message msg = e.nextElement();
				msg.resolveRef(this);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("Preface file not found", e);
		} catch (SAXException e) {
			LOGGER.error("Preface file could not be loaded", e);
		} catch (IOException e) {
			LOGGER.error("Preface file IO exception", e);
		} catch (ParserConfigurationException e) {
			LOGGER.error("Preface file Parser config exception", e);
		}
	}

	/*
	 * pass down to each message the appropriately localized URLs (eg for
	 * images) from StudySpace (add more args here as needed)
	 */
	public void setHrefs(String srvltPath, String imgPath) {
		Enumeration<Message> e = this.allMessages.elements();
		while (e.hasMoreElements()) {
			Message msg = e.nextElement();
			msg.setHrefs(srvltPath, imgPath);
		}
	}

	/**
	 * Search by the ID and returns a welcome page.
	 * 
	 * @param wpId
	 *            Id of the welcome page that is required.
	 * @return WelcomePage
	 */
	public WelcomePage getWelcomePage(String wpId) {
		WelcomePage wp = null;
		if (this.welcomePages != null) {
			wp = this.welcomePages.get(wpId);
		}
		return wp;
	}

	/**
	 * Returns the thank you page, assume a universal thankyou page per study.
	 * 
	 * @return ThankYouPage
	 */
	public ThankyouPage getThankyouPage() {
		if (this.thankyouPage != null) {
			return this.thankyouPage;
		} else {
			return null;
		}
	}

	/**
	 * Searches by the survey ID & irb ID and returns a welcome page returns,
	 * null only if no welcome pages defined otherwise if no surveyID, irbID
	 * match, returns last WelcomePage as default.
	 * 
	 * @param surveyID
	 *            Survey Id whose welcome page has to be returned.
	 * @param irbID
	 *            Irb Id specific welcome page.
	 * @return WelcomePage
	 */
	public WelcomePage getWelcomePageSurveyIrb(String surveyID, String irbID) {
		WelcomePage wp = null; // returns null
		if (this.welcomePages != null) {
			Enumeration<WelcomePage> e = this.welcomePages.elements();
			while (e.hasMoreElements()) {
				wp = e.nextElement();
				if (wp.surveyId.equalsIgnoreCase(surveyID)) {
					if (wp.irbId.equalsIgnoreCase(irbID)) {
						return wp;
					}
				}
			}
		}
		return wp;
	}

	/**
	 * Searches by the ID and returns a IRB set.
	 * 
	 * @param irbId
	 *            Id of the IRB set that is needed from the preface file.
	 * @return IRBSet
	 */
	public IRBSet getIrbSet(String irbId) {
		IRBSet irb = null;
		if (this.irbSets != null) {
			irb = this.irbSets.get(irbId);
		}
		return irb;
	}

	/**
	 * Searches by the ID and returns a consent form
	 * 
	 * @param cfId
	 *            Id of the consent form that is needed from the perface file.
	 * @return ConsentForm
	 */
	public ConsentForm getConsentForm(String cfId) {
		ConsentForm cf = null;
		if (this.consentForms != null) {
			cf = this.consentForms.get(cfId);
		}
		return cf;
	}

	/**
	 * Searches by the survey ID & irb ID and returns a Consent form, returns
	 * null only if no consent form is defined otherwise if no surveyID, irbID
	 * match, returns last ConsentForm as default.
	 * 
	 * @param surveyID
	 *            Survey Id whose consent form has to be returned.
	 * @param irbID
	 *            Irb Id specific consent form.
	 * @return ConsentForm
	 */
	public ConsentForm getConsentFormSurveyIrb(String surveyID, String irbID) {
		ConsentForm cf = null;
		if (this.consentForms != null) {
			Enumeration<ConsentForm> e = this.consentForms.elements();
			while (e.hasMoreElements()) {
				cf = e.nextElement();
				if (cf.surveyId.equalsIgnoreCase(surveyID)) {
					if (cf.irbId.equalsIgnoreCase(irbID)) {
						return cf;
					}
				}
			}
		}
		return cf;
	}

	/**
	 * Searches by the ID and returns a message sequence.
	 * 
	 * @param seqId
	 *            Id of the message sequence that is needed from the preface
	 *            file.
	 * @return MessageSequence A valid message sequence or null incase the seqId
	 *         is not proper.
	 */
	public MessageSequence getMessageSequence(String seqId) {
		if (seqId == null) {
			return null;
		}
		return this.allMessageSequences.get(seqId);
	}

	/**
	 * Returns the message sequence for a given Message ID.
	 * 
	 * @param msgId
	 *            Id of the message sequence needed.
	 * @return MessageSequence
	 */

	public MessageSequence getMessageSequence4msgID(String msgId) {
		return this.messageSequencesByMsgID.get(msgId);
	}

	// extract out the message sequence matching a survey, irb combo --
	// deprecated because may not be just one
	// public Message_Sequence get_message_sequence(String survey_id, String
	// irb_id)
	// {
	// Message_Sequence msg_seq = null;
	// if (irb_id == null)
	// irb_id = "";
	// //get the message sequence from hashtable
	// for (Enumeration e = all_message_sequences.elements();
	// e.hasMoreElements();)
	// {
	// msg_seq = (Message_Sequence) e.nextElement();
	//
	// if((msg_seq.survey_id.indexOf(survey_id) != -1) &&
	// msg_seq.irb_id.equalsIgnoreCase(irb_id)) //irb is "" if unspec
	// break;
	// }
	// return msg_seq;//currently returns last message sequence by default
	// }

	/**
	 * Extracts array of all message sequences for a survey.
	 * 
	 * @param surveyId
	 *            Survey id whose message sequences are needed.
	 * @return Array Array of all the message sequences in the preface.
	 */
	public MessageSequence[] getMessageSequences(String surveyId) {

		/* return a 0-length array as default */
		MessageSequence[] msgSeqs = new MessageSequence[0];
		ArrayList<MessageSequence> tempList = new ArrayList<MessageSequence>();

		/* get the message sequence from hashtable */
		for (Enumeration<MessageSequence> e = this.allMessageSequences.elements(); e
				.hasMoreElements();) {
			MessageSequence msgSeq = e.nextElement();
			if (msgSeq.surveyId.indexOf(surveyId) != -1) {
				tempList.add(msgSeq);
			}
		}
		if (tempList.size() > 0) {
			msgSeqs = tempList.toArray(new MessageSequence[tempList.size()]);
		}
		return msgSeqs;
	}

	/**
	 * Add message for retrieval by ID and also for retrieval of message
	 * sequence by message ID.
	 * 
	 * @param newMsg
	 *            Message to be added to the hashtable.
	 * @param msgSeq
	 *            Message sequence to be added to the hashtable.
	 */
	public void addMessage(Message newMsg, MessageSequence msgSeq) {
		if (newMsg == null) {
			return;
		}
		this.allMessages.put(newMsg.id, newMsg);
		this.messageSequencesByMsgID.put(newMsg.id, msgSeq);
	}

	/**
	 * Returns the message with a given ID.
	 * 
	 * @param msgId
	 *            Id of the message in the preface file.
	 * @return Message
	 */
	public Message getMessage(String msgId) {

		/*
		 * the following is safe because all_messages guaranteed initialized to
		 * hashtable Bad XML IDref will return null.
		 */
		return this.allMessages.get(msgId);
	}

	/**
	 * Returns all initial invitation messages with a given ID.
	 * 
	 * @param svyId
	 *            Survey name whose initial messages are needed.
	 * @return Array Array of all the initial messages of the survey.
	 */
	public Message[] getAllInitialMessagesForSurveyID(String svyId) {
		ArrayList<Message> foundMsgs = new ArrayList<Message>();
		for (Enumeration<MessageSequence> e = this.allMessageSequences.elements(); e
				.hasMoreElements();) {
			MessageSequence msgSequence = e.nextElement();

			/* search by the survey ID */
			if (msgSequence.surveyId.matches(svyId)) {

				/* try using survey ID as a regexp */
				foundMsgs.add(msgSequence.getTypeMessage("invite"));
			}
		}
		return foundMsgs.toArray(new Message[foundMsgs.size()]);
	}

	/**
	 * return all invitation messages for a given survey ID -- DEPRECATED public
	 * Message[] get_all_messages_forSurveyID(String svy_id) { ArrayList
	 * foundMsgs = new ArrayList(); for (Enumeration e =
	 * all_messages.elements(); e.hasMoreElements();) { Message msg = (Message)
	 * e.nextElement(); //search by the survey ID
	 * if(msg.survey().equalsIgnoreCase(svy_id)) foundMsgs.add(msg); } return
	 * (Message[]) foundMsgs.toArray(new Message[foundMsgs.size()]); }
	 */

	/**
	 * Prints the dump of the preface file.
	 */
	@Override
	public String toString() {
		String resp = "<b>Preface: </b><br>Message sequences<br>";
		MessageSequence msgsq;
		Enumeration<MessageSequence> e1 = this.allMessageSequences.elements();
		while (e1.hasMoreElements()) {
			msgsq = e1.nextElement();
			resp += msgsq.toString();
		}
		resp += "<B>Total message count: " + this.allMessages.size() + "</b>";
		return resp;
	}
}
