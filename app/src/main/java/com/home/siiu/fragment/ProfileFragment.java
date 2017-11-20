package com.home.siiu.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.home.siiu.R;
import com.home.siiu.activity.LoginActivity;
import com.home.siiu.activity.MainActivity;
import com.home.siiu.data.Util;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {


    /*............................................................*/
    /*..........................Variables.........................*/
    /*............................................................*/


    /*..........................Constant..........................*/
    public static final String PREFS_AUTO = "PREFERENCES_AUTO";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_LIBRARY = 2;
    public String photoUrl = "https://firebasestorage.googleapis.com/v0/b/siiu-9f71c.appspot.com/o/default_avata.png?alt=media&token=52371e0d-d820-4225-bedd-4dafd289bfac";

    /*..........................Firebase..........................*/
    private FirebaseAuth auth;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private String userId;

    /*..........................Component.........................*/
    private Button saveBtn;
    private Button logoutBtn;
    private ImageView takePhoto;
    private CircleImageView avatar;
    private EditText userName;

    /*..........................Photo Path.........................*/
    private Uri photoFilePath;

    /*..........................Wait Dialog.......................*/
    private LovelyProgressDialog waitingDialog;

    /*............................Camera.........................*/
    private int rotation;


    /*............................................................*/
    /*..........................Functions.........................*/
    /*............................................................*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);;

        initComponent(view);
        loadUserInfo();
        setSaveBtnClick();
        setLogoutBtnClick();
        setPhotoTakeAction();
        return view;
    }

    private void initComponent(View view) {

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        saveBtn = view.findViewById(R.id.SaveBtn);
        logoutBtn = view.findViewById(R.id.Logout);
        takePhoto = view.findViewById(R.id.TakePhoto);
        avatar = view.findViewById(R.id.Avatar);
        userName = view.findViewById(R.id.UserNameEditText);
        waitingDialog = new LovelyProgressDialog(getContext()).setCancelable(false);
    }

    private void loadUserInfo() {

        waitingDialog.setIcon(R.drawable.ic_person_low)
                .setTitle("Loading Info...")
                .setTopColorRes(R.color.colorPrimary)
                .show();

        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.child("userName").exists()) {

                        userName.setText(dataSnapshot.child("userName").getValue().toString());
                    }

                    if (dataSnapshot.child("photoUrl").exists()) {

                        photoUrl = dataSnapshot.child("photoUrl").getValue().toString();
                    }
                    Picasso.with(getContext()).load(photoUrl).into(avatar);
                }
                waitingDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSaveBtnClick() {

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userName.getText().length() > 0) {

                    waitingDialog.setIcon(R.drawable.ic_person_low)
                            .setTitle("Saving...")
                            .setTopColorRes(R.color.colorPrimary)
                            .show();

                    if (photoFilePath != null) {

                        storageRef.child(userId).putFile(photoFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                storageRef.child(userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Map<String, Object> result = new HashMap<>();
                                        result.put("userName", userName.getText().toString());
                                        result.put("photoUrl", uri.toString());

                                        dbRef.child("users").child(userId).updateChildren(result);
                                        waitingDialog.dismiss();

                                        MainActivity activity = (MainActivity)getActivity();
                                        activity.gotoFragment(0);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                waitingDialog.dismiss();
                            }
                        });
                    } else {

                        Map<String, Object> result = new HashMap<>();
                        result.put("userName", userName.getText().toString());

                        dbRef.child("users").child(userId).updateChildren(result);
                        waitingDialog.dismiss();

                        MainActivity activity = (MainActivity)getActivity();
                        activity.gotoFragment(0);
                    }
                } else {

                    Toast.makeText(getContext(), "User name must be exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLogoutBtnClick() {

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                auth = FirebaseAuth.getInstance();

                SharedPreferences autoLogin = getActivity().getSharedPreferences(PREFS_AUTO, Context.MODE_PRIVATE);
                autoLogin.edit().putBoolean("AutoLogin", false).commit();
                auth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    private void setPhotoTakeAction() {

        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose One");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (items[i].equals("Take Photo")) {
                            takePhotoIntent();
                        } else if (items[i].equals("Choose from Library")) {
                            loadPhotoIntent();
                        } else if (items[i].equals("Cancel")) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void takePhotoIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imgFile = createImageFile();
        photoFilePath = null;
        if(imgFile != null) {
            photoFilePath = Uri.fromFile(imgFile);
        }

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFilePath);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void loadPhotoIntent() {

        Intent loadPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(loadPictureIntent, REQUEST_IMAGE_LIBRARY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_IMAGE_CAPTURE) {

                Bitmap imageBitmap = decodeUri(getContext(), photoFilePath, 100, true);
                avatar.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_LIBRARY) {

                Bitmap bm = null;
                if (data != null) {
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                avatar.setImageBitmap(bm);
                photoFilePath = data.getData();
            }
        }
    }

    private File createImageFile () {
        String fileName = "TEMP_CAMERA_IMG";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public Bitmap decodeUri(Context c, Uri uri, final int requiredSize, boolean bRotatedImage) {
        Bitmap orgBitmap = null;
        rotation = 0;

        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while(true) {
                if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            orgBitmap = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);

            ExifInterface ei = new ExifInterface(Util.getRealPathFromURI(getActivity(), uri, "IMAGE"));
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotation = 0;
                    break;
            }

            if(bRotatedImage && rotation != 0) {
                Bitmap rotatedBitmap = Util.rotateImage(orgBitmap, rotation);
                if(orgBitmap != null) {
                    orgBitmap.recycle();
                }

                orgBitmap = rotatedBitmap;
            }

            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
            System.gc();
        }

        return orgBitmap;
    }

}
