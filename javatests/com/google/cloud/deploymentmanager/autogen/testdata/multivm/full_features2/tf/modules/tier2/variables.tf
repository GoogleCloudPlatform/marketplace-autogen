variable "deployment_name" {
  type        = string
  description = "The name of the deployment and VM instance."
}

variable "zone" {
  type        = string
  description = "The zone for the solution to be deployed."
}

variable "instance_count" {
  type        = number
}
