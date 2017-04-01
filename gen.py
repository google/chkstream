#!/usr/bin/env python2.7
# Copyright 2017 Google.
#
# This file is licensed under the GPLv2+Classpath Exception, which full text
# is found in the LICENSE file at the root of this project.
#
# Google designates this particular file as subject to the "Classpath"
# exception as provided in the LICENSE file that accompanied this code.

import logging
from mako import lookup as mako_lookup
from mako import template as mako_template
import os
import shutil

MIN_EXCEPTIONS = 1
MAX_EXCEPTIONS = 5

PROJECTS = ('third_party/openjdk',)
FLAVOURS = ('java8', 'streamsupport')
ROOT_PKG_DIR = 'com/google/chkstream'
GEN_SRC_DIR = 'src/main/generated'


class Error(Exception):
  pass


def _GetOutputPath(project_root, tmpl_dir, input_file, flavour=None):
  """Gets the destination path for an output file given an input file.

  The output is relative to the root project.
  """
  root_pkg_tmpl_dir = os.path.join(tmpl_dir, ROOT_PKG_DIR)
  package_path = os.path.relpath(input_file, root_pkg_tmpl_dir)
  if flavour:
    output_file = os.path.join(
      project_root, flavour, GEN_SRC_DIR, ROOT_PKG_DIR, flavour, package_path)
  else:
    output_file = os.path.join(
        project_root, 'common', GEN_SRC_DIR, ROOT_PKG_DIR, package_path)
  output_file = os.path.normpath(output_file)
  return output_file


def CleanAll():
  for project in PROJECTS:
    folder = os.path.join(project, GEN_SRC_DIR)
    if os.path.exists(folder):
      logging.info('Cleaning folder "%s"', folder)
      shutil.rmtree(folder)


def GenAll():
  for project in PROJECTS:
    GenFlavourTemplates(project, os.path.join(project, 'tmpl/flavour'))
    GenNoFlavourTemplates(project, os.path.join(project, 'tmpl/noflavour'))


def GenFlavourTemplates(project_root, tmpl_dir):
  for flavour in FLAVOURS:
    GenSingleFiles(project_root, os.path.join(tmpl_dir, 'single'), flavour)
  for flavour in FLAVOURS:
    GenSplitFiles(project_root, os.path.join(tmpl_dir, 'split'), flavour)


def GenNoFlavourTemplates(project_root, tmpl_dir):
  GenSingleFiles(project_root, os.path.join(tmpl_dir, 'single'))
  GenSplitFiles(project_root, os.path.join(tmpl_dir, 'split'))


def GenSingleFiles(project_root, tmpl_dir, flavour=None):
  for dirpath, dirnames, filenames in os.walk(tmpl_dir):
    for filename in filenames:
      input_file = os.path.join(dirpath, filename)
      GenOneSingleFile(project_root, tmpl_dir, input_file, flavour)


def GenOneSingleFile(project_root, tmpl_dir, input_file, flavour=None):
  output_file = _GetOutputPath(project_root, tmpl_dir, input_file, flavour)
  logging.info('Generating %s', output_file)
  with open(input_file, 'r') as fh:
    template = fh.read()
  GenSource(
      template, output_file,
      MIN_EXCEPTIONS=MIN_EXCEPTIONS,
      MAX_EXCEPTIONS=MAX_EXCEPTIONS,
      flavour=flavour)


def GenSplitFiles(project_root, tmpl_dir, flavour=None):
  for dirpath, dirnames, filenames in os.walk(tmpl_dir):
    for filename in filenames:
      input_file = os.path.join(dirpath, filename)
      GenOneSplitFile(project_root, tmpl_dir, input_file, flavour)


def GenOneSplitFile(project_root, tmpl_dir, input_file, flavour=None):
  filename_noext = os.path.basename(input_file)
  (filename_noext, _) = os.path.splitext(filename_noext)
  for num_exceptions in xrange(MIN_EXCEPTIONS, MAX_EXCEPTIONS + 1):
    if num_exceptions == MIN_EXCEPTIONS and filename_noext == 'ChkStream':
      class_name = filename_noext
    else:
      class_name = '%s_Throw%d' % (filename_noext, num_exceptions)
    output_file = _GetOutputPath(project_root, tmpl_dir, input_file, flavour)
    output_file = os.path.join(
        os.path.dirname(output_file), class_name + '.java')
    logging.info('Generating %s', output_file)
    with open(input_file, 'r') as fh:
      template = fh.read()
    GenSource(
        template,
        output_file,
        class_name=class_name,
        MIN_EXCEPTIONS=MIN_EXCEPTIONS,
        MAX_EXCEPTIONS=MAX_EXCEPTIONS,
        num_e=num_exceptions,
        flavour=flavour)


def GenSource(template_str, output_file, **kwargs):
  lookup = mako_lookup.TemplateLookup()
  template = mako_template.Template(
      template_str, lookup=lookup, format_exceptions=True)
  #print template.code
  formatted = template.render(**kwargs)
  output_dir = os.path.dirname(output_file)
  if not os.path.exists(output_dir):
    os.makedirs(output_dir)
  with open(output_file, 'w') as fh:
    fh.write(formatted)


if __name__ == '__main__':
  logging.basicConfig(level=logging.INFO)
  CleanAll()
  GenAll()
