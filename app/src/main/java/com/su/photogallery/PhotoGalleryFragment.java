package com.su.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.su.photogallery.service.FlickrFetchr;

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
        new FetchItemsTask().execute(mPage);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
        mRecyclerView.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItemPosition =
                        ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int lastVisibleItemPosition =
                        ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;

                if (lastVisibleItemPosition == itemTotalCount && mPage < 100) {
                    Log.d(TAG, "last Position..."+mPage + ", lastVisibleItemPosition:" + lastVisibleItemPosition);
                    new FetchItemsTask().execute(++mPage);
                    setupAdapter();
                }
            }
        });
        return view;
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
            int page = (Integer) objects[0];
            return new FlickrFetchr().fetchItems(page);
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

        public void bind(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
        private List<GalleryItem> mGalleryItems;

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
            Drawable placeholder =
                    getResources().getDrawable(com.google.android.material.R.drawable.ic_m3_chip_close);
            holder.bind(placeholder);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

}
