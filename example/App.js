/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Component } from 'react';
import { Platform, StyleSheet, Text, View, TouchableOpacity } from 'react-native';
import { RNAliVodUpload, RNAliVodUploadEmitter } from 'rn-ali-vod-upload';
import ImageCropPicker from 'react-native-image-crop-picker';

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' + 'Shake or press menu button for dev menu',
});

export default class App extends Component {
  emitters = [];

  componentDidMount() {
    this.emitters.push(
      RNAliVodUploadEmitter.addListener('OnUploadProgress', (result) => {
        console.log('[progress]', Math.floor(result.progress * 100) + '%', result);
      })
    );
  }

  componentWillUnmount() {
    this.emitters.forEach((e) => e.remove());
  }

  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity
          onPress={() => {
            ImageCropPicker.openPicker({
              mediaType: 'video',
            })
              .then((video) => {
                RNAliVodUpload.uploadVideo({
                  path: video.path.replace('file://', ''),
                  title: ' ',
                  desc: '',
                  cateId: 1,
                  tags: ' ',
                  templateGroupId: '7e07b96efe1a3c0abf94f043bcd50411',
                  isShowWaterMark: true,
                  expriedTime: '1618474826000',
                  accessKeyId: 'STS.NUyqBVNZ5yGsYqgmwUyjGYEJH',
                  accessKeySecret: 'EjFLd4q6KfP7t27tSxsneJbi2QteQAR74dwhsWCZyWUY',
                  securityToken:
                    'CAIS+AF1q6Ft5B2yfSjIr5bMOvjio4UUzoWYW1fWiXcAdeVrtoDhqjz2IHFLeHltB+kZs/Q1lGhX5vcelqVoRoReREvCKM1565kPdb1/kG2G6aKP9rUhpMCPOwr6UmzWvqL7Z+H+U6muGJOEYEzFkSle2KbzcS7YMXWuLZyOj+wMDL1VJH7aCwBLH9BLPABvhdYHPH/KT5aXPwXtn3DbATgD2GM+qxsmufvjn5TEukaP1w2gl7dInemrfMj4NfsLFYxkTtK40NZxcqf8yyNK43BIjvwn0/AUpGme4YzNWQkJvEXXb/Cx7cduMAJ+YKwrAalAoeh9TS2aax8R/BqAAQsv5ad0GgOdBx2/RrhCCXnLVxS1WhS3aZ2A6ipeMxaJ94ES15HD8sw/+CLxL+Xc+C3OL8dY4g9mrem47KEVmjUDY35dPrhWa2RyDna5HXk51zaZNy+kErnZPrr3nHNQYqasB46/CoZB8ITB7rm14X4A6xqWmFET/iHphnuLO++9',
                })
                  .then((res) => {
                    console.log('success', res);
                  })
                  .catch((err) => {
                    console.log('err', err.code, err.message);
                    if (err.code === '401') {
                      //@test just test
                      // RNAliVodUpload.refreshSTSToken({
                      //   expriedTime: '1618474826000',
                      //   accessKeyId: 'STS.NTq8KoiVFoWZpP3g7a43fggvx',
                      //   accessKeySecret: '6PfPyQNJGmXBGESPVjBsziFtzLkFKTktpjC23xd8A5MU',
                      //   securityToken:
                      //     'CAIS+AF1q6Ft5B2yfSjIr5fEc/HbhIln2JWxcnaCgzc0OLxKiKLdmjz2IHFLeHltB+kZs/Q1lGhX5vcelqVoRoReREvCKM1565kPAKFn/WyG6aKP9rUhpMCPOwr6UmzWvqL7Z+H+U6muGJOEYEzFkSle2KbzcS7YMXWuLZyOj+wMDL1VJH7aCwBLH9BLPABvhdYHPH/KT5aXPwXtn3DbATgD2GM+qxsmufvjn5TEukaP1w2gl7dInemrfMj4NfsLFYxkTtK40NZxcqf8yyNK43BIjvwn0/AUpGme4YzNWQkJvEXXb/Cx7cduMAJ+YKwrAalAoeh9TS2aax8R/BqAAZrEcSnShU+NMu/EIiH3/zedO7rKTWD/2vQWRm+fNNoSansJ37uJrQeRoSzqb3dnmzLB8pa/D9vvYu5CKTlSvMVRF/TzZUaN2/zFb6qdGj2uPA7g0fnDG2gMRIogdzA0DgkRl3Ax8YnLdeweEyow+AcB+5Sp5eoWmLhi3axOiLZl',
                      // });
                    }
                  });
              })
              .catch((e) => {
                console.log('[openPicker video WARN]', e);
              });
          }}
        >
          <Text style={styles.welcome}>click test upload</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => {
            ImageCropPicker.openPicker({
              mediaType: 'video',
            })
              .then((video) => {
                RNAliVodUpload.getFirstFrameImage(video.path.replace('file://', ''))
                  .then((res) => {
                    console.log('success', res);
                    alert(res);
                  })
                  .catch((err) => {
                    console.log('err', err.code, err.message);
                  });
              })
              .catch((e) => {
                console.log('[openPicker video WARN]', e);
              });
          }}
        >
          <Text style={styles.welcome}>click test getFirstFrameImage</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
