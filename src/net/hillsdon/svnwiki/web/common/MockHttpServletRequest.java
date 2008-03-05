package net.hillsdon.svnwiki.web.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial implementation for testing.  Only those methods overridden in
 * this class do anything useful.
 * 
 * @author mth
 */
public final class MockHttpServletRequest extends NullHttpServletRequest {

  private Map<String, List<String>> _parameters = new LinkedHashMap<String, List<String>>();

  public void setParameter(final String name, final String value) {
    setParameter(name, Collections.singletonList(value));
  }
  
  public void setParameter(final String name, final List<String> value) {
    _parameters.put(name, value);
  }
  
  @Override
  public String[] getParameterValues(final String name) {
    List<String> list = _parameters.get(name);
    return list == null ? null : list.toArray(new String[list.size()]);
  }
  
  @Override
  public String getParameter(final String name) {
    List<String> entries = _parameters.get(name);
    if (entries != null && entries.size() > 0) {
      return entries.get(0);
    }
    return null;
  }
  
}
