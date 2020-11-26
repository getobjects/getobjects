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

package org.getobjects.appserver.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getobjects.appserver.associations.WODynamicKeyPathAssociation;
import org.getobjects.appserver.associations.WOQualifierAssociation;
import org.getobjects.appserver.associations.WORegExAssociation;
import org.getobjects.appserver.core.WOAssociation;
import org.getobjects.appserver.core.WOContext;
import org.getobjects.appserver.core.WODynamicElement;
import org.getobjects.appserver.core.WOElement;
import org.getobjects.appserver.core.WOElementWalker;
import org.getobjects.appserver.core.WORequest;
import org.getobjects.appserver.core.WOResponse;
import org.getobjects.eocontrol.EOQualifierEvaluation;
import org.getobjects.foundation.NSObject;

/**
 * WOConditional
 * <p>
 * Render a subsection or not depending on some component state.
 * <p>
 * Sample:
 * <pre>
 *   ShowIfRed: WOConditional {
 *     condition = currentColor;
 *     value     = "red";
 *   }</pre>
 * Renders:<br />
 *   This element does not render anything.
 * <p>
 * Bindings:
 * <pre>
 *   condition   [in] - boolean or object if used with value binding
 *   negate      [in] - boolean
 *   value/v     [in] - object
 *   match       [in] - object (Pattern or Matcher or Pattern-String)
 *   q/qualifier [in] - EOQualifier to be used as condition
 *   not         [in] - boolean (sets condition/negate=true)</pre>
 *
 * WOConditional is an aliased element:
 * <pre>&lt;wo:if var:value="obj.isTeam"&gt;...&lt;/wo:if&gt;</pre>
 */
public class WOConditional extends WODynamicElement {
  /*
   * TODO: would be nice to have NPS style operations, like
   *         operation="isNotEmpty" condition="item.lastname"
   *       since in Java we can't add such to objects using categories.
   *       key1/value1 key2/key2 value3/value3 key4/match4 etc => parser?
   */
  protected static Log log = LogFactory.getLog("WOConditional");

  protected EOQualifierEvaluation condition; /* this object has the bindings */
  protected WOElement template;

  public WOConditional
    (final String _name, final Map<String, WOAssociation> _assocs, final WOElement _template)
  {
    super(_name, _assocs, _template);

    final WOComplexCondition c = new WOComplexCondition();
    c.grabAssociations(_assocs);

    this.condition = c.optimize();
    this.template  = _template;

    /* validation */

    if (this.template == null)
      log().warn("conditional has not template");

    processMultiStack(_assocs);
  }

  /**
   * Check for multi-condition wrappings (key1/value1 & key2/value2/op2)
   */
  public void processMultiStack(final Map<String, WOAssociation> _assocs) {
    // TBD: this doesn't work if no 'condition' binding was given ...

    List<Map<String, WOAssociation>> multiStack = null;
    for (int idx = 1; idx < 10; idx++) {
      /* extract keyN/valueN/negateN bindings (or kN,vN,negN) */
      WOAssociation mkey    = grabAssociation(_assocs, "key" + idx);
      WOAssociation mvalue  = grabAssociation(_assocs, "value" + idx);
      WOAssociation mnegate = grabAssociation(_assocs, "negate" + idx);
      if (mkey    == null) mkey    = grabAssociation(_assocs, "k" + idx);
      if (mvalue  == null) mvalue  = grabAssociation(_assocs, "v" + idx);
      if (mnegate == null) mnegate = grabAssociation(_assocs, "neg" + idx);

      if (mkey == null && mvalue == null)
        break; /* stop condition, no more bindings for the N index */

      final Map<String, WOAssociation> assocs = new HashMap<>(4);

      if (mkey != null && mkey.isValueConstant()) {
        _assocs.put("condition", WOAssociation.associationWithKeyPath(key));
        final String key = mkey.stringValueInComponent(null);
      }
      else if (mkey != null)
        _assocs.put("condition", new WODynamicKeyPathAssociation(mkey));

      if (mvalue  != null) _assocs.put("value",  mvalue);
      if (mnegate != null) _assocs.put("negate", mnegate);

      if (multiStack == null)
        multiStack = new ArrayList<>(4);
      multiStack.add(assocs);
    }
    if (multiStack != null) {
      final int len = multiStack.size();
      for (int i = len - 1; i >= 0; i--) {
        this.template =
          new WOConditional("multi" + (i+1), multiStack.get(i), this.template);
      }
      multiStack = null;
    }
  }


  /* evaluate */

  protected boolean doShowInContext(final WOContext _ctx) {
    if (this.condition == null) {
      log.error("association has no condition!");
      return false;
    }
    return this.condition.evaluateWithObject(_ctx);
  }


  /* handle request */

  @Override
  public void takeValuesFromRequest(final WORequest _rq, final WOContext _ctx) {
    if (!doShowInContext(_ctx) || this.template == null)
      return;

    _ctx.appendElementIDComponent("1");
    this.template.takeValuesFromRequest(_rq, _ctx);
    _ctx.deleteLastElementIDComponent();
  }

  @Override
  public Object invokeAction(final WORequest _rq, final WOContext _ctx) {
    /* Note: In SOPE this works a bit different. SOPE encodes the on/off state
     *       in the element-id. This way the condition doesn't need to be
     *       evaluated.
     *       We do support arbitrary senderIDs, not just ID pathes.
     */

    if (!doShowInContext(_ctx) || this.template == null)
      return null;

    _ctx.appendElementIDComponent("1");
    final Object v = this.template.invokeAction(_rq, _ctx);
    _ctx.deleteLastElementIDComponent();

    return v;
  }


  /* generate response */

  @Override
  public void appendToResponse(final WOResponse _r, final WOContext _ctx) {
    if (!doShowInContext(_ctx) || this.template == null) {
      log.debug("not showing a conditional ...");
      return;
    }

    _ctx.appendElementIDComponent("1");
    this.template.appendToResponse(_r, _ctx);
    _ctx.deleteLastElementIDComponent();
  }

  @Override
  public void walkTemplate(final WOElementWalker _walker, final WOContext _ctx){
    if (!doShowInContext(_ctx) || this.template == null) {
      log.debug("not showing a conditional ...");
      return;
    }

    _ctx.appendElementIDComponent("1");
    _walker.processTemplate(this, this.template, _ctx);
    _ctx.deleteLastElementIDComponent();
  }


  /* description */

  @Override
  public void appendAttributesToDescription(final StringBuilder _d) {
    super.appendAttributesToDescription(_d);
  }


  /* condition classes */

  public static class WOConstCondition extends NSObject
    implements EOQualifierEvaluation
  {
    protected boolean doShow;

    public WOConstCondition(final boolean _doShow) {
      this.doShow = _doShow;
    }

    @Override
    public boolean evaluateWithObject(final Object _ctx) {
      return this.doShow;
    }

    @Override
    public void appendAttributesToDescription(final StringBuilder _d) {
      super.appendAttributesToDescription(_d);
      _d.append((this.doShow) ? " show" : " hide");
    }
  }

  public static class WOCheckCondition extends NSObject
    implements EOQualifierEvaluation
  {
    protected WOAssociation condition;
    protected boolean doNegate;

    public WOCheckCondition(final WOAssociation _condition, final boolean _negate) {
      this.condition = _condition;
      this.doNegate  = _negate;
    }

    @Override
    public boolean evaluateWithObject(final Object _ctx) {
      if (this.condition == null) {
        log.error("association has no 'condition' binding!");
        return false;
      }
      final Object cursor = _ctx instanceof WOContext
        ? ((WOContext)_ctx).cursor() : _ctx;
      final boolean doShow = this.condition.booleanValueInComponent(cursor);
      return this.doNegate ? !doShow : doShow;
    }

    @Override
    public void appendAttributesToDescription(final StringBuilder _d) {
      super.appendAttributesToDescription(_d);

      WODynamicElement.appendBindingToDescription(_d,
          "condition", this.condition);
      if (this.doNegate) _d.append(" negate");
    }
  }

  public static class WOComplexCondition extends NSObject
    implements EOQualifierEvaluation
  {
    protected WOAssociation condition;
    protected WOAssociation negate;
    protected WOAssociation value;
    protected WOAssociation match;

    public EOQualifierEvaluation optimize() {
      if (this.value == null && this.match == null && this.condition != null) {
        if (this.negate == null || this.negate.isValueConstant()) {
          final boolean doNegate = this.negate != null
            ? this.negate.booleanValueInComponent(null) : false;

          if (this.condition.isValueConstant()) {
            final boolean doShow = this.condition.booleanValueInComponent(null);
            return new WOConstCondition(doNegate ? !doShow : doShow);
          }
          return new WOCheckCondition(this.condition, doNegate);
        }
      }
      return this;
    }

    public boolean grabAssociations(final Map<String, WOAssociation> _assocs) {
      this.condition = grabAssociation(_assocs, "condition");
      this.negate    = grabAssociation(_assocs, "negate");
      this.value     = grabAssociation(_assocs, "value");
      this.match     = grabAssociation(_assocs, "match");

      if (this.value == null) this.value = grabAssociation(_assocs, "v");


      /* <wo:if not="..."> shortcut */

      if (this.negate == null && this.condition == null) {
        final WOAssociation n = grabAssociation(_assocs, "not");
        if (n != null) {
          this.condition = n;
          this.negate    = WOAssociation.associationWithValue(Boolean.TRUE);
        }
      }


      /* qualifier association */

      WOAssociation q = grabAssociation(_assocs, "qualifier");
      if (q == null) q = grabAssociation(_assocs, "q");
      if (q != null) {
        if (this.condition != null) {
          log.error
            ("WOConditional has a 'condition' and a 'qualifier' binding!");
        }
        else if (!q.isValueConstant()) {
          log.error("WOConditional does not yet support " +
              "dynamic qualifier bindings: "+q);
        }
        else {
          /* reassign condition to qualifier binding */
          this.condition =
            new WOQualifierAssociation(q.stringValueInComponent(null));
        }
      }


      /* use 'value' as 'condition' if the latter is not set */

      if (this.condition == null) {
        if (this.value != null) {
          this.condition = this.value;
          this.value = null;
        }
        else {
          log.error("missing 'condition' binding in element: " +
              this + ", " + _assocs);
        }
      }


      /* match binding */

      if (this.match != null && this.match.isValueConstant()) {
        // TBD: we might want to have a 'value mode'
        if (!(this.match instanceof WORegExAssociation)) {
          this.match =
            new WORegExAssociation(this.match.stringValueInComponent(null));
        }
      }


      /* validation */

      if (this.match != null && this.value != null)
        log.warn("both 'match' and 'value' bindings are set: " + this);

      return true;
    }

    @Override
    public boolean evaluateWithObject(final Object _ctx) {
      if (this.condition == null) {
        log.error("association has no 'condition' binding!");
        return false;
      }

      final Object  cursor = (_ctx instanceof WOContext)
        ? ((WOContext)_ctx).cursor() : _ctx;
      boolean doShow, doNegate = false;

      if (this.negate != null)
        doNegate = this.negate.booleanValueInComponent(cursor);

      if (this.match != null) {
        final Object  pato    = this.match.valueInComponent(cursor);
        Matcher matcher = null;

        if (pato == null) {
          if (log.isInfoEnabled())
            log.info("'match' binding returned no Object: " + this.match);
        }
        else if (pato instanceof Matcher)
          matcher = (Matcher)pato;
        else if (pato instanceof Pattern) {
          final String s = this.condition.stringValueInComponent(cursor);
          matcher = s != null ? ((Pattern)pato).matcher(s) : null;
        }
        else {
          final Pattern pat = Pattern.compile(pato.toString());
          if (pat == null)
            log.warn("could not compile pattern in 'match' binding: " + pato);
          else
            matcher = pat.matcher(this.condition.stringValueInComponent(cursor));
        }

        doShow = matcher != null ? matcher.matches() : false;
      }
      else if (this.value != null) {
        final Object v = this.value.valueInComponent(cursor);
        final Object o = this.condition.valueInComponent(cursor);

        if (v == o)
          doShow = true;
        else if (o == null || v == null)
          doShow = false;
        else {
          if (v instanceof Pattern && !(o instanceof Pattern))
            doShow = ((Pattern)v).matcher(v.toString()).matches();
          else
            doShow = o.equals(v);
        }
      }
      else
        doShow = this.condition.booleanValueInComponent(cursor);

      return doNegate ? !doShow : doShow;
    }

    @Override
    public void appendAttributesToDescription(final StringBuilder _d) {
      super.appendAttributesToDescription(_d);

      WODynamicElement.appendBindingsToDescription(_d,
          "condition", this.condition,
          "negate",    this.negate,
          "value",     this.value,
          "match",     this.match);
    }
  }
}
