/*
 * Copyright 2017 Google.
 *
 * This file is licensed under the GPLv2+Classpath Exception, which full text
 * is found in the LICENSE file at the root of this project.
 *
 * Google designates this particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this code.
 */

<%!
  split = True
  for_each_stream_impl = True

  def get_filename(num_exceptions, min_exceptions, specialization=None):
    return 'FunctionUtils_Throw%d.java' % num_exceptions
%>

<%
  exc_extend_list = ', '.join(['?' for i in xrange(0, num_e)])
  throws_list = 'throws ' + ', '.join(['E%d' % i for i in xrange(0, num_e)])
  _ThrowN = '' if num_e == MIN_EXCEPTIONS else '_Throw%d' % num_e
%>

package com.google.chkstream.${stream_impl}.function;

% if stream_impl == 'java8':
import java.util.function.*;
% else:
import java8.util.function.*;
% endif

import com.google.chkstream.ChkStreamWrappedException;
import com.google.chkstream.function.ChkConsumers.*;
import com.google.chkstream.function.ChkFunctions.*;
import com.google.chkstream.function.ChkPredicates.*;
import com.google.chkstream.function.ChkRunnables.*;

public class FunctionUtils_Throw${num_e} {

    // Consumer
  
    public <T> Consumer<T> wrapChkConsumer(
        final ChkConsumer${_ThrowN}<T, ${exc_extend_list}> consumer) {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                try {
                    consumer.accept(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    % for specialization in SPECIALIZATIONS:
    public ${specialization}Consumer wrapChk${specialization}Consumer(
        final Chk${specialization}Consumer${_ThrowN}<${exc_extend_list}>
            consumer) {
        return new ${specialization}Consumer() {
            @Override
            public void accept(${specialization.lower()} t) {
                try {
                    consumer.accept(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
    % endfor
  
    // Function
  
    public <T, R> Function<T, R> wrapChkFunction(
        final ChkFunction${_ThrowN}<T, R, ${exc_extend_list}> function) {
        return new Function<T, R>() {
            @Override
            public R apply(T t) {
                try {
                    return function.apply(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    % for specialization in SPECIALIZATIONS:
    public <T> To${specialization}Function<T>
        wrapChkTo${specialization}Function(
            final ChkTo${specialization}Function${_ThrowN}
                <T, ${exc_extend_list}>
                    function) {
        return new To${specialization}Function<T>() {
            @Override
            public ${specialization.lower()} applyAs${specialization}(T t) {
                try {
                    return function.applyAs${specialization}(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    public <U> ${specialization}Function<U>
        wrapChk${specialization}Function(
            final Chk${specialization}Function${_ThrowN}
                <U, ${exc_extend_list}>
                    function) {
        return new ${specialization}Function<U>() {
            @Override
            public U apply(${specialization.lower()} t) {
                try {
                    return function.apply(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    public ${specialization}UnaryOperator
        wrapChk${specialization}UnaryOperator(
            final Chk${specialization}UnaryOperator${_ThrowN}
                <${exc_extend_list}>
                    function) {
        return new ${specialization}UnaryOperator() {
            @Override
            public ${specialization.lower()} applyAs${specialization}(
                ${specialization.lower()} t) {
                try {
                    return function.applyAs${specialization}(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    % for dest_specialization in SPECIALIZATIONS:
    % if dest_specialization != specialization:
    public ${specialization}To${dest_specialization}Function
        wrapChk${specialization}To${dest_specialization}Function(
            final Chk${specialization}To${dest_specialization}Function${_ThrowN}
                <${exc_extend_list}>
                    function) {
        return new ${specialization}To${dest_specialization}Function() {
            @Override
            public ${dest_specialization.lower()} applyAs${dest_specialization}(
                ${specialization.lower()} t) {
                try {
                    return function.applyAs${dest_specialization}(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
    % endif
    % endfor
    % endfor
  
    // Predciates
  
    public <T> Predicate<T> wrapChkPredicate(
        final ChkPredicate${_ThrowN}<T, ${exc_extend_list}> predicate) {
        return new Predicate<T>() {
            @Override
            public boolean test(T t) {
                try {
                    return predicate.test(t);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
  
    % for specialization in SPECIALIZATIONS:
    public ${specialization}Predicate wrapChk${specialization}Predicate(
        final Chk${specialization}Predicate${_ThrowN}<${exc_extend_list}>
            predicate) {
        return new ${specialization}Predicate() {
            @Override
            public boolean test(${specialization.lower()} value) {
                try {
                    return predicate.test(value);
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
    % endfor
  
    // Runnables
  
    public Runnable wrapChkRunnable(
        final ChkRunnable${_ThrowN}<${exc_extend_list}> runnable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    throw new ChkStreamWrappedException(e);
                }
            }
        };
    }
}
