/*
  Copyright (C) 2007-2008 Helge Hess

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getobjects.appserver.core.WOApplication;
import org.getobjects.appserver.core.WOContext;
import org.getobjects.appserver.publisher.IGoContext;
import org.getobjects.appserver.publisher.IGoObject;
import org.getobjects.appserver.publisher.IGoObjectRendererFactory;
import org.getobjects.eocontrol.EOArrayDataSource;
import org.getobjects.eocontrol.EODataSource;
import org.getobjects.foundation.NSObject;
import org.getobjects.foundation.UString;

/**
 * GoProductManager
 * <p>
 * Products are the extension mechanism for the Go subsystem of Go. Currently
 * we also have "WOFramework"s which are loaded/linked by WOResourceManager. We
 * probably abandon those altogether and only use GoProduct's.
 */
public class GoProductManager extends NSObject
  implements IGoObject, IGoObjectRendererFactory
{
  // TODO: document more
  // TODO: finish implementation, fix the crap
  
  protected static final Log log = LogFactory.getLog("GoProductManager");
  
  protected WOApplication application;
  protected ConcurrentHashMap<String, GoProductInfo> nameToProduct;
  protected ConcurrentHashMap<String, GoProductInfo> shortNameToProduct;
  
  public GoProductManager(final WOApplication _app) {
    this.application        = _app;
    this.nameToProduct      = new ConcurrentHashMap<String, GoProductInfo>(16);
    this.shortNameToProduct = new ConcurrentHashMap<String, GoProductInfo>(16);
  }

  /* product management */
    
  public boolean loadProduct(String _name, final String _qname) {
    if (_qname == null)
      return false;
    if (_name == null) {
      int idx = _qname.lastIndexOf('.');
      _name = idx != 0 ? _qname.substring(idx + 1) : _qname;
    }
    
    boolean isDebugOn = log.isDebugEnabled();
    if (isDebugOn) log.debug("load " + _name + ": " + _qname);
    
    GoProductInfo info;
    if ((info = this.nameToProduct.get(_qname)) == null) {
      info = new GoProductInfo(_qname);
      this.nameToProduct.put(_qname, info);
      this.shortNameToProduct.put(_name, info);
    }
    
    if (info.isLoaded()) { /* already loaded */
      if (isDebugOn) log.debug("  already loaded: " + _name);
      return true;
    }
    
    // TODO: check for threading issues
    if (!info.loadProductIntoApplication(this.application)) {
      if (isDebugOn) log.debug("  failed to load: " + _name);
      return false;
    }
    
    if (isDebugOn) log.debug("  did load: " + _name);
    return true;
  }
  
  
  /* renderers */
  
  public Object rendererForObjectInContext(Object _result, WOContext _ctx) {
    // TODO: we could somehow cache this, probably worth the effort
    for (GoProductInfo info: this.nameToProduct.values()) {
      Object renderer = info.rendererForObjectInContext(_result, _ctx);
      if (renderer != null) return renderer;
    }
    return null;
  }
  
  
  /* resource lookup */

  public Object lookupName(String _name, IGoContext _ctx, boolean _acquire) {
    if (_name == null || _ctx == null)
      return null;
    
    GoProductInfo info = this.nameToProduct.get(_name);
    if (info == null) info = this.shortNameToProduct.get(_name);
    if (info == null)
      return null;
    
    // TODO: should we perform auto-loading?
    return info.product();
  }

  /* folderish */
  
  public boolean isFolderish() {
    return true;
  }
  
  public List<Object> productList() {
    List<Object> children = new ArrayList<Object>(16);
    
    for (GoProductInfo info: this.nameToProduct.values())
      children.add(info.product());
    
    return children;
  }
  
  public EODataSource folderDataSource() {
    return new EOArrayDataSource(this.productList());
  }

  
  /* description */
  
  @Override
  public void appendAttributesToDescription(final StringBuilder _d) {
    super.appendAttributesToDescription(_d);
    
    if (this.nameToProduct == null)
      _d.append(" NO NAMES");
    else {
      _d.append(" names=" + UString.componentsJoinedByString(
          this.shortNameToProduct.keySet(), ","));
    }
  }
  
}
