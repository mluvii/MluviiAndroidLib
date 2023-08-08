
MluviiLibrary SDK for Android
=============================

Introduction
------------

MluviiLibrary is an Android library for embedding communication functionalities in Android applications. It allows developers to integrate both chat and audio/video (A/V) communication within their apps. The following instructions guide you through the process of setting up and using the SDK in your application.

Features
--------

-   Live chat integration
-   Operator status handling
-   A/V communication
-   Customizable callbacks for various states such as online, offline, busy
-   URL handling within the chat
-   Inclusion of custom data
-   SSL support
-   Optimized WebView creation for mluvii widget

Installation
------------

Add the MluviiLibrary SDK package to your Android project.

Usage
-----

### Initialization

1.  Create a WebView for Mluvii widget: You can get an optimized WebView by calling `getMluviiWebView()` method.

### Setting Callbacks

You can set up various callbacks to handle different states or events:

-   `setStatusOnlineCallback(Callable<Void> function)`: To handle when the operator goes online.
-   `setStatusOfflineCallback(Callable<Void> function)`: To handle when the operator goes offline.
-   `setStatusBusyCallback(Callable<Void> function)`: To handle when the operator is busy.
-   `setCloseChatFunc(Callable<Void> function)`: To handle when the chat is closed.
-   `setChatLoadedCallback(Callable<Void> function)`: To handle when the chat page is loaded.
-   `setUrlCallbackFunc(UrlCallback function)`: To handle URL changes within the chat.

### Running the Chat

To run the chat, call the `runChat()` method.

### Adding Custom Data

To add custom data, use the `addCustomData(String name, String value)` method.

### Resetting URL

If you need to reset the URL to its initial value, call the `resetUrl()` method.

Examples
--------

### Creating a WebView for Mluvii widget:

javaCopy code

`WebView mluviiWebView = MluviiLibrary.getMluviiWebView(activity, url, companyId, tenantId, presetName, language, scope);`

### Setting a Callback for Online Status:

javaCopy code

`MluviiLibrary.setStatusOnlineCallback(new Callable<Void>() {
    public Void call() {
        // Handle online status
        return null;
    }
});`

Permissions
-----------

Make sure you grant the necessary permissions for camera, microphone, etc., in your app's manifest file.

Support and Contributions
-------------------------

For any issues, questions, or contributions, please refer to the official documentation or contact the support team.

License
-------

Please see the license file included with this SDK for details on redistribution and usage.

* * * * *

Note: Make sure to customize the installation, usage, and examples based on your specific requirements and integration details.
