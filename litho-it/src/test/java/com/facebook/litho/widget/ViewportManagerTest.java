/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.widget;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ViewportManager}
 */
@RunWith(ComponentsTestRunner.class)
public class ViewportManagerTest {

  private LayoutInfo mLayoutInfo;
  private ViewportInfo.ViewportChanged mViewportChangedListener;
  private Handler mMainThreadHandler;

  @Before
  public void setup() {
    mLayoutInfo = mock(LayoutInfo.class);
    mViewportChangedListener = mock(ViewportInfo.ViewportChanged.class);
    mMainThreadHandler = mock(Handler.class);
  }

  @Test
  public void testOnViewportChangedWhileScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_DRAGGING, 0, 0);

    setVisibleItemPositionInMockedLayoutManager(0, 5);
    setFullyVisibleItemPositionInMockedLayoutManager(1, 4);
    setTotalItemInMockedLayoutManager(20);

    viewportManager.setDataChangedIsVisible(false);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(0, 5, 1, 4, false);
  }

  @Test
  public void testOnViewportChangedWhileScrollingWithNoItemsFullyVisible() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_DRAGGING, 0, 0);

    // The second and third items are visible partially but neither is fully visible
    setVisibleItemPositionInMockedLayoutManager(1, 2);
    setFullyVisibleItemPositionInMockedLayoutManager(-1, -1);
    setTotalItemInMockedLayoutManager(20);

    viewportManager.setDataChangedIsVisible(true);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(1, 2, -1, -1, true);
  }

  @Test
  public void testOnViewportChangedWithoutScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 5, 20);

    setVisibleItemPositionInMockedLayoutManager(5, 20);
    setFullyVisibleItemPositionInMockedLayoutManager(7, 18);
    setTotalItemInMockedLayoutManager(20);

    viewportManager.setDataChangedIsVisible(true);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(5, 20, 7, 18, true);
  }

  @Test
  public void testNoViewportChangedWithScrolling() {
    setFullyVisibleItemPositionInMockedLayoutManager(7, 9);
    setTotalItemInMockedLayoutManager(13);

    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_DRAGGING, 5, 10);

    setVisibleItemPositionInMockedLayoutManager(5, 10);
    setFullyVisibleItemPositionInMockedLayoutManager(7, 9);
    setTotalItemInMockedLayoutManager(13);

    viewportManager.onViewportChanged();

    verifyZeroInteractions(mViewportChangedListener);
  }

  @Test
  public void testNoViewportChangedWithoutScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 5, 10);

    setVisibleItemPositionInMockedLayoutManager(5, 10);
    setFullyVisibleItemPositionInMockedLayoutManager(7, 9);
    setTotalItemInMockedLayoutManager(12);

    viewportManager.setDataChangedIsVisible(false);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(5, 10, 7, 9, false);
  }

  @Test
  public void testOnViewportChangedFromRemovalWithScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_DRAGGING, -1, 0);
    viewportManager.onViewportChangedAfterViewRemoval(1);

    verifyZeroInteractions(mMainThreadHandler);
  }

  @Test
  public void testOnViewportChangedFromRemovalWithoutScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 0, 1);
    viewportManager.onViewportChangedAfterViewRemoval(0);

    verify(mMainThreadHandler).post(any(Runnable.class));
  }

  @Test
  public void testOnViewportChangedFromAddWithScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_DRAGGING, -1, 0);
    viewportManager.onViewportchangedAfterViewAdded(1);

    verifyZeroInteractions(mMainThreadHandler);
  }

  @Test
  public void testOnViewportChangedFromAddWithoutScrolling() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 0, 1);
    viewportManager.onViewportchangedAfterViewAdded(0);

    verify(mMainThreadHandler).post(any(Runnable.class));
  }

  @Test
  public void testTotalItemChangedWhileVisiblePositionsRemainTheSame() {
    setTotalItemInMockedLayoutManager(13);
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 5, 10);

    setVisibleItemPositionInMockedLayoutManager(5, 10);
    setFullyVisibleItemPositionInMockedLayoutManager(7, 9);
    setTotalItemInMockedLayoutManager(12);

    viewportManager.setDataChangedIsVisible(true);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(5, 10, 7, 9, true);
  }

  @Test
  public void testTotalItemChangedWhileNoItemsFullyVisible() {
    setTotalItemInMockedLayoutManager(13);
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 5, 6);

    // The seventh and eighth items are visible partially but neither is fully visible
    setVisibleItemPositionInMockedLayoutManager(6, 7);
    setFullyVisibleItemPositionInMockedLayoutManager(-1, -1);
    setTotalItemInMockedLayoutManager(12);

    viewportManager.setDataChangedIsVisible(false);
    viewportManager.onViewportChanged();

    verify(mViewportChangedListener).viewportChanged(6, 7, -1, -1, false);
  }

  @Test
  public void testInsertInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isInsertInVisibleRange(7, 2, 7);
    assertThat(isInRange).isTrue();
  }

  @Test
  public void testInsertNotInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isInsertInVisibleRange(7, 2, 6);
    assertThat(isInRange).isFalse();
  }

  @Test
  public void testUpdateInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isUpdateInVisibleRange(5, 2);
    assertThat(isInRange).isTrue();
  }

  @Test
  public void testUpdateNotInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isUpdateInVisibleRange(7, 2);
    assertThat(isInRange).isFalse();
  }

  @Test
  public void testMoveInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isMoveInVisibleRange(8, 5, 6);
    assertThat(isInRange).isTrue();

    viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    isInRange = viewportManager.isMoveInVisibleRange(5, 8, 6);
    assertThat(isInRange).isTrue();
  }

  @Test
  public void testMoveNotInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isMoveInVisibleRange(7, 9, 6);
    assertThat(isInRange).isFalse();
  }

  @Test
  public void testRemoveInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, 6);
    boolean isInRange = viewportManager.isRemoveInVisibleRange(6, 2);
    assertThat(isInRange).isTrue();

    getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 3, 6);
    isInRange = viewportManager.isRemoveInVisibleRange(2, 2);
    assertThat(isInRange).isTrue();
  }

  @Test
  public void testRemoveNotInVisibleRange() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 3, 6);
    boolean isInRange = viewportManager.isRemoveInVisibleRange(2, 1);
    assertThat(isInRange).isFalse();
  }

  @Test
  public void testChangeSetIsVisibleForInitialisation() {
    ViewportManager viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, -1, 2);

    assertThat(viewportManager.isInsertInVisibleRange(6, 2, -1)).isTrue();
    assertThat(viewportManager.isUpdateInVisibleRange(6, 2)).isTrue();
    assertThat(viewportManager.isMoveInVisibleRange(6, 2, 10)).isTrue();
    assertThat(viewportManager.isRemoveInVisibleRange(6, 2)).isTrue();

    viewportManager = getViewportManager(RecyclerView.SCROLL_STATE_IDLE, 1, -1);

    assertThat(viewportManager.isInsertInVisibleRange(6, 2, 10)).isTrue();
    assertThat(viewportManager.isUpdateInVisibleRange(6, 2)).isTrue();
    assertThat(viewportManager.isMoveInVisibleRange(6, 2, -1)).isTrue();
    assertThat(viewportManager.isRemoveInVisibleRange(6, 2)).isTrue();
  }

  private void setVisibleItemPositionInMockedLayoutManager(
      int firstVisibleItemPosition,
      int lastVisibleItemPosition) {
    when(mLayoutInfo.findFirstVisibleItemPosition()).thenReturn(firstVisibleItemPosition);
    when(mLayoutInfo.findLastVisibleItemPosition()).thenReturn(lastVisibleItemPosition);
  }

  private void setFullyVisibleItemPositionInMockedLayoutManager(
      int firstFullyVisiblePosition,
      int lastFullyVisiblePosition) {
    when(mLayoutInfo.findFirstFullyVisibleItemPosition())
        .thenReturn(firstFullyVisiblePosition);
    when(mLayoutInfo.findLastFullyVisibleItemPosition())
        .thenReturn(lastFullyVisiblePosition);
  }

  private void setTotalItemInMockedLayoutManager(int totalItem) {
    when(mLayoutInfo.getItemCount()).thenReturn(totalItem);
  }

  private ViewportManager getViewportManager(
      int scrollState,
      int firstFullyVisiblePosition,
      int lastFullyVisiblePosition) {
    ViewportManager viewportManager = new ViewportManager(
        firstFullyVisiblePosition,
        lastFullyVisiblePosition,
        mLayoutInfo,
        mMainThreadHandler,
        scrollState);
    viewportManager.addViewportChangedListener(mViewportChangedListener);

    return viewportManager;
  }
}
