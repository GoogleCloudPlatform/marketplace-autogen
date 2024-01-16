locals {
  network_interfaces = google_compute_instance.instance[*].network_interface[0]
  instance_ips       = [for nic in local.network_interfaces:  length(nic.access_config) > 0 ? nic.access_config[0].nat_ip : nic.network_ip]
}
output "instance_ips" {
  description = "The IPs of all compute instances."
  value = local.instance_ips
}
