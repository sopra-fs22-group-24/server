# SoPra Project Group 24 Uno Extreme Server

## Introduction
We are a Team of 4 Students at the UZH of Zürich.
For the course Software Enginnering Lab we had to build a web app.
We wanted to make a fun project and therefore we decided to implement a game.
The game we choose is the popular game Uno Extreme.

## Technologies
- gradle (build tool)
- java 15 
- spring boot
- Heroku (for automatic deployment via github)
- Sonarcloud (for Coverage and bug detection)


## High Level Components
### Game
- GameController handles all requests via websockets and calls the GameService with the data.
- GameService validates and processes the data and stores the changes in the game repository.
- JpaEntity Game & GameRepository stores the game data such as players their hands the deck and the discard pile.

### User
- UserController via Rest protocol
- UserService Handles the User data from the controller
### Lobby 
- LobbyController Rest and Websocket Endpoints
- LobbyService Handles the Lobby data from the controller




## Launch and Deployement
### Setup this Template with your IDE of choice
Building with Gradle
You can use the local Gradle Wrapper to build the application.

- macOS: ./gradlew
- Linux: ./gradlew
- Windows: ./gradlew.bat
More Information about Gradle Wrapper and Gradle.

#### Build
./gradlew build
#### Run
./gradlew bootRun
#### Test
./gradlew test
#### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application once the content of a file has been changed and you save the file.

Start two terminal windows and run:

- ./gradlew build --continuous

and in the other one:

- ./gradlew bootRun

If you want to avoid running all tests with every change, use the following command instead:

- ./gradlew build --continuous -xtest



## Roadmap
- Sorting Cards according to Color before returning them
- Profile Picture displayed in lobbies
- Additonal Cards like Change Hands




## Authors and acknowledgment
### Authors
- Livia Stöckli
- Florian Rüegsegger
- Lea Kehrli
- Mauro Hirt

### Acknowledgment
- Kyrill Hux: Project Assistant teacher
- Thomas Fritz: Professor
- Roy Rutishauser: Head asssistant teacher
## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

