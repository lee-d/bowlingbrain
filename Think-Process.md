# Bowling-Brain Think Process

## Rough architecture

### Techstack

* [ ]  MongoDB -> Easy to setup, able to save objects without thinking too much about normalization and data types **(Should be included in first draft)**
* [ ]  Keycloak -> Quick to setup for first stable authentication/authorization. (Must not be included in first draft)
* [ ]  Java/Spring Boot Backend -> Comfortable to use for me and lots of support for quick POC **(Must be included in first draft)**
* [ ]  OpenApi Generator -> One api.yaml/contract results in compatible code for backend and frontend. Avoids a lot of boilerplate code for me. **(Wish it would included in first draft)**
* [ ]  Podman/Docker -> Great to start up a real mongo/keycloak instance and test against it. **(Must be included in first draft)**
* [ ]  Angular 21 -> Fullstack-Tech-Lead should probably provide a frontend :) Also quick setup possible with lots of libs and help to build first draft **(Must be included in first draft)**
* [ ]  Behaviour Driven Regressiontests like Playwright or Cucumber/Gherkin -> Nice to make sure new features don't break existing and wanted feature works as expected in SIT **(Nice to have)**
* [ ]  Claude -> Claude is an amazing agent that can amazingly speed up development. **(Must be included as AI won't be leaving software development :))**

## Development process

1. Think about the base entity structure
2. Use claude code to create all entities plus repositories

### Entity structure

* Roll
  * smashedPins: int -> [1 - 9]
  * strike: boolean
* Frame
  * rolls: List<Roll>`<Role>` -> max. size 2, only last frame max. size 3
  * prevFrame: Frame
  * nextFrame: Frame
* Game
  * players: List`<Player>`
  * currentPlayer: Player
  *
* Player
  * name: String
  * frames: List`<Frame>`, max. size 10
