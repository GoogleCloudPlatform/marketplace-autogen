variable "deployment_name" {
  type        = string
  description = "The name of the deployment and VM instance."
}

variable "zone" {
  type        = string
  description = "The zone for the solution to be deployed."
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

variable "instance_count" {
  type        = number
}
