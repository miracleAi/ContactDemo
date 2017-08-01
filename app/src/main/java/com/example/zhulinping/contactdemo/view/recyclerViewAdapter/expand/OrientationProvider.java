package com.example.zhulinping.contactdemo.view.recyclerViewAdapter.expand;

import android.support.v7.widget.RecyclerView;

/**
 * Interface for getting the orientation of a RecyclerView from its LayoutManager
 */
public interface OrientationProvider {

  public int getOrientation(RecyclerView recyclerView);

  public boolean isReverseLayout(RecyclerView recyclerView);
}
