package com.mluvii.mluviilibrary;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import android.os.Handler;

/**
 * Created by jaros on 2/23/2018.
 */

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

    private static String injectedString = "var _close = window.close; window.close = function (){ if(window['mluviiLibrary']){ window['mluviiLibrary'].closeChat(); } _close();}";

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
     * Spusteni chatu na zatim skyte webview
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

    private static String createUrlString(String url, String companyId, String tenantId, String presetName, String language){
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


    /**
     * Function that returns webview optimized for mluvii widget
     * @param activity Activity for creating webview
     * @param url url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getMluviiWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language){
        if(mluviiWebView == null) {
            CHAT_URL = createUrlString(url,companyId,tenantId,presetName,language);
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
            });

            mluviiWebView.getSettings().setJavaScriptEnabled(true);
            mluviiWebView.getSettings().setDomStorageEnabled(true);
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
    public static WebView getMluviiVideoWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language){
        if(mluviiVideoWebView == null) {
            CHAT_URL = createUrlString(url,companyId,tenantId,presetName,language);
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
            });

            mluviiVideoWebView.getSettings().setJavaScriptEnabled(true);
            mluviiVideoWebView.getSettings().setDomStorageEnabled(true);
            mluviiVideoWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(),"mluviiLibrary");

            mluviiVideoWebView.canGoBackOrForward(0);
            mluviiVideoWebView.loadUrl(CHAT_URL);
        }
        return mluviiVideoWebView;
    }

}
