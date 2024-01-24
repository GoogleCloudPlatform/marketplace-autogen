# Google Cloud Marketplace Terraform Module

This module deploys a product from Google Cloud Marketplace.

## Usage
The provided test configuration can be used by executing:

```
terraform plan --var-file marketplace_test.tfvars --var project_id=<YOUR_PROJECT>
```

## Inputs
| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
| project_id | The ID of the project in which to provision resources. | `string` | `null` | yes |
| goog_cm_deployment_name | The name of the deployment and VM instance. | `string` | `null` | yes |
| zone | The zone for the solution to be deployed. | `string` | `null` | yes |
| domain | Your domain | `string` | `"google.com"` | no |
| adminEmailAddress | The e-mail address used to create the administrator account for WordPress. | `string` | `"aX9-YD_8W.3@example.com"` | no |
| optionalEmailAddress | Please enter a valid email address | `string` | `null` | yes |
| showConditionals |  | `bool` | `true` | no |
| generateOptionalPassword |  | `bool` | `true` | no |
| tier2LocalSSDs |  | `number` | `0` | no |
| superExtraDiskName |  | `string` | `"the-super-extra-disk"` | no |
| imageCaching |  | `string` | `"none"` | no |
| imageCompression |  | `bool` | `false` | no |
| imageSizing |  | `bool` | `true` | no |
| extraLbZone0 |  | `string` | `"us-west1-a"` | no |
| extraLbZone1 |  | `string` | `null` | yes |
| enable_cloud_logging | Enables Cloud Logging. | `bool` | `false` | no |
| enable_cloud_monitoring | Enables Cloud Monitoring. | `bool` | `true` | no |
| main_instance_count | A custom description for instance count. | `number` | `3` | no |
| main_source_image | The image name for the disk for the VM instance. | `string` | `"projects/click-to-deploy-images/global/images/jenkins-v20180130"` | no |
| main_machine_type | The machine type to create, e.g. e2-small | `string` | `"e2-standard-2"` | no |
| main_boot_disk_type | The boot disk type for the VM instance. | `string` | `"pd-standard"` | no |
| main_boot_disk_size | The boot disk size for the VM instance in GBs | `number` | `10` | no |
| main_disk1_type | Very-extra disk | `string` | `"pd-standard"` | no |
| main_disk1_size | Very-extra disk size in GB | `number` | `10` | no |
| main_disk2_type | Extra disk | `string` | `"pd-standard"` | no |
| main_disk2_size | Extra disk size in GB | `number` | `10` | no |
| main_disk3_type | Super Extra disk | `string` | `"pd-standard"` | no |
| main_disk3_size | Super Extra disk size in GB | `number` | `10` | no |
| main_networks | The network name to attach the VM instance. | `list(string)` | `["default"]` | no |
| main_sub_networks | The sub network name to attach the VM instance. | `list(string)` | `[]` | no |
| main_external_ips | The external IPs assigned to the VM for public access. | `list(string)` | `["NONE"]` | no |
| main_enable_tcp_80 | Allow HTTP traffic from the Internet | `bool` | `true` | no |
| main_tcp_80_source_ranges | Source IP ranges for HTTP traffic | `string` | `""` | no |
| main_enable_tcp_443 | Allow HTTPS traffic from the Internet | `bool` | `true` | no |
| main_tcp_443_source_ranges | Source IP ranges for HTTPS traffic | `string` | `""` | no |
| main_enable_icmp | Allow ICMP traffic from other VMs in this deployment | `bool` | `true` | no |
| main_enable_tcp_7000-7001 | Allow TCP port 7000-7001 traffic between VMs in this group | `bool` | `true` | no |
| main_enable_udp | Allow UDP traffic from the Internet | `bool` | `true` | no |
| main_udp_source_ranges | Source IP ranges for UDP traffic | `string` | `""` | no |
| main_enable_cloud_readonly_api | Allow read only access to all of Google Cloud Platform APIs on the VM | `bool` | `true` | no |
| main_enable_cloud_api | Allow full access to all of Google Cloud Platform APIs on the VM | `bool` | `true` | no |
| main_enable_compute_readonly_api | Allow read access to Google Compute Engine APIs on the VM | `bool` | `true` | no |
| main_enable_compute_api | Allow read write access to Google Compute Engine APIs on the VM | `bool` | `true` | no |
| main_enable_source_read_write_api | Allow read write access to Google Cloud Source Repositories APIs on the VM | `bool` | `true` | no |
| main_enable_projecthosting_api | Allow project hosting access to (Deprecated) Google Code Project Hosting APIs on the VM | `bool` | `true` | no |
| tier2_instance_count |  | `number` | `2` | no |
| tier2_source_image | The image name for the disk for the VM instance. | `string` | `"projects/click-to-deploy-images/global/images/jenkins-v20180130"` | no |
| tier2_machine_type | The machine type to create, e.g. e2-small | `string` | `"e2-standard-2"` | no |
| tier2_boot_disk_type | The boot disk type for the VM instance. | `string` | `"pd-standard"` | no |
| tier2_boot_disk_size | The boot disk size for the VM instance in GBs | `number` | `10` | no |
| tier2_networks | The network name to attach the VM instance. | `list(string)` | `["default"]` | no |
| tier2_sub_networks | The sub network name to attach the VM instance. | `list(string)` | `[]` | no |
| tier2_external_ips | The external IPs assigned to the VM for public access. | `list(string)` | `["EPHEMERAL"]` | no |
| tier2_enable_tcp_9878 | Allow TCP port 9878 traffic between VMs in this group | `bool` | `true` | no |
| tier2_enable_udp_2555 | Allow UDP port 2555 traffic from other VMs in this deployment | `bool` | `true` | no |
| tier2_accelerator_type | The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80. | `string` | `"nvidia-tesla-k80"` | no |
| tier2_accelerator_count | The number of the guest accelerator cards exposed to this instance. | `number` | `2` | no |
| tier3_instance_count | Specify a value between 1 and 10. | `number` | `1` | no |
| tier3_source_image | The image name for the disk for the VM instance. | `string` | `"projects/click-to-deploy-images/global/images/jenkins-v20180130"` | no |
| tier3_machine_type | The machine type to create, e.g. e2-small | `string` | `"e2-standard-2"` | no |
| tier3_boot_disk_type | The boot disk type for the VM instance. | `string` | `"pd-standard"` | no |
| tier3_boot_disk_size | The boot disk size for the VM instance in GBs | `number` | `10` | no |
| tier3_networks | The network name to attach the VM instance. | `list(string)` | `["default"]` | no |
| tier3_sub_networks | The sub network name to attach the VM instance. | `list(string)` | `[]` | no |
| tier3_external_ips | The external IPs assigned to the VM for public access. | `list(string)` | `["EPHEMERAL"]` | no |
| tier3_enable_tcp_9000 | Allow TCP port 9000 traffic between VMs in this group | `bool` | `true` | no |
| tier3_enable_udp_2333 | Allow UDP port 2333 traffic from other VMs in this deployment | `bool` | `true` | no |
| tier3_accelerator_type | The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80. | `string` | `"nvidia-tesla-v100"` | no |
| tier3_accelerator_count | The number of the guest accelerator cards exposed to this instance. | `number` | `0` | no |

## Outputs

| Name | Description |
|------|-------------|
| total_instance_count | Total compute instances. |
| site_url | Site Url |
| admin_url | Admin Url |
| admin_user | Username for Admin password. |
| admin_password | Password for Admin. |
| ghost_mysql_user | Username for Ghost MySQL password. |
| ghost_mysql_password | Password for Ghost MySQL. |
| this_is_optional_user | Username for This is optional password. |
| this_is_optional_password | Password for This is optional. |

## Requirements
### Terraform

Be sure you have the correct Terraform version (1.2.0+), you can choose the binary here:

https://releases.hashicorp.com/terraform/

### Configure a Service Account
In order to execute this module you must have a Service Account with the following project roles:

- `roles/compute.admin`
- `roles/iam.serviceAccountUser`

If you are using a shared VPC:

- `roles/compute.networkAdmin` is required on the Shared VPC host project.

### Enable API
In order to operate with the Service Account you must activate the following APIs on the project where the Service Account was created:

- Compute Engine API - `compute.googleapis.com`
