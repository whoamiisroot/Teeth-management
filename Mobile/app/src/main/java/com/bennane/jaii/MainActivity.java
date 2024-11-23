package com.bennane.jaii;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    float angle1;
    float angle2;
    float angle3;
    float angle4;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button camera;


    private ArrayList<Double> leftTaperAngles = new ArrayList<>();
    private ArrayList<Double> rightTaperAngles = new ArrayList<>();
    private int clickCounter = 0;
    private List<View> drawnLines = new ArrayList<>();

    private Point firstClickedCorner;
    private Point secondClickedCorner;
    private Point thirdClickedCorner;
    private Point fourthClickedCorner;
    Mat dilated;
    private List<Point> cornerCoordinates = new ArrayList<>();

    Button calculateNextAngles;

    Button select;
    Button clear;

    ImageView imageView;
    Bitmap bitmap;
    Bitmap bitmap2;
    Mat mat;

    Mat resultmat;
    int SELECT_CODE = 100 , CAMERA_CODE=101;
    private TextView leftAngleTextView;
    private TextView rightAngleTextViewVerticale;
    private TextView leftAngleTextViewVerticale;

    private View lastClickedCorner;

    private TextView rightAngleTextView;
    private long studentId;

    long pwId;
    float as1Value;
    float af1Value;
    float af2Value;
    float as2Value;
    float bf1Value;
    float bs1Value;
    float bs2Value;
    float bf2Value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        studentId = intent.getLongExtra("STUDENT_ID", -1);
        pwId = intent.getIntExtra("PW_ID", -1);
        String toothName = intent.getStringExtra("toothName");
        Log.d("toothname",toothName);
        Log.d("pwid",""+pwId);
        TextView textoothname=findViewById(R.id.toothname);
        textoothname.setText("Your Tp Tooth Name is " + toothName );
        textoothname.setTextColor(Color.RED);
        CheckBox checkboxImageFront = findViewById(R.id.checkboxImageFront);
        CheckBox checkboxImageSide = findViewById(R.id.checkboxImageSide);

        camera =findViewById(R.id.camera);
        Button btnSendPW = findViewById(R.id.btnSendPW);
        btnSendPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkboxImageFront.isChecked()) {
                    af1Value = angle1;
                    af2Value= angle2;
                    bf1Value=angle3;
                    bf2Value=angle4;
                    as1Value = 0;
                    bs2Value=0;
                    bs1Value=0;
                    as2Value=0;
                    // If "Image Front" is checked, send angles to af1
                    sendAngleToUpdateServer(af1Value,as1Value,af2Value,as2Value,bf1Value,bs1Value,bf2Value,bs2Value);
                } else if (checkboxImageSide.isChecked()) {
                    as1Value = angle1;
                    as2Value= angle2;
                    bs1Value=angle3;
                    bs2Value=angle4;
                    af1Value = 0;
                    bf2Value=0;
                    bf1Value=0;
                    af2Value=0;
                    sendAngleToUpdateServer(af1Value,as1Value,af2Value,as2Value,bf1Value,bs1Value,bf2Value,bs2Value);
                }
                else {
                    Log.e("CheckBoxError", "No checkbox is checked");
                }
            }
        });




        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_CODE);
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_edit_profile) {
                // Handle navigation to Edit Profile activity
                // For example, you can use Intent to start a new activity

                Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
                editProfileIntent.putExtra("STUDENT_ID", studentId);
                startActivity(editProfileIntent);

                // Close the drawer after handling the click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }if (id == R.id.nav_logout) {
                // Handle logout button click
                logout();
                return true;
            }

            // Handle other menu items as needed

            return false;
        });




        leftAngleTextView = findViewById(R.id.leftAngleTextView);
        rightAngleTextView = findViewById(R.id.rightAngleTextView);
        calculateNextAngles = findViewById(R.id.calculateNextAngles);
        rightAngleTextViewVerticale=findViewById(R.id.rightAngleTextViewVerticale);
        leftAngleTextViewVerticale=findViewById(R.id.leftAngleTextViewVertivale);

        if (OpenCVLoader.initDebug()) Log.d("oki", "oki");
        else Log.d("la", "la");

        select = findViewById(R.id.select);
        clear=findViewById(R.id.clear);
        imageView = findViewById(R.id.imageView);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_CODE);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
        calculateNextAngles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UndrawSemiInfiniteLineOnMat(dilated, firstClickedCorner, secondClickedCorner);
                UndrawSemiInfiniteLineOnMat(dilated, thirdClickedCorner, fourthClickedCorner);

                clickCounter = 0;
                firstClickedCorner = null;
                secondClickedCorner = null;
                thirdClickedCorner = null;
                fourthClickedCorner = null;

                ViewGroup parentView = (ViewGroup) imageView.getParent();

                for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
                    View child = parentView.getChildAt(i);
                    if (child instanceof View) {
                        if (((View) child).getBackground() instanceof ColorDrawable) {
                            int color = ((ColorDrawable) ((View) child).getBackground()).getColor();
                            if (color == Color.RED) {
                                parentView.removeView(child);
                            }
                        } else if (child.getTag() != null && child.getTag().equals("line")) {
                            parentView.removeView(child);
                        }
                    }
                }

                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float x = event.getX();
                        float y = event.getY();

                        checkIfPointOnCorner2(x, y, cornerCoordinates);

                        return true;
                    }
                });
            }
        });

    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_CODE && data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                imageView.setImageBitmap(bitmap);

                mat = new Mat();
                dilated=new Mat();
                Utils.bitmapToMat(bitmap, mat);

                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

                Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);

                Mat edges = new Mat();
                Imgproc.Canny(mat, edges, 50, 150);

                int dilateSize = 1;
                Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(dilateSize, dilateSize));
                Imgproc.dilate(edges, dilated, dilateKernel);


                double proximityThreshold = 10.0;
                cornerCoordinates = highlightCorners(dilated, proximityThreshold);

                for (int i = 0; i < cornerCoordinates.size(); i++) {
                    Point coord = cornerCoordinates.get(i);
                    Log.d("TAG", "Corner " + (i + 1) + ": (" + coord.x + ", " + coord.y + ")");
                }

                Utils.matToBitmap(dilated, bitmap);
                imageView.setImageBitmap(bitmap);

                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float x = event.getX();
                        float y = event.getY();

                        checkIfPointOnCorner(x, y, cornerCoordinates);

                        return true;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode==CAMERA_CODE&&data!=null){
            bitmap=(Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            mat = new Mat();
            dilated=new Mat();
            Utils.bitmapToMat(bitmap, mat);

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

            Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);

            Mat edges = new Mat();
            Imgproc.Canny(mat, edges, 50, 150);

            int dilateSize = 1;
            Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(dilateSize, dilateSize));
            Imgproc.dilate(edges, dilated, dilateKernel);


            double proximityThreshold = 10.0;
            cornerCoordinates = highlightCorners(dilated, proximityThreshold);

            for (int i = 0; i < cornerCoordinates.size(); i++) {
                Point coord = cornerCoordinates.get(i);
                Log.d("TAG", "Corner " + (i + 1) + ": (" + coord.x + ", " + coord.y + ")");
            }

            Utils.matToBitmap(dilated, bitmap);
            imageView.setImageBitmap(bitmap);

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();

                    checkIfPointOnCorner(x, y, cornerCoordinates);

                    return true;
                }
            });
        }

    }

    private double calculateDistance(Point point1, Point point2) {
        return Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
    }


    private void drawLinesBasedOnProximity(Mat canvas, double proximityThreshold, List<Point> cornerCoordinates) {
        Scalar lineColor = new Scalar(0, 0, 255);
        for (int i = 0; i < cornerCoordinates.size() - 1; ++i) {
            Point start = cornerCoordinates.get(i);
            Point end = cornerCoordinates.get(i + 1);

            double distance = calculateDistance(start, end);

            if (distance < proximityThreshold) {
                Imgproc.line(canvas, start, end, lineColor, 2);
            }
        }
    }

    private List<Point> highlightCorners(Mat canvas, double proximityThreshold) {
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(canvas, corners, 100, 0.01, 10);

        Scalar color = new Scalar(255, 0, 0);

        MatOfPoint2f corners2f = new MatOfPoint2f();
        corners.convertTo(corners2f, CvType.CV_32F);

        List<Point> cornerCoordinates = new ArrayList<>();
        List<Point> cornerPoints = new ArrayList<>();
        cornerPoints.addAll(corners2f.toList());

        if (cornerPoints.isEmpty()) {
            Log.e("TAG", "No corners detected.");
            return cornerCoordinates;
        }

        for (Point point : cornerPoints) {
            Imgproc.circle(canvas, point, 2, color, -1);

            cornerCoordinates.add(new Point(point.x, point.y));
        }

        drawLinesBasedOnProximity(canvas, proximityThreshold, cornerCoordinates);

        return cornerCoordinates;
    }

    private void checkIfPointOnCorner(float x, float y, List<Point> cornerCoordinates) {
        Matrix invertMatrix = new Matrix();
        imageView.getImageMatrix().invert(invertMatrix);

        float[] pts = {x, y};
        invertMatrix.mapPoints(pts);

        for (Point corner : cornerCoordinates) {
            double clickThreshold = 5.0;

            if (calculateDistance(new Point(pts[0], pts[1]), corner) < clickThreshold) {
                Log.d("TAG", "Clicked on corner at (" + corner.x + ", " + corner.y + ")");

                switch (clickCounter) {
                    case 0:
                        firstClickedCorner = corner;
                        break;
                    case 2:
                        secondClickedCorner = corner;
                        Log.d("TAG","hi"+secondClickedCorner);
                        drawSemiInfiniteLineOnMat(dilated, firstClickedCorner, secondClickedCorner);
                        angle1 = (float) calculateAngle(firstClickedCorner, secondClickedCorner);
                        leftAngleTextView.setText("Lingual 15°/à l'horizontal : " + angle1 + " degrees");
                        Utils.matToBitmap(dilated, bitmap);
                        imageView.setImageBitmap(bitmap);
                        break;
                    case 4:
                        thirdClickedCorner = corner;
                        break;
                    case 6:
                        fourthClickedCorner = corner;
                        drawSemiInfiniteLineOnMat(dilated, thirdClickedCorner, fourthClickedCorner);
                        angle2 = (float) calculateAngle(thirdClickedCorner, fourthClickedCorner);
                        rightAngleTextView.setText("Vestibulaire à 45 °/à l'horizontal :  " + angle2 + " degrees");

                        Utils.matToBitmap(dilated, bitmap);
                        imageView.setImageBitmap(bitmap);
                        break;
                }

                View redCircle = new View(getBaseContext());
                redCircle.setBackgroundColor(Color.RED);
                float[] imageViewPoint = new float[]{(float) corner.x, (float) corner.y};
                imageView.getImageMatrix().mapPoints(imageViewPoint);
                float adjustedX = imageViewPoint[0] * imageView.getScaleX() + imageView.getX();
                float adjustedY = imageViewPoint[1] * imageView.getScaleY() + imageView.getY();

                int circleSize = 20;
                redCircle.setLayoutParams(new ViewGroup.LayoutParams(circleSize, circleSize));
                redCircle.setX(adjustedX - circleSize / 2);
                redCircle.setY(adjustedY - circleSize / 2);

                ViewGroup parentView = (ViewGroup) imageView.getParent();
                parentView.addView(redCircle);
                clickCounter++;

                if (clickCounter == 8) {
                    imageView.setOnTouchListener(null);
                }
                break;
            }
        }
    }

    private void drawSemiInfiniteLineOnMat(Mat canvas, Point start, Point end) {
        Scalar lineColor = new Scalar(100, 100, 0, 255);

        double extendFactor = 1000.0;
        Point extendedEnd = new Point(end.x + extendFactor * (end.x - start.x), end.y + extendFactor * (end.y - start.y));

        Imgproc.line(canvas, start, extendedEnd, lineColor, 2, Imgproc.LINE_8, 0);

        ImageView lineView = new ImageView(this);
        lineView.setTag("line");

        Log.d("TAG", "inmethod");
    }


    private void UndrawSemiInfiniteLineOnMat(Mat canvas, Point start, Point end) {
        double extendFactor = 1000.0;
        Point extendedEnd = new Point(end.x + extendFactor * (end.x - start.x), end.y + extendFactor * (end.y - start.y));

        Scalar transparentColor = new Scalar(0, 0, 0, 0);
        Imgproc.line(canvas, start, extendedEnd, transparentColor, 2, Imgproc.LINE_8, 0);

        Scalar color = new Scalar(255, 0, 0);
        for (Point corner : cornerCoordinates) {
            double proximityThreshold = 10.0;
            Imgproc.circle(canvas, corner, 2, color, -1);
        }
        double proximityThreshold = 10.0;

        drawLinesBasedOnProximity(canvas, proximityThreshold, cornerCoordinates);

        Utils.matToBitmap(dilated, bitmap);
        imageView.setImageBitmap(bitmap);
    }









    private double calculateAngle(Point startPoint, Point endPoint) {
        double deltaY = endPoint.y - startPoint.y;
        double deltaX = endPoint.x - startPoint.x;

        double angleRad = Math.atan2(deltaY, deltaX);

        double angleDeg = Math.toDegrees(angleRad);
        if (angleDeg<0){
            angleDeg=angleDeg*-1;
        }


        return Math.round(angleDeg * 100.0) / 100.0;
    }
    private void checkIfPointOnCorner2(float x, float y, List<Point> cornerCoordinates) {
        Matrix invertMatrix = new Matrix();
        imageView.getImageMatrix().invert(invertMatrix);

        float[] pts = {x, y};
        invertMatrix.mapPoints(pts);

        for (Point corner : cornerCoordinates) {
            double clickThreshold = 5.0;

            if (calculateDistance(new Point(pts[0], pts[1]), corner) < clickThreshold) {
                Log.d("TAG", "Clicked on corner at (" + corner.x + ", " + corner.y + ")");

                switch (clickCounter) {
                    case 0:
                        firstClickedCorner = corner;
                        break;
                    case 2:
                        secondClickedCorner = corner;
                        Log.d("TAG","hi"+secondClickedCorner);
                        drawSemiInfiniteLineOnMat(dilated, firstClickedCorner, secondClickedCorner);
                        angle3 = (float) calculateAngle2(firstClickedCorner, secondClickedCorner);
                        leftAngleTextViewVerticale.setText("Lingual 45°/à la verticale : " + angle3 + " degrees");
                        Utils.matToBitmap(dilated, bitmap);
                        imageView.setImageBitmap(bitmap);
                        break;
                    case 4:
                        thirdClickedCorner = corner;
                        break;
                    case 6:
                        fourthClickedCorner = corner;
                        drawSemiInfiniteLineOnMat(dilated, thirdClickedCorner, fourthClickedCorner);
                        angle4 = (float) calculateAngle2(thirdClickedCorner, fourthClickedCorner);
                        rightAngleTextViewVerticale.setText("Vestibulaire 15°/à la verticale : " + angle4 + " degrees");

                        Utils.matToBitmap(dilated, bitmap);
                        imageView.setImageBitmap(bitmap);
                        break;
                }

                View redCircle = new View(getBaseContext());
                redCircle.setBackgroundColor(Color.RED);
                float[] imageViewPoint = new float[]{(float) corner.x, (float) corner.y};
                imageView.getImageMatrix().mapPoints(imageViewPoint);
                float adjustedX = imageViewPoint[0] * imageView.getScaleX() + imageView.getX();
                float adjustedY = imageViewPoint[1] * imageView.getScaleY() + imageView.getY();

                int circleSize = 20;
                redCircle.setLayoutParams(new ViewGroup.LayoutParams(circleSize, circleSize));
                redCircle.setX(adjustedX - circleSize / 2);
                redCircle.setY(adjustedY - circleSize / 2);

                ViewGroup parentView = (ViewGroup) imageView.getParent();
                parentView.addView(redCircle);
                clickCounter++;

                if (clickCounter == 8) {
                    imageView.setOnTouchListener(null);
                }
                break;
            }
        }
    }
    private double calculateAngle2(Point startPoint, Point endPoint) {
        double deltaY = endPoint.y - startPoint.y;
        double deltaX = endPoint.x - startPoint.x;

        double angleRad = Math.atan2(deltaY, deltaX);

        double angleDeg = Math.toDegrees(angleRad);



        double adjustedAngle = (angleDeg + 90) % 360;

        if(adjustedAngle<0){
            adjustedAngle=adjustedAngle*-1;
        }


        return Math.round(adjustedAngle * 100.0) / 100.0;
    }
    private void logout() {

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);

        finish();
    }


    private void sendAngleToUpdateServer(float af1Value,float as1Value,float af2Value, float as2Value,float bf1Value,float bs1Value,float bf2Value,float bs2Value)
    {
        // Construct the URL with studentId and pwId
        String updateUrl = "http://10.0.2.2:8080/api/studentpws/update/" + studentId + "/" + pwId;

        // Create a JSONObject for the request body
        JSONObject jsonBody = new JSONObject();
        try {
            // Add the "af1" field to the request body
            jsonBody.put("af1", af1Value);
            jsonBody.put("as1",as1Value);
            jsonBody.put("af2", af2Value);
            jsonBody.put("as2",as2Value);
            jsonBody.put("bf1", bf1Value);
            jsonBody.put("bs1",bs1Value);
            jsonBody.put("bf2", af1Value);
            jsonBody.put("bs2",as1Value);
            String localTime = getCurrentLocalTime();
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the exception or log it appropriately
        }

        // Create a JsonObjectRequest to update the record
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                updateUrl,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the server response if needed
                        Log.d("UpdateResponse", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error response if needed
                        Log.e("UpdateError", error.toString());
                    }
                }
        );

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request);
    }
    private String getCurrentLocalTime() {
        // Get the current local time in a specific format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String localTime = sdf.format(new Date());

        return localTime;
    }

}