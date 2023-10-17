package com.meeting.webview;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeetingActivity extends AppCompatActivity {
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final String TAG = MeetingActivity.class.getSimpleName();
    private WebView webView;
    private WebSettings webSettings;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    public String roomUrl = ""; // Replace by your own

    private String roomParameters = "?skipMediaPermissionPrompt";

    private static final int PERMISSION_REQUEST_CODE = 1;
    private String[] requiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    @Override
    public void onBackPressed() {
        webView.destroy();
        super.onBackPressed();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_webview);
        webView = findViewById(R.id.webView);

       // if(roomUrl.isEmpty()||roomUrl==null)
        WebUtils.configureWebView(this.webView);
        webView.setWebViewClient(new Client());
        webView.setWebChromeClient(new ChromeClient());
        webView.setDownloadListener(new DownloadListener() {
                    @Override
                    public void onDownloadStart(String url, String userAgent,
                                                String contentDisposition, String mimeType,
                                                long contentLength) {

                        if (url.startsWith("data:")) {  //when url is base64 encoded data
                            String path = createAndSaveFileFromBase64Url(url);
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());

                            return;
                        }

                              }
                });

    }

    public String createAndSaveFileFromBase64Url(String url) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);
        try {
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();

        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }
    public class ChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            runOnUiThread(() -> {
                request.grant(request.getResources());

//                if(request.getOrigin().toString().equals("file:///")) {
//                    Log.d(TAG, "GRANTED");
//                    request.grant(request.getResources());
//                } else {
//                    Log.d(TAG, "DENIED");
//                    request.deny();
//                }
            });
        }
        // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Unable to create Image File", ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }
            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            return true;
        }
        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            // Create AndroidExampleFolder at sdcard
            // Create AndroidExampleFolder at sdcard
            File imageStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES)
                    , "AndroidExampleFolder");
            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }
            // Create camera captured image file path and name
            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);
            // Camera capture image intent
            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[] { captureIntent });
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
        }
        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }
        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {
            openFileChooser(uploadMsg, acceptType);
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
    public class Client extends WebViewClient {
        ProgressDialog progressDialog;
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // If url contains mailto link then open Mail Intent
            if (url.contains("mailto:")) {
                // Could be cleverer and use a regex
                //Open links in new browser
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                // Here we can open new activity
                return true;
            }else {
                // Stay within this webview and load url
                view.loadUrl(url);
                return true;
            }
        }
        //Show loader on url load
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Then show progress  Dialog
            // in standard case YourActivity.this
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(MeetingActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();
            }
        }
        // Called when all page resources loaded
        public void onPageFinished(WebView view, String url) {
            try {
                // Close progressDialog
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (this.webView.getUrl() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.isPendingPermissions()) {

                this.requestCameraAndAudioPermissions();
            } else {
                this.loadLowCodeEmbedRoomUrl();
            }
        }
    }

    private void loadLowCodeEmbedRoomUrl() {
       this.webView.loadUrl(roomUrl+roomParameters);
      //  this.webView.loadDataWithBaseURL(null,roomUrl1,"text/html", "utf-8",null);
       // this.webView.loadUrl("https://vc-dev3.enablex.io");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (this.grantResultsContainsDenials(grantResults)) {
                    this.loadLowCodeEmbedRoomUrl();

                    // Show some permissions required dialog.
                } else {
                    // All necessary permissions granted, continue loading.
                    this.loadLowCodeEmbedRoomUrl();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraAndAudioPermissions() {
        this.requestPermissions(this.getPendingPermissions(), PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private String[] getPendingPermissions() {
        List<String> pendingPermissions = new ArrayList<>();
        for (String permission : this.requiredPermissions) {
            if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                pendingPermissions.add(permission);
            }
        }
        return pendingPermissions.toArray(new String[pendingPermissions.size()]);
    }

    private boolean isPendingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return this.getPendingPermissions().length > 0;
    }

    private boolean grantResultsContainsDenials(int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }
}