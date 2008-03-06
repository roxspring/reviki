package net.hillsdon.reviki.vc;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;

import junit.framework.TestCase;

public class TestChangeNotificationDispatcher extends TestCase {

  private static final ChangeInfo REVISION_FOUR_CHANGE = new ChangeInfo("PageThreeChange", "PageThreeChange", "", new Date(), 4, "", StoreKind.PAGE, ChangeType.MODIFIED);
  private static final ChangeInfo REVISION_FIVE_CHANGE = new ChangeInfo("PageFourChange", "PageFourChange", "", new Date(), 5, "", StoreKind.PAGE, ChangeType.MODIFIED);

  private BasicSVNOperations _operations;
  private ChangeSubscriber _syncedUptoThreeSubscriber;
  private ChangeSubscriber _syncedUptoFourSubscriber;
  private ChangeNotificationDispatcher _dispatcher;
  private ChangeSubscriber _syncedUptoFiveSubscriber;

  @Override
  protected void setUp() throws Exception {
    _operations = createMock(BasicSVNOperations.class);
    _syncedUptoThreeSubscriber = createMock(ChangeSubscriber.class);
    _syncedUptoFourSubscriber = createMock(ChangeSubscriber.class);
    _syncedUptoFiveSubscriber = createMock(ChangeSubscriber.class);
  }
  
  public void testDispatchesChangesSkippingThoseNotApplicableToSubscribersBasedOnSyncedRevision() throws Exception {
    expect(_syncedUptoThreeSubscriber.getHighestSyncedRevision()).andReturn(3L).anyTimes();
    expect(_syncedUptoFourSubscriber.getHighestSyncedRevision()).andReturn(4L).anyTimes();
    expect(_syncedUptoFiveSubscriber.getHighestSyncedRevision()).andReturn(5L).anyTimes();
    expect(_operations.getLatestRevision()).andReturn(5L).once();
    expect(_operations.log("", -1, false, true, 4, 5)).andReturn(asList(REVISION_FIVE_CHANGE, REVISION_FOUR_CHANGE));
    _syncedUptoThreeSubscriber.handleChanges(5, asList(REVISION_FOUR_CHANGE, REVISION_FIVE_CHANGE));
    expectLastCall();
    _syncedUptoFourSubscriber.handleChanges(5, asList(REVISION_FIVE_CHANGE));
    expectLastCall();
    // Note we don't expect a call for _syncedUptoFiveSubscriber.
    
    replay(_operations, _syncedUptoThreeSubscriber, _syncedUptoFourSubscriber, _syncedUptoFiveSubscriber);
    _dispatcher = new ChangeNotificationDispatcher(_operations, _syncedUptoThreeSubscriber, _syncedUptoFourSubscriber);
    _dispatcher.sync();
    verify(_operations, _syncedUptoThreeSubscriber, _syncedUptoFourSubscriber, _syncedUptoFiveSubscriber);
    
    assertEquals(5L, _dispatcher.getLastSynced());
  }
  
}