/*
 * Copyright 2017 Google.
 *
 * This file is licensed under the GPLv2+Classpath Exception, which full text
 * is found in the LICENSE file at the root of this project.
 *
 * Google designates this particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this code.
 */

<%! for_each_stream_impl = True %>

package com.google.chkstream.${stream_impl};

% if stream_impl == 'java8':
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

% else:
import java8.util.stream.DoubleStream;
import java8.util.stream.IntStream;
import java8.util.stream.LongStream;
import java8.util.stream.Stream;
% endif

/**
 * Static methods for creating new instances of {@link ChkStream}.
 *
 * @author Alexander Dorokhine
 */
public final class ChkStreams {
    private ChkStreams() {}

    /**
     * A builder for initializing a {@link ChkStream} with a checked exception.
     *
     * <p>Call {@link #canThrow(Class)} to start a {@link ChkStream}.
     * {@link ChkStream} implements the same method so more exceptions can be
     * added later.
     *
     * @author Alexander Dorokhine
     */
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
            Class<E> exceptionClass) {
            return new ChkStream<T, E>(exceptionClass, stream);
        }
    }

    /**
     * Returns a builder for a {@link ChkStream} wrapping the given
     * {@link Stream}.
     */
    public static <T> ChkStreamStarter<T> of(Stream<T> stream) {
        return new ChkStreamStarter<T>(stream);
    }

    // Support the specializations
    % for specialization in SPECIALIZATIONS:
    public static final class Chk${specialization}StreamStarter {
        private final ${specialization}Stream stream;

        private Chk${specialization}StreamStarter(
            ${specialization}Stream stream) {
            this.stream = stream;
        }

        /**
         * Initializes a {@link Chk${specialization}Stream} with the given
         * exception type.
         *
         * <p>Additional exceptions can be added later by calling
         * {@link Chk${specialization}Stream#canThrow(Class)}.
         *
         * @param exceptionClass Class of the exception this stream can throw.
         * @return the newly created stream.
         */
        public <E extends Exception> Chk${specialization}Stream<E> canThrow(
            Class<E> exceptionClass) {
            return new Chk${specialization}Stream<E>(exceptionClass, stream);
        }
    }

    /**
     * Returns a builder for a {@link Chk${specialization}Stream} wrapping the
     * given {@link ${specialization}Stream}.
     */
    public static Chk${specialization}StreamStarter of${specialization}(
        ${specialization}Stream stream) {
        return new Chk${specialization}StreamStarter(stream);
    }
    % endfor
}
