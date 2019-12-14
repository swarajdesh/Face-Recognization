package com.example.faceapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_IMAGE_CAPTURE = 124;
    private FirebaseVisionTextRecognizer textRecognizer;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;

    Button btnCamera;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        FirebaseApp.initializeApp(this);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    // Captures image using intent
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras .get("data");
            imageView.setImageBitmap(imageBitmap);
            detectFace(imageBitmap);

            // converts image into bitimage for processing

        }
        super.onActivityResult(requestCode, resultCode, data);


    }

    private void detectFace(Bitmap imageBitmap) {

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();
        try{
            image = FirebaseVisionImage.fromBitmap(imageBitmap);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch(Exception e){
            e.printStackTrace();
        }


        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText = "";
                int i=1;
                for(FirebaseVisionFace face: firebaseVisionFaces){
                    resultText=resultText.concat("\n"+i+".")
                            .concat("\nSmile: "+face.getSmilingProbability()*100+"%")
                            .concat("\nLeftEye: "+face.getLeftEyeOpenProbability()*100+"%")
                             .concat("\nRightEye: "+face.getRightEyeOpenProbability()*100+"%");
                    //calculates percentage of countors
                    i++;
                }
                if(firebaseVisionFaces.size()==0){
                    Toast.makeText(MainActivity.this, "No Faces", Toast.LENGTH_SHORT).show();
                    //condition to see if there is no face
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString("face",resultText);
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(),"face");
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

}



