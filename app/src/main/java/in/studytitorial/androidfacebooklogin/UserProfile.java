package in.studytitorial.androidfacebooklogin;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserProfile extends AppCompatActivity {
    JSONObject response, profile_pic_data, profile_pic_url;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    ShareDialog shareDialog;

    public static int IMAGE_GETTING = 2;

    Button galleryX;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if(requestCode == IMAGE_GETTING) {

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // i guess we just take 1 permiision at a time when we acces camera like ..or another time when we click get libaray ....on their own time they will store at grant[0] location u are just required to check request code

                    getLocationOfPhoto();
                }

            }
        }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        galleryX = (Button) findViewById(R.id.galleryimage);



        if(Build.VERSION.SDK_INT < 23) {

            getLocationOfPhoto();

        } else
            // for alove L version we need to get permission at the door to get throught that location ...
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  // checkSelfPermission is a method avail in 23 api ..without if condition of (SDK_INT < 23 ) you cant implement it..

                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_GETTING );

            } else {

                getLocationOfPhoto();
            }










        galleryX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOption();
            }
        });

        Intent intent = getIntent();
        String jsondata = intent.getStringExtra("userProfile");
        TextView user_name = (TextView) findViewById(R.id.xuserName);
        ImageView user_picture = (ImageView) findViewById(R.id.profilePic);
        TextView user_email = (TextView) findViewById(R.id.email);
        Button button = (Button) findViewById(R.id.button);

        //===================================



        shareDialog = new ShareDialog(this);  // you can post quote with content url  =====please see the docs   most of the stuff is dericated ============
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setQuote("When it Rains")
                            .setContentUrl(Uri.parse("https://www.studytutorial.in/wp-content/uploads/2017/02/FacebookLoginButton-min-300x136.png"))
                            .build();
                    shareDialog.show(linkContent);  // Show facebook ShareDialog
                }
            }
        });






        try {
            response = new JSONObject(jsondata);
            user_email.setText(response.get("email").toString());
            user_name.setText(response.get("name").toString());
            profile_pic_data = new JSONObject(response.get("picture").toString());
            profile_pic_url = new JSONObject(profile_pic_data.getString("data"));
            Picasso.with(this).load(profile_pic_url.getString("url"))
                    .into(user_picture);

        } catch(Exception e){
            e.printStackTrace();
        }
    }



    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode == IMAGE_GETTING && resultCode == RESULT_OK && data != null){
            Uri uri =  data.getData();  // getting location (ofcourse it a uri) ..got that in uri variable so that we can get image from there
            // lets get the image from there //  lets go to media store to get the image

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);  // we need contentResolver to get the content...without him we cant get the content...

                sharePhotoFacebook(bitmap);



            } catch (IOException e) {
                e.printStackTrace();
            }

        }




    }


    //===============================================


  public  void sharePhotoFacebook(Bitmap bitmap){

      SharePhoto photo = new SharePhoto.Builder()
              .setBitmap(bitmap)
              .build();
      SharePhotoContent contentx = new SharePhotoContent.Builder()
              .addPhoto(photo)
              .build();

      shareDialog.show(contentx);




  }



    private void showOption() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(
                UserProfile.this);
        dialog.setMessage("Browse");
        dialog.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        dialog.dismiss();
                    }
                });

        dialog.setNeutralButton("Gallery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Build.VERSION.SDK_INT < 23) {

                            getLocationOfPhoto();

                        } else
                            // for alove L version we need to get permission at the door to get throught that location ...
                            if (UserProfile.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  // checkSelfPermission is a method avail in 23 api ..without if condition of (SDK_INT < 23 ) you cant implement it..

                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_GETTING);

                            } else {

                                getLocationOfPhoto();
                            }

                        dialog.dismiss();
                    }
                });

        dialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog d = dialog.create();
        d.show();
    }

    public void getLocationOfPhoto() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  // content:// (url) - External_Content_Uri ..location location of images in sd card and Phone Internal Storage (MediaStore -- sd card and phone internal storage)
        startActivityForResult(intent, IMAGE_GETTING);

    }















}
