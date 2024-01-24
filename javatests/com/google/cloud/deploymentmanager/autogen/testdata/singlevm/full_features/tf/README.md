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
| source_image | The image name for the disk for the VM instance. | `string` | `"projects/bitnami-launchpad/global/images/bitnami-wordpress-4-5-3-1-r16-linux-debian-8-x86-64"` | no |
| zone | The zone for the solution to be deployed. | `string` | `"us-west1-b"` | no |
| machine_type | The machine type to create, e.g. e2-small | `string` | `"f1-micro"` | no |
| boot_disk_type | The boot disk type for the VM instance. | `string` | `"pd-standard"` | no |
| boot_disk_size | The boot disk size for the VM instance in GBs | `number` | `10` | no |
| disk1_type | The super-extra-great disk | `string` | `"pd-standard"` | no |
| disk1_size | The super-extra-great disk size in GB | `number` | `1000` | no |
| disk2_type | The less great disk | `string` | `"pd-standard"` | no |
| disk2_size | The less great disk size in GB | `number` | `500` | no |
| disk3_type | The third disk | `string` | `"pd-standard"` | no |
| disk3_size | The third disk size in GB | `number` | `10` | no |
| networks | The network name to attach the VM instance. | `list(string)` | `["default"]` | no |
| sub_networks | The sub network name to attach the VM instance. | `list(string)` | `[]` | no |
| external_ips | The external IPs assigned to the VM for public access. | `list(string)` | `["EPHEMERAL"]` | no |
| ip_forward | Whether to allow sending and receiving of packets with non-matching source or destination IPs. | `bool` | `false` | no |
| enable_tcp_80 | Allow HTTP traffic from the Internet | `bool` | `true` | no |
| tcp_80_source_ranges | Source IP ranges for HTTP traffic | `string` | `""` | no |
| enable_tcp_443 | Allow HTTPS traffic from the Internet | `bool` | `true` | no |
| tcp_443_source_ranges | Source IP ranges for HTTPS traffic | `string` | `""` | no |
| enable_icmp | Allow ICMP traffic from the Internet | `bool` | `true` | no |
| icmp_source_ranges | Source IP ranges for ICMP traffic | `string` | `""` | no |
| accelerator_type | The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80. | `string` | `"nvidia-tesla-k80"` | no |
| accelerator_count | The number of the guest accelerator cards exposed to this instance. | `number` | `1` | no |
| domain | Your Wordpress blog domain | `string` | `null` | yes |
| adminEmailAddress | The e-mail address used to create the administrator account for WordPress. | `string` | `null` | yes |
| optionalEmailAddress | Please enter a valid email address | `string` | `"default_value@example.com"` | no |
| showConditionalRowAndAction |  | `bool` | `true` | no |
| generateOptionalPassword |  | `bool` | `true` | no |
| installPhpMyAdmin |  | `bool` | `true` | no |
| imageCaching |  | `string` | `"none"` | no |
| imageCompression |  | `bool` | `false` | no |
| imageSizing |  | `bool` | `true` | no |
| imageCacheSize |  | `number` | `50000` | no |
| cacheExpiration |  | `number` | `120` | no |
| thirdDiskName |  | `string` | `"third-disk"` | no |
| extraLbZone0 |  | `string` | `"us-west1-a"` | no |
| extraLbZone1 |  | `string` | `null` | yes |
| enable_cloud_logging | Enables Cloud Logging. | `bool` | `true` | no |
| enable_cloud_monitoring | Enables Cloud Monitoring. | `bool` | `false` | no |
| enable_compute_api | Allow read write access to Google Compute Engine APIs on the VM | `bool` | `false` | no |
| enable_compute_readonly_api | Allow read access to Google Compute Engine APIs on the VM | `bool` | `true` | no |

## Outputs

| Name | Description |
|------|-------------|
| site_url | Site Url |
| admin_url | Admin Url |
| admin_user | Username for Admin password. |
| admin_password | Password for Admin. |
| mysql_root_user | Username for MySQL root password. |
| mysql_root_password | Password for MySQL root. |
| this_is_optional_user | Username for This is optional password. |
| this_is_optional_password | Password for This is optional. |
| instance_self_link | Self-link for the compute instance. |
| instance_zone | Zone for the compute instance. |
| instance_machine_type | Machine type for the compute instance. |
| instance_nat_ip | External IP of the compute instance. |
| instance_network | Self-link for the network of the compute instance. |

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
