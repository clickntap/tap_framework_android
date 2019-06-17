package com.clickntap.tap;

import android.content.Intent;

public interface TapCallbackManager {
    public boolean onActivityResult(int requestCode, int resultCode, Intent data);
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    public static class Factory {
        public static TapCallbackManager create() {
            return new TapCallbackManagerImpl();
        }
    }
}
