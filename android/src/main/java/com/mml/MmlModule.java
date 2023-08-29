package com.mml;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
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

    public static int quality = 100;
    private final ReactApplicationContext reactContext;
    private boolean getArtistFromSong = false;
    private boolean getDurationFromSong = true;
    private boolean getTitleFromSong = true;
    private boolean getIDFromSong = false;
    private boolean getCoverFromSong = false;
    private boolean getGenreFromSong = false;
    private boolean getAlbumFromSong = true;
    private boolean uri = true;
    public static String fallback = "#fff";
    private boolean Checker = false;
    private Number pixelSpacing = 5;
    private int minimumSongDuration = 0;
    private int songsPerIteration = 0;
    private final int version = Build.VERSION.SDK_INT;
    private final boolean androidLow = version <= 19;
    private final boolean versionQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    public MmlModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    /**
     * Callback is used for asynchronous use and sending multiple messages
     * I don't use Promises for that
     * In the next updates, group sending will be added
     * */
    @ReactMethod
    public void getAll(ReadableMap options, String Type, final Callback successCallback, final Callback errorCallback) throws IOException {

        String perResult;
        final String RMI = Manifest.permission.READ_MEDIA_IMAGES, RMA = Manifest.permission.READ_MEDIA_AUDIO, RMV = Manifest.permission.READ_MEDIA_VIDEO, RES = Manifest.permission.READ_EXTERNAL_STORAGE;
//        WritableMap params = Arguments.createMap();
//        params.putString("eventProperty", "someValue");
//        WritableMap params2 = Arguments.createMap();
//        params2.putString("eventProperty", "someValue2");
//        WritableMap params3 = Arguments.createMap();
//        params3.putString("eventProperty", "someValue3");
        switch (Type) { // Objects.requireNonNull(Type)
            case "Images":
                perResult = RMI;
//                sendEvent(reactContext, "EventReminder", params);
//                sendEvent(reactContext, "EventReminder", params2);
//                sendEvent(reactContext, "EventReminder", params3);
                break;
            case "Videos":
                perResult = RMV;
//                sendEvent(reactContext, "EventReminder", params);
                break;
            case "Musics":
                perResult = RMA;
//                sendEvent(reactContext, "EventReminder", params);
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + Type);
        }
        final String Per = version <= 28 ? RES : perResult;
        if (ContextCompat.checkSelfPermission(reactContext, Per) == PackageManager.PERMISSION_GRANTED) {

            if (options.hasKey("artist")) getArtistFromSong = options.getBoolean("artist");
            if (options.hasKey("duration")) getDurationFromSong = options.getBoolean("duration");
            if (options.hasKey("title")) getTitleFromSong = options.getBoolean("title");
            if (options.hasKey("id")) getIDFromSong = options.getBoolean("id");
            if (options.hasKey("cover")) getCoverFromSong = options.getBoolean("cover");
            if (options.hasKey("genre")) getGenreFromSong = options.getBoolean("genre");
            if (options.hasKey("album")) getAlbumFromSong = options.getBoolean("album");
            if (options.hasKey("check")) Checker = options.getBoolean("check");
            if (options.hasKey("batchNumber")) songsPerIteration = options.getInt("batchNumber");
            if (options.hasKey("uri")) uri = options.getBoolean("uri");
            if (options.hasKey("fallback")) fallback = options.getString("fallback");
            if (options.hasKey("pixelSpacing")) pixelSpacing = options.getInt("pixelSpacing");
            if (options.hasKey("quality")) quality = options.getInt("quality");
            minimumSongDuration = options.hasKey("minimumSongDuration") && options.getInt("minimumSongDuration") > 0 ? options.getInt("minimumSongDuration") : 0;

            Thread bgThread;
            switch (Type) {
                case "Images":
                    if (androidLow) {
                        getImages(successCallback, errorCallback);
                    } else {
                        bgThread = new Thread(null,
                                () -> {
                                    try {
                                        getImages(successCallback, errorCallback);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, "asyncTask", 1024
                        );
                        bgThread.start();
                    }
                    break;
                case "Videos":
                    if (androidLow) {
                        getVideos(successCallback, errorCallback);
                    } else {
                        bgThread = new Thread(null,
                                () -> {
                                    try {
                                        getVideos(successCallback, errorCallback);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, "asyncTask", 1024
                        );
                        bgThread.start();
                    }
                    break;
                case "Musics":
                    if (androidLow) {
                        getSongs(successCallback, errorCallback);
                    } else {
                        bgThread = new Thread(null,
                                () -> {
                                    try {
                                        getSongs(successCallback, errorCallback);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, "asyncTask", 1024
                        );
                        bgThread.start();
                    }
                    break;
                default:
                    successCallback.invoke("No type");
            }
        } else {
            errorCallback.invoke("none");
        }
    }

    private void getImages(final Callback successCallback, final Callback errorCallback) throws IOException {
        ContentResolver Resolver = Objects.requireNonNull(getCurrentActivity()).getContentResolver();
        Uri musicUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.DESCRIPTION,
                MediaStore.Images.Media.MIME_TYPE
        };
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Cursor cursor = Resolver.query(musicUri, projection, null, null, sortOrder);
        try (cursor) {
            int pointer = 0;
            assert cursor != null;
            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap items;
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                do {
                    try {
                        long Id = cursor.getLong(idColumn);
                        items = new WritableNativeMap();
                        if (getIDFromSong) {
                            String str = String.valueOf(Id);
                            items.putString("id", str);
                        }
                        String songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        long BUCKET_DISPLAY_NAME = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        long DATE_ADDED = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                        long DATE_TAKEN = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                        long DATE_MODIFIED = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                        long SIZE = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                        long HEIGHT = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                        long WIDTH = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                        long LATITUDE = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE));
                        long LONGITUDE = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE));
                        long ORIENTATION = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
                        long DESCRIPTION = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION));
                        long MIME_TYPE = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                        if (songPath != null && !songPath.equals("")) {
                            String fileName = songPath.substring(songPath.lastIndexOf("/") + 1);
                            items.putString("url", songPath);
                            items.putString("fileName", fileName);
                            items.putString("BUCKET_DISPLAY_NAME", String.valueOf(BUCKET_DISPLAY_NAME));
                            items.putString("DATE_ADDED", String.valueOf(DATE_ADDED));
                            items.putString("DATE_TAKEN", String.valueOf(DATE_TAKEN));
                            items.putString("DATE_MODIFIED", String.valueOf(DATE_MODIFIED));
                            items.putString("SIZE", String.valueOf(SIZE));
                            items.putString("HEIGHT", String.valueOf(HEIGHT));
                            items.putString("WIDTH", String.valueOf(WIDTH));
                            items.putString("LATITUDE", String.valueOf(LATITUDE));
                            items.putString("LONGITUDE", String.valueOf(LONGITUDE));
                            items.putString("ORIENTATION", String.valueOf(ORIENTATION));
                            items.putString("DESCRIPTION", String.valueOf(DESCRIPTION));
                            items.putString("MIME_TYPE", String.valueOf(MIME_TYPE));
                            jsonArray.pushMap(items);

                            if (songsPerIteration > 0) {
                                if (songsPerIteration > cursor.getCount()) {
                                    if (pointer == (cursor.getCount() - 1)) {
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
                                    } else if (pointer == (cursor.getCount() - 1)) {
                                        WritableMap params = Arguments.createMap();
                                        params.putArray("batch", jsonArray);
                                        sendEvent(reactContext, "onBatchReceived", params);
                                    }
                                }
                                pointer++;
                            }
                        }
                    } catch (Exception e) {
                        // An error in one message should not prevent from getting the rest
                        // There are cases when a corrupted file can't be read and a RuntimeException is raised
                        // Let's discuss how to deal with these kind of exceptions
                        // This song will be ignored, and incremented the pointer in order to this plugin work
                        pointer++;
                        continue; // This is redundant, but adds meaning
                    }
                } while (cursor.moveToNext());
                if (songsPerIteration == 0) {
                    successCallback.invoke(jsonArray);
                }
            } else {
                errorCallback.invoke((Object) null);
            }
        } catch (Exception e) {
            errorCallback.invoke("Error: " + e);
        }
    }

    private void getVideos(final Callback successCallback, final Callback errorCallback) throws IOException {
        ContentResolver Resolver = Objects.requireNonNull(getCurrentActivity()).getContentResolver();
        Uri Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.RESOLUTION
        };
        Cursor cursor = Resolver.query(Uri, projection, null, null, null);
        try (cursor) {
            int pointer = 0;
            assert cursor != null;
            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap items;
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
                do {
                    try {
                        long Id = cursor.getLong(idColumn);

                        items = new WritableNativeMap();
                        if (getIDFromSong) {
                            String str = String.valueOf(Id);
                            items.putString("id", str);
                        }
                        String songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                        long DISPLAY_NAME = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                        long DURATION = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        long DATE_TAKEN = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                        long SIZE = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                        long RESOLUTION = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
                        if (songPath != null && !songPath.equals("")) {

                            String fileName = songPath.substring(songPath.lastIndexOf("/") + 1);
                            items.putString("url", songPath);
                            items.putString("fileName", fileName);
                            items.putString("DISPLAY_NAME", String.valueOf(DISPLAY_NAME));
                            items.putString("DURATION", String.valueOf(DURATION));
                            items.putString("DATE_TAKEN", String.valueOf(DATE_TAKEN));
                            items.putString("SIZE", String.valueOf(SIZE));
                            items.putString("RESOLUTION", String.valueOf(RESOLUTION));

                            jsonArray.pushMap(items);

                            if (songsPerIteration > 0) {
                                if (songsPerIteration > cursor.getCount()) {
                                    if (pointer == (cursor.getCount() - 1)) {
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
                                    } else if (pointer == (cursor.getCount() - 1)) {
                                        WritableMap params = Arguments.createMap();
                                        params.putArray("batch", jsonArray);
                                        sendEvent(reactContext, "onBatchReceived", params);
                                    }
                                }
                                pointer++;
                            }
                        }
                    } catch (Exception e) {
                        // An error in one message should not prevent from getting the rest
                        // There are cases when a corrupted file can't be read and a RuntimeException is raised
                        // Let's discuss how to deal with these kind of exceptions
                        // This song will be ignored, and incremented the pointer in order to this plugin work
                        pointer++;
                        continue; // This is redundant, but adds meaning
                    }
                } while (cursor.moveToNext());
                if (songsPerIteration == 0) {
                    successCallback.invoke(jsonArray);
                }
            } else {
                errorCallback.invoke((Object) null);
            }
        } catch (Exception e) {
            errorCallback.invoke("Error: " + e);
        }
    }

    private void getSongs(final Callback successCallback, final Callback errorCallback) throws IOException {
        ContentResolver musicResolver = Objects.requireNonNull(getCurrentActivity()).getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        if (minimumSongDuration > 0) {
            selection += " AND " + MediaStore.Audio.Media.DURATION + " >= " + minimumSongDuration;
        }
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, sortOrder);
        try (musicCursor) {
            int pointer = 0;
            if (musicCursor != null && musicCursor.moveToFirst()) {
                if (musicCursor.getCount() > 0) {
                    WritableArray jsonArray = new WritableNativeArray();
                    WritableMap items;
//                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    final MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                    try {
                        do {
                            try {
                                long songId = musicCursor.getLong(idColumn);
                                if (!Checker) {
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
                                                        if (!file.exists()) {
                                                            FileManager.FileSaver(file, songImage);
                                                        }
                                                        items.putString("cover", "file://" + pathToImg + "/" + coverName);
                                                        if (uri) {
                                                            try {
                                                                WritableMap Picker = ImageColorPicker.getColor(pixelSpacing, songImage);
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
                                } else {
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
                } else {
                    errorCallback.invoke("Error, you dont' have any songs");
                }
            } else {
                errorCallback.invoke("Something get wrong with musicCursor");
            }
        } catch (Exception e) {
            errorCallback.invoke("Error: " + e);
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}