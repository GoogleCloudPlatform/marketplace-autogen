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
  default     = "us-west1-b"
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
  default     = ["EPHEMERAL"]
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
