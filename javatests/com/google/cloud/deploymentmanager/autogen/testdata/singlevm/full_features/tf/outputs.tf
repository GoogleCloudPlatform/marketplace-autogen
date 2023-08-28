locals {
  network_interface = google_compute_instance.instance.network_interface[0]
}

output "admin_user" {
  description = "Username for Admin password."
  value       = var.domain
}

output "admin_password" {
  description = "Password for Admin."
  value       = random_password.admin.result
  sensitive   = true
}

output "mysql_root_user" {
  description = "Username for MySQL root password."
  value       = "root"
}

output "mysql_root_password" {
  description = "Password for MySQL root."
  value       = random_password.mysql_root.result
  sensitive   = true
}

output "this_is_optional_user" {
  description = "Username for This is optional password."
  value       = "root2"
}

output "this_is_optional_password" {
  description = "Password for This is optional."
  value       = random_password.this_is_optional.result
  sensitive   = true
}

output "instance_self_link" {
  description = "Self-link for the compute instance."
  value       = google_compute_instance.instance.self_link
}

output "instance_zone" {
  description = "Self-link for the compute instance."
  value       = var.zone
}

output "instance_machine_type" {
  description = "Machine type for the compute instance."
  value       = var.machine_type
}

output "instance_nat_ip" {
  description = "External IP of the compute instance."
  value       = length(local.network_interface.access_config) > 0 ? local.network_interface.access_config[0].nat_ip : null
}

output "instance_network" {
  description = "Machine type for the compute instance."
  value       = local.network_interface
}
