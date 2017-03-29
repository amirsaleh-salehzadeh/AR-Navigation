package com.honours.genar.myapplication2.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import java.util.List;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> {
    private static List<POICollection> mDataset;

    private AdapterListener  adapterListener;
    public static RecyclerView mRecyclerView;
    private static Context context;
    public interface AdapterListener{
        void CollectionSelected(POICollection c);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mTextView;
        public Switch mToggleButton;
        public POICollection collection;
        public ImageView imgCol;


        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.txtCollectionName);
            mToggleButton = (Switch)v.findViewById(R.id.swEnabled);
            imgCol = (ImageView)v.findViewById(R.id.imgColl);
            mToggleButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Switch s = (Switch)view;
            boolean b = s.isChecked();
            collection.setActive(b);
            //Toast.makeText(context,"onClick working " + collection.getName() + " value = " + b,Toast.LENGTH_SHORT).show();
        }
    }

    public CollectionsAdapter(List<POICollection> myDataset, AdapterListener listener, RecyclerView rv, Context context) {
        mDataset = myDataset;
        adapterListener = listener;
        mRecyclerView = rv;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CollectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collection_view, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPos = mRecyclerView.getChildPosition(view);
                POICollection col = mDataset.get(itemPos);
                adapterListener.CollectionSelected(col);
            }
        });
        final ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.collection = mDataset.get(position);
        holder.mTextView.setText(holder.collection.getName());
        holder.mToggleButton.setChecked(holder.collection.isActive());
        holder.imgCol.setImageBitmap(holder.collection.getBitmap());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void refresh(List<POICollection> myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }



}
