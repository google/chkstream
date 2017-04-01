/*
 * Copyright 2017 Google.
 *
 * This file is licensed under the GPLv2+Classpath Exception, which full text
 * is found in the LICENSE file at the root of this project.
 *
 * Google designates this particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this code.
 */
package com.google.chkstream;

public class ChkStreamWrappedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ChkStreamWrappedException(Exception e) {
        super(e);
    }
}
