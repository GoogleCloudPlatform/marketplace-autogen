
output "site_url" {
  description = "Site Url"
  value       = "http://${module.main.instance_ips[0]}/"
}

output "admin_url" {
  description = "Admin Url"
  value       = "http://${module.tier2.instance_ips[0]}/"
}
