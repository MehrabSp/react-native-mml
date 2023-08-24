package com.mml;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
// import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import java.io.IOException;

abstract class MmlSpec extends ReactContextBaseJavaModule {
  MmlSpec(ReactApplicationContext context) {
    super(context);
  }

  public abstract void getAll(ReadableMap options, Callback successCallback, Callback errorCallback) throws IOException;
}
