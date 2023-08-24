package com.mml;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MmlModule extends MmlSpec {
    public static final String NAME = "Mml";

    private final ReactApplicationContext reactContext;
    private boolean getArtistFromSong = false;
    private boolean getDurationFromSong = true;
    private boolean getTitleFromSong = true;
    private boolean getIDFromSong = false;
    private boolean getCoverFromSong = false;
    private boolean getGenreFromSong = false;
    private boolean getAlbumFromSong = true;
    private boolean uri = true;
    private String fallback = "#fff";
    private boolean Checker = false;
    private Number pixelSpacing = 5;
    private int minimumSongDuration = 0;
    private int songsPerIteration = 0;
    private final int version = Build.VERSION.SDK_INT;
    private final boolean versionQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    private final String Per = version <= 28 ? Manifest.permission.READ_EXTERNAL_STORAGE : Manifest.permission.READ_MEDIA_AUDIO;

    public MmlModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getAll(ReadableMap options, Callback successCallback, Callback errorCallback) throws IOException {

        if (options.hasKey("artist")) getArtistFromSong = options.getBoolean("artist");
        if (options.hasKey("duration"))  getDurationFromSong = options.getBoolean("duration");
        if (options.hasKey("title")) getTitleFromSong = options.getBoolean("title");
        if (options.hasKey("id")) getIDFromSong = options.getBoolean("id");
        if (options.hasKey("cover")) getCoverFromSong = options.getBoolean("cover");
        if (options.hasKey("genre")) getGenreFromSong = options.getBoolean("genre");
        if (options.hasKey("album")) getAlbumFromSong = options.getBoolean("album");
        if (options.hasKey("check")) Checker = options.getBoolean("check");
        if (options.hasKey("batchNumber")) songsPerIteration = options.getInt("batchNumber");
        if (options.hasKey("uri")) uri = options.getBoolean("uri");
        if (options.hasKey("fallback")) fallback = options.getString("fallback");
        if(options.hasKey("pixelSpacing")) pixelSpacing = options.getInt("pixelSpacing");
        minimumSongDuration = options.hasKey("minimumSongDuration") && options.getInt("minimumSongDuration") > 0 ? options.getInt("minimumSongDuration") : 0;

        if (ContextCompat.checkSelfPermission(reactContext, Per) == PackageManager.PERMISSION_GRANTED) {
            if(version <= 19){
                getSongs(successCallback,errorCallback);
            }else{
                Thread bgThread = new Thread(null,
                        () -> {
                            try {
                                getSongs(successCallback,errorCallback);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, "asyncTask", 1024
                );
                bgThread.start();
            }
        } else {
            successCallback.invoke("No Permission");
        }
    }

    private void getSongs(final Callback successCallback, final Callback errorCallback) throws IOException {
        ContentResolver musicResolver = Objects.requireNonNull(getCurrentActivity()).getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        if(minimumSongDuration > 0){
            selection += " AND " + MediaStore.Audio.Media.DURATION + " >= " + minimumSongDuration;
        }
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, sortOrder);
        int pointer = 0;
        if (musicCursor != null && musicCursor.moveToFirst()) {
            if (musicCursor.getCount() > 0) {
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap items;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                try {
                    do {
                        try {
                            long songId = musicCursor.getLong(idColumn);
                            if(!Checker) {
                                items = new WritableNativeMap();
                                if (getIDFromSong) {
                                    String str = String.valueOf(songId);
                                    items.putString("id", str);
                                }
                                String songPath = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                                if (songPath != null && !songPath.equals("")) {
                                    String fileName = songPath.substring(songPath.lastIndexOf("/") + 1);
                                    items.putString("url", songPath);
                                    items.putString("fileName", fileName);
                                    mmr.setDataSource(songPath);
//                                int songIntDuration = Integer.parseInt(songTimeDuration);
                                    items.putString("album", getAlbumFromSong ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) : null);
                                    items.putString("author", getArtistFromSong ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) : null);
                                    items.putString("title", getTitleFromSong ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) : null);
                                    items.putString("genre", getGenreFromSong ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) : null);
                                    items.putString("duration", getDurationFromSong ? mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) : null);
                                    if (getCoverFromSong) { //&& !file.exists()
                                        try {
                                            byte[] albumImageData = mmr.getEmbeddedPicture();
                                            if (albumImageData != null) {
                                                Bitmap songImage = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                                                if (songImage != null) {
                                                    String coverName = "q" + songId + ".jpg";
                                                    String pathToImg = versionQ ? Objects.requireNonNull(getCurrentActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)).getAbsolutePath() : getCurrentActivity().getDir("images", Context.MODE_PRIVATE).getAbsolutePath();
                                                    File file = new File(pathToImg, coverName);
                                                    if(!file.exists()){ FileManager.FileSaver(file, songImage); }
                                                    items.putString("cover", "file://" + pathToImg + "/" + coverName);
                                                    if (uri) {
                                                        try {
                                                            WritableMap Picker = ImageColorPicker.getColor(fallback, pixelSpacing, songImage);
                                                            items.putMap("ColorPicker", Picker);
                                                        } catch (Exception e) {
                                                            errorCallback.invoke(e.getMessage());
                                                        }
                                                    } else {
                                                        items.putNull("ColorPicker");
                                                    }
                                                } else {
                                                    items.putNull("ColorPicker");
                                                    items.putNull("cover");
                                                }
                                            } else {
                                                items.putNull("cover");
                                            }
                                        } catch (Exception e) {
                                            errorCallback.invoke("Error No embed image!!");
                                        }
                                    }
//                                    else {
//                                        items.putString("cover", "file://" + pathToImg + "/" + coverName);
//                                    }
                                    jsonArray.pushMap(items);

                                    if (songsPerIteration > 0) {
                                        if (songsPerIteration > musicCursor.getCount()) {
                                            if (pointer == (musicCursor.getCount() - 1)) {
                                                WritableMap params = Arguments.createMap();
                                                params.putArray("batch", jsonArray);
                                                sendEvent(reactContext, "onBatchReceived", params);
                                            }
                                        } else {
                                            if (songsPerIteration == jsonArray.size()) {
                                                WritableMap params = Arguments.createMap();
                                                params.putArray("batch", jsonArray);
                                                sendEvent(reactContext, "onBatchReceived", params);
                                                jsonArray = new WritableNativeArray();
                                            } else if (pointer == (musicCursor.getCount() - 1)) {
                                                WritableMap params = Arguments.createMap();
                                                params.putArray("batch", jsonArray);
                                                sendEvent(reactContext, "onBatchReceived", params);
                                            }
                                        }
                                        pointer++;
                                    }
                                }
                            }else{
                                jsonArray.pushInt((int) songId);
                            }
                        } catch (Exception e) {
                            // An error in one message should not prevent from getting the rest
                            // There are cases when a corrupted file can't be read and a RuntimeException is raised
                            // Let's discuss how to deal with these kind of exceptions
                            // This song will be ignored, and incremented the pointer in order to this plugin work
                            pointer++;
                            continue; // This is redundant, but adds meaning
                        }
                    } while (musicCursor.moveToNext());
                    if (songsPerIteration == 0) {
                        successCallback.invoke(jsonArray);
                    }
                } catch (RuntimeException e) {
                    errorCallback.invoke(e.toString());
                } catch (Exception e) {
                    errorCallback.invoke(e.getMessage());
                } finally {
                    mmr.release();
                }
            }else{
                errorCallback.invoke("Error, you dont' have any songs");
            }
        }else{
            errorCallback.invoke("Something get wrong with musicCursor");
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}