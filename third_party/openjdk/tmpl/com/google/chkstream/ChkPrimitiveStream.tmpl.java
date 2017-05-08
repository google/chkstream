/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

<%!
  split = True
  for_each_stream_impl = True
  for_each_specialization = True

  def get_class_name(num_exceptions, min_exceptions, specialization):
    class_name = 'Chk%sStream' % specialization
    if num_exceptions != min_exceptions:
      class_name = '%s_Throw%d' % (class_name, num_exceptions)
    return class_name

  def get_filename(num_exceptions, min_exceptions, specialization):
    return get_class_name(
        num_exceptions, min_exceptions, specialization) + '.java'
%>

<%
  throws_list = 'throws ' + ', '.join(['E%d' % i for i in xrange(0, num_e)])
  _ThrowN = '' if num_e == MIN_EXCEPTIONS else '_Throw%d' % num_e
  class_name = get_class_name(num_e, MIN_EXCEPTIONS, specialization)
  boxed_type = specialization
  if boxed_type == 'Int':
    boxed_type = 'Integer'

  def exc_list(extends=False, declare=False):
    string = ', '.join(
        ['%sE%d%s' % (
            '? extends ' if extends else '',
            i,
            ' extends Exception' if declare else '')
            for i in xrange(0, num_e)])
    return string

  def class_type(extends=False, declare=False):
    return '%s<%s>' % (
        class_name, exc_list(extends, declare))
%>

package com.google.chkstream.${stream_impl};

% if stream_impl == 'java8':
import java.util.${specialization}SummaryStatistics;
import java.util.Optional;
import java.util.OptionalDouble;
% if specialization != 'Double':
import java.util.Optional${specialization};
% endif
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.${specialization}BinaryOperator;
import java.util.function.${specialization}Function;
import java.util.function.Obj${specialization}Consumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.chkstream.java8.function.FunctionUtils_Throw${num_e};

% else:
import java8.util.${specialization}SummaryStatistics;
import java8.util.Optional;
import java8.util.OptionalDouble;
% if specialization != 'Double':
import java8.util.Optional${specialization};
% endif
import java8.util.PrimitiveIterator;
import java8.util.Spliterator;
import java8.util.function.BiConsumer;
import java8.util.function.BinaryOperator;
import java8.util.function.${specialization}BinaryOperator;
import java8.util.function.${specialization}Function;
import java8.util.function.Obj${specialization}Consumer;
import java8.util.function.Supplier;
import java8.util.stream.DoubleStream;
import java8.util.stream.IntStream;
import java8.util.stream.LongStream;
import java8.util.stream.${specialization}Streams;
import java8.util.stream.Stream;

import com.google.chkstream.streamsupport.function.FunctionUtils_Throw${num_e};
import com.google.chkstream.lang.AutoCloseable;

% endif

import com.google.chkstream.ChkStreamWrappedException;
import com.google.chkstream.function.ChkConsumers.*;
import com.google.chkstream.function.ChkFunctions.*;
import com.google.chkstream.function.ChkPredicates.*;
import com.google.chkstream.function.ChkRunnables.*;

/**
 * A sequence of elements supporting sequential and parallel aggregate
 * operations.
 *
 * <p>{@link ChkStream} is similar to {@link Stream} except that it can throw
 * checked exceptions that have been added to the stream via the
 * {@link #canThrow(Class) method.
 *
 * <p>See the
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html">
 * official {@link Stream} documentation for details.</a>
 *
 * @see Stream
 */
public class ${class_type(declare=True)}
    implements AutoCloseable {
    private final ${specialization}Stream stream;
    % for i in xrange(0, num_e):
    private final Class<E${i}> e${i}Class;
    % endfor
    private final FunctionUtils_Throw${num_e} functionUtils;

    ${class_name}(
        % for i in xrange(0, num_e):
        Class<E${i}> e${i}Class,
        % endfor,
        ${specialization}Stream stream) {
      this.stream = stream;
      % for i in xrange(0, num_e):
      this.e${i}Class = e${i}Class;
      % endfor
      this.functionUtils = new FunctionUtils_Throw${num_e}();
    }

    // Methods from BaseStream.

    /**
     * Returns an iterator for the elements of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return the element iterator for this stream
     */
    public PrimitiveIterator.Of${specialization} iterator() {
        return stream.iterator();
    }

    /**
     * Returns a spliterator for the elements of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return the element spliterator for this stream
     */
    public Spliterator.Of${specialization} spliterator() {
        return stream.spliterator();
    }

    /**
     * Returns whether this stream, if a terminal operation were to be executed,
     * would execute in parallel.  Calling this method after invoking an
     * terminal stream operation method may yield unpredictable results.
     *
     * @return {@code true} if this stream would execute in parallel if executed
     */
    public boolean isParallel() {
        return stream.isParallel();
    }

    /**
     * Returns an equivalent stream that is sequential.  May return
     * itself, either because the stream was already sequential, or because
     * the underlying stream state was modified to be sequential.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @return a sequential stream
     */
    public ${class_type()} sequential() {
        return fromStream(stream.sequential());
    }

    /**
     * Returns an equivalent stream that is parallel.  May return
     * itself, either because the stream was already parallel, or because
     * the underlying stream state was modified to be parallel.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @return a parallel stream
     */
    public ${class_type()} parallel() {
        return fromStream(stream.parallel());
    }

    /**
     * Returns an equivalent stream that is
     * <a href="package-summary.html#Ordering">unordered</a>.  May return
     * itself, either because the stream was already unordered, or because
     * the underlying stream state was modified to be unordered.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @return an unordered stream
     */
    public ${class_type()} unordered() {
        return fromStream(stream.unordered());
    }

    /**
     * Returns an equivalent stream with an additional close handler.  Close
     * handlers are run when the {@link #close()} method
     * is called on the stream, and are executed in the order they were
     * added.  All close handlers are run, even if earlier close handlers throw
     * exceptions.  If any close handler throws an exception, the first
     * exception thrown will be relayed to the caller of {@code close()}, with
     * any remaining exceptions added to that exception as suppressed exceptions
     * (unless one of the remaining exceptions is the same exception as the
     * first exception, since an exception cannot suppress itself.)  May
     * return itself.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param closeHandler A task to execute when the stream is closed
     * @return a stream with a handler that is run if the stream is closed
     */
    public ${class_type()} onClose(
        final ChkRunnable${_ThrowN}
            <${exc_list(extends=True)}>
                closeHandler) {
      return fromStream(
          stream.onClose(functionUtils.wrapChkRunnable(closeHandler)));
    }

    /**
     * Closes this stream, causing all close handlers for this stream pipeline
     * to be called.
     *
     * @see AutoCloseable#close()
     */
    @Override
    public void close() ${throws_list} {
        try {
            stream.close();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
        }
    }

    /**
     * Returns a stream consisting of the elements of this stream that match
     * the given predicate.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to each element to determine if it
     *                  should be included
     * @return the new stream
     */
    public ${class_type()} filter(
        final Chk${specialization}Predicate${_ThrowN}
            <${exc_list(extends=True)}>
                predicate) {
      return fromStream(
          stream.filter(
              functionUtils.wrapChk${specialization}Predicate(predicate)));
    }

    /**
     * Returns an object-valued {@code Stream} consisting of the results of
     * applying the given function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">
     *     intermediate operation</a>.
     *
     * @param <U> the element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    public <U> ChkStream${_ThrowN}<U, ${exc_list()}> mapToObj(
        Chk${specialization}Function${_ThrowN}
            <? extends U,
             ${exc_list(extends=True)}>
                mapper) {
        Stream<U> newStream =
            stream.mapToObj(
                functionUtils.wrapChk${specialization}Function(mapper));
        return new ChkStream${_ThrowN}<U, ${exc_list()}>(
            ${''.join(['e%dClass,' % i for i in xrange(0, num_e)])}
            newStream);
    }

    /**
     * Returns a stream consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    public
    Chk${specialization}Stream${_ThrowN}
        <${exc_list()}>
            map(
                Chk${specialization}UnaryOperator${_ThrowN}
                    <${exc_list(extends=True)}>
                        mapper) {
        return fromStream(
            stream.map(
                functionUtils.wrapChk${specialization}UnaryOperator(mapper)));
    }

    % for dest_specialization in SPECIALIZATIONS:
    % if dest_specialization != specialization:
    /**
     * Returns an {@code ${dest_specialization}Stream} consisting of the results
     * of applying the given function to the elements of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">
     *     intermediate operation</a>.
     *
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element
     * @return the new stream
     */
    public
    Chk${dest_specialization}Stream${_ThrowN}<${exc_list()}>
        mapTo${dest_specialization}(
            final Chk${specialization}To${dest_specialization}Function${_ThrowN}
                <${exc_list(extends=True)}>
                    mapper) {
        ${dest_specialization}Stream newStream =
            stream.mapTo${dest_specialization}(
                functionUtils.wrapChk${specialization}To${dest_specialization}Function(
                    mapper));
        return new Chk${dest_specialization}Stream${_ThrowN}<${exc_list()}>(
            ${''.join(['e%dClass,' % i for i in xrange(0, num_e)])}
            newStream);
    }
    % endif
    % endfor

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with the contents of a mapped stream produced by applying
     * the provided mapping function to each element.  Each mapped stream is
     * {@link java.util.stream.BaseStream#close() closed} after its contents
     * have been placed into this stream.  (If a mapped stream is {@code null}
     * an empty stream is used, instead.)
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @apiNote
     * The {@code flatMap()} operation has the effect of applying a one-to-many
     * transformation to the elements of the stream, and then flattening the
     * resulting elements into a new stream.
     *
     * <p><b>Examples.</b>
     *
     * <p>If {@code orders} is a stream of purchase orders, and each purchase
     * order contains a collection of line items, then the following produces a
     * stream containing all the line items in all the orders:
     * <pre>{@code
     *     orders.flatMap(order -> order.getLineItems().stream())...
     * }</pre>
     *
     * <p>If {@code path} is the path to a file, then the following produces a
     * stream of the {@code words} contained in that file:
     * <pre>{@code
     *     ChkStream<String> lines = Files.lines(path, StandardCharsets.UTF_8);
     *     ChkStream<String> words = lines.flatMap(line -> Stream.of(line.split(" +")));
     * }</pre>
     * The {@code mapper} function passed to {@code flatMap} splits a line,
     * using a simple regular expression, into an array of words, and then
     * creates a stream of words from that array.
     *
     * @param <R> The element type of the new stream
     * @param mapper a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *               <a href="package-summary.html#Statelessness">stateless</a>
     *               function to apply to each element which produces a stream
     *               of new values
     * @return the new stream
     */
    public ${class_type()} flatMap(
        final Chk${specialization}Function${_ThrowN}
            <? extends ${specialization}Stream,
             ${exc_list(extends=True)}>
                  mapper) {
        return fromStream(
            stream.flatMap(
                functionUtils.wrapChk${specialization}Function(mapper)));
    }

    /**
     * Like #flatMap(ChkFunction${_ThrowN}), except the function returns a
     * {@link ChkStream} of the same generic type as this stream.
     */
    public ${class_type()} flatMapChk(
        final Chk${specialization}Function${_ThrowN}
            <? extends ${class_name}<${exc_list(extends=True)}>,
             ${exc_list(extends=True)}>
                  mapper) {
        return fromStream(stream.flatMap(
            new ${specialization}Function<${specialization}Stream>() {
                @Override
                public ${specialization}Stream apply(
                    ${specialization.lower()} t) {
                    ${class_name}<${exc_list(extends=True)}> subStream;
                    try {
                        subStream = mapper.apply(t);
                    } catch (Exception e) {
                        throw new ChkStreamWrappedException(e);
                    }
                    return subStream.toStream();
                }
            }));
    }

    /**
     * Returns a stream consisting of the distinct elements (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * <p>For ordered streams, the selection of distinct elements is stable
     * (for duplicated elements, the element appearing first in the encounter
     * order is preserved.)  For unordered streams, no stability guarantees
     * are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @apiNote
     * Preserving stability for {@code distinct()} in parallel pipelines is
     * relatively expensive (requires that the operation act as a full barrier,
     * with substantial buffering overhead), and stability is often not needed.
     * Using an unordered stream source (such as {@link #generate(Supplier)})
     * or removing the ordering constraint with {@link #unordered()} may result
     * in significantly more efficient execution for {@code distinct()} in parallel
     * pipelines, if the semantics of your situation permit.  If consistency
     * with encounter order is required, and you are experiencing poor performance
     * or memory utilization with {@code distinct()} in parallel pipelines,
     * switching to sequential execution with {@link #sequential()} may improve
     * performance.
     *
     * @return the new stream
     */
    public ${class_type()} distinct() {
        return fromStream(stream.distinct());
    }

    /**
     * Returns a stream consisting of the elements of this stream, sorted
     * according to natural order.
     *
     * <p>For ordered streams, the sort is stable.  For unordered streams, no
     * stability guarantees are made.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @return the new stream
     */
    public ${class_type()} sorted() {
        return fromStream(stream.sorted());
    }

    /**
     * Returns a stream consisting of the elements of this stream, additionally
     * performing the provided action on each element as elements are consumed
     * from the resulting stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * <p>For parallel stream pipelines, the action may be called at
     * whatever time and in whatever thread the element is made available by the
     * upstream operation.  If the action modifies shared state,
     * it is responsible for providing the required synchronization.
     *
     * @apiNote This method exists mainly to support debugging, where you want
     * to see the elements as they flow past a certain point in a pipeline:
     * <pre>{@code
     *     Stream.of("one", "two", "three", "four")
     *         .filter(e -> e.length() > 3)
     *         .peek(e -> System.out.println("Filtered value: " + e))
     *         .map(String::toUpperCase)
     *         .peek(e -> System.out.println("Mapped value: " + e))
     *         .collect(Collectors.toList());
     * }</pre>
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *                 non-interfering</a> action to perform on the elements as
     *                 they are consumed from the stream
     * @return the new stream
     */
    public ${class_type()} peek(
        final Chk${specialization}Consumer${_ThrowN}
            <${exc_list(extends=True)}>
                action) {
        return fromStream(
            stream.peek(
                functionUtils.wrapChk${specialization}Consumer(action)));
    }

    /**
     * Returns a stream consisting of the elements of this stream, truncated
     * to be no longer than {@code maxSize} in length.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * stateful intermediate operation</a>.
     *
     * @apiNote
     * While {@code limit()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code maxSize}, since {@code limit(n)}
     * is constrained to return not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order.  Using an unordered
     * stream source (such as {@link #generate(Supplier)}) or removing the
     * ordering constraint with {@link #unordered()} may result in significant
     * speedups of {@code limit()} in parallel pipelines, if the semantics of
     * your situation permit.  If consistency with encounter order is required,
     * and you are experiencing poor performance or memory utilization with
     * {@code limit()} in parallel pipelines, switching to sequential execution
     * with {@link #sequential()} may improve performance.
     *
     * @param maxSize the number of elements the stream should be limited to
     * @return the new stream
     * @throws IllegalArgumentException if {@code maxSize} is negative
     */
    public ${class_type()} limit(long maxSize) {
        return fromStream(stream.limit(maxSize));
    }

    /**
     * Returns a stream consisting of the remaining elements of this stream
     * after discarding the first {@code n} elements of the stream.
     * If this stream contains fewer than {@code n} elements then an
     * empty stream will be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">stateful
     * intermediate operation</a>.
     *
     * @apiNote
     * While {@code skip()} is generally a cheap operation on sequential
     * stream pipelines, it can be quite expensive on ordered parallel pipelines,
     * especially for large values of {@code n}, since {@code skip(n)}
     * is constrained to skip not just any <em>n</em> elements, but the
     * <em>first n</em> elements in the encounter order.  Using an unordered
     * stream source (such as {@link #generate(Supplier)}) or removing the
     * ordering constraint with {@link #unordered()} may result in significant
     * speedups of {@code skip()} in parallel pipelines, if the semantics of
     * your situation permit.  If consistency with encounter order is required,
     * and you are experiencing poor performance or memory utilization with
     * {@code skip()} in parallel pipelines, switching to sequential execution
     * with {@link #sequential()} may improve performance.
     *
     * @param n the number of leading elements to skip
     * @return the new stream
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public ${class_type()} skip(long n) {
        return fromStream(stream.skip(n));
    }

    /**
     * Performs an action for each element of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>The behavior of this operation is explicitly nondeterministic.
     * For parallel stream pipelines, this operation does <em>not</em>
     * guarantee to respect the encounter order of the stream, as doing so
     * would sacrifice the benefit of parallelism.  For any given element, the
     * action may be performed at whatever time and in whatever thread the
     * library chooses.  If the action accesses shared state, it is
     * responsible for providing the required synchronization.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     */
    public void forEach(
        final Chk${specialization}Consumer${_ThrowN}
            <${exc_list(extends=True)}>
                action) ${throws_list} {
        try {
            stream.forEach(
                functionUtils.wrapChk${specialization}Consumer(action));
      } catch (ChkStreamWrappedException e) {
          rethrowException(e);
      }
    }

    /**
     * Performs an action for each element of this stream, in the encounter
     * order of the stream if the stream has a defined encounter order.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * <p>This operation processes the elements one at a time, in encounter
     * order if one exists.  Performing the action for one element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * performing the action for subsequent elements, but for any given element,
     * the action may be performed in whatever thread the library chooses.
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     * @see #forEach(Consumer)
     */
    public void forEachOrdered(
        final Chk${specialization}Consumer${_ThrowN}
            <${exc_list(extends=True)}>
                action) ${throws_list} {
        try {
            stream.forEachOrdered(
                functionUtils.wrapChk${specialization}Consumer(action));
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
        }
    }

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an array containing the elements of this stream
     */
    public ${specialization.lower()}[] toArray() ${throws_list} {
        try {
            return stream.toArray();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using the provided identity value and an
     * <a href="package-summary.html#Associativity">associative</a>
     * accumulation function, and returns the reduced value.  This is equivalent
     * to:
     * <pre>{@code
     *     T result = identity;
     *     for (T element : this stream)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code identity} value must be an identity for the accumulator
     * function. This means that for all {@code t},
     * {@code accumulator.apply(identity, t)} is equal to {@code t}.
     * The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote Sum, min, max, average, and string concatenation are all special
     * cases of reduction. Summing a stream of numbers can be expressed as:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * or:
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>While this may seem a more roundabout way to perform an aggregation
     * compared to simply mutating a running total in a loop, reduction
     * operations parallelize more gracefully, without needing additional
     * synchronization and with greatly reduced risk of data races.
     *
     * @param identity the identity value for the accumulating function
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return the result of the reduction
     */
    public ${specialization.lower()} reduce(
        ${specialization.lower()} identity,
        ${specialization}BinaryOperator accumulator) ${throws_list} {
        try {
            return stream.reduce(identity, accumulator);
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return -1;
        }
    }

    /**
     * Performs a <a href="package-summary.html#Reduction">reduction</a> on the
     * elements of this stream, using an
     * <a href="package-summary.html#Associativity">associative</a> accumulation
     * function, and returns an {@code Optional} describing the reduced value,
     * if any. This is equivalent to:
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : this stream) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     *
     * but is not constrained to execute sequentially.
     *
     * <p>The {@code accumulator} function must be an
     * <a href="package-summary.html#Associativity">associative</a> function.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values
     * @return an {@link Optional} describing the result of the reduction
     * @throws NullPointerException if the result of the reduction is null
     * @see #reduce(Object, BinaryOperator)
     * @see #min()
     * @see #max()
     */
    public Optional${specialization} reduce(
        ${specialization}BinaryOperator op) ${throws_list} {
        try {
            return stream.reduce(op);
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Performs a <a href="package-summary.html#MutableReduction">mutable
     * reduction</a> operation on the elements of this stream.  A mutable
     * reduction is one in which the reduced value is a mutable result container,
     * such as an {@code ArrayList}, and elements are incorporated by updating
     * the state of the result rather than by replacing the result.  This
     * produces a result equivalent to:
     * <pre>{@code
     *     R result = supplier.get();
     *     for (T element : this stream)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>Like {@link #reduce(Object, BinaryOperator)}, {@code collect} operations
     * can be parallelized without requiring additional synchronization.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @apiNote There are many existing classes in the JDK whose signatures are
     * well-suited for use with method references as arguments to {@code collect()}.
     * For example, the following will accumulate strings into an {@code ArrayList}:
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
     *                                                ArrayList::addAll);
     * }</pre>
     *
     * <p>The following will take a stream of strings and concatenates them into a
     * single string:
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     *
     * @param <R> type of the result
     * @param supplier a function that creates a new result container. For a
     *                 parallel execution, this function may be called
     *                 multiple times and must return a fresh value each time.
     * @param accumulator an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for incorporating an additional element into a result
     * @param combiner an <a href="package-summary.html#Associativity">associative</a>,
     *                    <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                    <a href="package-summary.html#Statelessness">stateless</a>
     *                    function for combining two values, which must be
     *                    compatible with the accumulator function
     * @return the result of the reduction
     */
    public <R> R collect(
        Supplier<R> supplier,
        Obj${specialization}Consumer<R> accumulator,
        BiConsumer<R, R> combiner) ${throws_list} {
        try {
            return stream.collect(supplier, accumulator, combiner);
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns the sum of elements in this stream.  This is a special case
     * of a <a href="package-summary.html#Reduction">reduction</a>
     * and is equivalent to:
     * <pre>{@code
     *     return reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return the sum of elements in this stream
     */
    public ${specialization.lower()} sum() ${throws_list} {
      try {
          return stream.sum();
      } catch (ChkStreamWrappedException e) {
          rethrowException(e);
          return -1;
      }
    }

    /**
     * Returns an {@code Optional} describing the minimum element of this
     * stream, or an empty optional if this stream is empty.  This is a special
     * case of a <a href="package-summary.html#Reduction">reduction</a>
     * and is equivalent to:
     * <pre>{@code
     *     return reduce(Integer::min);
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return an {@code Optional} containing the minimum element of this
     * stream, or an empty {@code Optional} if the stream is empty
     */
    public Optional${specialization} min() ${throws_list} {
        try {
            return stream.min();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns an {@code Optional} describing the maximum element of this
     * stream, or an empty optional if this stream is empty.  This is a special
     * case of a <a href="package-summary.html#Reduction">reduction</a>
     * and is equivalent to:
     * <pre>{@code
     *     return reduce(Integer::max);
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an {@code Optional} containing the maximum element of this
     * stream, or an empty {@code Optional} if the stream is empty
     */
    public Optional${specialization} max() ${throws_list} {
        try {
            return stream.max();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns the count of elements in this stream.  This is a special case of
     * a <a href="package-summary.html#Reduction">reduction</a> and is
     * equivalent to:
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
     *
     * @return the count of elements in this stream
     */
    public long count() ${throws_list} {
        try {
            return stream.count();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return -1;
        }
    }

    /**
     * Returns an {@code OptionalDouble} describing the arithmetic mean of elements of
     * this stream, or an empty optional if this stream is empty.  This is a
     * special case of a
     * <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an {@code OptionalDouble} containing the average element of this
     * stream, or an empty optional if the stream is empty
     */
    public OptionalDouble average() ${throws_list} {
        try {
            return stream.average();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns an {@code IntSummaryStatistics} describing various
     * summary data about the elements of this stream.  This is a special
     * case of a <a href="package-summary.html#Reduction">reduction</a>.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">terminal
     * operation</a>.
     *
     * @return an {@code IntSummaryStatistics} describing various summary data
     * about the elements of this stream
     */
    public ${specialization}SummaryStatistics summaryStatistics()
        ${throws_list} {
        try {
            return stream.summaryStatistics();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns whether any elements of this stream match the provided
     * predicate.  May not evaluate the predicate on all elements if not
     * necessary for determining the result.  If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>existential quantification</em> of the
     * predicate over the elements of the stream (for some x P(x)).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if any elements of the stream match the provided
     * predicate, otherwise {@code false}
     */
    public boolean anyMatch(
        final Chk${specialization}Predicate${_ThrowN}
            <${exc_list(extends=True)}>
                predicate) ${throws_list} {
        try {
            return stream.anyMatch(
                functionUtils.wrapChk${specialization}Predicate(predicate));
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return false;
        }
    }

    /**
     * Returns whether all elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>universal quantification</em> of the
     * predicate over the elements of the stream (for all x P(x)).  If the
     * stream is empty, the quantification is said to be <em>vacuously
     * satisfied</em> and is always {@code true} (regardless of P(x)).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either all elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean allMatch(
        final Chk${specialization}Predicate${_ThrowN}
            <${exc_list(extends=True)}>
                predicate) ${throws_list} {
        try {
            return stream.allMatch(
                functionUtils.wrapChk${specialization}Predicate(predicate));
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return false;
        }
    }

    /**
     * Returns whether no elements of this stream match the provided predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result.  If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @apiNote
     * This method evaluates the <em>universal quantification</em> of the
     * negated predicate over the elements of the stream (for all x ~P(x)).  If
     * the stream is empty, the quantification is said to be vacuously satisfied
     * and is always {@code true}, regardless of P(x).
     *
     * @param predicate a <a href="package-summary.html#NonInterference">non-interfering</a>,
     *                  <a href="package-summary.html#Statelessness">stateless</a>
     *                  predicate to apply to elements of this stream
     * @return {@code true} if either no elements of the stream match the
     * provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean noneMatch(
        final Chk${specialization}Predicate${_ThrowN}
            <${exc_list(extends=True)}>
                predicate) ${throws_list} {
        try {
            return stream.noneMatch(
                functionUtils.wrapChk${specialization}Predicate(predicate));
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return false;
        }
    }

    /**
     * Returns an {@link Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty.  If the stream has
     * no encounter order, then any element may be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @return an {@code Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     */
    public Optional${specialization} findFirst() ${throws_list} {
        try {
            return stream.findFirst();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    /**
     * Returns an {@link Optional} describing some element of the stream, or an
     * empty {@code Optional} if the stream is empty.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * <p>The behavior of this operation is explicitly nondeterministic; it is
     * free to select any element in the stream.  This is to allow for maximal
     * performance in parallel operations; the cost is that multiple invocations
     * on the same source may not return the same result.  (If a stable result
     * is desired, use {@link #findFirst()} instead.)
     *
     * @return an {@code Optional} describing some element of this stream, or an
     * empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     * @see #findFirst()
     */
    public Optional${specialization} findAny() ${throws_list} {
        try {
            return stream.findAny();
        } catch (ChkStreamWrappedException e) {
            rethrowException(e);
            return null;
        }
    }

    <%
      banned_combinations = [
          ('Long', 'Int'),
          ('Double', 'Int'),
          ('Double', 'Long'),
      ]
    %>
    % for dest_specialization in SPECIALIZATIONS:
    % if specialization != dest_specialization:
    % if (specialization, dest_specialization) not in banned_combinations:
    /**
     * Returns a {@code ${dest_specialization}Stream} consisting of the elements
     * of this stream.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @return a {@code ${dest_specialization}Stream} consisting of the elements
     * of this stream.
     */
    public Chk${dest_specialization}Stream${_ThrowN}<${exc_list()}>
        as${dest_specialization}Stream() {
        ${dest_specialization}Stream newStream =
            stream.as${dest_specialization}Stream();
        return new Chk${dest_specialization}Stream${_ThrowN}<${exc_list()}>(
            ${''.join(['e%dClass,' % i for i in xrange(0, num_e)])}
            newStream);
    }
    % else:
    // Chk${specialization}#as${dest_specialization}Stream() not generated, it
    // is not an allowed combination.
    % endif
    % endif
    % endfor

    /**
     * Returns a {@code Stream} consisting of the elements of this stream,
     * each boxed to an {@code Integer}.
     *
     * <p>This is an <a href="package-summary.html#StreamOps">intermediate
     * operation</a>.
     *
     * @return a {@code Stream} consistent of the elements of this stream,
     * each boxed to an {@code Integer}
     */
    public ChkStream${_ThrowN}<${boxed_type}, ${exc_list()}> boxed() {
      Stream<${boxed_type}> newStream = stream.boxed();
      return new ChkStream${_ThrowN}<${boxed_type}, ${exc_list()}>(
          ${''.join(['e%dClass,' % i for i in xrange(0, num_e)])}
          newStream);
    }

    // Adapted static methods.

    /**
     * Creates a lazily concatenated stream whose elements are all the
     * elements of this stream followed by all the elements of the
     * given stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     *
     * @implNote
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowException}.
     *
     * @param <T> The type of stream elements
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public ${class_type()} concat(${specialization}Stream b) {
        ${specialization}Stream concatStream =
            ${specialization}Stream${'' if stream_impl == 'java8' else 's'}
                .concat(toStream(), b);
        return new ${class_type()}(
            ${', '.join(['e%dClass' % i for i in xrange(0, num_e)])},
            concatStream);
    }

    /**
     * Like {@link #concat(Stream)}, except that the stream to be concatenated
     * is a {@link ChkStream} of the same generic type.
     */
    public ${class_type()} concat(
        ${class_type(extends=True)} b) {
        return concat(b.toStream());
    }

    // New methods specific to ChkStream.

    % if num_e != MAX_EXCEPTIONS:
    <%
      next_class_type = (
          'Chk%sStream_Throw%d<%s>' % (
              specialization,
              num_e + 1,
              ', '.join(['E%d' % i for i in xrange(0, num_e)] + ['NewE'])))
    %>
    /**
     * Returns a stream consisting of the elements of this stream, but where the
     * functions passed to Stream operations can throw an additional checked
     * exception type.
     *
     * @param exceptionClass Class of the new exception the returned stream can
     *     throw.
     * @return the newly created stream.
     */
    public <NewE extends Exception>
    ${next_class_type} canThrow(Class<NewE> clazz) {
        return new ${next_class_type}(
            ${', '.join(
                ['e%dClass' % i for i in xrange(0, num_e)] + ['clazz'])},
            stream);
    }
    % else:
    // canThrow() not generated; this stream type has the max allowed exceptions
    % endif

    /**
     * Returns a {@link Stream} containing the elements of this ChkStream, but
     * which cannot throw checked exceptions.
     *
     * <p>Any checked exceptions thrown by stream operations that have already
     * been added will be wrapped in {@link ChkStreamWrappedException}, an
     * instance of {@link RuntimeException}.
     */
    public ${specialization}Stream toStream() {
        return stream;
    }

    // Private methods.

    private ${class_type()} fromStream(${specialization}Stream stream) {
        return new ${class_type()}(
            ${''.join(['e%dClass,' % i for i in xrange(0, num_e)])}
            stream);
    }

    @SuppressWarnings("unchecked")
    private void rethrowException(ChkStreamWrappedException wrapE)
        ${throws_list} {
        Throwable e = wrapE.getCause();
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        % for i in xrange(0, num_e):
        if (e${i}Class.isInstance(e)) { throw (E${i}) e; }
        % endfor
        throw wrapE;
    }
}
