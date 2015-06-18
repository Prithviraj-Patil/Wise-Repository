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
package edu.ucla.wise.emailscheduler;

public class WISE_TimeUtils {

    public static final long MILLISECONDS_IN_A_DAY = 86400000;
    public static final long MILLISECONDS_IN_AN_HOUR = 3600000;
    public static final long MILLISECONDS_IN_A_MINUTE = 60000;

    public static String convertMillisToHumanReadableForm(long milliseconds) {

        if (milliseconds < 0) {
            return "CANNOT_CONVERT";
        }

        if (milliseconds > 86400000) {
            return getNumberOfDays(milliseconds);
        } else {
            return getNumberOfHours(milliseconds);
        }

    }

    public static String getNumberOfDays(long milliseconds) {

        long days = milliseconds / MILLISECONDS_IN_A_DAY;

        long remainder = milliseconds % MILLISECONDS_IN_A_DAY;

        return days + " days, " + getNumberOfHours(remainder);
    }

    public static String getNumberOfHours(long milliseconds) {
        long hours = milliseconds / MILLISECONDS_IN_AN_HOUR;

        long remainder = milliseconds % MILLISECONDS_IN_AN_HOUR;

        return hours + " hours, " + getNumberOfMinutes(remainder);
    }

    public static String getNumberOfMinutes(long milliseconds) {
        long minutes = milliseconds / MILLISECONDS_IN_A_MINUTE;

        long remainder = milliseconds % MILLISECONDS_IN_A_MINUTE;

        return minutes + " minutes, " + (remainder / 1000) + " seconds";
    }

}
