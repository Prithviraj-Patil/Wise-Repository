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

import edu.ucla.wise.commons.databank.DataBank;

/**
 * This email thread will spawn action of sending reminders.
 */
public class EmailScheduler {

    public static void main(String[] args) {

        /* start the email sending procedure */
        java.util.Date today = new java.util.Date();
        if (args.length < 1) {
            System.out.println("Usage: EmailScheduler [application_name]");
            return;
        }
        String appName = args[0];
        System.out.print("Launching Email Manager on " + today.toString() + " for studies assigned to " + appName
                + " on this server.");

        StudySpace[] allSpaces = new StudySpace[0];

        allSpaces = StudySpaceMap.getInstance().getAll();

        if ((allSpaces == null) || (allSpaces.length < 1)) {
            System.out.print("Error while getting the  study spaces" + " assigned to " + appName + " on this server.");
            return;
        }

        /* iterate over all Study_Spaces that this server manages */
        for (int i = 0; i < allSpaces.length; i++) {
            StudySpace ss = allSpaces[i];
            DataBank db = ss.db;
            System.out.println("\nStudy_Space " + ss.studyName + " CONNECTING to database: " + db.getDbdata());
            System.out.println(db.sendReminders());
        }
    }
}
