package mluviipoc.mluvii.com.webviewapp;

import android.net.Uri;
import android.webkit.ValueCallback;

public interface FileChooserInterface {
    void fileChooser(ValueCallback<Uri[]> filePathCallback);
}
