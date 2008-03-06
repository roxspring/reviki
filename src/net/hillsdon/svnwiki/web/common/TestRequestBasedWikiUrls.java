/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.svnwiki.web.common;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.configuration.DeploymentConfiguration;
import net.hillsdon.svnwiki.configuration.PerWikiInitialConfiguration;

/**
 * Test for {@link RequestBasedWikiUrls}.
 * 
 * @author mth
 */
public class TestRequestBasedWikiUrls extends TestCase {

  private DeploymentConfiguration _configuration;
  private MockHttpServletRequest _request;

  @Override
  protected void setUp() throws Exception {
    _configuration = null;
    _request = new MockHttpServletRequest();
    _request.setContextPath("/svnwiki");
    _request.setRequestURL("http://www.example.com/svnwiki/some/page");
    _request.setRequestURI("/svnwiki/some/page");
  }
  

  public void testNullWiki() {
    RequestBasedWikiUrls urls = new RequestBasedWikiUrls(_request, new PerWikiInitialConfiguration(_configuration, null, "foo"));
    assertEquals("http://www.example.com/svnwiki/pages/", urls.root());
    assertEquals("http://www.example.com/svnwiki/pages/Spaced+Out", urls.page("Spaced Out"));
    assertEquals("http://www.example.com/svnwiki/pages/RecentChanges/atom.xml", urls.feed());
    assertEquals("http://www.example.com/svnwiki/pages/FindPage", urls.search());
  }

  public void testGivenNameWiki() {
    RequestBasedWikiUrls urls = new RequestBasedWikiUrls(_request, new PerWikiInitialConfiguration(_configuration, "foo", "foo"));
    assertEquals("http://www.example.com/svnwiki/pages/foo/", urls.root());
    assertEquals("http://www.example.com/svnwiki/pages/foo/Spaced+Out", urls.page("Spaced Out"));
    assertEquals("http://www.example.com/svnwiki/pages/foo/RecentChanges/atom.xml", urls.feed());
    assertEquals("http://www.example.com/svnwiki/pages/foo/FindPage", urls.search());
  }
  
}