<h1 align="center">
    RandomItemBattle
</h1>

<p align="center">
    <img src="https://img.shields.io/badge/java-%23ED8B00.svg?logo=openjdk&logoColor=white&style=for-the-badge" alt="project language java" />
    <img src="https://img.shields.io/badge/modloader-fabric-cccccc?style=for-the-badge" alt="fabric modloader" />
    <img src="https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge" alt="project license" />
    <img src="https://img.shields.io/github/languages/code-size/Zwylair/RandomItemBattle?style=for-the-badge" alt="code size" />
</p>

## About

`RandomItemBattle` is a mod to help make the game more comfortable, where a random item is given out at a certain interval and players compete in an empty world with only bedrock blocks below the players and a 3x3 bedrock centre.

`RandomItemBattle` only provides the gameplay part of the game, but there is a `/rib create_playground` command for creating playground in your world.
## Commands

All mod's commands are subcommands of `/rib` command.

### Managing game

* Start game: `/rib game start`
* Stop game: `/rib game stop`
* Pause item giving: `/rib items pause`
* Resume item giving: `/rib items resume`

### Configuring positions

* Add start position: `/rib startpos add`
* Remove start position: `/rib startpos remove`
* Remove all start positions: `/rib startpos clear`
* Configure center position: `/rib set_center_position`

### Configuring game

* Configure timeout: `/rib timeout <time_in_seconds>`
* Create playground (with auto adding startpos and centerpos): `/rib create_playground`

## License

This project is under the [MIT license](./LICENSE).
