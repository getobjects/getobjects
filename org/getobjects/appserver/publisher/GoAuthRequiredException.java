/*
  Copyright (C) 2006-2008 Helge Hess

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
package org.getobjects.appserver.publisher;

import org.getobjects.appserver.core.WOMessage;

/**
 * GoAuthRequiredException
 * <p>
 * If an object is protected Go will raise this exception.
 */
public class GoAuthRequiredException extends GoSecurityException {
  private static final long serialVersionUID = 1L;

  public GoAuthRequiredException(IGoAuthenticator _auth, String _reason) {
    super(_auth, _reason);
  }
  
  /* HTTP support */
  
  public int httpStatus() {
    return WOMessage.HTTP_STATUS_UNAUTHORIZED; /* 401 */
  }
}
