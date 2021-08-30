#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#
# turn on/off creation of IKS cluster
variable create_iks_cluster {
  default = true
}

variable "iks_cluster_count" {
  description = "Number of clusters to create.   The clusters will all share the same characteristics for worker sizes, and worker pools"
  default     = 1
}

variable resource_group_id {
  type        = string
}

#n Id of the host VPC
variable vpc_id {
  type        = string
}

####################################################################################
##
## Cluster variables
##
####################################################################################

# location of the region to deploy the cluster, must be the same as the parent VPC
variable cluster_id {
  type        = string
}

# Timeout value for creating or updating objects
variable iks_cluster_create_or_update_timeout {
  description = "Time to tell terraform to wait when creating or updating a cluster object"
  default = "240m"
}

# the IDs of the IKS subnets to be used for the IKS cluster
variable iks_subnet_ids {
	default = []
}

# location of the region to deploy the cluster, must be the same as the parent VPC
variable region {
  type        = string
}

####################################################################################
##
## spark worker pool variables
##
####################################################################################
variable create_iks_cluster_custom_worker_pool1 {
 	description = "Create the first optional worker pool"
  	default     = true
}

variable iks_cluster_custom_worker_pool1_name {
  default = "custom-poo1-1"
}

# cluster worker count
variable iks_cluster_custom_worker_pool1_count {
	description = "worker count of the second optional worker pool"
  	default     = 1
}

variable iks_cluster_custom_worker_pool1_labels {
 	description = "custom worker pool1 labels"
 	default = {worker-type = "spark"}
}

# worker host type
variable iks_cluster_custom_worker_pool1_flavor {
	description = "worker host flavor"
 	default = "cx2.2x4"
}

# taint key name
variable iks_cluster_custom_worker_pool1_taint_key_name {
	description = "taint key name"
	default = "autoscale"
}

# taint value
variable iks_cluster_custom_worker_pool1_taint_value {
	description = "taint value"
	default = "enabled"
}

# taint effect
variable iks_cluster_custom_worker_pool1_taint_effect {
	description = "taint effect"
	default = "NoExecute"
}