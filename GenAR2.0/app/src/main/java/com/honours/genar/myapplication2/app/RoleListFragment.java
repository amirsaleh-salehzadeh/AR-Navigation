package com.honours.genar.myapplication2.app;

import android.app.Activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;

/**
 * This class handles the displaying of all Roles for the user.
 * Users can select a Role which then notifies the listener in order to swap to the Details fragment for Roles
 */
public class RoleListFragment extends Fragment implements RolesAdapter.AdapterListener{

    // Listener to allow communication of fragments
    private OnRoleSelectedListener listener;

    // Interface for RolesActivity to make use of in order to swap fragments
    public interface OnRoleSelectedListener {
        void onRoleSelected(Role role);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        //Get Reference to Items
        RecyclerView mRecyclerView = (RecyclerView)view.findViewById(R.id.list_recycler_view);

        //Layout Manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        RecyclerView.Adapter mAdapter = new RolesAdapter(ARData.getRoles(),this,mRecyclerView,getActivity());
        mRecyclerView.setAdapter(mAdapter);


        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().getActionBar().setTitle("Roles");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (OnRoleSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnRoleSelectedListener");
        }
    }

    @Override
    public void RoleSelected(Role c) {
        listener.onRoleSelected(c);
    }


}
