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
package edu.ucla.wise.persistence.data;

public class Answer {

    public static enum Type {
        TEXT, INTEGER, DECIMAL
    };

    private final Object answer;
    private final Type type;

    public Answer(Object answer, Type type) {

        if (type == Type.TEXT) {
            String answerAsString = (String) answer;
            this.answer = answerAsString;
        } else {
            int answerAsInt = (int) answer;
            this.answer = answerAsInt;
        }

        this.type = type;

    }

    public Object getAnswer() {
        return this.answer;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        if (this.type == Type.TEXT) {
            return (String) this.answer;
        } else {
            return "" + this.answer;
        }
    }

    public static Answer getAnswer(Object answer, String type) {
        if ("text".equals(type) || "textarea".equals(type)) {
            return new Answer(answer, Type.TEXT);
        } else {
            return new Answer(answer, Type.INTEGER);
        }
    }
}
