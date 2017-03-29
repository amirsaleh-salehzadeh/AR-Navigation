package com.honours.genar.myapplication2.app;

import android.app.Activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import datamanipulation.ServerDataSource;

/**
 * This class handles the displaying of all Collections for the user.
 * Users can select a Collection which then notifies the roleListener in order to swap to the Details fragment for Collections
 */
public class CollectionListFragment extends Fragment implements CollectionsAdapter.AdapterListener, ServerDataSource.OnCollectionsRefreshListener{

    // Listener to allow communication of fragments
    private OnCollectionSelectedListener listener;

    private static SwipeRefreshLayout collectionSwipeRefreshLayout;

    // Interface for CollectionsActivity to make use of in order to swap fragments
    public interface OnCollectionSelectedListener {
        void onCollectionSelected(POICollection collection);
    }

    CollectionsAdapter collectionAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        collectionSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);

        collectionSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ServerDataSource.COLLECTION_REFRESH = true;
                ServerDataSource.fetchPOICollections();
            }
        });

        ServerDataSource.collectionListener = this;

        //Get Reference to Items
        RecyclerView mRecyclerView = (RecyclerView)view.findViewById(R.id.list_recycler_view);

        //Layout Manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        collectionAdapter = new CollectionsAdapter(ARData.getPOICollections(),this,mRecyclerView,getActivity().getApplicationContext());
        mRecyclerView.setAdapter(collectionAdapter);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().getActionBar().setTitle("Sites");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (OnCollectionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCollectionSelectedListener");
        }
    }


    @Override
    public void CollectionSelected(POICollection c) {
        listener.onCollectionSelected(c);
    }

    @Override
    public void onCollectionsRefreshed() {

        if (collectionSwipeRefreshLayout == null)
            return;

        collectionSwipeRefreshLayout.setRefreshing(false);

        collectionAdapter.refresh(ARData.getPOICollections());
    }

}
