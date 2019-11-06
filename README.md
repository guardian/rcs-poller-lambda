# rcs-poller-lambda

A lambda that polls RCS for rights updates, parses the returned xml and sends it to an SNS Topic.

![Architecture Diagram](rcs-poller-architecture.png?raw=true)

### Run locally
Get `composer` credentials and run: `sbt run`

### Deploy

Deploy this project using riffraff. You can push your branch to the repository, test the changes out in CODE, then deploy to PROD upon merging your pull request to master.