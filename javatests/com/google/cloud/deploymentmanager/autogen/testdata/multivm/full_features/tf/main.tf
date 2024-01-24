provider "google" {
  project  = var.project_id
}

module "main" {
  source          = "./modules/main"
  count           = var.main_instance_count > 0 ? 1 : 0

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.main_instance_count
  zone            = var.zone

  source_image    = var.main_source_image
  machine_type    = var.main_machine_type
  boot_disk_type  = var.main_boot_disk_type
  boot_disk_size  = var.main_boot_disk_size

  disk1_type = var.main_disk1_type
  disk1_size = var.main_disk1_size
  disk2_type = var.main_disk2_type
  disk2_size = var.main_disk2_size
  disk3_type = var.main_disk3_type
  disk3_size = var.main_disk3_size

  networks        = var.main_networks
  sub_networks    = var.main_sub_networks
  external_ips    = var.main_external_ips
  enable_tcp_80  = var.main_enable_tcp_80
  tcp_80_source_ranges = var.main_tcp_80_source_ranges
  enable_tcp_443  = var.main_enable_tcp_443
  tcp_443_source_ranges = var.main_tcp_443_source_ranges
  enable_icmp  = var.main_enable_icmp
  enable_tcp_7000-7001  = var.main_enable_tcp_7000-7001
  enable_udp  = var.main_enable_udp
  udp_source_ranges = var.main_udp_source_ranges

  enable_cloud_readonly_api = var.main_enable_cloud_readonly_api
  enable_cloud_api = var.main_enable_cloud_api
  enable_compute_readonly_api = var.main_enable_compute_readonly_api
  enable_compute_api = var.main_enable_compute_api
  enable_source_read_write_api = var.main_enable_source_read_write_api
  enable_projecthosting_api = var.main_enable_projecthosting_api
  admin_password = random_password.admin.result
  ghost_mysql_password = random_password.ghost_mysql.result
  this_is_optional_password = random_password.this_is_optional.result

  domain = var.domain
  generateOptionalPassword = var.generateOptionalPassword
  superExtraDiskName = var.superExtraDiskName
  imageCaching = var.imageCaching
  imageCompression = var.imageCompression
  imageSizing = var.imageSizing
  extraLbZone0 = var.extraLbZone0
  extraLbZone1 = var.extraLbZone1
  tier2_instance_count = var.tier2_instance_count
  enable_cloud_logging    = var.enable_cloud_logging
  enable_cloud_monitoring = var.enable_cloud_monitoring
}

module "tier2" {
  source          = "./modules/tier2"
  count           = var.tier2_instance_count > 0 ? 1 : 0

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier2_instance_count
  zone            = var.zone

  source_image    = var.tier2_source_image
  machine_type    = var.tier2_machine_type
  boot_disk_type  = var.tier2_boot_disk_type
  boot_disk_size  = var.tier2_boot_disk_size

  networks        = var.tier2_networks
  sub_networks    = var.tier2_sub_networks
  external_ips    = var.tier2_external_ips
  enable_tcp_9878  = var.tier2_enable_tcp_9878
  enable_udp_2555  = var.tier2_enable_udp_2555

  accelerator_type  = var.tier2_accelerator_type
  accelerator_count = var.tier2_accelerator_count

  admin_password = random_password.admin.result
  ghost_mysql_password = random_password.ghost_mysql.result
  this_is_optional_password = random_password.this_is_optional.result

  domain = var.domain
  showConditionals = var.showConditionals
  generateOptionalPassword = var.generateOptionalPassword
  tier2LocalSSDs = var.tier2LocalSSDs
  imageCaching = var.imageCaching
  imageCompression = var.imageCompression
  imageSizing = var.imageSizing
  extraLbZone0 = var.extraLbZone0
  extraLbZone1 = var.extraLbZone1
  main_instance_count = var.main_instance_count
  enable_cloud_logging    = var.enable_cloud_logging
  enable_cloud_monitoring = var.enable_cloud_monitoring
}

module "tier3" {
  source          = "./modules/tier3"
  count           = var.tier3_instance_count > 0 ? 1 : 0

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier3_instance_count
  zone            = var.zone

  source_image    = var.tier3_source_image
  machine_type    = var.tier3_machine_type
  boot_disk_type  = var.tier3_boot_disk_type
  boot_disk_size  = var.tier3_boot_disk_size

  networks        = var.tier3_networks
  sub_networks    = var.tier3_sub_networks
  external_ips    = var.tier3_external_ips
  enable_tcp_9000  = var.tier3_enable_tcp_9000
  enable_udp_2333  = var.tier3_enable_udp_2333

  accelerator_type  = var.tier3_accelerator_type
  accelerator_count = var.tier3_accelerator_count

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
