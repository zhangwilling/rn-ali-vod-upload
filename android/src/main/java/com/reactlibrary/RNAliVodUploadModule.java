package com.reactlibrary;

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
    String videoPath = params.getString("path");
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
            .setImagePath(imagePath)        // 图片地址 必须要有
            .setVideoPath(videoPath)        // 视频地址
            .setAccessKeyId(accessKeyId)    // 临时accessKeyId
            .setAccessKeySecret(accessKeySecret)    // 临时accessKeySecret
            .setSecurityToken(securityToken)        // securityToken
            .setExpriedTime(expriedTime)            // STStoken过期时间
            .setTemplateGroupId(templateGroupId)
            .setSvideoInfo(svideoInfo)              // 短视频视频信息
            .setVodHttpClientConfig(vodHttpClientConfig)    //网络参数
            .build();


    vodsVideoUploadClient.uploadWithVideoAndImg(vodSessionCreateInfo, new VODSVideoUploadCallback() {
      @Override
      public void onUploadSucceed(String videoId, String imageUrl) {
        //上传成功返回视频ID和图片URL.
        Log.d(TAG, "onUploadSucceed" + "videoId:" + videoId + "imageUrl" + imageUrl);
        WritableMap map = Arguments.createMap();
        map.putString("videoId", videoId);
        map.putString("imageUrl", imageUrl);
        promise.resolve(map);
        vodsVideoUploadClient.release();
      }

      @Override
      public void onUploadFailed(String code, String message) {
        //上传失败返回错误码和message.错误码有详细的错误信息请开发者仔细阅读
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
        // 通知 RN 调用 refreshSTSToken
        promise.reject("401", "token 过期，请重新操作");
        vodsVideoUploadClient.release();
      }

      @Override
      public void onUploadRetry(String code, String message) {
        //上传重试的提醒
        Log.d(TAG, "onUploadRetry" + "code" + code + "message" + message);
      }

      @Override
      public void onUploadRetryResume() {
        //上传重试成功的回调.告知用户重试成功
        Log.d(TAG, "onUploadRetryResume");
      }
    });
  }

  @ReactMethod
  public void refreshSTSToken(ReadableMap params) {
    String accessKeyId = params.getString("accessKeyId");
    String accessKeySecret = params.getString("accessKeySecret");
    String securityToken = params.getString("securityToken");
    String expriedTime = params.getString("expriedTime");
    vodsVideoUploadClient.refreshSTSToken(accessKeyId, accessKeySecret, securityToken,expriedTime);
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
//    // 清除所有文件
//    //    this.clearFiles();
//    //上传初始化
//    uploader.init(accessKeyId, accessKeySecret, securityToken, expriedTime, listeners);
//
//    promise.resolve("init");
//    OSSLog.logInfo("[VodUpload] init");
//  }
//
//
//  /**
//   * 添加文件
//   *
//   * */
//  @ReactMethod
//  public void addFile(ReadableMap file) {
//    // taskId 用于标记文件上传任务
////    String taskId = file.getString("taskId");
//    List<String> tags = new ArrayList<String>();
//    tags.add(file.getString("tags"));
//    String filePath = file.getString("path");
//    VodInfo vodInfo = new VodInfo();
//    vodInfo.setTitle(file.getString("title"));
//    vodInfo.setDesc(file.getString("desc"));
//    vodInfo.setCateId(file.getInt("cateId"));
//    vodInfo.setTags(tags);
//    // 借用 userData 作为临时存储字段
////    vodInfo.setUserData(taskId);
//    uploader.setTemplateGroupId(file.getString("templateGroupId"));
//    vodInfo.setIsShowWaterMark(file.getBoolean("isShowWaterMark"));
//    uploader.addFile(filePath, vodInfo);
//    OSSLog.logInfo("[VodUpload] add file");
//  }
//
//  /* 管理上传队列 */
//  /**
//   * 从队列中删除上传文件
//   * 如果待删除的文件正在上传中，则取消上传并自动上传下一个文件。
//   *
//   *  目前不完整，后面再追加
//   */
//  @ReactMethod
//  public void deleteFile(int index) {
//    uploader.deleteFile(index);
//  }
//
//  /**
//   * 清空上传队列
//   * 如果有文件在上传，则取消上传
//   */
//  @ReactMethod
//  public void clearFiles() {
//    uploader.clearFiles();
//  }
//
//  /**
//   * 获取上传文件队列
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
//   * 将文件标记为取消
//   * 文件任保留在上传列表中。如果待取消的文件正在上传中，则取消上传并自动上传下一个文件
//   */
//  @ReactMethod
//  public void cancelFile(int index) {
//    uploader.cancelFile(index);
//  }
//
//
//  /* 上传控制 */
//
//  /**
//   * 开始上传
//   */
//  @ReactMethod
//  public void start() {
//    uploader.start();
//    OSSLog.logInfo("[VodUpload] start");
//  }
//
//  /**
//   * 停止上传
//   * 如果有文件正在上传中，则取消上传
//   */
//  @ReactMethod
//  public void stop() {
//    uploader.stop();
//  }
//
//  /**
//   * 暂停上传
//   */
//  @ReactMethod
//  public void pause() {
//    uploader.pause();
//  }
//
//  /**
//   * 恢复上传
//   */
//  @ReactMethod
//  public void resume() {
//    uploader.resume();
//  }
//
//
//  /**
//   * token过期后继续上传
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