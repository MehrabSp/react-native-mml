import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-mml' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const MmlModule = isTurboModuleEnabled
  ? require('./NativeMml').default
  : NativeModules.Mml;

const Mml = MmlModule
  ? MmlModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function getAll(options: any): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    if (Platform.OS === 'android') {
      Mml.getAll(
        options,
        (tracks: any) => {
          resolve(tracks);
        },
        (error: any) => {
          reject(error);
        }
      );
    } else {
      console.log('Media Library only work for android');
    }
  });
}
export function getCheck(): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    if (Platform.OS === 'android') {
      Mml.getAll(
        { check: true },
        (tracks: any) => {
          resolve(tracks);
        },
        (error: any) => {
          reject(error);
        }
      );
    } else {
      console.log('Media Library only work for android');
    }
  });
}
