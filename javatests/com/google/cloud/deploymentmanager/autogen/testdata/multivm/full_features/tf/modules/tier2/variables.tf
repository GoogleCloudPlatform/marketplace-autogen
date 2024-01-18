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

variable "enable_tcp_9878" {
  description = "Allow TCP port 9878 traffic from the Internet"
  type        = bool
  default     = true
}

variable "tcp_9878_source_ranges" {
  description = "Source IP ranges for TCP port 9878 traffic"
  type        = string
  default     = ""
}

variable "enable_udp_2555" {
  description = "Allow UDP port 2555 traffic from the Internet"
  type        = bool
  default     = true
}

variable "udp_2555_source_ranges" {
  description = "Source IP ranges for UDP port 2555 traffic"
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
