package com.scenekey.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.scenekey.R;
import com.scenekey.helper.Constant;
import com.scenekey.helper.CustomProgressBar;
import com.scenekey.helper.Permission;
import com.scenekey.helper.SessionManager;
import com.scenekey.helper.Validation;
import com.scenekey.model.UserInfo;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleyMultipartRequest;
import com.scenekey.volleymultipart.VolleySingleton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.scenekey.helper.Constant.MY_PERMISSIONS_REQUEST_CAMERA;
import static com.scenekey.helper.Constant.MY_PERMISSIONS_REQUEST_LOCATION;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener ,LocationListener{

    private Context context=this;
    private static  final String TAG="RegistrationActivity";

    private EditText etRegiFullName,etRegiEmail,etRegiPwd;
    private Button btnRegiSignUp;
    private ImageView imgRegiMale,imgRegiFemale,imgUserImage;
    private RelativeLayout relativeImgProfilePic;
    private TextView tvRegiLogin;
    private Utility utility;
    private CustomProgressBar customProgressBar;
    private SessionManager sessionManager;

    private Permission permission;
    private String maleFemale="male";
    private Double latitude=0.0, longitude =0.0;

    private  LocationManager locationManager;
    private boolean checkGPS;

    private Bitmap profileImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setStatusBarColor();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        utility = new Utility(this);
        sessionManager = new SessionManager(this);
        initView();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bgImage));
        }
    }


    private void initView() {
        etRegiFullName=findViewById(R.id.etRegiFullName);
        etRegiEmail=findViewById(R.id.etRegiEmail);
        etRegiPwd=findViewById(R.id.etRegiPwd);
        imgRegiMale=findViewById(R.id.imgRegiMale);
        imgRegiFemale=findViewById(R.id.imgRegiFemale);
        imgUserImage=findViewById(R.id.imgUserImage);

        imgRegiMale.setOnClickListener(this);
        imgRegiFemale.setOnClickListener(this);
        findViewById(R.id.btnRegiSignUp).setOnClickListener(this);
        findViewById(R.id.tvRegiLogin).setOnClickListener(this);
        findViewById(R.id.relativeImgProfilePic).setOnClickListener(this);

        initMembers();
    }

    private void initMembers() {
        permission = new Permission(context);

        permission.checkLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initLocation();
    }

    private  void initLocation() {
        try {
            // get GPS status
            checkGPS = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // get network provider status
            boolean checkNetwork = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           /* CustomPopup customPopup=new CustomPopup(LoginActivity.this);
            customPopup.setMessage(getString(R.string.eLocationPermission_new));
            customPopup.show();*/
                return;
            }
            if (!checkGPS && !checkNetwork) {
                Utility.e(TAG, "GPS & Provider not avaialble");
                // utility.checkGpsStatus();
            } else {
                if (checkGPS) {
                    assert locationManager != null;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (checkNetwork) {
                    assert locationManager != null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, this);
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Utility utility=new Utility(context);

        switch (v.getId()){
            case R.id.tvRegiLogin:
                Intent iLogin = new Intent(context, LoginActivity.class);
                // Closing all the Activities
                iLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(iLogin);
                finish();
                break;

            case R.id.btnRegiSignUp:
                Validation  validation=new Validation(context);
                if (utility.checkInternetConnection()){
                if (validation.isFullNameValid(etRegiFullName)&&validation.isEmailValid(etRegiEmail)&&validation.isPasswordValid(etRegiPwd))
                {
                    String fullName=etRegiFullName.getText().toString().trim();
                    String email=etRegiEmail.getText().toString().trim();
                    String pwd=etRegiPwd.getText().toString().trim();

                   // Utility.showToast(context,getString(R.string.underDevelopment),0);
                    if (latitude!=0.0d&& longitude !=0.0d) {
                        doRegistration(fullName,email,pwd,maleFemale);
                    }/*else if (checkGPS&&latitude==0.0&&longitude==0.0){
                        showErrorPopup();
                    }*/else if (!checkGPS){
                        utility.checkGpsStatus();
                    }else{
                        showErrorPopup();
                    }


                }
                }else{
                    Utility.showToast(context,getString(R.string.internetConnectivityError),0);
                }

                break;

            case R.id.relativeImgProfilePic:
                if (permission.checkCameraPermission()) {
                    selectImage();
                }
                break;

            case R.id.imgRegiMale:
                maleFemale="male";

                imgRegiMale.setImageResource(R.drawable.active_male_ico);
                imgRegiFemale.setImageResource(R.drawable.inactive_female_ico);
                break;

            case R.id.imgRegiFemale:
                maleFemale="female";

                imgRegiMale.setImageResource(R.drawable.inactive_male_ico);
                imgRegiFemale.setImageResource(R.drawable.active_female_ico);
                break;
        }
    }

    private void showErrorPopup() {
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_popup_with_btn);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //      deleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id

        TextView tvCancel, tvTryAgain,tvTitle,tvMessages;

        tvTitle = dialog.findViewById(R.id.tvTitle);
        tvMessages = dialog.findViewById(R.id.tvMessages);
        tvCancel = dialog.findViewById(R.id.tvPopupCancel);
        tvTryAgain = dialog.findViewById(R.id.tvPopupOk);

        tvTitle.setText(R.string.gps_new);
        tvMessages.setText(R.string.couldntGetLocation);

        tvTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                final CustomProgressBar dialog = new CustomProgressBar(context);
                dialog.show();


                new Handler().postDelayed(new Runnable() {
                    // Using handler with postDelayed called runnable run method
                    @Override
                    public void run() {
                        dialog.dismiss();
                        btnRegiSignUp.callOnClick();
                    }
                }, 3 * 1000); // wait for 3 seconds
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void doRegistration(final String fullName, final String email, final String pwd, final String maleFemale) {

        if (utility.checkInternetConnection()) {

            customProgressBar=new CustomProgressBar(context);
            customProgressBar.setCancelable(false);
            customProgressBar.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://dev.scenekey.com/event/webservices/registration", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        String messageCode = jsonObject.getString("messageCode");

                        if (status.equalsIgnoreCase("success")) {
                            customProgressBar.dismiss();

                            JSONObject userDetail = jsonObject.getJSONObject("userDetail");
                            UserInfo userInfo = new UserInfo();
                            userInfo.userID = userDetail.getString("userid");
                            userInfo.facebookId = userDetail.getString("userFacebookId");
                            String socialType  = userDetail.getString("socialType");
                            userInfo.userName = userDetail.getString("userName");
                            userInfo.email = userDetail.getString("userEmail");
                            userInfo.fullName = userDetail.getString("fullname");
                            userInfo.password = userDetail.getString("password");
                            userInfo.userImage = userDetail.getString("userImage");
                            String age = userDetail.getString("age");
                            String dob = userDetail.getString("dob");
                            String gender = userDetail.getString("gender");
                            String userDeviceId = userDetail.getString("userDeviceId");
                            String deviceType = userDetail.getString("deviceType");
                            userInfo.userGender = userDetail.getString("userGender");
                            String userStatus = userDetail.getString("userStatus");
                            userInfo.loginTime = userDetail.getString("userLastLogin");
                            String registered_date = userDetail.getString("registered_date");
                            String usertype = userDetail.getString("usertype");
                            String artisttype = userDetail.getString("artisttype");
                            String stagename = userDetail.getString("stagename");
                            userInfo.venuName = userDetail.getString("venuename");
                            userInfo.address = userDetail.getString("address");
                            String fullAddress = userDetail.getString("fullAddress");
                            userInfo.latitude = userDetail.getString("lat");
                            userInfo.longitude = userDetail.getString("longi");
                            userInfo.latitude = userDetail.getString("adminLat");
                            userInfo.longitude = userDetail.getString("adminLong");
                            String user_status = userDetail.getString("user_status");
                            userInfo.makeAdmin = userDetail.getString("makeAdmin");
                            userInfo.keyPoints = userDetail.getString("key_points");
                            userInfo.bio= userDetail.getString("bio");

                            sessionManager.createSession(userInfo);

                            Intent intent = new Intent(RegistrationActivity.this,IntroActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }

                    customProgressBar.dismiss();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(RegistrationActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    customProgressBar.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("address", "");
                    params.put("deviceType", "");
                    params.put("fbusername", "");
                    params.put("firstname", fullName);
                    params.put("fullAddress", "");
                    params.put("fullname", fullName);
                    params.put("lastname", "");
                    params.put("lat", String.valueOf(latitude));
                    params.put("longi", String.valueOf(longitude));
                    params.put("password", pwd);
                    params.put("socialType", "");
                    params.put("stagename", "");
                    params.put("userEmail", email);
                    params.put("userFacebookId", "");
                    params.put("userGender", maleFemale);

                    /*if (profileImageBitmap == null){
                        params.put("profilePic", "");
                    }*/
                    return params;
                }

               /* @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<String, DataPart>();
                    if (profileImageBitmap != null) {
                        params.put("profileImage", new VolleyMultipartRequest.DataPart("profilePic.jpg", AppHelper.getFileDataFromDrawable(profileImageBitmap), "image/jpeg"));
                    }
                    return params;
                }*/
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(RegistrationActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(RegistrationActivity.this,getString( R.string.internetConnectivityError), Toast.LENGTH_SHORT).show();
        }
    }


    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    //for camera
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constant.CAMERA_REQUEST);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Constant.GALLERY_REQUEST);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // Logic to handle location object
            latitude=location.getLatitude();
            longitude =location.getLongitude();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Utility.e("Latitude","disable");
        if (provider.equals("network")){
           try {
               utility.checkGpsStatus();
           }catch (Exception e){
               e.printStackTrace();
           }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Utility.e("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Utility.e("Latitude","status");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //   Bitmap bitmap;
        if (resultCode != 0) {
            switch (requestCode) {
                case Constant.GALLERY_REQUEST:
                    try {
                        Uri uri = data.getData();
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        // int size=resizeBitmap(bitmap);
                        //  profileImageBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                        imgUserImage.setImageBitmap(profileImageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constant.CAMERA_REQUEST:
                    profileImageBitmap = (Bitmap) data.getExtras().get("data");
                    imgUserImage.setImageBitmap(profileImageBitmap);
                    break;

                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        //success permission granted & call camera method
                        selectImage();
                    }
                } else {
                    // permission denied, boo! Disable the
                    Toast.makeText(context, R.string.cameraPermissionDeny, Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //success permission granted & call Location method
                        //   getDeviceLocation();
                    }
                } else {
                    // permission denied, boo! Disable the
                    Utility.e(TAG,getString(R.string.locationPermissionDeny));
                    // Toast.makeText(context, "deny Location Permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

}
