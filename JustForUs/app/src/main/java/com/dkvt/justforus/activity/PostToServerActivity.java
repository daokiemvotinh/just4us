package com.dkvt.justforus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dkvt.justforus.R;
import com.dkvt.justforus.externalclass.ImageLoader;
import com.dkvt.justforus.externalclass.RestAPI;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class PostToServerActivity extends Activity implements OnClickListener {

    public Context mContext;

    private ImageView imgView;
    private EditText etCommentStory;
    private ImageButton btnBack;
    private ImageButton btnUpload;

    String pathImageStory;
    Bitmap bmpImageStory;
    String idImg;
    String comment;
    String statement;
    int ID;
    private ImageLoader loader;

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.post_to_server);
        mContext = this;
        loader = new ImageLoader(this);
        imgView = (ImageView) findViewById(R.id.story_photo);
        etCommentStory = (EditText) findViewById(R.id.comment);
        btnBack = (ImageButton) findViewById(R.id.cancel);
        btnUpload = (ImageButton) findViewById(R.id.upload);

        btnBack.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        statement = getIntent().getStringExtra("STATEMENT");
        if (statement.compareTo("INSERT") == 0) {
            // Show photo from camera crop
            pathImageStory = getIntent().getStringExtra("pathImageStory");
            ID = getIntent().getIntExtra("ID", 0);
            bmpImageStory = BitmapFactory.decodeFile(pathImageStory);
            imgView.setImageBitmap(bmpImageStory);
        }
        if (statement.compareTo("UPDATE") == 0) {
            // Show photo to update comment
            pathImageStory = getIntent().getStringExtra("pathImgStory");
            idImg = getIntent().getStringExtra("idImg");
            comment = getIntent().getStringExtra("comment");
            etCommentStory.setText(comment);
            loader.DisplayImage(pathImageStory, imgView, null);
        }
    }

    @SuppressLint("ShowToast")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel:
                onBackPressed();
                break;
            case R.id.upload:
                new UploadMyStoryAsyncTask(mContext).execute();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                Toast.makeText(mContext, "Đăng ảnh thành công!", Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    public class UploadMyStoryAsyncTask extends AsyncTask<Void, Void, Void> {
        // array data to send to server
        private String SERVERURL = "http://ixoso.net/home/UploadFileToServer";
        private ProgressDialog dialogUpload;
        private Context mContext;
        private static final String TAG = "UploadMyStoryAsyncTask";


        public UploadMyStoryAsyncTask(Context context) {
            mContext = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            dialogUpload = new ProgressDialog(mContext);
            dialogUpload.setMessage("Loading ...");
            dialogUpload.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (dialogUpload.isShowing()) {
                dialogUpload.cancel();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (statement.compareTo("INSERT") == 0) {
                String[] arrExt = {"png", "jpg", "gif", "jpeg", "bitmap"};
                String fileExt = MimeTypeMap
                        .getFileExtensionFromUrl(pathImageStory);
                String dirImagesDes = null;
                if (Arrays.asList(arrExt).contains(fileExt)
                        && bmpImageStory != null) {
                    dirImagesDes = processImage(pathImageStory);
                }

                try {
                    String status = etCommentStory.getText().toString();
                    RestAPI api = new RestAPI();
                    JSONObject jsonObj = api.CreateNewStatus(ID, status, dirImagesDes);
                } catch (IOException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ((Activity) mContext).finish();
            return null;
        }

        // Main code for processing image algorithm on the server
        String processImage(String inputImageFilePath) {

            String urlImageResult = null;
            // get file from path file
            File inputFile = new File(inputImageFilePath);
            long fileSize = inputFile.length();
            try {
                // create file stream for captured image file
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                // upload photo
                final HttpURLConnection conn = uploadPhoto(fileInputStream,
                        fileSize);

                // get processed photo from server
                if (conn != null) {
                    urlImageResult = getResultImage(conn);
                }

                // close stream
                fileInputStream.close();
                return urlImageResult;
            } catch (FileNotFoundException ex) {
                Log.e(TAG, ex.toString());
                return null;
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
                return null;
            }
        }

        // get image result from server and display it in result view
        String getResultImage(HttpURLConnection conn) {
            // retrieve the response from server
            InputStream is;
            try {
                is = conn.getInputStream();
                int ch;
                StringBuffer sb = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }
                is.close();
                return sb.toString();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return null;
            }
        }

        // upload photo to server
        HttpURLConnection uploadPhoto(FileInputStream fileInputStream,
                                      long fileSize) {
            int serverResponseCode = 0;
            final String serverFileName = (int) Math.round(Math.random() * 1000) + ".jpg";
            final String lineEnd = "\r\n";
            final String twoHyphens = "--";
            final String boundary = "*****";

            try {
                URL url = new URL(SERVERURL);
                // Open a HTTP connection to the URL
                final HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setDoInput(true);// Allow Inputs
                conn.setDoOutput(true);// Allow Outputs
                conn.setUseCaches(false);// Don't use a cached copy.
                conn.setRequestMethod("POST");// Use a post method.
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                // conn.connect();
                DataOutputStream dos = new DataOutputStream(
                        conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                        + serverFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();
                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                long total = 0;
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                dos.flush();// flush data to server
                return conn;
            } catch (MalformedURLException ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return null;
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
                return null;
            }
        }

    }

}
