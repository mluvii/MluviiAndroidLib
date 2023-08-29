package mluviipoc.mluvii.com.webviewapp;

import static mluviipoc.mluvii.com.webviewapp.MluviiLibrary.REQUEST_SELECT_FILE;
import static mluviipoc.mluvii.com.webviewapp.MluviiLibrary.getCameraCaptureUri;
import static mluviipoc.mluvii.com.webviewapp.MluviiLibrary.getCameraUriCallback;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 654;
    private static String TAG = "MLUVII WEBVIEW APP";

    private static WebView mluviiWebView = null;
    private static WebView videoWebView = null;


    /**
     * Inicializacni parametry pro Mluvii library webView
     */
    private static String mluviiServer = "app.mluvii.com";
    private static String mluviiCompanyId = "730a66b3-4316-43d2-8f6e-6ffc8058f992";
    private static String mluviiTenantId = "437";
    private static String mluviiPresetName = "KC";
    private static String mluviiLanguageCode = "cs";
    private static String mluviiScope = null;

    /**
     * Current stat of the operators group
     */
    private static int status = 0;
    /**
     * Customer button to indicate state of the operator and invoke the webview
     */
    private Button btn = null;

    /**
     * Customer button to indicate state of the operator and invoke the webview
     */
    private Button btnWithArgs = null;

    /**
     * Customer button to indicate state of the operator and invoke the webview
     */
    private Button videoBtn = null;

    private ValueCallback<Uri[]> uploadMessages = null;
    private ValueCallback<Uri> uploadMessage = null;
    /**
     * CHAT_URL points on the chat widget. There can be more urls changed within the client app to direst
     * chat to different operator groups/different widgets.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                //Can add explanation why do you need this specific permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                        REQUEST_CAMERA_PERMISSION);

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                        REQUEST_CAMERA_PERMISSION);
            }
        }

        /**
         * Callback na Online stav z widgetu
         */
        MluviiLibrary.setStatusOnlineCallback(new Callable<Void>()  {
            public Void call(){
                Log.d("MLUVII_STATUS", "STATUS ONLINE");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#00ff00"));
                        btn.setText("ONLINE");
                        btn.setPadding(5,5,5,5);
                        btnWithArgs.setBackgroundColor(Color.parseColor("#00ff00"));
                        btnWithArgs.setText("ONLINE WITH ARGS");
                        videoBtn.setBackgroundColor(Color.parseColor("#00ff00"));
                        videoBtn.setText("Video");
                    }
                });
                status = 1;
                return null;
            }
        });

        /**
         * Callback na Busy stav z widgetu
         */
        MluviiLibrary.setStatusBusyCallback( new Callable<Void>()  {
            public Void call(){
                Log.d("MLUVII_STATUS", "STATUS BUSY");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#ffff00"));
                        btn.setText("BUSY");
                        btn.setPadding(5,5,5,5);
                        btnWithArgs.setBackgroundColor(Color.parseColor("#ffff00"));
                        btnWithArgs.setText("BUSY WITH ARGS");
                        videoBtn.setBackgroundColor(Color.parseColor("#ffff00"));
                        videoBtn.setText("Video BUSY");
                    }
                });
                status = 2;
                return null;
            }
        });

        MluviiLibrary.setUrlCallbackFunc(new MluviiLibrary.UrlCallback(){

            @Override
            public Void call() throws Exception{
                Log.d("MLUVII_URL_CALLBACK","TEst url: "+this.url);
                return null;
            }
        });

        MluviiLibrary.setParamSetFunc(new MluviiLibrary.UrlCallback(){

            @Override
            public Void call() throws Exception{
                Log.d("MLUVII_PARAM_SET","parametr nastaven ");
                MluviiLibrary.runChat();
                return null;
            }
        });

        /**
         * Callback na Offline stav z widgetu
         */
        MluviiLibrary.setStatusOfflineCallback( new Callable<Void>()  {
            public Void call(){
                status = 0;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#ff0000"));
                        btn.setText("OFFLINE");
                        btn.setPadding(5,5,5,5);
                        btnWithArgs.setBackgroundColor(Color.parseColor("#ff0000"));
                        btnWithArgs.setText("OFFLINE WITH ARGS");
                    }
                });
                Log.d("MLUVII_STATUS", "STATUS OFFLINE");
                return null;
            }
        });

        MluviiLibrary.setChatLoadedCallback( new Callable<Void>()  {
            public Void call(){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        mluviiWebView.setLayoutParams(openedMluviiParams);
                    }
                });
                return null;
            }
        });


        /**
         * Callback na Zavreni chatu - nutno reload puvodni URL neb chat widget to presmeruje
         */
        MluviiLibrary.setCloseChatFunc(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams mluviiParams = new RelativeLayout.LayoutParams(0,0);
                        //mluviiWebView.loadUrl(CHAT_URL);
                        MluviiLibrary.resetUrl();
                        mluviiWebView.setLayoutParams(mluviiParams);
                        videoWebView.setLayoutParams(mluviiParams);
                    }
                });
                return null;
            }
        });

        /**
         * Definovani funkce pro kliknuti na odkaz
         */



        /**
         * Inicializace WebView z MluviiLibrary
         */
        mluviiWebView = MluviiLibrary.getAndRunMluviiWebView(this, mluviiServer,mluviiCompanyId, mluviiTenantId, mluviiPresetName, mluviiLanguageCode);
        videoWebView = MluviiLibrary.getMluviiVideoWebView(this, mluviiServer,mluviiCompanyId, mluviiTenantId, mluviiPresetName, mluviiLanguageCode);
        /**
         * Nastaveni velikosti 0,0 na webview, aby nebylo videt, dokud neni potreba
         */
        RelativeLayout.LayoutParams mluviiParams = new RelativeLayout.LayoutParams(0,0);
        RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mluviiWebView.setLayoutParams(openedMluviiParams);
        //mluviiWebView.setLayoutParams(mluviiParams);

        videoWebView.setLayoutParams(mluviiParams);

        /**
         * Tlaciko, ktere zobrazuje stav Widgetu - Seda, nespojeno se serverem - Cervena, offline - Zluta, busy - Zelena, online
         */
        btn = new Button(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(status == 1){*/
                    //MluviiLibrary.runChat();
                    //mluviiWebView = MluviiLibrary.getAndRunMluviiWebView();
                    RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mluviiWebView.setLayoutParams(openedMluviiParams);
                /*}*/
            }
        });

        btnWithArgs = new Button(this);
        btnWithArgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(status == 1){*/
                MluviiLibrary.addCustomData("android","ano");
                //MluviiLibrary.runChat();
                RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mluviiWebView.setLayoutParams(openedMluviiParams);
                /*}*/
            }
        });

        videoBtn = new Button(this);
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MluviiLibrary.runVideo();
                RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                videoWebView.setLayoutParams(openedMluviiParams);
            }
        });

        /**
         * Linear layout s Tlacitkem
         */
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        params1.setMargins(5, 5, 5, 5);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.setMargins(5, 5, 5, 5);
        linearLayout.setLayoutParams(params1);
        linearLayout.addView(btn, params2);
        linearLayout.addView(btnWithArgs, params2);
        linearLayout.addView(videoBtn, params2);

        /**
         * Nastaveni zakladniho layoutu
         */
        final RelativeLayout layout = new RelativeLayout(this);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(5, 5, 5, 5);
        layout.addView(linearLayout);
        layout.addView(mluviiWebView);
        layout.addView(videoWebView);
        layout.setLayoutParams(params);
        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)    {
                uploadMessages = MluviiLibrary.getFilePathCallbacks();

                if (uploadMessages == null)
                    return;
                if (data == null && getCameraCaptureUri() != null){
                    uploadMessages.onReceiveValue(new Uri[]{getCameraCaptureUri()});
                }
                else
                    uploadMessages.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));

                uploadMessages = null;
            }
        } else {
            uploadMessage = MluviiLibrary.getFilePathCallback();
            if (null == uploadMessage) return;
            Uri result = data == null || resultCode != MainActivity.RESULT_OK ? null : (data.getData() != null ? data.getData() : getCameraCaptureUri());
            uploadMessage.onReceiveValue(result);
            uploadMessage = null;
        }

    }
}

