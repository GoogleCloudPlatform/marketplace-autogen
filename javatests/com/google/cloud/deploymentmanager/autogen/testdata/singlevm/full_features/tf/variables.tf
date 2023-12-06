variable "project_id" {
  description = "The ID of the project in which to provision resources."
  type        = string
}

// Marketplace requires this variable name to be declared
variable "goog_cm_deployment_name" {
  description = "The name of the deployment and VM instance."
  type        = string
}

variable "source_image" {
  description = "The image name for the disk for the VM instance."
  type        = string
  default     = "projects/bitnami-launchpad/global/images/bitnami-wordpress-4-5-3-1-r16-linux-debian-8-x86-64"
}

variable "zone" {
  description = "The zone for the solution to be deployed."
  type        = string
  default     = "us-west1-b"
}

variable "machine_type" {
  description = "The machine type to create, e.g. e2-small"
  type        = string
  default     = "f1-micro"
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
  description = "The super-extra-great disk"
  type        = string
  default     = "pd-standard"
}

variable "disk1_size" {
  description = "The super-extra-great disk size in GB"
  type        = number
  default     = 1000
}

variable "disk2_type" {
  description = "The less great disk"
  type        = string
  default     = "pd-standard"
}

variable "disk2_size" {
  description = "The less great disk size in GB"
  type        = number
  default     = 500
}

variable "disk3_type" {
  description = "The third disk"
  type        = string
  default     = "pd-standard"
}

variable "disk3_size" {
  description = "The third disk size in GB"
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
  default     = ["EPHEMERAL"]
}

variable "ip_forward" {
  description = "Whether to allow sending and receiving of packets with non-matching source or destination IPs."
  type        = bool
  default     = false
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
  description = "Allow ICMP traffic from the Internet"
  type        = bool
  default     = true
}

variable "icmp_source_ranges" {
  description = "Source IP ranges for ICMP traffic"
  type        = string
  default     = ""
}

variable "accelerator_type" {
  description = "The accelerator type resource exposed to this instance. E.g. nvidia-tesla-k80."
  type        = string
  default     = "nvidia-tesla-k80"
}

variable "accelerator_count" {
  description = "The number of the guest accelerator cards exposed to this instance."
  type        = number
  default     = "1"
}

variable "domain" {
  description = "Your Wordpress blog domain"
  type        = string
}

variable "adminEmailAddress" {
  description = "The e-mail address used to create the administrator account for WordPress."
  type        = string
}

variable "optionalEmailAddress" {
  type        = string
  default     = "default_value@example.com"
}

variable "showConditionalRowAndAction" {
  type        = bool
  default     = true
}

variable "generateOptionalPassword" {
  type        = bool
  default     = true
}

variable "installPhpMyAdmin" {
  type        = bool
  default     = true
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

variable "imageCacheSize" {
  type        = number
  default     = 50000
}

variable "cacheExpiration" {
  type        = number
  default     = 120
}

variable "thirdDiskName" {
  type        = string
  default     = "third-disk"
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
  default     = true
}

variable "enable_cloud_monitoring" {
  description = "Enables Cloud Monitoring."
  type        = bool
  default     = false
}

variable "enable_compute_api" {
  description = "Allow read write access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = false
}

variable "enable_compute_readonly_api" {
  description = "Allow read access to Google Compute Engine APIs on the VM"
  type        = bool
  default     = true
}
