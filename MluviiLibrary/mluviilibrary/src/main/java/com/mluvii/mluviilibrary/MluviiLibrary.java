package com.mluvii.mluviilibrary;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import android.os.Handler;
import android.widget.Toast;

public class MluviiLibrary {

    /**
     * Pri prvnim volani naplnime hodnotou chatu - kvuli handlovani kliku na odkaz v chatu
     */
    private static String CHAT_URL;
    /**
     * Interface na volani Android library z webview
     */

    private static class InterfaceBetweenJavascriptAndLibrary{

        /**
         * Nastaveni stavu operatora na balicku zobrazenem na strance nacetle ve webview
         * @param value Operator status
         */
        @JavascriptInterface
        public void setOperatorStatus(String value) {
            Log.d("MLUVII_STATUS","Mluvii status changed to: "+value);
            if(value.equals("1") && onlineFunc != null){
                try {
                    onlineFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(value.equals("0") && offlineFunc != null){
                try {
                    offlineFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(value.equals("2") && busyFunc != null){
                try {
                    busyFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Volani zavreni chatu z webview
         */
        @JavascriptInterface
        public void closeChat(){
            Log.d("MLUVII_JAVASCRIPT","Close called");
            resetUrl();
            if (closeChatFunc != null) {
                try {
                    closeChatFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static WebView mluviiWebView = null;
    private static WebView mluviiVideoWebView = null;
    private static Callable<Void> onlineFunc = null;
    private static Callable<Void> offlineFunc = null;
    private static Callable<Void> busyFunc = null;
    private static Callable<Void> closeChatFunc = null;
    private static Callable<Void> chatLoaded = null;
    private static int REQUEST_SELECT_FILE = 65456;
    private static Uri mCapturedImageURI = null;

    private static ValueCallback<Uri[]>  valueCallbacks = null;
    private static ValueCallback<Uri> valueCallback;

    private static final String MLUVII_LIBRARY_LOG = "MLUVII_LIBRARY";

    private static String injectedString = " if(_close == null) {var _close = window.close; window.close = function (){ if(window['mluviiLibrary']){ console.log('ENDED'); window['mluviiLibrary'].closeChat(); } _close();}}";

    public static String getItFromMluviiLib() {
        return "ww";
    }

    /**
     * Nastaveni funkce, ktera se vola po nacteni strankz s chatem / Stanka otevrena po zavolani funkce runCHAT
     * @param function Function to call when chat page is loaded
     */
    public static void setChatLoadedCallback(Callable<Void> function){
        chatLoaded = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na online
     * @param function Function to call
     */
    public static void setStatusOnlineCallback(Callable<Void> function){
        onlineFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na Offline
     * @param function Function to call
     */
    public static void setStatusOfflineCallback(Callable<Void> function){
        offlineFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na busy
     * @param function Function to call
     */
    public static void setStatusBusyCallback(Callable<Void> function){
        busyFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zavreni chatu
     * @param function Function to call
     */
    public static void setCloseChatFunc(Callable<Void> function){
        closeChatFunc = function;
    }

    /**
     * Tato funkce vraci value callbacks potrebne pro nahravani souboru
     */
    public static ValueCallback<Uri[]> getFilePathCallbacks() { return valueCallbacks; }

    /**
     * Tato funkce vraci value callback potrebny pro nahravani souboru
     */
    public static ValueCallback<Uri> getFilePathCallback() { return valueCallback; }

    /**
     *  Nastaveni cisla, kterym se pozna pozadavek na sdileni souboru jdouci z mluvii library
     */
    public static void setSelectFileNumber(int number) { REQUEST_SELECT_FILE = number; }

    /**
     * Spusteni chatu na zatim skryte webview
     */
    public static void runChat(){
        if(Build.VERSION.SDK_INT  >= 19) {
            Log.d("MLUVII_SDK","Cool evaluate");
            mluviiWebView.evaluateJavascript("openChat()", null);
        } else {
            Log.d("MLUVII_SDK","Low evaluate");
            mluviiWebView.loadUrl("javascript: openChat()");
        }
    }

    public static void runVideo(){
        if(Build.VERSION.SDK_INT  >= 19) {
            Log.d("MLUVII_SDK","Cool evaluate");
            mluviiVideoWebView.evaluateJavascript("$owidget.openAppOnCurrentPage('av');", null);
        } else {
            Log.d("MLUVII_SDK","Low evaluate");
            mluviiVideoWebView.loadUrl("javascript: $owidget.openAppOnCurrentPage('av');");
        }
    }

    private static String createUrlString(String url, String companyId, String tenantId, String presetName, String language, String scope){
        StringBuilder builder = new StringBuilder();
        builder.append("https://");
        builder.append(url);
        builder.append("/MobileSdkWidget");
        builder.append("?c=");
        builder.append(companyId);
        if(tenantId != null) {
            builder.append("&t=");
            try {
                builder.append(URLEncoder.encode(tenantId,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(tenantId);
            }
        }
        if(language != null) {
            builder.append("&l=");
            try {
                builder.append(URLEncoder.encode(language,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(language);
            }
        }
        if(presetName != null){
            builder.append("&p=");
            try {
                builder.append(URLEncoder.encode(presetName,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(presetName);
            }
        }
        if(scope != null){
            builder.append("&s=");
            try {
                builder.append(URLEncoder.encode(scope, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(scope);
            }
        }
        return builder.toString();
    }

    public static void resetUrl(){
        Log.d("MLUVII_URL_LIB", "RESET_URL");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){

            @Override
            public void run(){
                Log.d("MLUVII_URL_RESET", "RESETTED_TO: "+CHAT_URL);
                mluviiWebView.loadUrl(CHAT_URL);

            }
        });
    }
    
    public static void addCustomData(String name, String value){
        String customDataString = "$owidget.addCustomData('"+name+"', '"+value+"')";
        if(Build.VERSION.SDK_INT  >= 19) {
            Log.d("MLUVII_SDK","Cool evaluate");
            mluviiWebView.evaluateJavascript(customDataString, null);
        } else {
            Log.d("MLUVII_SDK","Low evaluate");
            mluviiWebView.loadUrl("javascript: "+customDataString);
        }
    }

    /**
     * Function that returns webview optimized for mluvii widget
     * @param activity Activity for creating webview
     * @param url url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getMluviiWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language, String scope){
        if(mluviiWebView == null) {

            CHAT_URL = createUrlString(url,companyId,tenantId,presetName,language, scope);
            Log.d("MLUVII_URL", CHAT_URL);
            mluviiWebView = new WebView(activity);
            /**
             * Optimized WebviewClient without SSL ERROR to use it on localhost and not allowing to show url in native browser
             */
            mluviiWebView.setWebViewClient(new WebViewClient(){
                                               @Override
                                               public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                   /**
                                                    * Otevre chatove URL v okne chatu
                                                    */
                                                   Log.d("MLUVII_URL_CHANGE","changing url to "+url);
                                                   if(url.contains("GuestFrame?") || url.contains(CHAT_URL) || url.contains("widget") ) {
                                                       view.loadUrl(url);
                                                       return false;
                                                   } else {
                                                       /**
                                                        * Otevre jine URL (obrazek, file ...)
                                                        */
                                                       view.getContext().startActivity(
                                                               new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                                       return true;
                                                   }
                                               }

                                               @Override
                                               public void onPageFinished(WebView view, String url) {
                                                   if(Build.VERSION.SDK_INT  >= 19) {
                                                       mluviiWebView.evaluateJavascript(injectedString, null);
                                                   } else{
                                                       mluviiWebView.loadUrl("javascript: "+injectedString);
                                                   }
                                               }

                                           }
            );
            /**
             * Optimized WebChromeClient to Pass console messages and request permissions for camera, mic, etc. for future use
             */
            mluviiWebView.setWebChromeClient(new WebChromeClient(){

                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if(Build.VERSION.SDK_INT  >= 21) {
                        Log.d("MLUVII","Permission request granted");
                        request.grant(request.getResources());
                        //request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("MluviiConsole", "CNSL_MSG: "+consoleMessage.message());
                    return true;
                }

                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    // make sure there is no existing message
                    if(Build.VERSION.SDK_INT >= 21) {
                        valueCallbacks = filePathCallback;
                        Intent intent = fileChooserParams.createIntent();
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        try {
                            Log.d("MLUVII","OPEN UPLOAD");
                            activity.startActivityForResult(intent, REQUEST_SELECT_FILE);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    return true;
                }

                protected void openFileChooser(ValueCallback<Uri> filePathCallback)
                {
                    valueCallback = filePathCallback;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    valueCallback = uploadMsg;

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");

                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                    activity.startActivityForResult(chooserIntent, REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                            String acceptType, String capture) {
                    openFileChooser(uploadMsg, acceptType);
                }

            });

            WebSettings mWebSettings = mluviiWebView.getSettings();
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
            mluviiWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(),"mluviiLibrary");

            mluviiWebView.canGoBackOrForward(0);
            mluviiWebView.loadUrl(CHAT_URL);
        }
        return mluviiWebView;
    }

    /**
     * Function that returns webview optimized for mluvii widget and immediately opens chat window
     * @param activity Activity for creating webview
     * @param url url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getAndRunMluviiWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language, String scope){
        if(mluviiWebView == null) {
            CHAT_URL = createUrlString(url,companyId,tenantId,presetName,language, scope);
            Log.d("MLUVII_URL", CHAT_URL);
            mluviiWebView = new WebView(activity);
            /**
             * Optimized WebviewClient without SSL ERROR to use it on localhost and not allowing to show url in native browser
             */
            mluviiWebView.setWebViewClient(new WebViewClient(){
                                               @Override
                                               public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                   /**
                                                    * Otevre chatove URL v okne chatu
                                                    */
                                                   Log.d("MLUVII_URL_CHANGE","changing url to "+url);
                                                   if(url.contains("localhost")){
                                                       String changedUrl = url.replace("localhost","10.0.2.2");
                                                       Log.d("MLUVII_REPLACED_URL", changedUrl);
                                                       view.loadUrl(changedUrl);
                                                       return false;
                                                   }
                                                   if(url.contains("GuestFrame?") || url.contains(CHAT_URL) || url.contains("widget") ) {
                                                       view.loadUrl(url);
                                                       return false;
                                                   } else {
                                                       /**
                                                        * Otevre jine URL (obrazek, file ...)
                                                        */
                                                       view.getContext().startActivity(
                                                               new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                                       return true;
                                                   }
                                               }

                                               @Override
                                               public void onPageFinished(WebView view, String url) {
                                                   if(Build.VERSION.SDK_INT  >= 19) {
                                                       mluviiWebView.evaluateJavascript(injectedString, null);
                                                       runChat();
                                                   } else{
                                                       mluviiWebView.loadUrl("javascript: "+injectedString);
                                                       runChat();
                                                   }
                                               }

                                               @Override
                                               public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                                   handler.proceed(); // Ignore SSL certificate errors
                                               }
                                           }
            );

            /**
             * Optimized WebChromeClient to Pass console messages and request permissions for camera, mic, etc. for future use
             */
            mluviiWebView.setWebChromeClient(new WebChromeClient(){

                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if(Build.VERSION.SDK_INT  >= 21) {
                        Log.d("MLUVII","Permission request granted");
                        request.grant(request.getResources());
                        //request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("MluviiConsole", "CNSL_MSG: "+consoleMessage.message());
                    return true;
                }

                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    // make sure there is no existing message
                    if(Build.VERSION.SDK_INT >= 21) {
                        valueCallbacks = filePathCallback;

                        Intent intent = fileChooserParams.createIntent();
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        try {
                            Log.d("MLUVII","OPEN UPLOAD");
                            activity.startActivityForResult(intent, REQUEST_SELECT_FILE);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    return true;
                }

                protected void openFileChooser(ValueCallback<Uri> filePathCallback)
                {
                    valueCallback = filePathCallback;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    Log.d("MLUVII","OPEN UPLOAD");
                    activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    valueCallback = uploadMsg;

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");

                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                    // On select image call onActivityResult method of activity
                    activity.startActivityForResult(chooserIntent, REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                            String acceptType, String capture) {
                    Log.d("MLUVII","OPEN UPLOAD");
                    openFileChooser(uploadMsg, acceptType);
                }

            });

            WebSettings mWebSettings = mluviiWebView.getSettings();
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);

            mluviiWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(),"mluviiLibrary");

            mluviiWebView.canGoBackOrForward(0);
            mluviiWebView.loadUrl(CHAT_URL);
        }
        return mluviiWebView;
    }

    /**
     * Function that returns webview optimized for mluvii widget
     * @param activity Activity for creating webview
     * @param url url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getMluviiVideoWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language, String scope){
        if(mluviiVideoWebView == null) {
            CHAT_URL = createUrlString(url,companyId,tenantId,presetName,language, scope);
            Log.d("MLUVII_URL", CHAT_URL);
            mluviiVideoWebView = new WebView(activity);
            /**
             * Optimized WebviewClient without SSL ERROR to use it on localhost and not allowing to show url in native browser
             */
            mluviiVideoWebView.setWebViewClient(new WebViewClient(){
                                                    @Override
                                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                        /**
                                                         * Otevre chatove URL v okne chatu
                                                         */
                                                        Log.d("MLUVII_URL_CHANGE","changing url to "+url);
                                                        if(url.contains("GuestFrame?") || url.contains(CHAT_URL) || url.contains("widget") ) {
                                                            view.loadUrl(url);
                                                            return false;
                                                        } else {
                                                            /**
                                                             * Otevre jine URL (obrazek, file ...)
                                                             */
                                                            view.getContext().startActivity(
                                                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                                            return true;
                                                        }
                                                    }

                                                    @Override
                                                    public void onPageFinished(WebView view, String url) {
                                                        if(Build.VERSION.SDK_INT  >= 19) {
                                                            mluviiVideoWebView.evaluateJavascript(injectedString, null);
                                                        } else{
                                                            mluviiVideoWebView.loadUrl("javascript: "+injectedString);
                                                        }
                                                    }

                                                    @Override
                                                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                                        handler.proceed(); // Ignore SSL certificate errors
                                                    }

                                                }
            );
            /**
             * Optimized WebChromeClient to Pass console messages and request permissions for camera, mic, etc. for future use
             */
            mluviiVideoWebView.setWebChromeClient(new WebChromeClient(){

                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if(Build.VERSION.SDK_INT  >= 21) {
                        Log.d("MLUVII","Permission request granted, " +request.getResources());

                        request.grant(request.getResources());
                        //request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE, PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("MluviiConsole", "CNSL_MSG: "+consoleMessage.message());
                    return true;
                }

                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    // make sure there is no existing message
                    if(Build.VERSION.SDK_INT >= 21) {
                        valueCallbacks = filePathCallback;
                        Intent intent = fileChooserParams.createIntent();
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        try {
                            Log.d("MLUVII","OPEN UPLOAD");
                            activity.startActivityForResult(intent, REQUEST_SELECT_FILE);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    return true;
                }

                protected void openFileChooser(ValueCallback<Uri> filePathCallback)
                {
                    valueCallback = filePathCallback;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    valueCallback = uploadMsg;

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");

                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                    // On select image call onActivityResult method of activity
                    activity.startActivityForResult(chooserIntent, REQUEST_SELECT_FILE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                            String acceptType, String capture) {
                    openFileChooser(uploadMsg, acceptType);
                }

            });

            mluviiVideoWebView.setWebContentsDebuggingEnabled(true);

            WebSettings mWebSettings = mluviiVideoWebView.getSettings();
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);

            mluviiVideoWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(),"mluviiLibrary");

            mluviiVideoWebView.canGoBackOrForward(0);
            mluviiVideoWebView.loadUrl(CHAT_URL);
        }
        return mluviiVideoWebView;
    }

}
