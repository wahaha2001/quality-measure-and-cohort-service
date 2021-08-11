# An image pull secret is required to pull the test image

if kubectl get secret -n ${CLUSTER_NAMESPACE} | grep -q ${TEST_IMAGE_PULL_SECRET}; then
	echo "Kubernetes secret ${TEST_IMAGE_PULL_SECRET} already exists in namespace ${CLUSTER_NAMESPACE}"
else
	echo "Copying kubernetes secret ${TEST_IMAGE_PULL_SECRET} from default namespace to ${CLUSTER_NAMESPACE} namespace"
	kubectl get secret ${TEST_IMAGE_PULL_SECRET} -n default -o json | jq 'del(.metadata["namespace","creationTimestamp","resourceVersion","selfLink","uid"])' | kubectl apply -n ${CLUSTER_NAMESPACE} -f -
fi