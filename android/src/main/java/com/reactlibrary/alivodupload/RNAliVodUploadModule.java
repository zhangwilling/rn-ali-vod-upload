package com.reactlibrary.alivodupload;

import android.util.Log;
import com.alibaba.sdk.android.vod.upload.VODSVideoUploadCallback;
import com.alibaba.sdk.android.vod.upload.VODSVideoUploadClient;
import com.alibaba.sdk.android.vod.upload.VODSVideoUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.model.SvideoInfo;
import com.alibaba.sdk.android.vod.upload.session.VodHttpClientConfig;
import com.alibaba.sdk.android.vod.upload.session.VodSessionCreateInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import javax.annotation.Nullable;
import java.io.File;

public class RNAliVodUploadModule extends ReactContextBaseJavaModule {
  private static final String TAG = "RNAliVodUploadModule";
  private final ReactApplicationContext reactContext;
  //  private VODUploadClient uploader;
  private VODSVideoUploadClient vodsVideoUploadClient;


  public RNAliVodUploadModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNAliVodUpload";
  }

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }


  @ReactMethod
  public void uploadVideo(ReadableMap params, final Promise promise) {
    String path = params.getString("path");
    String videoPath = path.startsWith("file://") ? path.replace("file://", "") : path;
    String accessKeyId = params.getString("accessKeyId");
    String accessKeySecret = params.getString("accessKeySecret");
    String securityToken = params.getString("securityToken");
    String expriedTime = params.getString("expriedTime");


    String templateGroupId = params.getString("templateGroupId");
    String desc = params.getString("desc");
    Integer cateId = params.getInt("cateId");
    Boolean isShowWaterMark = params.getBoolean("isShowWaterMark");

    final String imagePath = Utils.getFirstFramePath(videoPath, reactContext);

    vodsVideoUploadClient = new VODSVideoUploadClientImpl(reactContext.getApplicationContext());
    vodsVideoUploadClient.init();

    VodHttpClientConfig vodHttpClientConfig = new VodHttpClientConfig.Builder()
            .setMaxRetryCount(2)
            .setConnectionTimeout(15 * 1000)
            .setSocketTimeout(15 * 1000)
            .build();

    SvideoInfo svideoInfo = new SvideoInfo();
    svideoInfo.setTitle(new File(videoPath).getName());
    svideoInfo.setDesc(desc);
    svideoInfo.setCateId(cateId);
    svideoInfo.setShowWaterMark(isShowWaterMark);

    VodSessionCreateInfo vodSessionCreateInfo = new VodSessionCreateInfo.Builder()
            .setImagePath(imagePath)        // ???????????? ????????????
            .setVideoPath(videoPath)        // ????????????
            .setAccessKeyId(accessKeyId)    // ??????accessKeyId
            .setAccessKeySecret(accessKeySecret)    // ??????accessKeySecret
            .setSecurityToken(securityToken)        // securityToken
            .setExpriedTime(expriedTime)            // STStoken????????????
            .setTemplateGroupId(templateGroupId)
            .setSvideoInfo(svideoInfo)              // ?????????????????????
            .setVodHttpClientConfig(vodHttpClientConfig)    //????????????
            .build();


    vodsVideoUploadClient.uploadWithVideoAndImg(vodSessionCreateInfo, new VODSVideoUploadCallback() {
      @Override
      public void onUploadSucceed(String videoId, String imageUrl) {
        //????????????????????????ID?????????URL.
        Log.d(TAG, "onUploadSucceed" + "videoId:" + videoId + "imageUrl" + imageUrl);
        WritableMap map = Arguments.createMap();
        map.putString("videoId", videoId);
        map.putString("imageUrl", imageUrl);
        promise.resolve(map);
        vodsVideoUploadClient.release();
      }

      @Override
      public void onUploadFailed(String code, String message) {
        //??????????????????????????????message.?????????????????????????????????????????????????????????
        Log.d(TAG, "onUploadFailed" + "code" + code + "message" + message);
        promise.reject(code, message);
        vodsVideoUploadClient.release();
      }

      @Override
      public void onUploadProgress(long uploadedSize, long totalSize) {
        WritableMap params = Arguments.createMap();
        params.putDouble("uploadedSize", uploadedSize);
        params.putDouble("totalSize", totalSize);
        params.putDouble("progress", (float) uploadedSize / totalSize);
        sendEvent(reactContext, "OnUploadProgress", params);
      }

      @Override
      public void onSTSTokenExpried() {
        Log.d(TAG, "onSTSTokenExpried");
        // ?????? RN ?????? refreshSTSToken
        promise.reject("401", "token ????????????????????????");
        vodsVideoUploadClient.release();
      }

      @Override
      public void onUploadRetry(String code, String message) {
        //?????????????????????
        Log.d(TAG, "onUploadRetry" + "code" + code + "message" + message);
      }

      @Override
      public void onUploadRetryResume() {
        //???????????????????????????.????????????????????????
        Log.d(TAG, "onUploadRetryResume");
      }
    });
  }

  @ReactMethod
  public void getFirstFrameImage(String videoPath, final Promise promise) {
    String path = videoPath.startsWith("file://") ? videoPath.replace("file://", "") : videoPath;
    final String imagePath = Utils.getFirstFramePath(path , reactContext);
    promise.resolve(imagePath);
  }

  @ReactMethod
  public void refreshSTSToken(ReadableMap params) {
    String accessKeyId = params.getString("accessKeyId");
    String accessKeySecret = params.getString("accessKeySecret");
    String securityToken = params.getString("securityToken");
    String expriedTime = params.getString("expriedTime");
    vodsVideoUploadClient.refreshSTSToken(accessKeyId, accessKeySecret, securityToken,expriedTime);
  }

  //?????????????????????????????????????????????????????????resume??????
  @ReactMethod
  public void cancel() {
    vodsVideoUploadClient.cancel();
  }
  
//
//  @ReactMethod
//  public void init(ReadableMap params, Promise promise) {
//    final String accessKeyId = params.getString("accessKeyId");
//    final String accessKeySecret = params.getString("accessKeySecret");
//    final String securityToken = params.getString("securityToken");
//    final String expriedTime = params.getString("expriedTime");
//    uploader = new VODUploadClientImpl(reactContext.getApplicationContext());
//
//    // setup callback
//    VODUploadCallback listeners = new VODUploadCallback() {
//      public void onUploadSucceed(UploadFileInfo info) {
//        WritableMap params = Arguments.createMap();
////        params.putString("videoId", videoId);
//        sendEvent(reactContext, "onUploadSucceed", params);
//      }
//
//      public void onUploadFailed(UploadFileInfo info, String code, String message) {
//        OSSLog.logError("[VodUpload] onUploadFailed" + info.getFilePath() + " " + code + " " + message);
//        WritableMap params = Arguments.createMap();
//        params.putString("code", code);
//        params.putString("message", message);
//        sendEvent(reactContext, "onUploadFailed", params);
//      }
//
//      public void onUploadProgress(UploadFileInfo info, long uploadedSize, long totalSize) {
//        OSSLog.logInfo("[VodUpload] onProgress" + info.getFilePath() + " " + uploadedSize + " " + totalSize);
//
//        WritableMap params = Arguments.createMap();
//        params.putDouble("uploadedSize", uploadedSize);
//        params.putDouble("totalSize", totalSize);
//        params.putDouble("progress", (float) uploadedSize / totalSize);
//        sendEvent(reactContext, "OnUploadProgress", params);
//      }
//
//      public void onUploadTokenExpired() {
//        OSSLog.logError("[VodUpload] onUploadTokenExpired");
//        WritableMap params = Arguments.createMap();
//        params.putString("message", "upload token expired.");
//        sendEvent(reactContext, "OnUploadTokenExpired",params);
//      }
//
//      public void onUploadRetry(String code, String message) {
//        OSSLog.logDebug("[VodUpload] onUploadRetry");
//
//      }
//
//      public void onUploadRetryResume() {
//        OSSLog.logDebug("[VodUpload] onUploadRetryResume");
//      }
//
//      public void onUploadStarted(UploadFileInfo uploadFileInfo) {
//        OSSLog.logInfo("[VodUpload] onUploadStarted");
//      }
//    };
//
//    // ??????????????????
//    //    this.clearFiles();
//    //???????????????
//    uploader.init(accessKeyId, accessKeySecret, securityToken, expriedTime, listeners);
//
//    promise.resolve("init");
//    OSSLog.logInfo("[VodUpload] init");
//  }
//
//
//  /**
//   * ????????????
//   *
//   * */
//  @ReactMethod
//  public void addFile(ReadableMap file) {
//    // taskId ??????????????????????????????
////    String taskId = file.getString("taskId");
//    List<String> tags = new ArrayList<String>();
//    tags.add(file.getString("tags"));
//    String filePath = file.getString("path");
//    VodInfo vodInfo = new VodInfo();
//    vodInfo.setTitle(file.getString("title"));
//    vodInfo.setDesc(file.getString("desc"));
//    vodInfo.setCateId(file.getInt("cateId"));
//    vodInfo.setTags(tags);
//    // ?????? userData ????????????????????????
////    vodInfo.setUserData(taskId);
//    uploader.setTemplateGroupId(file.getString("templateGroupId"));
//    vodInfo.setIsShowWaterMark(file.getBoolean("isShowWaterMark"));
//    uploader.addFile(filePath, vodInfo);
//    OSSLog.logInfo("[VodUpload] add file");
//  }
//
//  /* ?????????????????? */
//  /**
//   * ??????????????????????????????
//   * ??????????????????????????????????????????????????????????????????????????????????????????
//   *
//   *  ?????????????????????????????????
//   */
//  @ReactMethod
//  public void deleteFile(int index) {
//    uploader.deleteFile(index);
//  }
//
//  /**
//   * ??????????????????
//   * ??????????????????????????????????????????
//   */
//  @ReactMethod
//  public void clearFiles() {
//    uploader.clearFiles();
//  }
//
//  /**
//   * ????????????????????????
//   */
//  @ReactMethod
//  public void listFiles(Promise promise) {
//    List<UploadFileInfo> filesList = uploader.listFiles();
//
//    WritableArray files = Arguments.createArray();
//
//    for (UploadFileInfo fileInfo : filesList) {
//      WritableMap fileMap = Arguments.createMap();
//      VodInfo vodInfo = fileInfo.getVodInfo();
//      fileMap.putString("name", vodInfo.getFileName());
//      fileMap.putString("title", vodInfo.getTitle());
//      fileMap.putString("size", vodInfo.getFileSize());
//      fileMap.putString("path", fileInfo.getFilePath());
//      fileMap.putInt("type", fileInfo.getFileType());
//      fileMap.putString("desc", vodInfo.getDesc());
//      fileMap.putInt("cateId", vodInfo.getCateId());
//      fileMap.putString("coverUrl", vodInfo.getCoverUrl());
//      files.pushMap(fileMap);
//    }
//    promise.resolve(files);
//  }
//
//  /**
//   * ????????????????????????
//   * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//   */
//  @ReactMethod
//  public void cancelFile(int index) {
//    uploader.cancelFile(index);
//  }
//
//
//  /* ???????????? */
//
//  /**
//   * ????????????
//   */
//  @ReactMethod
//  public void start() {
//    uploader.start();
//    OSSLog.logInfo("[VodUpload] start");
//  }
//
//  /**
//   * ????????????
//   * ????????????????????????????????????????????????
//   */
//  @ReactMethod
//  public void stop() {
//    uploader.stop();
//  }
//
//  /**
//   * ????????????
//   */
//  @ReactMethod
//  public void pause() {
//    uploader.pause();
//  }
//
//  /**
//   * ????????????
//   */
//  @ReactMethod
//  public void resume() {
//    uploader.resume();
//  }
//
//
//  /**
//   * token?????????????????????
//   */
//  @ReactMethod
//  public void resumeWithToken(ReadableMap params) {
//    final String accessKeyId = params.getString("accessKeyId");
//    final String accessKeySecret = params.getString("accessKeySecret");
//    final String securityToken = params.getString("securityToken");
//    final String expriedTime = params.getString("expriedTime");
//
//    uploader.resumeWithToken(accessKeyId, accessKeySecret, securityToken, expriedTime);
//  }
}