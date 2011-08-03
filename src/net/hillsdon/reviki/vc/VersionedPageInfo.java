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
package net.hillsdon.reviki.vc;

import java.util.Date;
import java.util.Map;

/**
 * Contents at a particular revision.
 *
 * @author mth
 */
public interface VersionedPageInfo extends PageInfo  {

  // Magic revisions.  Prefer isFoo() methods.
  long UNCOMMITTED = -2;
  long DELETED = -3;

  long getRevision();
  String getRevisionName();

  long getLastChangedRevision();
  String getLastChangedUser();
  Date getLastChangedDate();

  String getLockedBy();
  String getLockToken();
  boolean isLocked();
  boolean isNewOrLockedByUser(String user);
  Date getLockedSince();

  boolean isNewPage();
  boolean isDeleted();

  VersionedPageInfo withoutLockToken();

  VersionedPageInfo withAlternativeContent(String content);

  VersionedPageInfo withAlternativeAttributes(Map<String, String> attributes);

}
