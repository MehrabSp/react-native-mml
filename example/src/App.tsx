import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
//call from library
import { getAll, getCheck } from 'react-native-mml';

export default function App() {
  const All = async () => {
    //result (slow)
    console.log(await getAll({ title: true, cover: true }));
  };
  const Checker = async () => {
    //result (very fast)
    console.log(await getCheck());
  };
  return (
    <View style={styles.container}>
      <Pressable
        onPress={() => {
          console.log('press');
          All();
          Checker();
        }}
      >
        <Text>Go</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'red',
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
