package com.zero.study.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.zero.study.databinding.ActivityRxBinding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Admin
 */
public class RxActivity extends AppCompatActivity {
    public static final String DIR_NAME = "ZERO";
    ActivityRxBinding binding;
    protected final String TAG = RxActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        Glide.with(RxActivity.this).load("file:///android_asset/ss.jpg").into(binding.ivPic);
        try {
            InputStream inputStream = getAssets().open("wide.jpg");
            binding.ivLargePic.setInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private File saveBitmap(Bitmap bitmap) throws IOException {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
        Log.i(TAG, "save file to disk:" + file.getAbsolutePath());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return file;
    }


    /**
     * 压缩
     *
     * @param bitmap：原始图
     * @return newBimap ：压缩后图
     */
    public Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }


    private Bitmap addTextWatermarkToBitmap(Bitmap originalBitmap, String waterMarkText) {
        // 创建一个新的 Bitmap 对象，大小与原始 Bitmap 相同
        Bitmap bitmapWithWatermark = originalBitmap.copy(originalBitmap.getConfig(), true);

        // 创建一个画布，将原始 Bitmap 绘制到画布上
        Canvas canvas = new Canvas(bitmapWithWatermark);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        // 设置文字大小
        paint.setTextSize(80);
        paint.setAntiAlias(true);
        // 设置阴影效果
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
        // 计算文字绘制的位置（这里简单地将文字绘制在右下角）
        int xPos = originalBitmap.getWidth() / 2;
        int yPos = originalBitmap.getHeight() - 50;
        // 在画布上绘制文字水印
        canvas.drawText(waterMarkText, xPos, yPos, paint);

        return bitmapWithWatermark;
    }

}