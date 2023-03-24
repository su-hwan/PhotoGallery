package com.su.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.su.photogallery.service.FlickrFetchr;
import com.su.photogallery.service.PollService;
import com.su.photogallery.service.QueryPreferences;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private int mPage = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //<activity android:name=".MyActivity"
        //      android:configChanges="screenSize|orientation"
        //        setRetainInstance(true);

        //setHasOptionsMenu(true);

        updateItems();
        //Intent intent = PollService.newIntent(getActivity());
        //startActivity(intent);
        //PollService.setServiceAlarm(getActivity(), true);

    }

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
        //RecyclerView의 스크롤이 맨 마지막에 도달했을 때 액션
//        mRecyclerView.addOnScrollListener(new OnScrollListener() {
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int firstVisibleItemPosition =
//                        ((GridLayoutManager) recyclerView.getLayoutManager())
//                        .findFirstVisibleItemPosition();
//                int lastVisibleItemPosition =
//                        ((GridLayoutManager) recyclerView.getLayoutManager())
//                        .findLastCompletelyVisibleItemPosition();
//                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
//
//                if (lastVisibleItemPosition == itemTotalCount && mPage < 100) {
//                    Log.d(TAG,
//                            "last Position..." + mPage + ", lastVisibleItemPosition:" +
//                            lastVisibleItemPosition);
//                    new FetchItemsTask().execute(++mPage);
//                    setupAdapter();
//                }
//            }
//        });


        Toolbar toolbar = view.findViewById(R.id.fragment_toolbar);
        toolbar.inflateMenu(R.menu.menu_photo_gallery);

        optionsMenu(toolbar);

        return view;
    }


    public void optionsMenu(Toolbar toolbar) {
        Menu menu = toolbar.getMenu();
        SearchView searchView = toolbar.findViewById(R.id.menu_item_search);
        SearchView.SearchAutoComplete searchAutoComplete =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            String oldText = "";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "query text : " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                searchView.clearFocus();
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.i(TAG, "new text : "+ newText + ", old text:" + oldText);
                if ("".equals(newText) && !"".equals(oldText)) {
                    QueryPreferences.setStoredQuery(getActivity(), "");
                    updateItems();
                }
                oldText = newText;
                return true;
            }

        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_clear:
                        QueryPreferences.setStoredQuery(getActivity(), null);
                        updateItems();
                        return true;
                    case R.id.menu_item_toggle_polling:

                        boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                        PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                        if (shouldStartAlarm) {
                            toggleItem.setTitle(R.string.stop_polling);
                        } else {
                            toggleItem.setTitle(R.string.start_polling);
                        }

                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask().execute(query);
    }

    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new GalleryAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Object, Void, List<GalleryItem>> {
        private static final String TAG = "FetchItemsTask";

        @Override
        protected List<GalleryItem> doInBackground(Object... objects) {
            String query = null;
            FlickrFetchr flickrFetchr = new FlickrFetchr();
            if (objects.length != 0) {
                query = (String) objects[0];
            }
            //query = "robot";
            if (query != null && !"".equals(query.trim())) {
                return flickrFetchr.searchPhotos(query);
            } else {
                return flickrFetchr.fetchRecentPhotos();
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            //super.onPostExecute(galleryItems);
            mItems = galleryItems;
            setupAdapter();
        }
    }

    private class GalleryHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bind(GalleryItem item) {
            Picasso.get()
                    .load(item.getUrl())
                    .placeholder(com.google.android.material.R.drawable.ic_m3_chip_close)
                    .into(mImageView);
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
        private final List<GalleryItem> mGalleryItems;

        public GalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(requireActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new GalleryHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

    }

}
