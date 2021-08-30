#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

# tf gives an error when trying to get the VPC by name saying "Error: No VPC found with name..."
#data "ibm_is_vpc" "vpc" {
#  name = var.vpc_name
#}

data "ibm_resource_group" "resource_group_cloudsvc" {
  name = var.vpc_rg_cloudsvc
}

data "ibm_resource_group" "resource_group_kube" {
  name = var.vpc_rg_kube
}

data "ibm_container_vpc_cluster" "cluster" {
  name  = var.vpc_cluster_name
  resource_group_id = data.ibm_resource_group.resource_group_kube.id
}

module "spark_worker_pool" {
  source            = "./modules/spark_worker_pool"
  vpc_id            = var.vpc_id
  resource_group_id = data.ibm_resource_group.resource_group_kube.id
  cluster_id        = data.ibm_container_vpc_cluster.cluster.id
  create_iks_cluster = var.create_iks_cluster
  iks_cluster_count = var.iks_cluster_count
  iks_cluster_create_or_update_timeout = var.iks_cluster_create_or_update_timeout
  iks_subnet_ids = var.iks_subnet_ids
  region = var.region
  create_iks_cluster_custom_worker_pool1 = var.create_iks_cluster_custom_worker_pool1
  iks_cluster_custom_worker_pool1_name = var.iks_cluster_custom_worker_pool1_name
  iks_cluster_custom_worker_pool1_count = var.iks_cluster_custom_worker_pool1_count
  iks_cluster_custom_worker_pool1_labels = var.iks_cluster_custom_worker_pool1_labels
  iks_cluster_custom_worker_pool1_flavor = var.iks_cluster_custom_worker_pool1_flavor
  iks_cluster_custom_worker_pool1_taint_key_name = var.iks_cluster_custom_worker_pool1_taint_key_name
  iks_cluster_custom_worker_pool1_taint_value = var.iks_cluster_custom_worker_pool1_taint_value
  iks_cluster_custom_worker_pool1_taint_effect = var.iks_cluster_custom_worker_pool1_taint_effect
}