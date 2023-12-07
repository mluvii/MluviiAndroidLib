package mluviipoc.mluvii.com.webviewapp;

import static mluviipoc.mluvii.com.webviewapp.MluviiLibrary.mCameraPhotoPath;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity implements FileChooserInterface {
    private static final int REQUEST_CAMERA_PERMISSION = 654;

    private static final String TAG = "MLUVII WEBVIEW APP";
    /**
     * Current stat of the operators group
     */
    private static int status = 0;
    /**
     * Inicializacni parametry pro Mluvii library webView
     */
    private final String mluviiServer = "app.mluvii.com";
    private final String mluviiCompanyId = "69ed0915-195c-4379-baf7-36cf4770fbf9";
    private final String mluviiTenantId = "820";
    private final String mluviiPresetName = null;
    private final String mluviiLanguageCode = "en";
    private final String mluviiScope = null;
    private WebView mluviiWebView = null;
    private WebView videoWebView = null;
    private ValueCallback<Uri[]> uploadMessages = null;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    uploadMessages.onReceiveValue(null);
                    uploadMessages = null;

                }else
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri[] results;

                    // Check that the response is a good one
                    if (result.getData() == null || (result.getData().getDataString() == null && result.getData().getClipData() == null)) {
                        // If there is not data, then we may have taken a photo
                        results = new Uri[]{mCameraPhotoPath};

                    } else {
                        String dataString = result.getData().getDataString();
                        if (!TextUtils.isEmpty(dataString)) {
                            results = new Uri[]{Uri.parse(dataString)};
                        } else {
                            ClipData mClipData = result.getData().getClipData();
                            results = new Uri[mClipData.getItemCount()];
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                results[i] = uri;
                            }
                        }
                    }
                    uploadMessages.onReceiveValue(results);
                    uploadMessages = null;
                }
            });
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

    /**
     * CHAT_URL points on the chat widget. There can be more urls changed within the client app to direst
     * chat to different operator groups/different widgets.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView.setWebContentsDebuggingEnabled(true);

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
        MluviiLibrary.setStatusOnlineCallback(new Callable<Void>() {
            public Void call() {
                Log.d("MLUVII_STATUS", "STATUS ONLINE");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#00ff00"));
                        btn.setText("ONLINE");
                        btn.setPadding(5, 5, 5, 5);
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
        MluviiLibrary.setStatusBusyCallback(new Callable<Void>() {
            public Void call() {
                Log.d("MLUVII_STATUS", "STATUS BUSY");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#ffff00"));
                        btn.setText("BUSY");
                        btn.setPadding(5, 5, 5, 5);
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

        MluviiLibrary.setUrlCallbackFunc(new MluviiLibrary.UrlCallback() {

            @Override
            public Void call() throws Exception {
                Log.d("MLUVII_URL_CALLBACK", "TEst url: " + this.url);
                return null;
            }
        });

        MluviiLibrary.setParamSetFunc(new MluviiLibrary.UrlCallback() {

            @Override
            public Void call() throws Exception {
                Log.d("MLUVII_PARAM_SET", "parametr nastaven ");
                MluviiLibrary.runChat();
                return null;
            }
        });

        /**
         * Callback na Offline stav z widgetu
         */
        MluviiLibrary.setStatusOfflineCallback(new Callable<Void>() {
            public Void call() {
                status = 0;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn.setBackgroundColor(Color.parseColor("#ff0000"));
                        btn.setText("OFFLINE");
                        btn.setPadding(5, 5, 5, 5);
                        btnWithArgs.setBackgroundColor(Color.parseColor("#ff0000"));
                        btnWithArgs.setText("OFFLINE WITH ARGS");
                    }
                });
                Log.d("MLUVII_STATUS", "STATUS OFFLINE");
                return null;
            }
        });

        MluviiLibrary.setChatLoadedCallback(new Callable<Void>() {
            public Void call() {
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
                        RelativeLayout.LayoutParams mluviiParams = new RelativeLayout.LayoutParams(0, 0);
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
        mluviiWebView = MluviiLibrary.getAndRunMluviiWebView(this, mluviiServer, mluviiCompanyId, mluviiTenantId, mluviiPresetName, mluviiLanguageCode, this);
        videoWebView = MluviiLibrary.getMluviiVideoWebView(this, mluviiServer, mluviiCompanyId, mluviiTenantId, mluviiPresetName, mluviiLanguageCode, this);
        /**
         * Nastaveni velikosti 0,0 na webview, aby nebylo videt, dokud neni potreba
         */
        RelativeLayout.LayoutParams mluviiParams = new RelativeLayout.LayoutParams(0, 0);
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
                MluviiLibrary.addCustomData("android", "ano");
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
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params1.setMargins(5, 5, 5, 5);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
    public void fileChooser(ValueCallback<Uri[]> filePathCallback) {
        FileManagerOpen(filePathCallback);
    }

    private void FileManagerOpen(ValueCallback<Uri[]> filePathCallback) {

        uploadMessages = filePathCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            System.out.println(getFileWriteDestinationPath(this,
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            ));
            mCameraPhotoPath = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(
                            getFileWriteDestinationPath(this,
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                            )));

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    mCameraPhotoPath);
        } else {
            takePictureIntent = null;
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

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

        someActivityResultLauncher.launch(chooserIntent);

    }

    public String getFileWriteDestinationPath(Activity activity, int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            switch (type) {
                case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                    return makeImagesFolder(activity) + "IMG_" + timeStamp + ".jpg";
                case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                    return makeImagesFolder(activity) + "VID_" + timeStamp + ".mp4";
                default:
                    return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String makeImagesFolder(Context context) {
        File fileImages = new File(
                Environment.getExternalStorageDirectory().toString() + "/Android/data/"
                        + context.getApplicationContext().getPackageName()
                        + "/Files/Images"
        );
        if (!fileImages.mkdirs()) {
            fileImages.mkdirs();
        }
        return fileImages.getAbsolutePath() + File.separator;
    }

}

