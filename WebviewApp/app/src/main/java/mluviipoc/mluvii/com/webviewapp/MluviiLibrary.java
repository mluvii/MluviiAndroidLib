package mluviipoc.mluvii.com.webviewapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public class MluviiLibrary {

    private static final String injectedString = "var _close = window.close; window.close = function (){ if(window['mluviiLibrary']){ window['mluviiLibrary'].closeChat(); } _close();}; var mluviiEventHandler = function(event,sessionId){if(window['mluviiLibrary']){ window['mluviiLibrary'].mluviiEvent(event,sessionId); }}; window.mluviiEventHandler = mluviiEventHandler;";
    private static final ValueCallback<Uri[]> valueCallbacks = null;
    public static int REQUEST_SELECT_FILE = 65456;
    static Uri mCameraPhotoPath;
    /**
     * Pri prvnim volani naplnime hodnotou chatu - kvuli handlovani kliku na odkaz v chatu
     */
    private static String CHAT_URL;
    private static WebView mluviiWebView = null;
    private static WebView mluviiVideoWebView = null;
    private static Callable<Void> onlineFunc = null;
    private static Callable<Void> offlineFunc = null;
    private static Callable<Void> busyFunc = null;
    private static Callable<Void> closeChatFunc = null;
    private static Callable<Void> chatLoaded = null;
    private static Callable<Void> paramSet = null;
    private static UrlCallback urlCallback = null;
    private static MluviiEventCallback mluviiEventCallback = null;
    private static File mTempPhotoFile;
    private static Uri cameraPhotoUri;

    /**
     * Nastaveni funkce, ktera se vola po nacteni stranky s chatem / Stanka otevrena po zavolani funkce runCHAT
     *
     * @param function Function to call when chat page is loaded
     */
    public static void setChatLoadedCallback(Callable<Void> function) {
        chatLoaded = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na online
     *
     * @param function Function to call
     */
    public static void setStatusOnlineCallback(Callable<Void> function) {
        onlineFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na Offline
     *
     * @param function Function to call
     */
    public static void setStatusOfflineCallback(Callable<Void> function) {
        offlineFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zmene stavu na busy
     *
     * @param function Function to call
     */
    public static void setStatusBusyCallback(Callable<Void> function) {
        busyFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri zavreni chatu
     *
     * @param function Function to call
     */
    public static void setCloseChatFunc(Callable<Void> function) {
        closeChatFunc = function;
    }

    /**
     * Nastaveni funkce, ktera se vola pri pokusu o otevreni odkazu
     *
     * @param function Function to call
     */
    public static void setUrlCallbackFunc(UrlCallback function) {
        urlCallback = function;
    }

    public static void setParamSetFunc(UrlCallback function) {
        paramSet = function;
    }

    public static void setMluviiEventCallbackFunc(MluviiEventCallback function) {
        mluviiEventCallback = function;
    }

    /**
     * Tato funkce vraci value callbacks potrebne pro nahravani souboru
     */
    public static ValueCallback<Uri[]> getFilePathCallbacks() {
        return valueCallbacks;
    }

    public static Uri getCameraCaptureUri() {
        return cameraPhotoUri;
    }

    /**
     * Tato funkce vraci value callback potrebny pro nahravani souboru
     */
    public static ValueCallback<Uri[]> getFilePathCallback() {
        return valueCallbacks;
    }

    public static Uri getCameraUriCallback() {
        return cameraPhotoUri;
    }

    /**
     * Nastaveni cisla, kterym se pozna pozadavek na sdileni souboru jdouci z mluvii library
     */
    public static void setSelectFileNumber(int number) {
        REQUEST_SELECT_FILE = number;
    }

    /**
     * Spusteni chatu na zatim skyte webview
     */
    public static void runChat() {
        Log.d("MLUVII_SDK", "Cool evaluate");
        mluviiWebView.evaluateJavascript("openChat()", null);
    }

    public static void runVideo() {
        if (Build.VERSION.SDK_INT >= 19) {
            Log.d("MLUVII_SDK", "Cool evaluate");
            mluviiVideoWebView.evaluateJavascript("$owidget.openAppForSDK('av');", null);
        } else {
            Log.d("MLUVII_SDK", "Low evaluate");
            mluviiVideoWebView.loadUrl("javascript: $owidget.openAppForSDK('av');");
        }
    }

    private static String createUrlString(String url, String companyId, String tenantId, String presetName, String language) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://");
        builder.append(url);
        builder.append("/MobileSdkWidget");
        builder.append("?c=");
        builder.append(companyId);
        if (tenantId != null) {
            builder.append("&t=");
            try {
                builder.append(URLEncoder.encode(tenantId, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(tenantId);
            }
        }
        if (language != null) {
            builder.append("&l=");
            try {
                builder.append(URLEncoder.encode(language, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(language);
            }
        }
        if (presetName != null) {
            builder.append("&p=");
            try {
                builder.append(URLEncoder.encode(presetName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(presetName);
            }
        }
        return builder.toString();
    }

    private static String createUrlString(String url, String companyId, String tenantId, String presetName, String language, String scope) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://");
        builder.append(url);
        builder.append("/MobileSdkWidget");
        builder.append("?c=");
        builder.append(companyId);
        if (tenantId != null) {
            builder.append("&t=");
            try {
                builder.append(URLEncoder.encode(tenantId, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(tenantId);
            }
        }
        if (language != null) {
            builder.append("&l=");
            try {
                builder.append(URLEncoder.encode(language, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(language);
            }
        }
        if (presetName != null) {
            builder.append("&p=");
            try {
                builder.append(URLEncoder.encode(presetName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(presetName);
            }
        }
        if (scope != null) {
            builder.append("&s=");
            try {
                builder.append(URLEncoder.encode(scope, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(scope);
            }
        }
        return builder.toString();
    }

    public static void resetUrl() {
        Log.d("MLUVII_URL_LIB", "RESET_URL");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Log.d("MLUVII_URL_RESET", "RESETTED_TO: " + CHAT_URL);
                if (mluviiWebView != null) {
                    mluviiWebView.loadUrl(CHAT_URL);
                }
                if (mluviiVideoWebView != null) {
                    mluviiVideoWebView.loadUrl(CHAT_URL);
                }

            }
        });
    }

    public static void returnDataFromJs(String data) {
        if (paramSet != null) {
            try {
                paramSet.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addCustomData(String name, String value) {
        String customDataString = "$owidget.addCustomData(\"" + name + "\",\"" + value + "\")";
        if (Build.VERSION.SDK_INT >= 19) {
            Log.d("MLUVII_SDK", "Cool evaluate" + customDataString);
            if (mluviiWebView != null) {
                mluviiWebView.evaluateJavascript(customDataString, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        returnDataFromJs(s);
                    }
                });
            }
            if (mluviiVideoWebView != null) {
                mluviiVideoWebView.evaluateJavascript(customDataString, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        returnDataFromJs(s);
                    }
                });
            }
        } else {
            Log.d("MLUVII_SDK", "Low evaluate");
            if (mluviiWebView != null) {
                mluviiWebView.loadUrl("javascript: " + customDataString);
            }
            if (mluviiVideoWebView != null) {
                mluviiVideoWebView.loadUrl("javascript: " + customDataString);
            }
        }
    }


    /**
     * Function that returns webview optimized for mluvii widget and immediately opens chat window
     *
     * @param activity Activity for creating webview
     * @param url      url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getAndRunMluviiWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language, FileChooserInterface mFileChooserInterface) {
        if (mluviiWebView == null) {
            CHAT_URL = createUrlString(url, companyId, tenantId, presetName, language);
            Log.d("MLUVII_URL", CHAT_URL);
            mluviiWebView = new WebView(activity);
            /**
             * Optimized WebviewClient without SSL ERROR to use it on localhost and not allowing to show url in native browser
             */
            WebSettings mWebSettings = mluviiWebView.getSettings();
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
            mluviiWebView.setWebViewClient(new WebViewClient() {
                                               @Override
                                               public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                   /**
                                                    * Otevre chatove URL v okne chatu
                                                    */
                                                   Log.d("MLUVII_URL_CHANGE", "changing url to " + url);
                                                   if (url.contains("localhost")) {
                                                       String changedUrl = url.replace("localhost", "10.0.2.2");
                                                       Log.d("MLUVII_REPLACED_URL", changedUrl);
                                                       view.loadUrl(changedUrl);
                                                       return false;
                                                   }
                                                   if (url.contains("GuestFrame?") || url.contains(CHAT_URL) || url.contains("widget")) {
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
                                                   if (Build.VERSION.SDK_INT >= 19) {
                                                       mluviiWebView.evaluateJavascript(injectedString, null);
                                                       runChat();
                                                   } else {
                                                       mluviiWebView.loadUrl("javascript: " + injectedString);
                                                       runChat();
                                                   }
                                               }
                                           }
            );

            /**
             * Optimized WebChromeClient to Pass console messages and request permissions for camera, mic, etc. for future use
             */
            mluviiWebView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        Log.d("MLUVII", "Permission request granted");
                        request.grant(request.getResources());
                        //request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("MluviiConsole", "CNSL_MSG: " + consoleMessage.message());
                    return true;
                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    mFileChooserInterface.fileChooser(filePathCallback);
                    return true;
                }
            });


            mluviiWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(), "mluviiLibrary");

            mluviiWebView.canGoBackOrForward(0);
            mluviiWebView.loadUrl(CHAT_URL);
        }
        return mluviiWebView;
    }

    /**
     * Function that returns webview optimized for mluvii widget
     *
     * @param activity Activity for creating webview
     * @param url      url to load in webview
     * @return WebView with loaded page with URL from param
     */
    public static WebView getMluviiVideoWebView(final Activity activity, String url, String companyId, String tenantId, String presetName, String language, FileChooserInterface mFileChooserInterface) {
        if (mluviiVideoWebView == null) {
            CHAT_URL = createUrlString(url, companyId, tenantId, presetName, language);
            Log.d("MLUVII_URL", CHAT_URL);
            mluviiVideoWebView = new WebView(activity);
            /**
             * Optimized WebviewClient without SSL ERROR to use it on localhost and not allowing to show url in native browser
             */
            WebView.setWebContentsDebuggingEnabled(true);

            WebSettings mWebSettings = mluviiVideoWebView.getSettings();
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);
            mluviiVideoWebView.setWebViewClient(new WebViewClient() {
                                                    @Override
                                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                        /**
                                                         * Otevre chatove URL v okne chatu
                                                         */
                                                        Log.d("MLUVII_URL_CHANGE", "changing url to " + url);
                                                        if (url.contains("GuestFrame?") || url.contains(CHAT_URL) || url.contains("widget")) {
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
                                                        if (Build.VERSION.SDK_INT >= 19) {
                                                            mluviiVideoWebView.evaluateJavascript(injectedString, null);
                                                        } else {
                                                            mluviiVideoWebView.loadUrl("javascript: " + injectedString);
                                                        }
                                                    }
                                                }
            );
            /**
             * Optimized WebChromeClient to Pass console messages and request permissions for camera, mic, etc. for future use
             */
            mluviiVideoWebView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        Log.d("MLUVII", "Permission request granted, " + request.getResources());

                        //request.grant(request.getResources());
                        request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE, PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    }
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("MluviiConsole", "CNSL_MSG: " + consoleMessage.message());
                    return true;
                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    mFileChooserInterface.fileChooser(filePathCallback);
                    return true;
                }

            });


            mluviiVideoWebView.addJavascriptInterface(new InterfaceBetweenJavascriptAndLibrary(), "mluviiLibrary");

            mluviiVideoWebView.canGoBackOrForward(0);
            mluviiVideoWebView.loadUrl(CHAT_URL);
        }
        return mluviiVideoWebView;
    }


    /**
     * Interface na volani Android library z webview
     */

    private static class InterfaceBetweenJavascriptAndLibrary {

        /**
         * Nastaveni stavu operatora na balicku zobrazenem na strance nacetle ve webview
         *
         * @param value Operator status
         */
        @JavascriptInterface
        public void setOperatorStatus(String value) {
            Log.d("MLUVII_STATUS", "Mluvii status changed to: " + value);
            if (value.equals("1") && onlineFunc != null) {
                try {
                    onlineFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (value.equals("0") && offlineFunc != null) {
                try {
                    offlineFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (value.equals("2") && busyFunc != null) {
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
        public void closeChat() {
            Log.d("MLUVII_JAVASCRIPT", "Close called");
            resetUrl();
            if (closeChatFunc != null) {
                try {
                    closeChatFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Volani zavreni chatu z webview
         */
        @JavascriptInterface
        public void mluviiEvent(String event, long sessionId) {
            Log.d("MLUVII_JAVASCRIPT", "Close called");
            if (mluviiEventCallback != null) {
                mluviiEventCallback.Event = event;
                mluviiEventCallback.SessionId = sessionId;
                try {
                    mluviiEventCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class MluviiEventCallback implements Callable<Void> {
        public String Event;
        public long SessionId;

        @Override
        public Void call() throws Exception {
            return null;
        }
    }

    public static class UrlCallback implements Callable<Void> {
        public String url;

        @Override
        public Void call() throws Exception {
            return null;
        }
    }

}
