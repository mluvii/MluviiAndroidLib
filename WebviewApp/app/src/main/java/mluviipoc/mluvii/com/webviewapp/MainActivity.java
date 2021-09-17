package mluviipoc.mluvii.com.webviewapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mluvii.mluviilibrary.MluviiLibrary;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MLUVII WEBVIEW APP";

    private static WebView mluviiWebView = null;


    /**
     * Inicializacni parametry pro Mluvii library webView
     */
    private static String mluviiServer = "app.mluvii.com";
    private static String mluviiCompanyId = "295b1064-cf5b-4a5d-9e05-e7a74f86ae5e";
    private static String mluviiTenantId = null;
    private static String mluviiPresetName = "MPSDK";
    private static String mluviiLanguageCode = null;
    private static String mluviiScope = null;
    private static Integer counter = 1;

    /**
     * Current stat of the operators group
     */
    private static int status = 0;
    /**
     * Customer button to indicate state of the operator and invole the webview
     */
    private Button btn = null;

    /**
     * CHAT_URL points on the chat widget. There can be more urls changed within the client app to direst
     * chat to different operator groups/different widgets.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    }
                });
                status = 2;
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

        counter++;
        /*if (counter % 2 == 0){
            mluviiPresetName = "MPSDK";
        } else {
            mluviiPresetName = "MPSDKQ";
        }*/

        mluviiWebView = MluviiLibrary.getMluviiWebView(this, mluviiServer,mluviiCompanyId, mluviiTenantId, mluviiPresetName, mluviiLanguageCode, mluviiScope);
        /**
         * Nastaveni velikosti 0,0 na webview, aby nebylo videt, dokud neni potreba
         */
        RelativeLayout.LayoutParams mluviiParams = new RelativeLayout.LayoutParams(0,0);
        mluviiWebView.setLayoutParams(mluviiParams);


        /**
         * Tlaciko, ktere zobrazuje stav Widgetu - Seda, nespojeno se serverem - Cervena, offline - Zluta, busy - Zelena, online
         */
        btn = new Button(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(status == 1){
                    MluviiLibrary.addCustomData("puvod", "ahoooj " + counter);
                MluviiLibrary.addCustomData("uvod", "ahoooj " + counter);
                MluviiLibrary.addCustomData("test", "ahoooj " + counter);
                    MluviiLibrary.runChat();

                    RelativeLayout.LayoutParams openedMluviiParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mluviiWebView.setLayoutParams(openedMluviiParams);
                //}
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(params1);
        linearLayout.addView(btn);

        final RelativeLayout layout = new RelativeLayout(this);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(linearLayout);
        layout.addView(mluviiWebView);
        layout.setLayoutParams(params);
        setContentView(layout);
    }
}

