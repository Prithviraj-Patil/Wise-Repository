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

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * This class contains a typical reminder message sequence and its properties
 * 
 */
public class Reminder extends Message {
    public static final Logger LOGGER = Logger.getLogger(Reminder.class);
    /** Instance Variables */
    public int triggerDays, maxCount;

    /**
     * the constructor for reminder
     * 
     * @param n
     *            XML Node from which the details of the trigger days and max
     *            count are obtained.
     */
    public Reminder(Node n) {
        super(n);
        try {
            // System.out.println("reminder id=" + id);
            this.triggerDays = Integer.parseInt(n.getAttributes().getNamedItem("Trigger_Days").getNodeValue());
            this.maxCount = Integer.parseInt(n.getAttributes().getNamedItem("Max_Count").getNodeValue());
            // System.out.println("max count=" + max_count);
        } catch (NumberFormatException e) {
            LOGGER.error(
                    "WISE EMAIL - REMINDER: ID = " + this.id + "; Subject = " + this.subject + " --> " + e.toString(),
                    null);
            return;
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE EMAIL - REMINDER: ID = " + this.id + "; Subject = " + this.subject + " --> " + e.toString(),
                    null);
            return;
        }
    }
}
