# rn-ali-vod-upload

## Getting started

`$ npm install rn-ali-vod-upload --save`

### Mostly automatic installation

`$ react-native link rn-ali-vod-upload`

### Manual installation

#### iOS

//参考工程配置：https://help.aliyun.com/document_detail/100448.html

1. 添加以下系统依赖库。

   - AVFoundation.framework
   - CoreMedia.framework
   - SystemConfiguration.framework
   - MobileCoreServices.framework
   - libresolv.9.tbd

2. 在主工程的 `Build Phases` 的 `Link Binary With Libraries` 中 「+」通过 「Add Other」的 「Add Files」加入 `/node_modules/rn-ali-vod-upload/ios/AliyunSDK` 下的两个 `.framework` 文件

3. 在主工程的 `Build Settings` 下配置 `Framework Search Paths` 加入 `$(SRCROOT)/../node_modules/rn-ali-vod-upload/ios/AliyunSDK`

#### Android

// 参考工程配置：https://help.aliyun.com/document_detail/100490.html

1. 在 `android/build.gradle` 的 `allprojects` 的 `repositories` 中加入

```gradle

allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
        maven { url "https://maven.aliyun.com/nexus/content/repositories/releases" }
    }
}
```

2. 在 `android/app/build.gradle` 的 `dependencies` 加入

```gradle
dependencies {
    implementation project(':rn-ali-vod-upload')
    implementation 'com.aliyun.dpa:oss-android-sdk:2.9.5'

}
```

3. 配置 `AndroidManifest.xml` 加入以下权限：

```xml

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />

```

## Usage

```javascript
import RNAliVodUpload from 'rn-ali-vod-upload';

const emitters = [];

const arrayLen = emitters.push(
  RNAliVodUploadEmitter.addListener('OnUploadProgress', (result) => {
    console.log(result);
  })
);

const emitterIdx = arrayLen - 1;

const vodConfig = {
  title: 'test',
  desc: ' ',
  cateId: 1,
  tags: ' ',
  templateGroupId: 'test',
  isShowWaterMark: true,
};

RNAliVodUpload.uploadVideo({
  path: filePath,
  ...vodConfig,
  expriedTime,
  accessKeyId,
  accessKeySecret,
  securityToken,
})
  .then((res) => {
    removeEmitter(emitters, emitterIdx);
    return res.videoId;
  })
  .catch((err) => {
    removeEmitter(emitters, emitterIdx);
    if (err.code === '401') {
    }
    console.log('上传err:', err);
    opts.onError(err);
  });

function removeEmitter(emitters, emitterIdx) {
  const removedEmitter = emitters.splice(emitterIdx, 1)[0];
  removedEmitter.remove();
}
```

## 常见问题

...
