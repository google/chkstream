#!/usr/bin/env python2.7
# Copyright 2017 Google.
#
# This file is licensed under the GPLv2+Classpath Exception, which full text
# is found in the LICENSE file at the root of this project.
#
# Google designates this particular file as subject to the "Classpath"
# exception as provided in the LICENSE file that accompanied this code.

"""A script to format source code templates using Mako."""

__author__ = 'Alexander Dorokhine'

import logging
import os
import shutil

import mako_template


MIN_EXCEPTIONS = 1
MAX_EXCEPTIONS = 5

PROJECTS = ('third_party/openjdk',)
ROOT_PKG_DIR = 'com/google/chkstream'
GEN_SRC_DIR = 'src/main/generated'

STREAM_IMPLS = ['java8', 'streamsupport']
SPECIALIZATIONS = ['Int', 'Long', 'Double']


class Error(Exception):
  pass


def _GetOutputDir(project_root, tmpl_dir, input_file, stream_impl=None):
  """Gets the destination dir for an output file given an input file.

  The output is relative to the root project.
  """
  root_pkg_tmpl_dir = os.path.join(tmpl_dir, ROOT_PKG_DIR)
  package_path = os.path.relpath(input_file, root_pkg_tmpl_dir)
  package_dir = os.path.dirname(package_path)
  if stream_impl:
    output_dir  = os.path.join(
      project_root, stream_impl, GEN_SRC_DIR, ROOT_PKG_DIR, stream_impl,
      package_dir)
  else:
    output_dir = os.path.join(
        project_root, 'common', GEN_SRC_DIR, ROOT_PKG_DIR, package_dir)
  output_dir = os.path.normpath(output_dir)
  return output_dir


def CleanAll():
  for project in PROJECTS:
    for dirpath, dirnames, filenames in os.walk(project):
      for dirname in dirnames:
        if dirname == 'generated':
          folder = os.path.join(dirpath, dirname)
          if os.path.exists(folder):
            logging.info('Cleaning folder "%s"', folder)
            shutil.rmtree(folder)


def GenAll():
  for project in PROJECTS:
    GenProject(project, os.path.join(project, 'tmpl/'))


def GenProject(project_root, tmpl_dir):
  for dirpath, dirnames, filenames in os.walk(tmpl_dir):
    for filename in filenames:
      input_file = os.path.join(dirpath, filename)
      template = mako_template.MakoTemplate(input_file)
      if template.GetTemplateAttr('split'):
        GenOneSplitFile(project_root, tmpl_dir, template)
      else:
        GenOneSingleFile(project_root, tmpl_dir, template)


def GenOneSingleFile(project_root, tmpl_dir, template):
  stream_impls = (
      STREAM_IMPLS if template.GetTemplateAttr('for_each_stream_impl')
      else [None])
  for stream_impl in stream_impls:
    output_dir = _GetOutputDir(
        project_root, tmpl_dir, template.path, stream_impl)
    output_filename = os.path.basename(template.path)
    if output_filename.endswith('.tmpl.java'):
       output_filename = output_filename[:-len('.tmpl.java')] + '.java'
    output_path = os.path.join(output_dir, output_filename)
    logging.info('Generating %s', output_path)
    template.Bind(
        MIN_EXCEPTIONS=MIN_EXCEPTIONS,
        MAX_EXCEPTIONS=MAX_EXCEPTIONS,
        STREAM_IMPLS=STREAM_IMPLS,
        SPECIALIZATIONS=SPECIALIZATIONS,
        stream_impl=stream_impl)
    template.Output(output_path)


def GenOneSplitFile(project_root, tmpl_dir, template):
  stream_impls = (
      STREAM_IMPLS if template.GetTemplateAttr('for_each_stream_impl')
      else [None])
  specializations = (
      SPECIALIZATIONS if template.GetTemplateAttr('for_each_specialization')
      else [None])
  for stream_impl in STREAM_IMPLS:
    for specialization in SPECIALIZATIONS:
      for num_exceptions in xrange(MIN_EXCEPTIONS, MAX_EXCEPTIONS + 1):
        template.Bind(
            MIN_EXCEPTIONS=MIN_EXCEPTIONS,
            MAX_EXCEPTIONS=MAX_EXCEPTIONS,
            STREAM_IMPLS=STREAM_IMPLS,
            SPECIALIZATIONS=SPECIALIZATIONS,
            num_e=num_exceptions,
            stream_impl=stream_impl,
            specialization=specialization)
        output_filename = template.module.get_filename(
            num_exceptions, MIN_EXCEPTIONS, specialization)
        output_dir = _GetOutputDir(
            project_root, tmpl_dir, template.path, stream_impl)
        output_path = os.path.join(output_dir, output_filename)
        logging.info('Generating %s', output_path)
        template.Output(output_path)


if __name__ == '__main__':
  logging.basicConfig(level=logging.INFO)
  CleanAll()
  GenAll()
