package com.example.tux0.helpers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.tux0.Camara_cv;
import com.example.tux0.ObjectSearch;
import com.example.tux0.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageHelperActivity extends AppCompatActivity {

    private int REQUEST_PICK_IMAGE = 1000;
    private int REQUEST_CAPTURE_IMAGE = 1001;

    private ImageView inputImageView;
    private TextView outputTextView;
    private File photoFile;
    private ArrayList<String> DetectedName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        inputImageView = findViewById(R.id.Input);
        outputTextView = findViewById(R.id.Output);

        //갤러리 접근
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //앱의 갤러리 접근 권한이 DENIED인 경우 접근허락메시지 출력
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
                //PERMISSION_GRANTED == 0, PERMISSION_DENIED == -1
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(ImageHelperActivity.class.getSimpleName(), "grant result for" + permissions[0] + "is" + grantResults[0]);
    }

    public void onPickImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); //jpg,jpeg,등등

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    public void onStartCamera(View view){
        //create a file to share with Camera
        photoFile = createPhotoFile();

        Uri fileUri = FileProvider.getUriForFile(this,"com.example.fileprovider",photoFile);

        //create an intent
        Intent intent =new Intent(ImageHelperActivity.this, Camara_cv.class);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        //startActivitiyForResult
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    private File createPhotoFile(){
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"ML_IMAGE_HELPER");

        if (!photoFileDir.exists()){
            photoFileDir.mkdirs();

        }
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(photoFileDir.getPath() + File.separator +name);
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_PICK_IMAGE){
                Uri uri = data.getData();
                Bitmap bitmap = loadFromUri(uri);
                inputImageView.setImageBitmap(bitmap);
                runClassification(bitmap);
            }else if(requestCode == REQUEST_CAPTURE_IMAGE){
                Log.d("ML","received callback from camera");
                Bitmap bitmap = BitmapFactory.decodeFile((photoFile.getAbsolutePath()));
                runClassification(bitmap);
            }
        }
    }
    private Bitmap loadFromUri(Uri uri){
        Bitmap bitmap = null;


        try{
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                //sdk버전 27이상
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
            else{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
    protected void runClassification(Bitmap bitmap){

    }
    protected TextView getOutputTextView(){
        return outputTextView;
    }
    protected ImageView getInputImageView(){
        return inputImageView;
    }

    protected void drawDetectionResult(List<BoxWithLabel> boxes,Bitmap bitmap){
        DetectedName = new ArrayList<>();
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(outputBitmap);

        Paint penRect = new Paint();
        penRect.setColor(Color.RED);
        penRect.setStyle(Paint.Style.STROKE);
        penRect.setStrokeWidth(8f);

        Paint penLabel = new Paint();
        penLabel.setColor(Color.YELLOW);
        penLabel.setStyle(Paint.Style.FILL_AND_STROKE);
        penLabel.setTextSize(96f);

        for (BoxWithLabel boxWithLabel : boxes){
            canvas.drawRect(boxWithLabel.rect, penRect);

            //Rect
            Rect labelSize = new Rect(0,0,0,0);
            penLabel.getTextBounds(boxWithLabel.label,0,boxWithLabel.label.length(),labelSize);

            float fontSize = penLabel.getTextSize() * boxWithLabel.rect.width() / labelSize.width();
            if(fontSize < penLabel.getTextSize()){
                penLabel.setTextSize(fontSize);
            }
            canvas.drawText(boxWithLabel.label, 0, labelSize.height(),penLabel);
            DetectedName.add(boxWithLabel.label);
        }

        getInputImageView().setImageBitmap(outputBitmap);

        if(DetectedName.size()==1){
            Toast.makeText(this, DetectedName.get(0), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ObjectSearch.class);
            //intent.putExtra("first", DetectedName.get(0));
            intent.putExtra("second", "empty");
            intent.putExtra("third", "empty");
            intent.putStringArrayListExtra("Detected",DetectedName);
            startActivity(intent);
        }
        else if(DetectedName.size()==2){
            Toast.makeText(this, DetectedName.get(1), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ObjectSearch.class);
            intent.putExtra("first", DetectedName.get(0));
            intent.putExtra("second", DetectedName.get(1));
            intent.putExtra("third", "empty");
            startActivity(intent);
        }
        else if(DetectedName.size()==3){
            Toast.makeText(this, DetectedName.get(2), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ObjectSearch.class);
            intent.putExtra("first", DetectedName.get(0));
            intent.putExtra("second", DetectedName.get(1));
            intent.putExtra("third", DetectedName.get(2));
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "객체수를 조정해주세요. (1~3)", Toast.LENGTH_SHORT).show();
        }

       }
}