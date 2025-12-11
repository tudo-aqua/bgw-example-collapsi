# BGW Example: Collapsi

<div style="overflow-x:auto; white-space:nowrap;">
  <img src=screenshots/MainMenu.jpg alt=MainMenu style="height:180px; display:inline-block; margin-right:8px;">
  <img src=screenshots/InGame_2P.jpg alt=InGame_2P style="height:180px; display:inline-block; margin-right:8px;">
  <img src=screenshots/InGame_3P.jpg alt=InGame_3P style="height:180px; display:inline-block; margin-right:8px;">
  <img src=screenshots/Lobby_Local.jpg alt=LocalLobbyScene style="height:180px; display:inline-block; margin-right:8px;">
</div>

## About the Project

This is Collapsi, a small strategic board game that serves as an example project
for [BoardGameWork](https://tudo-aqua.github.io/bgw/) (BGW). It is programmed in [Kotlin](https://kotlinlang.org/)
according to the software architecture used in [TU Dortmund's programming lab](https://sopra.cs.tu-dortmund.de). 

A big thank you goes out to Mark Ball, the designer of Collapsi, for allowing us to implement his game. Please
visit the [official Collapsi website](https://riffleshuffleandroll.itch.io/collapsi) and 
his [YouTube channel](https://www.youtube.com/@riffleshuffleandroll).

## Getting Started

There are (so far) no pre-built binaries of the game. Please clone the repository (or download source archive) and execute with `./gradle run`.

Please note that you need a Java Development Kit (JDK) installed on your system. BGW requires Java 11 to run, although
newer versions should work as well. 
We recommend using [Azul Zulu OpenJDK](https://www.azul.com/downloads/?version=java-11-lts&package=jdk#zulu).


## Rules

Collapsi is a game for 2-4 players. Players take turns moving their pawns on the board
with fewer and fewer tiles to stand on. If a player can't make any legal moves, the tile below
them collapses and they're out of the game. The last player standing wins.

Watch the [Mark's video on YouTube](https://www.youtube.com/watch?v=6vYEHdjlw3g) for a thorough introduction
of the game and its rules.

## Features

- 2-4 players
- board sizes 4x4, 5x5, and 6x6
- hotseat or online (requires [BGW Net Server](https://github.com/tudo-aqua/bgw/tree/main/bgw-net/bgw-net-server)).
- bots with four levels of difficulty
- sounds and animations
- change animation speed mid-game
- save and load local games.
- undo and redo turns.

## License / Attribution

Copyright of Collapsi idea and rules by Mark Ball. 

This implementation is open source under the Apache 2.0 license, see [LICENSE.md](LICENSE.md).

For included resources, see [ATTRIBUTION.md](ATTRIBUTION.md).