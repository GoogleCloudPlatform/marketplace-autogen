locals {
  network_interface = google_compute_instance.instance.network_interface[0]
}

output "admin_user" {
  description = "Username for Admin password."
  value       = "user"
}

output "admin_password" {
  description = "Password for Admin."
  value       = random_password.admin.result
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
