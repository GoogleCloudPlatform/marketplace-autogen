variable "deployment_name" {
  type        = string
  description = "The name of the deployment and VM instance."
}

variable "zone" {
  description = "The zone for the solution to be deployed."
  type        = string
  default     = "us-west1-b"
}

variable "instance_count" {
  type        = number
}

variable "source_image" {
  description = "The image name for the disk for the VM instance."
  type        = string
  default     = "projects/click-to-deploy-images/global/images/jenkins-v20180130"
}

variable "machine_type" {
  description = "The machine type to create, e.g. e2-small"
  type        = string
  default     = "e2-standard-2"
}

variable "boot_disk_type" {
  description = "The boot disk type for the VM instance."
  type        = string
  default     = "pd-standard"
}

variable "boot_disk_size" {
  description = "The boot disk size for the VM instance in GBs"
  type        = number
  default     = 10
}

variable "disk1_type" {
  description = "Very-extra disk"
  type        = string
  default     = "pd-standard"
}

variable "disk1_size" {
  description = "Very-extra disk size in GB"
  type        = number
  default     = 10
}

variable "disk2_type" {
  description = "Extra disk"
  type        = string
  default     = "pd-standard"
}

variable "disk2_size" {
  description = "Extra disk size in GB"
  type        = number
  default     = 10
}

variable "disk3_type" {
  description = "Super Extra disk"
  type        = string
  default     = "pd-standard"
}

variable "disk3_size" {
  description = "Super Extra disk size in GB"
  type        = number
  default     = 10
}

variable "networks" {
  description = "The network name to attach the VM instance."
  type        = list(string)
  default     = ["default"]
}

variable "sub_networks" {
  description = "The sub network name to attach the VM instance."
  type        = list(string)
  default     = []
}

variable "external_ips" {
  description = "The external IPs assigned to the VM for public access."
  type        = list(string)
  default     = ["NONE"]
}

variable "enable_tcp_80" {
  description = "Allow HTTP traffic from the Internet"
  type        = bool
  default     = true
}

variable "tcp_80_source_ranges" {
  description = "Source IP ranges for HTTP traffic"
  type        = string
  default     = ""
}

variable "enable_tcp_443" {
  description = "Allow HTTPS traffic from the Internet"
  type        = bool
  default     = true
}

variable "tcp_443_source_ranges" {
  description = "Source IP ranges for HTTPS traffic"
  type        = string
  default     = ""
}

variable "enable_icmp" {
  description = "Allow ICMP traffic from other VMs in this deployment"
  type        = bool
  default     = true
}

variable "enable_tcp_7000-7001" {
  description = "Allow TCP port 7000-7001 traffic between VMs in this group"
  type        = bool
  default     = true
}

variable "enable_udp" {
  description = "Allow UDP traffic from the Internet"
  type        = bool
  default     = true
}

variable "udp_source_ranges" {
  description = "Source IP ranges for UDP traffic"
  type        = string
  default     = ""
}

variable "admin_password" {
  type        = string
  description = "Password for Admin."
  sensitive   = true
}

variable "ghost_mysql_password" {
  type        = string
  description = "Password for Ghost MySQL."
  sensitive   = true
}

variable "this_is_optional_password" {
  type        = string
  description = "Password for This is optional."
  sensitive   = true
}

variable "domain" {
  description = "Your domain"
  type        = string
  default     = "google.com"
}

variable "generateOptionalPassword" {
  type        = bool
  default     = true
}

variable "superExtraDiskName" {
  type        = string
  default     = "the-super-extra-disk"
}

variable "imageCaching" {
  type        = string
  default     = "none"
}

variable "imageCompression" {
  type        = bool
  default     = false
}

variable "imageSizing" {
  type        = bool
  default     = true
}

variable "extraLbZone0" {
  type        = string
  default     = "us-west1-a"
}

variable "extraLbZone1" {
  type        = string
}

variable "tier2_instance_count" {
  type        = number
}

variable "enable_cloud_logging" {
  description = "Enables Cloud Logging."
  type        = bool
  default     = false
}

variable "enable_cloud_monitoring" {
  description = "Enables Cloud Monitoring."
  type        = bool
  default     = true
}

variable "enable_cloud_readonly_api" {
  description = "Allow read only access to all of Google Cloud Platform APIs on the VM"
  type        = bool
  default     = true
}

variable "enable_cloud_api" {
  description = "Allow full access to all of Google Cloud Platform APIs on the VM"
  type        = bool
  default     = true
}

variable "enable_compute_readonly_api" {
  description = "Allow read access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = true
}

variable "enable_compute_api" {
  description = "Allow read write access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = true
}

variable "enable_source_read_write_api" {
  description = "Allow read write access to Google Cloud Source Repositories APIs on the VM"
  type        = bool
  default     = true
}

variable "enable_projecthosting_api" {
  description = "Allow project hosting access to (Deprecated) Google Code Project Hosting APIs on the VM"
  type        = bool
  default     = true
}
