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

public final class ChkFunctions {
  private ChkFunctions() {}

  % for num_e in xrange(MIN_EXCEPTIONS, MAX_EXCEPTIONS  + 1):
  <%
    exc_decl_list = ', '.join(
        ['E%d extends Exception' % i for i in xrange(0, num_e)])
    throws_list = 'throws ' + ', '.join(['E%d' % i for i in xrange(0, num_e)])
    _ThrowN = '' if num_e == MIN_EXCEPTIONS else '_Throw%d' % num_e
  %>

  /**
   * Represents a function that accepts one argument and produces a result.
   *
   * <p>This is a <a href="package-summary.html">functional interface</a>
   * whose functional method is {@link #apply(Object)}.
   *
   * @param <T> the type of the input to the function
   * @param <R> the type of the result of the function
   */
  public static interface ChkFunction${_ThrowN}
  <T, R, ${exc_decl_list}>
  {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) ${throws_list};
  }

  % for specialization in SPECIALIZATIONS:
  /**
   * Represents a function that accepts an object and returns ${specialization}.
   */
  public static interface ChkTo${specialization}Function${_ThrowN}
  <T, ${exc_decl_list}>
  {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    ${specialization.lower()} applyAs${specialization}(T t) ${throws_list};
  }

  /**
   * Represents a function that accepts ${specialization} and returns
   * ${specialization}.
   */
  public static interface Chk${specialization}UnaryOperator${_ThrowN}
  <${exc_decl_list}>
  {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    ${specialization.lower()} applyAs${specialization}
        (${specialization.lower()} t)
            ${throws_list};
  }

  /**
   * Represents a function that accepts a ${specialization} and returns an
   * object.
   */
  public static interface Chk${specialization}Function${_ThrowN}
  <U, ${exc_decl_list}>
  {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    U apply(${specialization.lower()} t) ${throws_list};
  }

  % for dest_specialization in SPECIALIZATIONS:
  % if dest_specialization != specialization:
  /**
   * Represents a function that accepts a ${specialization} and returns a
   * ${dest_specialization}.
   */
  public static interface
      Chk${specialization}To${dest_specialization}Function${_ThrowN}
          <${exc_decl_list}>
  {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    ${dest_specialization.lower()} applyAs${dest_specialization}(
        ${specialization.lower()} t)
            ${throws_list};
  }
  % endif
  % endfor  // dest_specialization
  % endfor  // specialization
  % endfor  // num_e
}
