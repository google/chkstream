/*
 * Copyright 2017 Google.
 *
 * This file is licensed under the GPLv2+Classpath Exception, which full text
 * is found in the LICENSE file at the root of this project.
 *
 * Google designates this particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this code.
 */
package com.dorokhine.chkstream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.chkstream.java8.ChkStreams;

public class ChkStreamThrowTest {
    @Test
    public void testCanThrow1() {
        try {
            ChkStreams.of(Stream.of(1)).canThrow(IOException.class).map(x -> {
                throw new IOException("Simulated error: " + x);
            }).toArray();
            fail();
        } catch (IOException e) {
            assertEquals("Simulated error: 1", e.getMessage());
        }
    }
}
