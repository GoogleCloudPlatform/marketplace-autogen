provider "google" {
  project  = var.project_id
}

module "main" {
  source          = "./modules/main"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.main_instance_count
  zone            = var.zone

  source_image    = var.main_source_image
  machine_type    = var.main_machine_type
  boot_disk_type  = var.main_boot_disk_type
  boot_disk_size  = var.main_boot_disk_size

  admin_password = random_password.admin.result
  ghost_mysql_password = random_password.ghost_mysql.result
  this_is_optional_password = random_password.this_is_optional.result
  enable_cloud_logging    = var.enable_cloud_logging
  enable_cloud_monitoring = var.enable_cloud_monitoring
}

module "tier2" {
  source          = "./modules/tier2"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier2_instance_count
  zone            = var.zone

  source_image    = var.tier2_source_image
  machine_type    = var.tier2_machine_type
  boot_disk_type  = var.tier2_boot_disk_type
  boot_disk_size  = var.tier2_boot_disk_size

  admin_password = random_password.admin.result
  ghost_mysql_password = random_password.ghost_mysql.result
  this_is_optional_password = random_password.this_is_optional.result
  enable_cloud_logging    = var.enable_cloud_logging
  enable_cloud_monitoring = var.enable_cloud_monitoring
}

module "tier3" {
  source          = "./modules/tier3"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier3_instance_count
  zone            = var.zone

  source_image    = var.tier3_source_image
  machine_type    = var.tier3_machine_type
  boot_disk_type  = var.tier3_boot_disk_type
  boot_disk_size  = var.tier3_boot_disk_size

  admin_password = random_password.admin.result
  ghost_mysql_password = random_password.ghost_mysql.result
  this_is_optional_password = random_password.this_is_optional.result
  enable_cloud_logging    = var.enable_cloud_logging
  enable_cloud_monitoring = var.enable_cloud_monitoring
}


resource "random_password" "admin" {
  length = 8
  special = false
}

resource "random_password" "ghost_mysql" {
  length = 8
  special = false
}

resource "random_password" "this_is_optional" {
  length = 8
  special = false
}
