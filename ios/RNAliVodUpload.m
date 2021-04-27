
#import "RNAliVodUpload.h"
#import <React/RCTConvert.h>

@implementation RNAliVodUpload

//- (dispatch_queue_t)methodQueue
//{
//    return dispatch_get_main_queue();
//}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
  return @[
//      @"onUploadSucceed",
//      @"OnUploadFailed",
      @"OnUploadProgress",
//      @"OnUploadTokenExpired",
//      @"OnUploadRerty",
//      @"OnUploadRertyResume",
//      @"OnUploadStarted",
  ];
}


RCT_EXPORT_METHOD(uploadVideo:(NSDictionary *)params
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    self.resolve = resolve;
    self.reject = reject;
    NSString *path = [RCTConvert NSString:params[@"path"]];
    NSString *videoPath = [path hasPrefix:@"file://"] ? [path stringByReplacingOccurrencesOfString:@"file://" withString:@"" ] : path;
    NSString *accessKeyId = [RCTConvert NSString:params[@"accessKeyId"]];
    NSString *accessKeySecret = [RCTConvert NSString:params[@"accessKeySecret"]];
    NSString *securityToken = [RCTConvert NSString:params[@"securityToken"]];
    NSString *expriedTime = [RCTConvert NSString:params[@"expriedTime"]];

    NSString *templateGroupId = [RCTConvert NSString:params[@"templateGroupId"]];
    NSString *desc = [RCTConvert NSString:params[@"desc"]];
    NSNumber *cateId = [RCTConvert NSNumber:params[@"cateId"]];
    BOOL isShowWaterMark = [RCTConvert BOOL:params[@"isShowWaterMark"]];

    
    self.client = [[VODUploadSVideoClient alloc] init];
    self.client.delegate = self;
    self.client.transcode = false;//是否转码，建议开启转码
    
    // init video info
    VodSVideoInfo *svideoInfo = [VodSVideoInfo new];
    svideoInfo.title = @"title";
    svideoInfo.cateId = cateId;
    svideoInfo.desc = desc;
    svideoInfo.templateGroupId = templateGroupId;
    svideoInfo.isShowWaterMark = isShowWaterMark;    
    
    // get fisrt pic
    UIImage *img = [self getScreenShotImageFromVideoPath:videoPath];
    
    // Create path.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *imgPath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"Image.png"];
    
    // Save image.
    [UIImagePNGRepresentation(img) writeToFile:imgPath atomically:YES];

    NSLog(@"video Path  %@", videoPath);
    NSLog(@"img Path  %@", imgPath);
    // upload
    [self.client uploadWithVideoPath:videoPath imagePath:imgPath svideoInfo: svideoInfo accessKeyId:accessKeyId accessKeySecret:accessKeySecret accessToken:securityToken];
    
}



/**
 上传成功
 
 */
- (void) uploadSuccessWithResult:(VodSVideoUploadResult *)result{
  
  self.resolve(@{
     @"videoId": result.videoId,
     @"imageUrl":result.imageUrl
  });
};
/**
 上传失败

 */
- (void)uploadFailedWithCode:(NSString *)code message:(NSString *)message{
  self.reject(
     code,
     message,
     nil
  );
};
/**
 上传进度
 @param uploadedSize 已上传的文件大小
 @param totalSize 文件总大小
 */
- (void)uploadProgressWithUploadedSize:(long long)uploadedSize totalSize:(long long)totalSize{
  NSLog(@"%l / %l", uploadedSize, totalSize);
  [self sendEventWithName:@"OnUploadProgress" body:@{
      @"uploadedSize": @(uploadedSize),
      @"totalSize": @(totalSize),
      @"progress": @(uploadedSize * 1.000000 / totalSize)
  }];
};
/**
 token过期
 */
- (void)uploadTokenExpired{
  NSLog(@"uploadTokenExpired");
  self.reject(@"401", @"token 过期，请重新操作", nil);
};
/**
 开始重试
 */
- (void)uploadRetry{
  NSLog(@"uploadRetry");
};
/**
 重试完成，继续上传
 */
- (void)uploadRetryResume{
  NSLog(@"uploadRetryResume");
};



/**
 * token过期后继续上传, 暂时不可用
 */
RCT_REMAP_METHOD(refreshSTSToken, refreshWithAccessKeyId: (NSDictionary *)params)
{
    NSString *accessKeyId = [RCTConvert NSString:params[@"accessKeyId"]];
    NSString *accessKeySecret = [RCTConvert NSString:params[@"accessKeySecret"]];
    NSString *securityToken = [RCTConvert NSString:params[@"securityToken"]];
    NSString *expriedTime = [RCTConvert NSString:params[@"expriedTime"]];
    [self.client refreshWithAccessKeyId:accessKeyId accessKeySecret:accessKeySecret accessToken:securityToken expireTime:expriedTime];
}


RCT_EXPORT_METHOD(cancel)
{
   [self.client cancel];
}


RCT_EXPORT_METHOD(getFirstFrameImage:(NSString *)videoPath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *path = [videoPath hasPrefix:@"file://"] ? [videoPath stringByReplacingOccurrencesOfString:@"file://" withString:@"" ] : videoPath;
    UIImage *img = [self getScreenShotImageFromVideoPath:path];
    
    // Create path.
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *imgPath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"Image.png"];
    //save image
    [UIImagePNGRepresentation(img) writeToFile:imgPath atomically:YES];
    resolve(imgPath);
}




// https://programtip.com/en/art-48580
- (UIImage *)getScreenShotImageFromVideoPath:(NSString *)filePath {
    UIImage *shotImage;
     //视频路径URL
     NSURL *fileURL = [NSURL fileURLWithPath:filePath];
     
     AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:fileURL options:nil];
     
     AVAssetImageGenerator *gen = [[AVAssetImageGenerator alloc] initWithAsset:asset];
     
     gen.appliesPreferredTrackTransform = YES;
     
     CMTime time = CMTimeMakeWithSeconds(0.0, 600);
     
     NSError *error = nil;
     
     CMTime actualTime;
     
     CGImageRef image = [gen copyCGImageAtTime:time actualTime:&actualTime error:&error];
     
     shotImage = [[UIImage alloc] initWithCGImage:image];
     
     CGImageRelease(image);
     
     return shotImage;
}



//
//RCT_EXPORT_METHOD(init:(NSDictionary *)params resolver: (RCTPromiseResolveBlock)resolve
//                  rejecter: (RCTPromiseRejectBlock)reject)
//{
//    NSString *accessKeyId = [RCTConvert NSString:params[@"accessKeyId"]];
//    NSString *accessKeySecret = [RCTConvert NSString:params[@"accessKeySecret"]];
//    NSString *securityToken = [RCTConvert NSString:params[@"securityToken"]];
//    NSString *expriedTime = [RCTConvert NSString:params[@"expriedTime"]];
//
//    // create VODUploadClient object
//    self.uploader = [VODUploadClient new];
//    //weakself
////    __weak typeof(self) weakSelf = self;
//    //setup callback；
//    OnUploadFinishedListener FinishCallbackFunc = ^(UploadFileInfo* fileInfo,  VodUploadResult* result){
//        NSLog(@"upload finished callback videoid:%@, imageurl:%@", result.videoId, result.imageUrl);
//         [self sendEventWithName:@"onUploadSucceed" body:@{
//             @"videoId": result.videoId,
//             @"imageUrl": result.imageUrl ? result.imageUrl : [NSNull null]
////             @"fileInfo": fileInfo
//         }];
//    };
//    OnUploadFailedListener FailedCallbackFunc = ^(UploadFileInfo* fileInfo, NSString *code, NSString* message){
//        NSLog(@"upload failed callback code = %@, error message = %@", code, message);
//         [self sendEventWithName:@"OnUploadFailed" body:@{
//             @"code":code,
//             @"message": message,
////             @"fileInfo": fileInfo
//         }];
//    };
//    OnUploadProgressListener ProgressCallbackFunc = ^(UploadFileInfo* fileInfo, long uploadedSize, long totalSize) {
//        NSLog(@"upload progress callback uploadedSize : %li, totalSize : %li", uploadedSize, totalSize);
//         [self sendEventWithName:@"OnUploadProgress" body:@{
//              @"uploadedSize": @(uploadedSize),
//              @"totalSize": @(totalSize),
//              @"progress": @(uploadedSize * 1.000000 / totalSize)
//          }];
//    };
//    OnUploadTokenExpiredListener TokenExpiredCallbackFunc = ^{
//        NSLog(@"upload token expired callback.");
//        //token过期，设置新的STS，继续上传。通知成功后, 获取新的 sts 再调用 resumeWithToken
//        // [self sendEventWithName:@"OnUploadTokenExpired" body:@{
//        //     @"message": @"upload token expired."
//        // }];
//    };
//    OnUploadRertyListener RetryCallbackFunc = ^{
//        NSLog(@"upload retry begin callback.");
//        // [self sendEventWithName:@"OnUploadRerty" body:@{
//        //     @"message": @"upload retry begin."
//        // }];
//    };
//    OnUploadRertyResumeListener RetryResumeCallbackFunc = ^{
//        NSLog(@"upload retry end callback.");
//        // [self sendEventWithName:@"OnUploadRertyResume" body:@{
//        //     @"message": @"upload retry resume."
//        // }];
//    };
//    OnUploadStartedListener UploadStartedCallbackFunc = ^(UploadFileInfo* fileInfo) {
//        NSLog(@"upload upload started callback.");
//        // [self sendEventWithName:@"OnUploadStarted" body:@{
//        //     @"message": @"upload upload started."
//        // }];
//    };
//    //init
//    VODUploadListener *listener = [[VODUploadListener alloc] init];
//    listener.finish = FinishCallbackFunc;
//    listener.failure = FailedCallbackFunc;
//    listener.progress = ProgressCallbackFunc;
//    listener.expire = TokenExpiredCallbackFunc;
//    listener.retry = RetryCallbackFunc;
//    listener.retryResume = RetryResumeCallbackFunc;
//    listener.started = UploadStartedCallbackFunc;
//    //init with STS
//    [self.uploader setKeyId: accessKeyId accessKeySecret: accessKeySecret secretToken: securityToken expireTime: expriedTime listener:listener ];
//    self.uploader.transcode = YES;
//    resolve(@"init");
//    NSLog(@"upload inited");
//}
//
///**
// * 添加文件，这里暂时没有区分类型。
// */
//RCT_EXPORT_METHOD(addFile:(NSDictionary *)file)
//{
//    NSString *filePath = [RCTConvert NSString:file[@"path"]];
//    NSString *taskId = [RCTConvert NSString:file[@"taskId"]];
//    NSString *title = [RCTConvert NSString:file[@"title"]];
//    NSString *desc = [RCTConvert NSString:file[@"desc"]];
//    NSNumber *cateId = [RCTConvert NSNumber:file[@"cateId"]];
//    NSString *tags = [RCTConvert NSString:file[@"tags"]];
//    NSString *templateGroupId = [RCTConvert NSString:file[@"templateGroupId"]];
//    BOOL isShowWaterMark = [RCTConvert BOOL:file[@"isShowWaterMark"]];
//
//    VodInfo *vodInfo = [[VodInfo alloc] init];
//    vodInfo.title = title;
//    vodInfo.desc = desc;
//    vodInfo.cateId = cateId;
//    vodInfo.tags = tags;
//    vodInfo.templateGroupId = templateGroupId;
//    vodInfo.isShowWaterMark = isShowWaterMark;
//
//    Boolean added = [self.uploader addFile:filePath vodInfo:vodInfo];
//
//    if (added) {
//        NSLog(@"addFile path = %@, type = %@, templateGroupId = %@", filePath, taskId, templateGroupId);
//    } else {
//        NSLog(@"addFile failure");
//    }
//}
//
///* 管理上传队列 */
///**
// * 从队列中删除上传文件
// * 如果待删除的文件正在上传中，则取消上传并自动上传下一个文件。
// *
// *  目前不完整，后面再追加
// */
//RCT_EXPORT_METHOD(deleteFile:(int)index)
//{
//    [self.uploader deleteFile:index];
////    resolve(@(index));
//}
//
///**
// * 清空上传队列
// * 如果有文件在上传，则取消上传。
// */
//RCT_EXPORT_METHOD(clearFiles)
//{
//    [self.uploader clearFiles];
//    NSLog(@"clear files");
//}
//
///**
// * 获取上传文件队列
// */
//RCT_EXPORT_METHOD(listFiles:(RCTPromiseResolveBlock) resolve
//                 rejecter:(RCTPromiseRejectBlock) reject)
//{
//   NSMutableArray *files = [self.uploader listFiles];
//   NSLog(@"listFiles %@", files);
//   resolve(files);
//}
//
///**
// * 将文件标记为取消
// * 文件任保留在上传列表中。如果待取消的文件正在上传中，则取消上传并自动上传下一个文件
// */
//RCT_EXPORT_METHOD(cancelFile:(int)index)
//{
//    [self.uploader cancelFile:index];
//
//}
//
//
///* 上传控制 */
//
///**
// * 开始上传
// */
//RCT_EXPORT_METHOD(start)
//{
//  [self.uploader start];
//  NSLog(@"start files");
//}
//
///**
// * 停止上传
// * 如果有文件正在上传中，则取消上传
// */
//RCT_EXPORT_METHOD(stop)
//{
//    [self.uploader stop];
//}
//
///**
// * 暂停上传
// */
//RCT_EXPORT_METHOD(pause)
//{
//    [self.uploader pause];
//}
//
///**
// * 恢复上传
// */
//RCT_EXPORT_METHOD(resume)
//{
//    [self.uploader resume];
//}
//
//
///**
// * token过期后继续上传
// */
//RCT_EXPORT_METHOD(resumeWithToken: (NSDictionary *)params)
//{
//    NSString *accessKeyId = [RCTConvert NSString:params[@"accessKeyId"]];
//    NSString *accessKeySecret = [RCTConvert NSString:params[@"accessKeySecret"]];
//    NSString *securityToken = [RCTConvert NSString:params[@"securityToken"]];
//    NSString *expriedTime = [RCTConvert NSString:params[@"expriedTime"]];
//
//    [self.uploader resumeWithToken: accessKeyId accessKeySecret: accessKeySecret secretToken: securityToken expireTime: expriedTime];
//}

@end
