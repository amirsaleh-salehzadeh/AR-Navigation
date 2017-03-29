package com.honours.genar.myapplication2.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import datamanipulation.ServerDataSource;


import java.util.List;

public class RolesAdapter extends RecyclerView.Adapter<RolesAdapter.ViewHolder> {
    private static List<Role> mDataset;

    private final AdapterListener  adapterListener;
    public static RecyclerView mRecyclerView;
    private static Activity activity;

    public interface AdapterListener{
        void RoleSelected(Role r);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AccessDialog.AccessDialogListener{

        public final TextView mTextView;
        public final RadioButton mRadioButton;
        public final ImageView mImageView;
        public Role role;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.txtCollectionName);
            mImageView = (ImageView)v.findViewById(R.id.imgLock);
            mRadioButton = (RadioButton)v.findViewById(R.id.rbtnSelected);
            mRadioButton.setOnClickListener(this);
        }

        public void setRole(Role r){

           role = r;

            if (role.getPassword() == null){
                mImageView.setVisibility(View.GONE);
            }
            else if (ARData.getAccessedRoles().contains(role.getName())){
                mImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
            }
            else{
                mImageView.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            }
        }

        @Override
        public void onClick(View view) {

            if (role.isActive()) return;

            if (role.getPassword() != null && !(ARData.getAccessedRoles().contains(role.getName()))){
                //TODO: Show dialog to enter key
                DialogFragment accessDialog = new AccessDialog();
                AccessDialog.mListener = this;
                accessDialog.show(activity.getFragmentManager(), "AccessDialog");
            }
            else{
                for (Role r : mDataset){
                    if (r.getName().equals(role.getName())){
                        r.setActive(true);
                        Toast.makeText(activity.getApplicationContext(),"Role set to "+role.getName()+" ",Toast.LENGTH_LONG).show();
                    }
                    else
                        r.setActive(false);
                }

                ARData.setCurrentRole(role);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }



        }

        @Override
        public void onDialogPositiveClick(String pass) {

            if (pass.equals(role.getPassword())){
                for (Role r : mDataset){
                    if (r.getName().equals(role.getName())){
                        r.setActive(true);
                        Toast.makeText(activity.getApplicationContext(),"Role set to "+role.getName()+" ",Toast.LENGTH_LONG).show();
                        mImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
                    }
                    else
                        r.setActive(false);
                }
                ServerDataSource.grantRoleAccess(role);
                ARData.setCurrentRole(role);
                mRecyclerView.getAdapter().notifyDataSetChanged();

            }
            else
            {
                mRadioButton.setChecked(false);
                Toast.makeText(activity.getApplicationContext(), "Access key entered for Role: " + role.getName() + " is incorrect.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onDialogNegativeClick() {
            mRadioButton.setChecked(false);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public RolesAdapter(List<Role> myDataset, AdapterListener listener, RecyclerView rv, Activity activity) {
        mDataset = myDataset;
        adapterListener = listener;
        mRecyclerView = rv;
        RolesAdapter.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RolesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.role_view, parent, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPos = mRecyclerView.getChildPosition(view);
                Role role = mDataset.get(itemPos);
                adapterListener.RoleSelected(role);
            }
        });

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setRole(mDataset.get(position));
        holder.mTextView.setText(holder.role.getName());
        holder.mRadioButton.setChecked(holder.role == ARData.getCurrentRole());

    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void refresh(List<Role> myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

}
