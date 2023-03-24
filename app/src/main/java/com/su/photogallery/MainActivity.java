package com.su.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
    @Override
    protected Fragment createFragment() {
        Log.i("photogallery.MainActivity", "Start Photo Gallery");
        return PhotoGalleryFragment.newInstance();
    }
}