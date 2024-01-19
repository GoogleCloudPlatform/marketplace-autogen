output "site_url" {
  description = "Site Url"
  value       = "http://${module.main.instance_ips[0]}/"
}

output "admin_url" {
  description = "Admin Url"
  value       = "http://${module.tier2.instance_ips[0]}/"
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

output "ghost_mysql_user" {
  description = "Username for Ghost MySQL password."
  value       = "ghost"
}

output "ghost_mysql_password" {
  description = "Password for Ghost MySQL."
  value       = random_password.ghost_mysql.result
  sensitive   = true
}

output "this_is_optional_user" {
  description = "Username for This is optional password."
  value       = "some-user"
}

output "this_is_optional_password" {
  description = "Password for This is optional."
  value       = random_password.this_is_optional.result
  sensitive   = true
}
