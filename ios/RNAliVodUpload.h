#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif
#import <React/RCTEventEmitter.h>
#import <VODUpload/VODUploadSVideoClient.h>
#import <AVFoundation/AVFoundation.h>


@interface RNAliVodUpload : RCTEventEmitter <RCTBridgeModule, VODUploadSVideoClientDelegate>
@property (nonatomic, strong) VODUploadSVideoClient *client;
@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;

@end


