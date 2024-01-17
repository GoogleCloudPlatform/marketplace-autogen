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

variable "main_instance_count" {
  description = "A custom description for instance count."
  type        = number
  default     = 3
}

variable "tier2_instance_count" {
  type        = number
  default     = 2
}

variable "tier3_instance_count" {
  description = "Specify a value between 1 and 10."
  type        = number
  default     = 1
}
