# rn-ali-vod-upload

## Getting started

`$ npm install rn-ali-vod-upload --save`

### Mostly automatic installation

`$ react-native link rn-ali-vod-upload`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `rn-ali-vod-upload` and add `RNAliVodUpload.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAliVodUpload.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`

- Add `import com.reactlibrary.RNAliVodUploadPackage;` to the imports at the top of the file
- Add `new RNAliVodUploadPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':rn-ali-vod-upload'
   project(':rn-ali-vod-upload').projectDir = new File(rootProject.projectDir, 	'../node_modules/rn-ali-vod-upload/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     compile project(':rn-ali-vod-upload')
   ```

## Usage

```javascript
import RNAliVodUpload from 'rn-ali-vod-upload';

// TODO: What to do with the module?
RNAliVodUpload;
```
