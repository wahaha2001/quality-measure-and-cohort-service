#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
echo "Executing preDockerBuild.sh"

set -xe

# pass in git credentials in .toolchain.maven.settings.xml
# this is required to download rest-service-framework dependencies
# in github packages and also the cql engine libraries
# actual git credential values are stored in key protect in the cloud cluster
# and injected via env variables when the toolchain runs
mvn clean install -f cohort-parent -s .toolchain.maven.settings.xml
#mvn clean install -pl cohort-util,cql-engine-addons,cohort-evaluator,cohort-model-datarow,cohort-evaluator-spark -am -s .toolchain.maven.settings.xml

ls -l
ls -l ./cohort-evaluator-spark
ls -l ./cohort-evaluator-spark/target
ls -l ./cohort-evaluator-spark/target/lib
