package com.kholabs.khoand.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kholabs.khoand.R;
import com.lyft.android.scissors.CropView;

import java.io.File;

import static android.graphics.Bitmap.CompressFormat.PNG;

public class CropImageActivity extends AppCompatActivity {
    private CropView cropView;
    private TextView txtUsePhoto;
    private Bitmap image = null;
    private Bitmap saveImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        Intent intent = this.getIntent();
        image = intent.getParcelableExtra("imageBitmap");

        if (image == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        cropView = (CropView)findViewById(R.id.crop_view);
        txtUsePhoto = (TextView)findViewById(R.id.txtUsePhoto);

        cropView.setImageBitmap(image);

        txtUsePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropView.crop();
                saveImage = cropView.getImageBitmap();

                Intent intent = new Intent();
                intent.putExtra("saveImageBitmap", saveImage);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private Uri getImageUri(String path) {

        return Uri.fromFile(new File(path));
    }
}
