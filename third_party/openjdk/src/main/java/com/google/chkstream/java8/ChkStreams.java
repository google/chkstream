/*
 * Copyright 2017 Google.
 *
 * This file is licensed under the GPLv2+Classpath Exception, which full text
 * is found in the LICENSE file at the root of this project.
 *
 * Google designates this particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this code.
 */
package com.google.chkstream.java8;

import java.util.stream.Stream;

public final class ChkStreams {
    private ChkStreams() {}

    public static final class ChkStreamStarter<T> {
        private final Stream<T> stream;

        private ChkStreamStarter(Stream<T> stream) {
            this.stream = stream;
        }

        /**
         * Initializes a {@link ChkStream} with the given exception type.
         *
         * <p>Additional exceptions can be added later by calling
         * {@link ChkStream#canThrow(Class)}.
         *
         * @param exceptionClass Class of the exception this stream can throw.
         * @return the newly created stream.
         */
        public <E extends Exception> ChkStream<T, E> canThrow(
                Class<E> exceptionClass)
        {
            return new ChkStream<>(exceptionClass, stream);
        }
    }

    /**
     * Returns a builder for a {@link ChkStream} wrapping the given
     * {@link Stream}.
     */
    public static <T> ChkStreamStarter<T> of(Stream<T> stream) {
        return new ChkStreamStarter<>(stream);
    }
}
