# rcs-poller-lambda

A lambda that polls RCS for rights updates, parses the returned xml and sends it to an SNS Topic.

![Architecture Diagram](rcs-poller-architecture.png?raw=true)

### Run locally
Get `composer` credentials and run: `sbt run`

### Deploy

Deploy this project to CODE or PROD using Riffraff. Continuous deployment is in place in the `main` branch, [previous deploys can be found here](https://riffraff.gutools.co.uk/deployment/history?projectName=rcs-poller-lambda&page=1).
