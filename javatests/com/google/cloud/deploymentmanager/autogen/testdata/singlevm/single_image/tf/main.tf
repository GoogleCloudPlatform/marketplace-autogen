provider "google" {
  project = var.project_id
}

locals {
  network_interfaces = [ for i, n in var.networks : {
    network     = n,
    subnetwork  = length(var.sub_networks) > i ? element(var.sub_networks, i) : null
    external_ip = length(var.external_ips) > i ? element(var.external_ips, i) : "NONE"
    }
  ]

  metadata = {
    bitnami-base-password = random_password.admin.result
    google-logging-enable = var.enable_cloud_logging ? "1" : "0"
    google-monitoring-enable = "0"
  }
}

resource "google_compute_instance" "instance" {
  name = "${var.goog_cm_deployment_name}-vm"
  machine_type = var.machine_type
  zone = var.zone

  tags = ["${var.goog_cm_deployment_name}-deployment"]

  boot_disk {
    device_name = "wordpress-vm-tmpl-boot-disk"

    initialize_params {
      size = var.boot_disk_size
      type = var.boot_disk_type
      image = var.source_image
    }
  }

  dynamic "scratch_disk" {
    for_each = range(var.localSsdsCount)
    content {
      interface = "SCSI"
    }
  }

  metadata = local.metadata

  dynamic "network_interface" {
    for_each = local.network_interfaces
    content {
      network = network_interface.value.network
      subnetwork = network_interface.value.subnetwork

      dynamic "access_config" {
        for_each = network_interface.value.external_ip == "NONE" ? [] : [1]
        content {
          nat_ip = network_interface.value.external_ip == "EPHEMERAL" ? null : network_interface.value.external_ip
        }
      }
    }
  }

  service_account {
    email = "default"
    scopes = compact([
      "https://www.googleapis.com/auth/cloud.useraccounts.readonly",
      "https://www.googleapis.com/auth/devstorage.read_only",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write"
    ])
  }
}

resource "google_compute_firewall" tcp_80 {
  count = var.enable_tcp_80 ? 1 : 0

  name = "${var.goog_cm_deployment_name}-tcp-80"
  network = element(var.networks, 0)

  allow {
    ports = ["80"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_80_source_ranges) : trimspace(range)])

  target_tags = ["${var.goog_cm_deployment_name}-deployment"]
}

resource "google_compute_firewall" tcp_443 {
  count = var.enable_tcp_443 ? 1 : 0

  name = "${var.goog_cm_deployment_name}-tcp-443"
  network = element(var.networks, 0)

  allow {
    ports = ["443"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_443_source_ranges) : trimspace(range)])

  target_tags = ["${var.goog_cm_deployment_name}-deployment"]
}

resource "random_password" "admin" {
  length = 8
  special = false
}
