variable "project_id" {
  description = "The ID of the project in which to provision resources."
  type        = string
}

// Marketplace requires this variable name to be declared
variable "goog_cm_deployment_name" {
  description = "The name of the deployment and VM instance."
  type        = string
}

variable "zone" {
  description = "The zone for the solution to be deployed."
  type        = string
}

variable "domain" {
  description = "Your domain"
  type        = string
  default     = "google.com"
}

variable "adminEmailAddress" {
  description = "The e-mail address used to create the administrator account for WordPress."
  type        = string
  default     = "aX9-YD_8W.3@example.com"
}

variable "optionalEmailAddress" {
  description = "Please enter a valid email address"
  type        = string
}

variable "showConditionals" {
  type        = bool
  default     = true
}

variable "generateOptionalPassword" {
  type        = bool
  default     = true
}

variable "tier2LocalSSDs" {
  type        = number
  default     = 0
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

variable "main_instance_count" {
  description = "A custom description for instance count."
  type        = number
  default     = 3
}

variable "main_source_image" {
  description = "The image name for the disk for the VM instance."
  type        = string
  default     = "projects/click-to-deploy-images/global/images/jenkins-v20180130"
}
variable "main_machine_type" {
  description = "The machine type to create, e.g. e2-small"
  type        = string
  default     = "e2-standard-2"
}

variable "main_boot_disk_type" {
  description = "The boot disk type for the VM instance."
  type        = string
  default     = "pd-standard"
}

variable "main_boot_disk_size" {
  description = "The boot disk size for the VM instance in GBs"
  type        = number
  default     = 10
}

variable "main_disk1_type" {
  description = "Very-extra disk"
  type        = string
  default     = "pd-standard"
}

variable "main_disk1_size" {
  description = "Very-extra disk size in GB"
  type        = number
  default     = 10
}

variable "main_disk2_type" {
  description = "Extra disk"
  type        = string
  default     = "pd-standard"
}

variable "main_disk2_size" {
  description = "Extra disk size in GB"
  type        = number
  default     = 10
}

variable "main_disk3_type" {
  description = "Super Extra disk"
  type        = string
  default     = "pd-standard"
}

variable "main_disk3_size" {
  description = "Super Extra disk size in GB"
  type        = number
  default     = 10
}

variable "main_networks" {
  description = "The network name to attach the VM instance."
  type        = list(string)
  default     = ["default"]
}

variable "main_sub_networks" {
  description = "The sub network name to attach the VM instance."
  type        = list(string)
  default     = []
}

variable "main_external_ips" {
  description = "The external IPs assigned to the VM for public access."
  type        = list(string)
  default     = ["NONE"]
}

variable "main_enable_tcp_80" {
  description = "Allow HTTP traffic from the Internet"
  type        = bool
  default     = true
}

variable "main_tcp_80_source_ranges" {
  description = "Source IP ranges for HTTP traffic"
  type        = string
  default     = ""
}

variable "main_enable_tcp_443" {
  description = "Allow HTTPS traffic from the Internet"
  type        = bool
  default     = true
}

variable "main_tcp_443_source_ranges" {
  description = "Source IP ranges for HTTPS traffic"
  type        = string
  default     = ""
}

variable "main_enable_icmp" {
  description = "Allow ICMP traffic from other VMs in this deployment"
  type        = bool
  default     = true
}

variable "main_enable_tcp_7000-7001" {
  description = "Allow TCP port 7000-7001 traffic between VMs in this group"
  type        = bool
  default     = true
}

variable "main_enable_udp" {
  description = "Allow UDP traffic from the Internet"
  type        = bool
  default     = true
}

variable "main_udp_source_ranges" {
  description = "Source IP ranges for UDP traffic"
  type        = string
  default     = ""
}

variable "main_enable_cloud_readonly_api" {
  description = "Allow read only access to all of Google Cloud Platform APIs on the VM"
  type        = bool
  default     = true
}

variable "main_enable_cloud_api" {
  description = "Allow full access to all of Google Cloud Platform APIs on the VM"
  type        = bool
  default     = true
}

variable "main_enable_compute_readonly_api" {
  description = "Allow read access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = true
}

variable "main_enable_compute_api" {
  description = "Allow read write access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = true
}

variable "main_enable_source_read_write_api" {
  description = "Allow read write access to Google Cloud Source Repositories APIs on the VM"
  type        = bool
  default     = true
}

variable "main_enable_projecthosting_api" {
  description = "Allow project hosting access to (Deprecated) Google Code Project Hosting APIs on the VM"
  type        = bool
  default     = true
}

variable "tier2_instance_count" {
  type        = number
  default     = 2
}

variable "tier2_source_image" {
  description = "The image name for the disk for the VM instance."
  type        = string
  default     = "projects/click-to-deploy-images/global/images/jenkins-v20180130"
}
variable "tier2_machine_type" {
  description = "The machine type to create, e.g. e2-small"
  type        = string
  default     = "e2-standard-2"
}

variable "tier2_boot_disk_type" {
  description = "The boot disk type for the VM instance."
  type        = string
  default     = "pd-standard"
}

variable "tier2_boot_disk_size" {
  description = "The boot disk size for the VM instance in GBs"
  type        = number
  default     = 10
}

variable "tier2_networks" {
  description = "The network name to attach the VM instance."
  type        = list(string)
  default     = ["default"]
}

variable "tier2_sub_networks" {
  description = "The sub network name to attach the VM instance."
  type        = list(string)
  default     = []
}

variable "tier2_external_ips" {
  description = "The external IPs assigned to the VM for public access."
  type        = list(string)
  default     = ["EPHEMERAL"]
}

variable "tier2_enable_tcp_9878" {
  description = "Allow TCP port 9878 traffic between VMs in this group"
  type        = bool
  default     = true
}

variable "tier2_enable_udp_2555" {
  description = "Allow UDP port 2555 traffic from other VMs in this deployment"
  type        = bool
  default     = true
}

variable "tier2_accelerator_type" {
  description = "The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80."
  type        = string
  default     = "nvidia-tesla-k80"
}

variable "tier2_accelerator_count" {
  description = "The number of the guest accelerator cards exposed to this instance."
  type        = number
  default     = "2"
}

variable "tier3_instance_count" {
  description = "Specify a value between 1 and 10."
  type        = number
  default     = 1
}

variable "tier3_source_image" {
  description = "The image name for the disk for the VM instance."
  type        = string
  default     = "projects/click-to-deploy-images/global/images/jenkins-v20180130"
}
variable "tier3_machine_type" {
  description = "The machine type to create, e.g. e2-small"
  type        = string
  default     = "e2-standard-2"
}

variable "tier3_boot_disk_type" {
  description = "The boot disk type for the VM instance."
  type        = string
  default     = "pd-standard"
}

variable "tier3_boot_disk_size" {
  description = "The boot disk size for the VM instance in GBs"
  type        = number
  default     = 10
}

variable "tier3_networks" {
  description = "The network name to attach the VM instance."
  type        = list(string)
  default     = ["default"]
}

variable "tier3_sub_networks" {
  description = "The sub network name to attach the VM instance."
  type        = list(string)
  default     = []
}

variable "tier3_external_ips" {
  description = "The external IPs assigned to the VM for public access."
  type        = list(string)
  default     = ["EPHEMERAL"]
}

variable "tier3_enable_tcp_9000" {
  description = "Allow TCP port 9000 traffic between VMs in this group"
  type        = bool
  default     = true
}

variable "tier3_enable_udp_2333" {
  description = "Allow UDP port 2333 traffic from other VMs in this deployment"
  type        = bool
  default     = true
}

variable "tier3_accelerator_type" {
  description = "The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80."
  type        = string
  default     = "nvidia-tesla-v100"
}

variable "tier3_accelerator_count" {
  description = "The number of the guest accelerator cards exposed to this instance."
  type        = number
  default     = "0"
}
