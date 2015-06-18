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
package edu.ucla.wise.client;

/**
 * UserHitCounter class is used to keep track of number of users that are
 * currently logged into the survey system.
 * 
 */
public class UserHitCounter {

    private static UserHitCounter userHitCounter;
    private long numberOfUserAccesses;

    /**
     * private constructor.
     */
    private UserHitCounter() {
        this.numberOfUserAccesses = 0;
    }

    /**
     * Initializes new userHitCounter if already not done.
     * 
     * @return UserHitCounter.
     */
    public static UserHitCounter getInstance() {
        if (userHitCounter == null) {
            initialize();
        }
        return userHitCounter;
    }

    /**
     * Initializes new userHitCounter object.
     */
    public static void initialize() {
        userHitCounter = new UserHitCounter();
    }

    /**
     * @return the numberOfUserAccesses
     */
    public long getNumberOfUserAccesses() {
        return this.numberOfUserAccesses;
    }

    /**
     * Increments the count of the users accessing the system.
     */
    public void incrementNumberOfUserAccesses() {
        this.numberOfUserAccesses++;
    }
}
