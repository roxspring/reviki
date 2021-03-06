/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.webtests;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.xml.sax.InputSource;

import junit.framework.AssertionFailedError;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.hillsdon.reviki.wiki.renderer.XHTML5Validator;

/**
 * Performs XHTML5 validation of the content.
 *
 * @author mth
 */
class ValidateOnContentChange extends WebWindowAdapter {
  private XHTML5Validator _validator = new XHTML5Validator();

  public void webWindowContentChanged(final WebWindowEvent event) {
    WebResponse response = event.getNewPage().getWebResponse();
    String content = response.getContentAsString();
    // We leave documents without a doctype alone for now.  These include
    // tomcat's default error pages.
    if (content.indexOf("<!DOCTYPE") != -1) {
      try {
        // The onchange attribute in full pages causes Rhino to blow up with VerifyErrors,
        // so it needs to be removed before trying to validate the HTML.
        String in = content.replace("onchange=\"this.form.submit()\"", "");
        _validator.validate(new InputSource(new StringReader(in)));
      }
      catch (IOException e) {
        throw new RuntimeException(String.format("Failed to read XHTML5 page content: %s", e.getMessage()));
      }
    }
    if (!event.getWebWindow().getWebClient().getCookieManager().isCookiesEnabled()) {
      final Page page = event.getNewPage();
      if (page instanceof HtmlPage) {
        final HtmlPage htmlPage = (HtmlPage) page;
        @SuppressWarnings("unchecked")
        final List<HtmlAnchor> anchors = (List<HtmlAnchor>) htmlPage.getByXPath("//a[@class!='inter-wiki' and @class!='external']");
        for(final HtmlAnchor a : anchors) {
          if (!a.getHrefAttribute().contains(";jsessionid=")) {
            final String message =
              "Found a link without a jsessionid!\n" +
              "Page Title: \t" + htmlPage.getTitleText() + "\n" +
              "Line: \t" + a.getStartLineNumber() + "\n" +
              "Link: \t" + a.asXml() + ".";
            throw new AssertionFailedError(message);

          }
        }
        @SuppressWarnings("unchecked")
        final List<HtmlForm> forms = (List<HtmlForm>) htmlPage.getByXPath("//form");
        for(final HtmlForm f : forms) {
          if (!f.getActionAttribute().contains(";jsessionid=")) {
            final String message =
              "Found a form without a jsessionid!\n" +
              "Page Title: \t" + htmlPage.getTitleText() + "\n" +
              "Line: \t" + f.getStartLineNumber() + "\n" +
              "Link: \t" + f.asXml() + ".";
            throw new AssertionFailedError(message);

          }
        }
      }
    }
  }
}
