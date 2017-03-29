package com.honours.genar.myapplication2.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;


public class RoleDetailsFragment extends Fragment {

    public static final String ARG_Details = "details";
    public static final String ARG_Name = "name";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_collection_detail, container, false);

        TextView txtDesc = (TextView)v.findViewById(R.id.txtCollectionDesc);

        Bundle bundle = this.getArguments();
        String description = bundle.getString(ARG_Details);
        String Name = bundle.getString(ARG_Name);

        getActivity().getActionBar().setTitle(Name);

        txtDesc.setText(description);

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }



}
