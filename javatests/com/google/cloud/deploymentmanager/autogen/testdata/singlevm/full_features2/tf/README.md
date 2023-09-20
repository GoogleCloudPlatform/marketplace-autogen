# Google Cloud Marketplace Terraform Module

This module deploys a product from Google Cloud Marketplace.

## Usage
Perform the following commands on the root folder:

- `terraform init` to get the plugins
- `terraform plan` to see the infrastructure plan
- `terraform apply` to apply the infrastructure build
- `terraform destroy` to destroy the built infrastructure

## Inputs

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
| instance_zone | Self-link for the compute instance. |
| instance_machine_type | Machine type for the compute instance. |
| instance_nat_ip | External IP of the compute instance. |
| instance_network | Machine type for the compute instance. |

## Requirements
### Terraform plugins
- [Terraform](https://developer.hashicorp.com/terraform/downloads)

### Configure a Service Account
In order to execute this module you must have a Service Account with the following project roles:

- roles/compute.admin
- roles/iam.serviceAccountUser
- roles/compute.networkAdmin

`roles/compute.networkAdmin` is required on the host project if a shared VPC is used.

### Enable API
In order to operate with the Service Account you must activate the following APIs on the project where the Service Account was created:

- Compute Engine API - compute.googleapis.com
