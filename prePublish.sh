#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

echo "${REGISTRY_URL}/${REGISTRY_NAMESPACE}/cohort-evaluator-spark:${BUILD_NUMBER}" > tests/sparkimage.txt

mvn help:evaluate -f cohort-parent -Dexpression=project.version -q -DforceStdout > tests/app-version.txt