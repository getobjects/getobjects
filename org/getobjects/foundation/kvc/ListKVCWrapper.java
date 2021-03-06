package org.getobjects.foundation.kvc;

import java.util.ArrayList;
import java.util.List;

import org.getobjects.foundation.NSKeyValueCoding;

public class ListKVCWrapper extends KVCWrapper {

  public static class ListAccessor implements IPropertyAccessor {

    public ListAccessor() {
    }

    @Override
    public boolean canReadKey(final String key) {
      return true;
    }

    @Override
    public Object get(final Object _instance, final String _key) {
      @SuppressWarnings("unchecked")
      final List<Object> l = (List<Object>)_instance;

      // TODO: avg, etc.
      // @see https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/KeyValueCoding/CollectionOperators.html
      if (_key.equals("@count"))
        return l.size();

      final List<Object> tmp = new ArrayList<>(l.size());
      for (final Object o : l) {
        final Object v = NSKeyValueCoding.Utility.valueForKey(o, _key);
        if (v != null)
          tmp.add(v);
      }
      return tmp;
    }

    @Override
    public boolean canWriteKey(final String key) {
      return true;
    }

    @Override
    public void set
      (final Object _target, final String _key, final Object _value)
    {
      @SuppressWarnings("unchecked")
      final List<Object> l = (List<Object>)_target;
      for (final Object o : l) {
        NSKeyValueCoding.Utility.takeValueForKey(o, _value, _key);
      }
    }

    @Override
    public Class getWriteType() {
      return Object.class;
    }
  }

  private static final IPropertyAccessor commonListAccessor = new ListAccessor();

  public ListKVCWrapper(final Class _class) {
    super(_class);

  }

  @Override
  public IPropertyAccessor getAccessor(final Object _target, final String _name) {
    // this is invoked by valueForKey/takeValueForKey of NSObject and
    // NSKeyValueCoding.DefaultImplementation
    IPropertyAccessor result;

    result = super.getAccessor(_target, _name);

    if (result == null)
      result = commonListAccessor;

    return result;
  }

}
