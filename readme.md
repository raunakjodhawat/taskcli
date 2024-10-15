# Task CLI

## Overview

Task CLI is a command-line interface (CLI) application for managing daily todos. It allows users to create, retrieve, update, and delete profiles and todos associated with those profiles. The application is built using Scala, ZIO, and ZIO CLI.

## Features

- **Create Profile**: Create a new profile if it does not exist.
- **Get Profiles**: Retrieve all existing profiles.
- **Update Profile**: Update the name of an existing profile.
- **Delete Profile**: Delete an existing profile.
- **Create Todo**: Add a new todo to a specific profile.
- **Get Todos**: Retrieve all todos for a specific profile.
- **Get All Todos**: Retrieve all todos across all profiles.
- **Get Todos for Today**: Retrieve all todos that are due today.

## Dependencies

- **Scala**: The programming language used for the project.
- **sbt**: The build tool for Scala.
- **ZIO**: A library for asynchronous and concurrent programming in Scala.
- **ZIO CLI**: A library for building command-line interfaces with ZIO.

## Installation

1. **Clone the repository**:
    ```sh
    git clone https://github.com/raunakjodhawat/your-repo.git
    cd your-repo
    ```

2. **Install dependencies**:
    ```sh
    sbt update
    ```

3. **Run the application**:
    ```sh
    sbt run
    ```

## Usage

### Commands

- **Create Profile**:
    ```sh
    task create --p <profileName>
    ```

- **Get Profiles**:
    ```sh
    task get --p
    ```

- **Update Profile**:
    ```sh
    task update --p <oldProfileName> <newProfileName>
    ```

- **Delete Profile**:
    ```sh
    task delete --p <profileName>
    ```

- **Create Todo**:
    ```sh
    task create --todo <todo> --date <date>
    ```

### Example

1. **Create a profile**:
    ```sh
    task create --p raunak
    ```

2. **Add a todo to the profile**:
    ```sh
    task create --todo "Finish project" --date "2024-10-15"
    ```

3. **Get all profiles**:
    ```sh
    task get --p
    ```

4. **Update a profile name**:
    ```sh
    task update --p raunak raunakj
    ```

5. **Delete a profile**:
    ```sh
    task delete --p raunakj
    ```

## Code Overview

### `FileManager.scala`

This object handles file operations related to profiles and todos. It uses ZIO for effectful operations.

- **getAllTodosForAProfile**: Retrieves all todos for a given profile.
- **getAllProfileNames**: Retrieves all profile names.
- **createTodoForAProfile**: Adds a new todo to a profile.
- **createProfile**: Creates a new profile.
- **deleteProfile**: Deletes an existing profile.
- **updateProfileName**: Updates the name of an existing profile.
- **getAllTodos**: Retrieves all todos across all profiles.
- **getAllTodoForToday**: Retrieves all todos that are due today.

### `Main.scala`

This object defines the CLI application using ZIO CLI.

- **task**: Defines the main command and its subcommands.
- **cliApp**: Configures the CLI application with name, version, summary, and command.

## Contributing

1. **Fork the repository**.
2. **Create a new branch**.
3. **Make your changes**.
4. **Submit a pull request**.

## License

This project is licensed under the MIT License.