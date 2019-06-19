package com.clickntap.tap;

import android.content.Intent;

public interface TapCallbackManager {
    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    class Factory {
        public static TapCallbackManager create() {
            return new TapCallbackManagerImpl();
        }
    }
}
