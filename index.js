import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNAliVodUpload } = NativeModules;

export const RNAliVodUploadEmitter = new NativeEventEmitter(RNAliVodUpload);

export { RNAliVodUpload };
