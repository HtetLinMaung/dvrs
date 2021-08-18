#! /bin/bash
git add --all && git commit -am "update" && git push;
mvn clean package && cd target/azure-functions/dvc-functions-20210719130501166 && func azure functionapp publish dvrs