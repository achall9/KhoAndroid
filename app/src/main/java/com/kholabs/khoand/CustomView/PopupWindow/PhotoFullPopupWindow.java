package com.kholabs.khoand.CustomView.PopupWindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.kholabs.khoand.GlideApp;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ConstantUtil;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Aladar-PC2 on 3/4/2018.
 */

public class PhotoFullPopupWindow extends PopupWindow {

    View view;
    Context mContext;
    PhotoView photoView;
    ProgressBar loading;
    ViewGroup parent;
    private static PhotoFullPopupWindow instance = null;



    public PhotoFullPopupWindow(Context ctx, int layout, View v, Bitmap bitmap) {
        super(((LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate( R.layout.popup_photo_full, null), ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(5.0f);
        }
        this.mContext = ctx;
        this.view = getContentView();
        ImageButton closeButton = (ImageButton) this.view.findViewById(R.id.ib_close);
        setOutsideTouchable(true);

        setFocusable(true);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                dismiss();
            }
        });
        //---------Begin customising this popup--------------------

        photoView = (PhotoView) view.findViewById(R.id.image);
        loading = (ProgressBar) view.findViewById(R.id.loading);
        photoView.setMaximumScale(6);
        parent = (ViewGroup) photoView.getParent();
        // ImageUtils.setZoomable(imageView);
        //----------------------------
        if (bitmap != null) {
            loading.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= 16) {
                parent.setBackground(new BitmapDrawable(mContext.getResources(), ConstantUtil.fastblur(Bitmap.createScaledBitmap(bitmap, 20, 20, true))));// ));
            } else {
                onPalette(Palette.from(bitmap).generate());

            }
            photoView.setImageBitmap(bitmap);
        }
        else
            photoView.setImageResource(R.drawable.blankprofile);
        //------------------------------
        showAtLocation(v, Gravity.CENTER, 0, 0);

    }

    public void onPalette(Palette palette) {
        if (null != palette) {
            ViewGroup parent = (ViewGroup) photoView.getParent().getParent();
            parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY));
        }
    }

}