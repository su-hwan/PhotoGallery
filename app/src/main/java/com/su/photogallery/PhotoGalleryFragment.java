package com.su.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.su.photogallery.service.FlickrFetchr;

import java.io.IOException;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i(TAG, "=========================== : ");
        new FetchItemsTask().execute();
    }

//    @SuppressLint("MissingInflatedId")
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
//        mRecyclerView = view.findViewById(R.id.fragment_photo_gallery_recycler_view);
//        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
//
//        return view;
//    }

    private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "FetchItemsTask";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String result = new FlickrFetchr().getUrlString("https://stackoverflow.com/");
                Log.i(TAG, "Fetched contents of URL : " + result);
            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch URL : " + e);
            }
            return null;
        }
    }
}
