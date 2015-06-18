<%@page import="edu.ucla.wise.commons.StudySpace"%>
<%@page import="edu.ucla.wise.commons.StudySpaceMap"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page language="java"%>
<%

	StudySpace[] studySpaces = StudySpaceMap.getInstance().getAll();

for(StudySpace studySpace: studySpaces){
	out.println(studySpace.studyName);
}

%>