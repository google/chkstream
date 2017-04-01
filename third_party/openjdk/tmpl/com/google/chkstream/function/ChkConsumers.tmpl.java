/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.google.chkstream.function;

public final class ChkConsumers {
  private ChkConsumers() {}

  % for num_e in xrange(MIN_EXCEPTIONS, MAX_EXCEPTIONS  + 1):
  <%
    exc_decl_list = ', '.join(
        ['E%d extends Exception' % i for i in xrange(0, num_e)])
    throws_list = 'throws ' + ', '.join(['E%d' % i for i in xrange(0, num_e)])
    _ThrowN = '' if num_e == MIN_EXCEPTIONS else '_Throw%d' % num_e
  %>

  /**
   * Represents an operation that accepts a single input argument and returns no
   * result. Unlike most other functional interfaces, {@code Consumer} is expected
   * to operate via side-effects.
   *
   * <p>This is a <a href="package-summary.html">functional interface</a>
   * whose functional method is {@link #accept(Object)}.
   *
   * @param <T> the type of the input to the operation
   */
  public static interface ChkConsumer${_ThrowN}
  <T, ${exc_decl_list}>
  {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) ${throws_list};
  }

  % for specialization in SPECIALIZATIONS:
  /**
   * Represents an operation that accepts a single input argument and returns no
   * result. Unlike most other functional interfaces, {@code Consumer} is expected
   * to operate via side-effects.
   *
   * <p>This is a <a href="package-summary.html">functional interface</a>
   * whose functional method is {@link #accept(Object)}.
   *
   * @param <T> the type of the input to the operation
   */
  public static interface Chk${specialization}Consumer${_ThrowN}
  <${exc_decl_list}>
  {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(${specialization.lower()} t) ${throws_list};
  }
  % endfor
  % endfor
}
