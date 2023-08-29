import { NativeModules, Platform } from 'react-native';

export interface Mml {
  options: any;
  type: 'Images' | 'Videos' | 'Musics';
  check?: boolean;
}

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

export const callToModule = function (
  { options, type }: any,
  Resolve: any,
  Reject: any
) {
  if (Platform.OS === 'android') {
    Mml.getAll(
      options,
      type,
      (tracks: any) => {
        Resolve(tracks);
      },
      (error: any) => {
        //reject
        Reject(error);
      }
    );
  } else {
    throw new Error(
      'Error: The react-native-mml library only supports Android!'
    );
  }
};

export function getAll({ options, type, check = false }: Mml) {
  if (check) options = { check: true };
  return new Promise((resolve, reject) => {
    callToModule({ options, type }, resolve, reject);
  });
}
