locals {
  metadata = {
    admin-password = var.admin_password
    ghost-db-password = var.ghost_mysql_password
    optional-password = var.this_is_optional_password
    google-logging-enable = var.enable_cloud_logging ? "1" : "0"
    google-monitoring-enable = var.enable_cloud_monitoring ? "1" : "0"
  }
}

resource "google_compute_instance" "instance" {
  count = var.instance_count
  name = "${var.deployment_name}-main-vm-${count.index}"
  zone = var.zone
  machine_type = "e2-medium"
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
    }
  }

  metadata = local.metadata

  network_interface {
    network = "default"
  }
}
