import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
//call from library
import { getAll } from 'react-native-mml';

export default function App() {
  //get
  //type: 'Musics'
  //type: 'Images'
  //type: 'Videos'
  const Musics = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Musics' }),
      'test!'
    ); // check test
  };
  const Images = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Images' }),
      'test!'
    ); // check test
  };
  const Videos = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Videos' }),
      'test!'
    ); // check test
  };

  //   const dateAddedSeconds = 1693068596;
  // const dateTakenMilliseconds = 1693068596000;

  // const dateAdded = new Date(dateAddedSeconds * 1000);
  // const dateTaken = new Date(dateTakenMilliseconds);

  // const dateFormat = new Intl.DateTimeFormat('en-US', {
  //   year: 'numeric',
  //   month: '2-digit',
  //   day: '2-digit',
  //   hour: '2-digit',
  //   minute: '2-digit',
  //   second: '2-digit'
  // });

  // const formattedDateAdded = dateFormat.format(dateAdded);
  // const formattedDateTaken = dateFormat.format(dateTaken);

  // console.log('DATE_ADDED: ' + formattedDateAdded);
  // console.log('DATE_TAKEN: ' + formattedDateTaken);

  return (
    <View style={styles.container}>
      <Pressable
        onPress={() => {
          Musics();
        }}
      >
        <Text>Get Musics</Text>
      </Pressable>
      <Pressable
        onPress={() => {
          Images();
        }}
      >
        <Text>Get Images</Text>
      </Pressable>
      <Pressable
        onPress={() => {
          Videos();
        }}
      >
        <Text>Get Videos</Text>
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
