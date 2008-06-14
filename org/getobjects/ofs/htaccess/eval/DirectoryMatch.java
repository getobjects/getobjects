/*
  Copyright (C) 2008 Helge Hess <helge.hess@opengroupware.org>

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
package org.getobjects.ofs.htaccess.eval;


/**
 * DirectoryMatch
 * <p>
 * Example:<pre>
 *   &lt;DirectoryMatch ".*\.gif"&gt;
 *     Satisfy All
 *   &lt;/DirectoryMatch&gt;</pre>
 * 
 * <p>
 * This section directive only executes its contained directives if the
 * <code>path</code> key of the lookup context matches the regular
 * expression given as the first parameter.
 * <p>
 * If the regular expression is prefixed with an exclamation mark
 * (<code>!</code>) the directive will negate the result of the match.
 */
public class DirectoryMatch extends KeyMatchEvaluation {
  
  @Override
  protected String matchKeyInConfig() {
    return "dirpath";
  }
  
}
