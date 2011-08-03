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
package net.hillsdon.reviki.wiki;

import java.io.IOException;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;

public class RenderedPageFactory {

  private final MarkupRenderer _renderer;

  public RenderedPageFactory(final MarkupRenderer renderer) {
    _renderer = renderer;
  }

  public RenderedPage create(PageInfo page, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return new RenderedPage(page.getName(), _renderer.render(page, urlOutputFilter));
  }

}
