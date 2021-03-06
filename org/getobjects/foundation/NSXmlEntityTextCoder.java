/*
  Copyright (C) 2008 Helge Hess

  This file is part of Go.

  Go is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the
  Free Software Foundation; either version 2, or (at your option) any
  later version.

  Go is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with Go; see the file COPYING.  If not, write to the
  Free Software Foundation, 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
*/
package org.getobjects.foundation;

import java.util.HashMap;
import java.util.Map;

public class NSXmlEntityTextCoder extends NSObject implements NSTextCoder {
  // TBD: not sure whether all this makes sense from a perf perspective ...
  //      (let me know ...)

  public static final NSXmlEntityTextCoder sharedCoder =
    new NSXmlEntityTextCoder();

  protected Map<String, String> entityStringMap =
    new HashMap<String, String>(5);

  public NSXmlEntityTextCoder() {
    this.entityStringMap.put("amp",  "&");
    this.entityStringMap.put("apos", "'");
    this.entityStringMap.put("quot", "\"");
    this.entityStringMap.put("lt",   "<");
    this.entityStringMap.put("gt",   ">");
  }

  public Exception decodeNumericEntity(StringBuilder _out, final String _s) {
    if (_out == null || _s == null) return null;
    if (!_s.startsWith("#") || _s.length() < 3)
      return new NSException("not a numeric entity");
    try {
      if (_s.charAt(1) == 'x') {
        _out.append(Character.toChars(Integer.parseInt(_s.substring(2), 16)));
      }
      else {
        _out.append(Character.toChars(Integer.parseInt(_s.substring(1), 10)));
      }
      return null;
    }
    catch (NumberFormatException e) {
      return e;
    }
  }

  public Exception decodeEntity(StringBuilder _out, final String _s) {
    if (_out == null || _s == null) return null;
    if (_s.startsWith("#"))
      return decodeNumericEntity(_out, _s);

    final String entity = this.entityStringMap.get(_s);
    if (entity != null) {
      _out.append(entity);
    }
    else {
      _out.append('&');
      _out.append(_s);
      _out.append(';');
    }
    return null;
  }

  /**
   * Escapes XML core entities with the matching characters. The decoder
   * is lenient and will work on all sorts of strings, even improperly
   * formatted content.
   *
   * NOTE: subclasses only need to add/remove entity mappings to the
   * entityStringMap instance variable.
   */

  public Exception decodeString(StringBuilder _out, final String _s) {
    if (_out == null || _s == null) return null;

    final int len = _s.length();
    if (len == 0)
      return null;

    int start = -1;
    for (int i = 0; i < len; i++) {
      char c = _s.charAt(i);
      if (c == '&') {
        if (start != -1) {
          _out.append(_s.substring(start - 1, i));
        }
        start = i + 1;
      }
      else  if (c == ';') {
        if (start != -1) {
          Exception e = decodeEntity(_out, _s.substring(start, i));
          if (e != null)
            return e;
          start = -1;
        }
        else {
          _out.append(c);
        }
      }
      else if (start == -1) {
        _out.append(c);
      }
    }
    if (start != -1) {
      _out.append(_s.substring(start - 1, len));
    }
    return null;
  }


  /**
   * Escapes XML special characters with the matching XML core entities.
   */
  public Exception encodeString(StringBuilder _out, final String _s) {
    if (_out == null || _s == null) return null;

    final char[] chars = _s.toCharArray();
    final int    len   = chars.length;
    if (len == 0)
      return null;

    // TBD: is the pre-scanning actually more efficient?
    int escapeCount = 0;
    for (int i = 0; i < len; i++) {
      switch (chars[i]) {
        case '&':  escapeCount += 5; break;
        case '<':  escapeCount += 4; break;
        case '>':  escapeCount += 4; break;
        case '"':  escapeCount += 7; break;
        case '\'': escapeCount += 6; break;
        default:
          break;
      }
    }
    if (escapeCount == 0) {
      _out.append(_s);
      return null;
    }

    // TBD: is this buffer actually more efficient?
    final char[] echars = new char[len + escapeCount];
    int j = 0;
    for (int i = 0; i < len; i++) {
      switch (chars[i]) {
        case '&':
          echars[j] = '&'; j++; echars[j] = 'a'; j++; echars[j] = 'm'; j++;
          echars[j] = 'p'; j++; echars[j] = ';'; j++;
          break;
        case '<':
          echars[j] = '&'; j++; echars[j] = 'l'; j++; echars[j] = 't'; j++;
          echars[j] = ';'; j++;
          break;
        case '>':
          echars[j] = '&'; j++; echars[j] = 'g'; j++; echars[j] = 't'; j++;
          echars[j] = ';'; j++;
          break;
        case '"':
          echars[j] = '&'; j++; echars[j] = 'q'; j++; echars[j] = 'u'; j++;
          echars[j] = 'o'; j++; echars[j] = 't'; j++; echars[j] = ';'; j++;
          break;
        case '\'':
          echars[j] = '&'; j++; echars[j] = 'a'; j++; echars[j] = 'p'; j++;
          echars[j] = 'o'; j++; echars[j] = 's'; j++; echars[j] = ';'; j++;
          break;

        default:
          echars[j] = chars[i];
          j++;
          break;
      }
    }

    _out.append(echars, 0, j);
    return null;
  }

  public Exception encodeChar(StringBuilder _out, final char _in) {
    if (_out == null) return null;

    switch(_in) {
    case '&': _out.append("&amp;");  break;
    case '<': _out.append("&lt;");   break;
    case '>': _out.append("&gt;");   break;
    case '"': _out.append("&quot;"); break;
    case 39:  _out.append("&apos;"); break;
    }

    return null;
  }

  public Exception encodeInt(StringBuilder _out, final int _in) {
    if (_out != null) _out.append(_in); /* nothing needs to be escaped */
    return null;
  }


  /* static method */

  public static String stringByEscapingXMLString(final String _s) {
    if (_s == null)
      return null;

    final char[] chars = _s.toCharArray();
    final int    len   = chars.length;
    if (len == 0)
      return "";

    int escapeCount = 0;
    for (int i = 0; i < len; i++) {
      switch (chars[i]) {
        case '&':  escapeCount += 5; break;
        case '<':  escapeCount += 4; break;
        case '>':  escapeCount += 4; break;
        case '"':  escapeCount += 7; break;
        case '\'': escapeCount += 6; break;
        default:
          break;
      }
    }
    if (escapeCount == 0)
      return _s;

    final char[] echars = new char[len + escapeCount];
    int j = 0;
    for (int i = 0; i < len; i++) {
      switch (chars[i]) {
        case '&':
          echars[j] = '&'; j++; echars[j] = 'a'; j++; echars[j] = 'm'; j++;
          echars[j] = 'p'; j++; echars[j] = ';'; j++;
          break;
        case '<':
          echars[j] = '&'; j++; echars[j] = 'l'; j++; echars[j] = 't'; j++;
          echars[j] = ';'; j++;
          break;
        case '>':
          echars[j] = '&'; j++; echars[j] = 'g'; j++; echars[j] = 't'; j++;
          echars[j] = ';'; j++;
          break;
        case '"':
          echars[j] = '&'; j++; echars[j] = 'q'; j++; echars[j] = 'u'; j++;
          echars[j] = 'o'; j++; echars[j] = 't'; j++; echars[j] = ';'; j++;
          break;
        case '\'':
          echars[j] = '&'; j++; echars[j] = 'a'; j++; echars[j] = 'p'; j++;
          echars[j] = 'o'; j++; echars[j] = 's'; j++; echars[j] = ';'; j++;
          break;

        default:
          echars[j] = chars[i];
          j++;
          break;
      }
    }

    return new String(echars, 0, j);
  }
}
