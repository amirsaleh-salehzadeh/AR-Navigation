package com.honours.genar.myapplication2.app;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;


public class CollectionsActivity extends Activity implements CollectionListFragment.OnCollectionSelectedListener{

    public static String TAG = "CollectionsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            CollectionListFragment listFrag = new CollectionListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            listFrag.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, listFrag);
            //transaction.addToBackStack(null);
            transaction.commit();
        }


    }

    @Override
    public void onCollectionSelected(POICollection collection) {

        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article

        CollectionDetailsFragment detailsFrag = (CollectionDetailsFragment) getFragmentManager().findFragmentById(R.id.details_fragment);

        if (detailsFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            // detailsFrag.updateArticleView(position);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            CollectionDetailsFragment newFragment = new CollectionDetailsFragment();
            Bundle args = new Bundle();

            /**
             * TODO: Pass through all the details of a Collection
             */

            args.putString(CollectionDetailsFragment.ARG_Details, collection.getDescription());
            args.putString(CollectionDetailsFragment.ARG_Name, collection.getName());
            newFragment.setArguments(args);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    MainActivity.fromTAG = TAG;
                    super.onBackPressed();
                }
        }
        return true;
    }

}