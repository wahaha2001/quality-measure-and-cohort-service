#
# (C) Copyright IBM Corp. 2021, 2021
#
# SPDX-License-Identifier: Apache-2.0
#

resource "ibm_container_vpc_worker_pool" "spark_worker_pool" {
  	count  	         = (var.create_iks_cluster && var.create_iks_cluster_custom_worker_pool1) ? var.iks_cluster_count : 0
  	vpc_id 		   	 = var.vpc_id
  	
	cluster = var.cluster_id
  	
  	worker_pool_name = var.iks_cluster_custom_worker_pool1_name
  	flavor           = var.iks_cluster_custom_worker_pool1_flavor
  	worker_count     = var.iks_cluster_custom_worker_pool1_count
  	
  	labels           = var.iks_cluster_custom_worker_pool1_labels
  	
  	resource_group_id = var.resource_group_id

	zones {
    	subnet_id = element(var.iks_subnet_ids, 0)
    	name      = format("%s-1", var.region)
  	}
  	
  	zones {
    	subnet_id =  element(var.iks_subnet_ids, 1)
    	name      = format("%s-2", var.region)
  	}
 
  	zones {
    	subnet_id =  element(var.iks_subnet_ids, 2)
    	name      = format("%s-3", var.region)
  	}
  	
  	timeouts {
  		create = var.iks_cluster_create_or_update_timeout
  		# update is not supported here 2021-06-08
		#update = var.iks_cluster_create_or_update_timeout
  	}	 
  
    taints {
      key = var.iks_cluster_custom_worker_pool1_taint_key_name
      value = var.iks_cluster_custom_worker_pool1_taint_value
      effect = var.iks_cluster_custom_worker_pool1_taint_effect
    }
}