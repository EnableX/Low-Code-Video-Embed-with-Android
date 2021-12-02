package com.meeting.webview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MeetingActivity  extends AppCompatActivity {

    public String roomUrl = "https://new.yourvideo.live/617bab415dae7262c0aca609"; // Replace by your own
    private String roomParameters = "?skipMediaPermissionPrompt";

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

                this.requestCameraAndAudioPermissions();
            } else {
                this.loadLowCodeEmbedRoomUrl();
            }
        }
    }

    private void loadLowCodeEmbedRoomUrl() {
        this.webView.loadUrl(roomUrl+ roomParameters);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (this.grantResultsContainsDenials(grantResults)) {
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

