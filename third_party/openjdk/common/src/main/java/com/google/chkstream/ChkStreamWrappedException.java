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

/**
 * A {@link RuntimeException} that wraps another {@link Exception} type.
 *
 * <p>This wrapper is transparently handled by the {@link ChkStream}
 * infrastructure. You should not be seeing it thrown out of @{link ChkStream}
 * methods, unless you call {@link ChkStream#toStream()} to get the underlying
 * raw {@link Stream} from a {@link ChkStream}.
 *
 * @author Alexander Dorokhine
 */
public class ChkStreamWrappedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ChkStreamWrappedException(Exception e) {
        super(e);
    }
}
