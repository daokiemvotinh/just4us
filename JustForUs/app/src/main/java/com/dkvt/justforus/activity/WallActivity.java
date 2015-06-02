package com.dkvt.justforus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dkvt.justforus.R;
import com.dkvt.justforus.adapter.WallAdapter;
import com.dkvt.justforus.externalclass.InternalStorageContentProvider;
import com.dkvt.justforus.externalclass.RestAPI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import eu.janmuller.android.simplecropimage.CropImage;

public class WallActivity extends ActionBarActivity implements View.OnClickListener {

    private Context mContext;
    WallAdapter adapter;
    ArrayList<HashMap<String, String>> mylist = new ArrayList();

    // Use for camera
    private Uri mImageCaptureUri;
    private LinearLayout btnPhotobookCamera;
    private LinearLayout btnPhotobookAlbum;
    public static LinearLayout llListStory;
    public static TextView tvCountImage;
    private Button btnBack;
    private ListView lstStory;
    public static ScrollView svNoPhotoStory;

    public static final String TAG = "Get Image Activity";
    public static String TEMP_PHOTO_FILE_NAME = "temp.jpg";
    public static final int PICK_FROM_CAMERA = 1;
    public static final int CROP_IMAGE = 2;
    public static final int PICK_FROM_FILE = 3;
    public static final int UPDATE_OR_INSERT_STORY = 4;
    public File mFileTemp;

    int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);
        mContext = this;

        // Declare Variable
        btnBack = (Button) findViewById(R.id.btn_previous);
        btnPhotobookCamera = (LinearLayout) findViewById(R.id.btn_photobook_camera);
        btnPhotobookAlbum = (LinearLayout) findViewById(R.id.btn_photobook_album);
        llListStory = (LinearLayout) findViewById(R.id.llliststory);
        tvCountImage = (TextView) findViewById(R.id.tvcountimage);
        lstStory = (ListView) findViewById(R.id.listStoty);
        svNoPhotoStory = (ScrollView) findViewById(R.id.sv_no_photo_story);

        // Event Onclick
        btnBack.setOnClickListener(this);
        btnPhotobookCamera.setOnClickListener(this);
        btnPhotobookAlbum.setOnClickListener(this);

        String value="";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("ID");
        }
        ID = Integer.parseInt(value);
        new ListStoryAsyncTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(),
                    TEMP_PHOTO_FILE_NAME);
            mImageCaptureUri = Uri.fromFile(mFileTemp);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
            mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
        }

        int id = v.getId();
        switch (id) {
            case R.id.btn_previous:
                onBackPressed();
                break;
            case R.id.btn_photobook_camera:
                // take picture from camera
                Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        mImageCaptureUri);
                captureIntent.putExtra("return-data", true);
                startActivityForResult(captureIntent, PICK_FROM_CAMERA);
                break;
            case R.id.btn_photobook_album:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(
                                intent,
                                "Chọn ảnh từ ..."),
                        PICK_FROM_FILE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_FILE:
                // get image from
                try {
                    InputStream inputStream = getContentResolver().openInputStream(
                            data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    startCropImage();
                } catch (Exception e) {
                    Log.e(TAG, e.toString(), e);
                }
                break;
            case PICK_FROM_CAMERA:
                // get image from camera
                startCropImage();
                break;
            case CROP_IMAGE:
                // crop image
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                Intent intent = new Intent(WallActivity.this, PostToServerActivity.class);
                intent.putExtra("pathImageStory", mFileTemp.getPath());
                intent.putExtra("ID", ID);
                intent.putExtra("STATEMENT", "INSERT");// open UpdateStory activty
                // with statement INSERT
                startActivityForResult(intent, UPDATE_OR_INSERT_STORY);
                break;
            case UPDATE_OR_INSERT_STORY:
                mylist.clear();
                new ListStoryAsyncTask().execute();
                break;
        }

    }

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void startCropImage() {
        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, false);
        intent.putExtra(CropImage.ASPECT_X, 2);
        intent.putExtra(CropImage.ASPECT_Y, 2);
        startActivityForResult(intent, CROP_IMAGE);
    }

    public class ListStoryAsyncTask extends AsyncTask<Void, Void, Void> {
        ListStoryAsyncTask() {
        }

        @SuppressWarnings("unchecked")
        protected Void doInBackground(Void... params) {
            try {
                RestAPI api = new RestAPI();
                JSONObject jsonObj = api.LoadWallData(ID);
                JSONArray jsonarray = new JSONArray(jsonObj.getString("Value"));
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject obj = jsonarray.getJSONObject(i);


                    String id = obj.getString("ID");
                    String userID = obj.getString("userID");

                    String status = obj.getString("status");
                    String urlImage = obj.getString("url");
                    String strTime = obj.getString("dateUpdate");

                    strTime = strTime.replace("/Date(", "").replace(")/", "");
                    long timevalue = Long.parseLong(strTime);
                    Date dateTime = new Date(timevalue);


                    // parse time
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String timePost = sdf.format(dateTime);
                    HashMap localHashMap = new HashMap();
                    localHashMap.put("idImg", id);
                    localHashMap.put("pathImgStory", "http://ixoso.net/images/userupload/" + urlImage);
                    localHashMap.put("comment", status);
                    localHashMap.put("timePost", timePost);
                    mylist.add(localHashMap);
                }


            } catch (Exception localException) {
                localException.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (mylist.size() > 0) {
                adapter = new WallAdapter(WallActivity.this,
                        mylist);
                lstStory.setAdapter(adapter);
                tvCountImage.setText(mylist.size() + " ảnh");
                llListStory.setVisibility(View.VISIBLE);
                svNoPhotoStory.setVisibility(View.GONE);
            } else {
                svNoPhotoStory.setVisibility(View.VISIBLE);
                llListStory.setVisibility(View.GONE);
            }

        }
    }

}
