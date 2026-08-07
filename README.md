# Instructions

- Install a MySQL database locally, with database name `game` (or change `spring.datasource.url` in `application.properties` correspondingly to accommodate an alternative name) and accessible on port `3306` (or, again, change `spring.datasource.url` in correspondence with the utilised port). Give the database a username and password, then run it.
    - Alternatively, install Docker Desktop and build an image using the `Dockerfile`. Run it on the configured port, passing `MYSQL_ROOT_PASSWORD`, `MYSQL_USER` and `MYSQL_PASSWORD` as environment variables. `MYSQL_ROOT_PASSWORD` is not used by this app, it is just to initialise the MySQL server since it is required by the base image.
        - `docker-compose.yml` is included for convenience. Use it to build and run the image once a `.env` file is configured (described below) with `docker compose up -d`. Stop and remove the container when done with `docker compose down`.
- Create a `.env` file in the project root by copying the template `.env.example`, where `{USERNAME}` and `{PASSWORD}` are replaced accordingly by the username and password for your MySQL database. `{ROOT_PASSWORD}` is not particularly important.
- Install maven such that you can run `mvn` commands.
- Run this app with `mvn spring-boot:run` when in the project root directory (you may need to compile this app first via `mvn clean compile` or `mvn clean install`).
- If on Windows, invoke the `game/next` endpoint with the provided `next.ps1` PowerShell script, or the `actuator/health` endpoint with the provided `health.ps1` PowerShell script, using PowerShell. Alternatives are possible on Unix-derived systems via bash or zsh.
    - Acceptable invocations of the `game/next` endpoint should save results to the running MySQL database, with contents given by `GameResultEntity.java`.
    - The `actuator/health` endpoint is used to state if the app is running.


# Tips

- You can initialise a simulation either specifying `board` or `boardName` in the call to the `game/next` endpoint. If both are provided, `boardName` is ignored.
- `boardName` initialises a simulation using a default, the set of which can be seen in `DefaultBoards.java`. For example, you can provide `boardName = 'PULSAR'`.
- If an invalid `boardName` is given, or `board` and `boardName` are both missing in the call to `game/next`, then the default board given in the `application.properties` variable `game.default-board` is used. If this property is somehow missing, `BLINKER` is used as the default.
- `board` can be provided in the call to the `game/next` endpoint as a representation of an array. It should be rectangular.
- Generation `0` is the initial state of the board. Generation `1` is the first evolution, and so on.
- `steps` provided in the call to `game/next` endpoint states the number of generations beyond the initial `0` generation that are simulated. Therefore, `steps = 10` will run the simulation up to and including generation `10`, that is, results for `0`, `1`, ..., `10` are simulated and saved. `steps` <= `0` will not run the simulation beyond the initial state.
- `boardName` will be saved to the database with a successful `game/next` invocation, even if `board` is provided. This is intended behaviour.
