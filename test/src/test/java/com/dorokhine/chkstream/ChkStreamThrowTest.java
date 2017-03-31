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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.chkstream.function.ChkFunction.ChkFunction_Throw1;
import com.google.chkstream.java8.ChkStream_Throw2;
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

    @Test
    public void testCanThrow2() {
        try {
            ChkStreams.of(Stream.of(1))
            .canThrow(IOException.class)
            .canThrow(InterruptedException.class)
            .map(x -> {
                if (x == 1) {
                    throw new InterruptedException(
                        "Simulated interrupt error: " + x);
                } else {
                    throw new IOException(
                        "Simulated io error: " + x);
                }
            }).toArray();
            fail();
        } catch (InterruptedException e) {
            assertEquals("Simulated interrupt error: 1", e.getMessage());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testConcatAndThrow() throws Exception {
        ChkStream_Throw2<Integer, IOException, InterruptedException> a =
                ChkStreams.of(Stream.of(1, 2))
                .canThrow(IOException.class)
                .canThrow(InterruptedException.class);
        ChkStream_Throw2<Integer, IOException, InterruptedException> b =
                ChkStreams.of(Stream.of(10, 20))
                .canThrow(IOException.class)
                .canThrow(InterruptedException.class);
        ChkStream_Throw2<Integer, IOException, InterruptedException> concat =
                a.concat(b);
        concat = concat.<Integer>map(
                x -> { throw new InterruptedException("e"); });
        try {
            concat.toArray();
            fail();
        } catch (InterruptedException e) {}
    }

    @Test
    public void testCanThrowDerived() throws IOException {
        try {
            ChkStreams.of(Stream.of(1)).canThrow(IOException.class).map(
                new ChkFunction_Throw1<Integer, Void, FileNotFoundException>() {
                    @Override
                    public Void apply(Integer t) throws FileNotFoundException {
                        throw new FileNotFoundException(
                            "Simulated error: " + t);
                    }
            }).toArray();
            fail();
        } catch (FileNotFoundException e) {
            assertEquals("Simulated error: 1", e.getMessage());
        }
    }
}
