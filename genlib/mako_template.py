# Copyright 2017 Google.
#
# This file is licensed under the GPLv2+Classpath Exception, which full text
# is found in the LICENSE file at the root of this project.
#
# Google designates this particular file as subject to the "Classpath"
# exception as provided in the LICENSE file that accompanied this code.

import logging
import os
from mako import lookup as mako_lookup
from mako import template as mako_template


class MakoTemplate(object):
  """An API to interact with a mako template file on disk."""

  def __init__(self, path):
    self.path = path
    logging.info('Parsing template %s', path)
    with open(path, 'rb') as fh:
      self._string = fh.read()
    # Load the template into mako
    lookup = mako_lookup.TemplateLookup()
    self._mako_template = mako_template.Template(
        self._string, lookup=lookup, format_exceptions=True)
    # print template.code
    self.module = self._mako_template.module

  def GetTemplateAttr(self, name, default=None):
    return getattr(self.module, name, default)

  def Bind(self, **kwargs):
    self._bound_params = kwargs

  def Output(self, output_path):
    formatted = self._mako_template.render(**self._bound_params)
    output_dir = os.path.dirname(output_path)
    if not os.path.exists(output_dir):
      os.makedirs(output_dir)
    with open(output_path, 'w') as fh:
      fh.write(formatted)
