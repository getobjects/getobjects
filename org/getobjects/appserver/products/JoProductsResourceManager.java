/*
  Copyright (C) 2007 Helge Hess

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
package org.getobjects.appserver.products;

import org.getobjects.appserver.core.WOResourceManager;

/**
 * JoProductsResourceManager
 * <p>
 * This is the main resource manager for a Go application. It wraps the
 * JoProductManager which manages the products.
 */
public class JoProductsResourceManager extends WOResourceManager {
  // TBD: implement me
  
  protected JoProductManager productManager;

  public JoProductsResourceManager(JoProductManager _pm, boolean _caching) {
    super(_caching);
    this.productManager = _pm;
  }

}
