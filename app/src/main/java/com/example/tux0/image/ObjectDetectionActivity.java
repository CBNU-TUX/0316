package com.example.tux0.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.example.tux0.helpers.BoxWithLabel;
import com.example.tux0.helpers.ImageHelperActivity;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectionActivity<FirebaseCustomRemoteModel> extends ImageHelperActivity {
    private ObjectDetector objectDetector;
    private List<Detection> detectedObjects;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Multiple object detection in static images
//        ObjectDetectorOptions options =
//                new ObjectDetectorOptions.Builder()
//                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                        .enableMultipleObjects()
//                        .enableClassification()
//                        .build();
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.
                builder().setScoreThreshold(0.5f).setMaxResults(3).build();

        //File modelFile = new File("assets/model2.tflite");

        try{ objectDetector = ObjectDetector.createFromFileAndOptions(this, "salad.tflite",options);}
        catch (IOException e){}
//        objectDetector = ObjectDetection.getClient(options);

    }


    @Override
    protected void runClassification(Bitmap bitmap) {
        super.runClassification(bitmap);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        TensorImage inputImage = TensorImage.fromBitmap(bitmap);

//        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
        try{
        detectedObjects = objectDetector.detect(inputImage);
        }
        catch(NullPointerException e){
            Log.d("NullPointer exception","NUll");
        }
        if(!detectedObjects.isEmpty()){
            StringBuilder builder = new StringBuilder();
            List<BoxWithLabel> boxes = new ArrayList<>();

            for(Detection object : detectedObjects){
                if(!object.getCategories().isEmpty()){
                    String label = object.getCategories().get(0).getLabel();
                    builder.append(label).append(": ")
                            .append(object.getCategories().get(0).getScore()).append("\n");
                    boxes.add(new BoxWithLabel(object.getBoundingBox(), label));
                    Log.d("ObjectDetection","Object detected: "+ label);

                }else{
                    builder.append("Unknown").append("\n");
                }
            }
            getOutputTextView().setText(builder.toString());
            drawDetectionResult(boxes, bitmap);
        }
        else{
            getOutputTextView().setText("Could not detect");
        }
    }
}
//        objectDetector.process(inputImage)
//                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
//                    @Override
//                    public void onSuccess(List<DetectedObject> detectedObjects) {
//                        if(!detectedObjects.isEmpty()){
//                            StringBuilder builder = new StringBuilder();
//                            List<BoxWithLabel> boxes = new ArrayList<>();
//
//                            for(DetectedObject object : detectedObjects){
//                                if(!object.getLabels().isEmpty()){
//                                    String label = object.getLabels().get(0).getText();
//                                    builder.append(label).append(": ")
//                                            .append(object.getLabels().get(0).getConfidence()).append("\n");
//                                    boxes.add(new BoxWithLabel(object.getBoundingBox(), label));
//                                    Log.d("ObjectDetection","Object detected: "+ label);
//
//                                }else{
//                                    builder.append("Unknown").append("\n");
//                                }
//                            }
//                            getOutputTextView().setText(builder.toString());
//                            drawDetectionResult(boxes, bitmap);
//                        }
//                         else{
//                            getOutputTextView().setText("Could not detect");
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });