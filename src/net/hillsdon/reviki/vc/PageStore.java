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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;



/**
 * A verson control based store of wiki pages with attachments.
 * 
 * All methods throw {@link PageStoreException} if they fail,
 * sometimes more informative subclasses individually documented.
 * 
 * If there is an authentication failure {@link PageStoreAuthenticationException}
 * will be thrown.
 * 
 * @author mth
 */
public interface PageStore {
  
  /**
   * @return The name of the wiki for which this PageStore stores pages.
   */
  String getWiki() throws PageStoreException;
  
  /**
   * @return The latest (highest) revision number.
   */
  long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException;
  
  /**
   * @return A all pages.
   */
  Set<PageReference> list() throws PageStoreException;

  /**
   * @param limit Maximum number of entries to return.
   * @return Names of recently changed pages.
   */
  List<ChangeInfo> recentChanges(long limit) throws PageStoreException;

  /**
   * @param ref The path.
   * @return Changes, most recent first.
   */
  List<ChangeInfo> history(PageReference ref) throws PageStoreException;

  /**
   * @param start Start revision (inclusive). 
   * @param end  End revision (inclusive).
   * @return Pages changed after that revision.
   */
  Collection<PageReference> getChangedBetween(long start, long end) throws PageStoreException;

  /**
   * The page info may represent a page that doesn't exist yet (or has been deleted,
   * we don't currently distinguish) check {@link PageInfo#isNewPage()}.
   * 
   * @param ref A page name.
   * @param revision Revision, -1 for head.
   * @return Information (including current content) for the page.
   */
  PageInfo get(PageReference ref, long revision) throws PageStoreException;
  
  /**
   * If the page is an existing page then try to take out a lock.
   * 
   * Check the lock owner on the returning page, you may not get the
   * lock.
   * 
   * For further documentation see {@link #get(PageReference, long)}.
   * 
   * @param ref A page name.
   * @return Information (including current content) for the page.
   */
  PageInfo tryToLock(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param lockToken The token for the lock, see {@link PageInfo#getLockToken()}. 
   */
  void unlock(PageReference ref, String lockToken) throws PageStoreException;
  
  /**
   * Edit pages by calling this method.  They don't need to exist yet.
   * 
   * Setting the page content to the empty string will delete the page.
   * 
   * @param ref A page name.s
   * @param lockToken  The token for the edit lock, if any, see {@link PageInfo#getLockToken()}.
   * @param baseRevision Used to check the edited copy was the latest.
   * @param content The new content.
   * @param commitMessage An optional commit message.
   * @return The new revision number.
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   */
  long set(PageReference ref, String lockToken, long baseRevision, String content, String commitMessage) throws InterveningCommitException, PageStoreException;

  /**
   * Renames an existing page.
   * 
   * @param from The existing page.
   * @param to The new page.
   * @param baseRevision Used to check the renamed copy was the latest.
   * @param commitMessage An optional commit message.
   * @return The new revision number.
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   */
  long rename(PageReference from, PageReference to, long baseRevision, String commitMessage) throws InterveningCommitException, PageStoreException;
  
  /**
   * Copy from 'from' at 'fromRevision' to the unused page head.
   * 
   * @param from From page.
   * @param fromRevision Valid revision of from page.
   * @param to A new page name.
   * @param commitMessage TODO
   * @return The revision in which the copy took place.
   * @throws PageStoreException On error.  Copying to a page that already exists is an error.
   */
  long copy(PageReference from, long fromRevision, PageReference to, String commitMessage) throws PageStoreException;
  
  /**
   * Add an attachment to a page.
   * 
   * @param ref The page name.
   * @param storeName The name to store the attachment as.
   * @param baseRevision The base revision.
   * @param in Data read from here.
   * @param commitMessage An optional commit message.
   */
  void attach(PageReference ref, String storeName, long baseRevision, InputStream in, String commitMessage) throws PageStoreException;

  /**
   * All attachments for the given page, with information on previous versions of the same.
   * 
   * @param ref A page name.
   * @return File names of all attachments.
   */
  Collection<AttachmentHistory> attachments(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param attachment Attachment on that page.
   * @param revision The revision to fetch, -1 for head.
   * @param sink Attachment is written here.
   * @throws NotFoundException If the attachment is not present in the given revision. 
   */
  void attachment(PageReference ref, String attachment, long revision, ContentTypedSink sink) throws PageStoreException, NotFoundException;

  /**
   * @throws PageStoreInvalidException If the underlying data repository is invalid/not present etc.
   * @throws PageStoreAuthenticationException If we couldn't authenticate (probably a good sign but can't be sure). 
   */
  void assertValid() throws PageStoreInvalidException, PageStoreAuthenticationException;
  
}
