# Bowling-Brain Think Process

## Rough architecture

### Techstack

* [x]  MongoDB -> Easy to setup, able to save objects without thinking too much about normalization and data types **(Should be included in first draft)**
* [ ]  Keycloak -> Quick to setup for first stable authentication/authorization. (Must not be included in first draft)
* [x]  Java/Spring Boot Backend -> Comfortable to use for me and lots of support for quick POC **(Must be included in first draft)**
* [x]  OpenApi Generator -> One api.yaml/contract results in compatible code for backend and frontend. Avoids a lot of boilerplate code for me. **(Wish it would included in first draft)**
* [x]  Podman/Docker -> Great to start up a real mongo/keycloak instance and test against it. **(Must be included in first draft)**
* [x]  Angular 21 -> Fullstack-Tech-Lead should probably provide a frontend :) Also quick setup possible with lots of libs and help to build first draft **(Must be included in first draft)**
* [ ]  Storybook -> Very nice to have to build a component library for the frontend and test components in isolation. **(Nice to have)**
* [ ]  Behaviour Driven Regressiontests like Playwright or Cucumber/Gherkin -> Nice to make sure new features don't break existing and wanted feature works as expected in SIT **(Nice to have)**
* [x]  Claude -> Claude is an amazing agent that can amazingly speed up development. **(Must be included as AI won't be leaving software development :))**
* [ ]  Kubernetes/Helm -> Good match for docker/podman if run on prem -> Well documented and widely used.
* [ ]  Cloud (AWS, Azure) -> Also possible if cloud approach legally allowed **(Would prefere running in the cloud.)**

### Entity structure

* Roll
  * smashedPins: int -> [0 - 10]
* Frame
  * rolls: List<Roll>`<Role>` -> max. size 2, only last frame max. size 3
  * score: int -> calculated based on rolls and next frames
* Game
  * players: List`<Player>`
  * currentPlayer: Player
  *
* Player
  * name: String
  * frames: List`<Frame>`, max. size 10


### Frontend
* Angular 21
  * Npm package manager
  * Bootstrap 5 for easy styling
  * Typescript for type safety and better code quality
