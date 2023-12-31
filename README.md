# react-native-mml

React Native Media Library

New turbo module (only for Android)

## New Update
A new update has been released
What has been added?

- Get all videos from memory
- Get all photos from memory
- Typing is correct for typescript

What does this module do?

- Reads and receives all the songs from the memory
- Metadata receives all songs
- Saves the cover of each song in the special location of the program (Scoped Storage).
- Ability to recognize colors from song covers for Android.
(This is still beta)
To use the library, you can see the GitHub sample folder

## Installation

```sh
npm install react-native-mml
```
```sh
yarn add react-native-mml
```

## Usage

```js
  //get
  //type: 'Musics'
  //type: 'Images'
  //type: 'Videos'
  const Musics = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Musics' }),
      'Musics!'
    );
  };
  const Images = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Images' }),
      'Images!'
    );
  };
  const Videos = async () => {
    console.log(
      await getAll({ options: { check: true, title: true }, type: 'Videos' }),
      'Videos!'
    );
  };
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## Acknowledgements

Thanks to the authors of these libraries for inspiration:

- [react-native-get-music-files](https://github.com/cinder92/react-native-get-music-files)
- [react-native-image-colors](https://github.com/osamaqarem/react-native-image-colors)


## License

MIT

---

-- MRB --
