# Client-Server Game

### Assignment 2 - T05G17

## Project Members

- Gonçalo Santos (up202108839@up.pt)
- Rui Carvalho (up202108807@up.pt)
- Gonçalo Pinto (up202108693@up.pt)

## Requirements

- Java SE 21 or later
- Git for version control

## Installation

1. Clone the repository from Gitlab

2. Navigate to the project directory:
`cd src`

## Running

1. Compile the code:
  - `javac Player.java`
  - `javac Token.java`
  - `javac DatabaseManager.java`
  - `javac Queue.java`
  - `javac MathServer.java`
  - `javac MathClient.java`

2. Run the server:
  - `java MathServer <port>` (ex: java MathServer 4000)

3. Run the client:
  - `java MathClient localhost <port>` (ex: java MathClient localhost 4000)

## Database
Here there are some users that can be used to login, otherwise it is possible to create a new user.
| name | password | highscore | rank |
|-----|-----|-----|-----|
| jony | 1234 | 100 | Gold |
| simao | 1234 | 100 | Gold |
| goncalo | 1234 | 100 | Gold |
| joao | 1234 | 70 | Silver |
| pedrito | 1234 | 10 | Bronze |
| juanito | 1234 | 0 | Bronze |
