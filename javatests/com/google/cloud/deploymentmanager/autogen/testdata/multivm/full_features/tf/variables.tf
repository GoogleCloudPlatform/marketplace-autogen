variable "project_id" {
  description = "The ID of the project in which to provision resources."
  type        = string
}

variable "zone" {
  description = "The zone for the solution to be deployed."
  type        = string
  default     = "us-west1-b"
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
