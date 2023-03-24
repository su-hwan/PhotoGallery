package com.su.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.su.photogallery.service.FlickrFetchr;
import com.su.photogallery.service.ThumbnailDownloader;

import java.util.ArrayList;
import java.util.List;

public class LooperPhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private int mPage = 1;
    private ThumbnailDownloader<GalleryHolder> mThumbnailDownloader;

    public static LooperPhotoGalleryFragment newInstance() {
        return new LooperPhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //<activity android:name=".MyActivity"
        //      android:configChanges="screenSize|orientation"
        //        setRetainInstance(true);
        new FetchItemsTask().execute(mPage);

        mThumbnailDownloader = new ThumbnailDownloader<>(new Handler());
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                    Log.d(TAG,
                            "last Position..." + mPage + ", lastVisibleItemPosition:" + lastVisibleItemPosition);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
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
            query = "robot";
            if (query != null && !"".equals(query.trim())) {
                Log.i(TAG, "search===============");
                return flickrFetchr.searchPhotos(query);
            } else {
                Log.i(TAG, "Recent===============");
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

        public void bind(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder>
            implements ThumbnailDownloader.ThumbnailDownloaderListener<GalleryHolder> {
        private List<GalleryItem> mGalleryItems;

        public GalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public GalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(requireActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            mThumbnailDownloader.setThumbnailDownloaderListener(this::onThumbnailDownloaded);
            return new GalleryHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryHolder holder, int position) {
            Drawable placeholder =
                    getResources().getDrawable(com.google.android.material.R.drawable.ic_m3_chip_close);
            holder.bind(placeholder);
            GalleryItem item = mGalleryItems.get(position);
            mThumbnailDownloader.queueThumbnail(holder, item.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        @Override
        public void onThumbnailDownloaded(GalleryHolder holder, Bitmap thumbnail) {
            Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
            holder.bind(drawable);
        }
    }

}
