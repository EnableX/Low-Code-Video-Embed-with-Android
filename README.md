# Low-Code-Video-Embed-with-Android

 # Documentation
Visit https://www.enablex.io/developer/video/low-code-video-embed/ to view the full Low-Code-Video-Embed developer guide documentation and get started.

Disclaimer
The EnableX help to the developer community to understand, How the Enablex Low-Code-Video-Embed product can be implemented using webview in Android Native App( Java/kotlin).

# How it works

# 1.Add  these  permission


<br \><uses-permission android:name="android.permission.CAMERA" /> <br \>
<uses-permission android:name="android.permission.INTERNET" /> <br \>
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" /> <br \>
<uses-permission android:name="android.permission.RECORD_AUDIO" /><br \>
<uses-permission android:name="android.permission.WAKE_LOCK" /><br \>




# 2.You need to use Webview class .Create custom WebChromeClient class and override onPermissionRequest method:

import android.app.Activity;<br />
import android.webkit.PermissionRequest;<br />
import android.webkit.WebChromeClient;<br />

public class CustomWebChromeClient extends WebChromeClient {
private Activity activity;

public CustomWebChromeClient(Activity parentActivity) {
super();
activity = parentActivity;
}

@Override
public void onPermissionRequest(final PermissionRequest request) {<br />
activity.runOnUiThread(() -> request.grant(request.getResources()));
}
}


# 3.Create a WebUtils helper class to configure the WebView. Here is a possible configuration:

import android.annotation.SuppressLint;<br />
import android.webkit.CookieManager;<br />
import android.webkit.WebSettings;<br />
import android.webkit.WebView;<br />

public class WebUtils {

    @SuppressLint("SetJavaScriptEnabled")
    public static void configureWebView(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }
}

#4.Request the permissions if needed.
  Add the ?skipMediaPermissionPrompt parameter to the room URL and load it.
  Here is an example:

  package com.meeting.webview;<br />
  
  import android.Manifest;<br />
  import android.content.pm.PackageManager;<br />
  import android.os.Build;<br />
  import android.os.Bundle;<br />
  import android.webkit.WebView;<br />
  import android.webkit.WebViewClient;<br />
  
  import androidx.annotation.NonNull;<br />
  import androidx.annotation.RequiresApi;<br />
  import androidx.appcompat.app.AppCompatActivity;<br />
  
  import java.util.ArrayList;<br />
  import java.util.List;<br />
  
  public class MeetingNew  extends AppCompatActivity {
  
     public String roomUrl = ""; // Replace by your ownUrl
  
  
     private static final int PERMISSION_REQUEST_CODE = 1;
     private String[] requiredPermissions = {
             Manifest.permission.CAMERA,
             Manifest.permission.MODIFY_AUDIO_SETTINGS,
             Manifest.permission.RECORD_AUDIO
     };
  
     private WebView webView;
  
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.meeting_webview);
         this.webView = findViewById(R.id.webView);
         WebUtils.configureWebView(this.webView);
         this.webView.setWebChromeClient(new CustomWebChromeClient(this));
         this.webView.setWebViewClient(new WebViewClient());
     }
  
     @Override
     protected void onResume() {
         super.onResume();
         if (this.webView.getUrl() == null) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.isPendingPermissions()) {
                 // This explicitly requests the camera and audio permissions.
                 // It's fine for a demo app but should probably be called earlier in the flow,
                 // on a user interaction instead of onResume.
                 this.requestCameraAndAudioPermissions();
             } else {
                 this.loadLowCodeRoomUrl();
             }
         }
     }
  
     private void loadLowCodeRoomUrl() {
         this.webView.loadUrl(roomUrl);
     }
  
     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         switch (requestCode) {
             case PERMISSION_REQUEST_CODE:
                 if (this.grantResultsContainsDenials(grantResults)) {
                     // Show some permissions required dialog.
                 } else {
                     // All necessary permissions granted, continue loading.
                     this.loadLowCodeRoomUrl();
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
  

