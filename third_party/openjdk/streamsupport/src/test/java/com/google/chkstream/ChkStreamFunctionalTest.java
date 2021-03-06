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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.chkstream.streamsupport.ChkStream;
import com.google.chkstream.streamsupport.ChkStreams;

import java8.util.stream.Collectors;
import java8.util.stream.IntStreams;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;

/** Tests for the streaming functionality (maps and transforms) of ChkStream. */
public class ChkStreamFunctionalTest {

    // Tests for transforms (non-sinks)

    @Test
    public void testMap() {
        assertThat(
                wrap(RefStreams.of(1, 2, 3))
                .map(x -> x * 10)
                .collect(Collectors.toList()))
        .containsExactly(10, 20, 30).inOrder();
    }

    @Test
    public void testFlatMap_Stream() throws IOException {
        List<String> results =
                ChkStreams.of(RefStreams.of(1, 2, 3))
                .canThrow(IOException.class)
                .flatMap(x -> RefStreams.of("yay " + x, "ok " + x, "nay " + x))
                .collect(Collectors.toList());
        assertThat(results).containsExactly(
                "yay 1", "ok 1", "nay 1",
                "yay 2", "ok 2", "nay 2",
                "yay 3", "ok 3", "nay 3").inOrder();
    }

    @Test
    public void testFlatMap_SameChkStream() throws IOException {
        List<String> results =
                ChkStreams.of(RefStreams.of(1, 2, 3))
                .canThrow(IOException.class)
                .flatMapChk(
                        x ->
                        ChkStreams.of(RefStreams.of("yay " + x, "ok " + x, "nay " + x))
                        .canThrow(IOException.class)
                        ).collect(Collectors.toList());
        assertThat(results).containsExactly(
                "yay 1", "ok 1", "nay 1",
                "yay 2", "ok 2", "nay 2",
                "yay 3", "ok 3", "nay 3").inOrder();
    }

    @Test
    public void testFilter() {
        assertThat(
                wrap(RefStreams.of(1, 2, 3, 4))
                .filter(x -> x % 2 == 0)
                .collect(Collectors.toList())
                ).containsExactly(2, 4).inOrder();
    }

    @Test
    public void testConcatSameType() throws IOException {
        ChkStream<Integer, RuntimeException> stream =
                wrap(RefStreams.of(1, 2, 3))
                .map(x -> x + 10)
                .concat(wrap(RefStreams.of(4, 5, 6)).map(x -> x + 20));
        List<Integer> results = stream.collect(Collectors.toList());
        assertThat(results).containsExactly(11, 12, 13, 24, 25, 26).inOrder();
    }

    @Test
    public void testConcatSafeType() throws IOException {
        ChkStream<Integer, RuntimeException> stream =
                wrap(RefStreams.of(1, 2, 3))
                .map(x -> x + 10)
                .concat(RefStreams.of(4, 5, 6).map(x -> x + 20));
        List<Integer> results = stream.collect(Collectors.toList());
        assertThat(results).containsExactly(11, 12, 13, 24, 25, 26).inOrder();
    }

    @Test
    public void testSort() {
        assertThat(
                wrap(RefStreams.of(50, 20, 100, 75))
                .sorted()
                .collect(Collectors.toList())
                ).containsExactly(20, 50, 75, 100).inOrder();
    }

    @Test
    public void testSortWithComparator() {
        assertThat(
                wrap(RefStreams.of(50, 20, 100, 75))
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList())
                ).containsExactly(100, 75, 50, 20).inOrder();
    }

    @Test
    public void testSortNonComparable() {
        ChkStream<IOException,RuntimeException> stream =
                wrap(RefStreams.of(new IOException("x"), new IOException("y")))
                .sorted();
        try {
            stream.collect(Collectors.toList());
            fail();
        } catch (ClassCastException e) {}
    }

    // Tests for sinks

    @Test
    public void testForEach() {
        List<Integer> list = new ArrayList<>();
        wrap(RefStreams.of(1, 2, 3, 4)).forEach(x -> list.add(x));
        assertThat(list).containsExactly(1, 2, 3, 4).inOrder();
    }


    @Test
    public void testCount() {
        assertEquals(4, wrap(RefStreams.of(1, 2, 3, 4)).count());
    }

    @Test
    public void testMin() {
        assertThat(
                wrap(RefStreams.of(50, 20, 100, 75)).min(Integer::compareTo).get()
                ).isEqualTo(20);
    }

    @Test
    public void testMinEmpty() {
        assertThat(
                wrap(RefStreams.<Integer>empty())
                .min(Integer::compareTo)
                .isPresent()
                ).isFalse();
    }

    @Test
    public void testMax() {
        assertThat(
                wrap(RefStreams.of(50, 20, 100, 75)).max(Integer::compareTo).get()
                ).isEqualTo(100);
    }

    @Test
    public void testMaxEmpty() {
        assertThat(
                wrap(RefStreams.<Integer>empty()).max(Integer::compareTo).isPresent()
                ).isFalse();
    }

    @Test
    public void testAllMatch() {
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).allMatch(x -> x <= 3)).isTrue();
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).allMatch(x -> x <= 2)).isFalse();

        // Make sure it short circuits
        assertThat(
                wrap(RefStreams.of(1, 4, 3))
                .filter(x -> {
                    // Make sure it never sees this line due to short circuit
                    assertThat(x).isNotEqualTo(3);
                    return true;
                })
                .allMatch(x -> x <= 3)).isFalse();

        // Vacuously true
        assertThat(wrap(RefStreams.empty()).allMatch(x -> false)).isTrue();
    }

    @Test
    public void testAnyMatch() {
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).anyMatch(x -> x <= 3)).isTrue();
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).anyMatch(x -> x <= 0)).isFalse();

        // Make sure it short circuits
        assertThat(
                wrap(RefStreams.of(1, 4, 3))
                .filter(x -> {
                    // Make sure it never sees this line due to short circuit
                    assertThat(x).isNotEqualTo(3);
                    return true;
                })
                .anyMatch(x -> x == 4)).isTrue();

        // Vacuously false
        assertThat(wrap(RefStreams.empty()).anyMatch(x -> false)).isFalse();
    }

    @Test
    public void testNoneMatch() {
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).noneMatch(x -> x <= 0)).isTrue();
        assertThat(
                wrap(RefStreams.of(1, 2, 3)).noneMatch(x -> x <= 3)).isFalse();

        // Make sure it short circuits
        assertThat(
                wrap(RefStreams.of(1, 4, 3))
                .filter(x -> {
                    // Make sure it never sees this line due to short circuit
                    assertThat(x).isNotEqualTo(3);
                    return true;
                })
                .noneMatch(x -> x == 4)).isFalse();

        // Vacuously true
        assertThat(wrap(RefStreams.empty()).noneMatch(x -> false)).isTrue();
    }

    @Test
    public void testConcat() {
        ChkStream<Integer,RuntimeException> a =
                wrap(RefStreams.of(1, 2, 3)).map(x -> x * 10);
        ChkStream<Integer,RuntimeException> b =
                wrap(RefStreams.of(4, 5, 6)).map(x -> x * 100);
        assertThat(a.concat(b).collect(Collectors.toList()))
        .containsExactly(10, 20, 30, 400, 500, 600).inOrder();
    }

    @Test
    public void testPrimitive() throws IOException, SQLException {
        double avg = ChkStreams.ofInt(IntStreams.range(5, 10))
            .canThrow(IOException.class)
            .average().getAsDouble();
        assertThat(avg).isWithin(0.01).of(7);
    }

    private static <T> ChkStream<T,RuntimeException> wrap(Stream<T> stream) {
        return ChkStreams.of(stream).canThrow(RuntimeException.class);
    }
}
