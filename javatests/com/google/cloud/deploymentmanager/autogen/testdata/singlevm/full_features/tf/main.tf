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
    bitnami-db-password = random_password.mysql_root.result
    optional-password = random_password.this_is_optional.result
    admin-username = "admin@local"
    user-username = "user@local"
    some-other-domain-metadata = var.domain
    install-phpmyadmin = title(var.installPhpMyAdmin)
    image-caching = var.imageCaching
    image-compression = title(var.imageCompression)
    image-sizing = title(var.imageSizing)
    image-cache-size = var.imageCacheSize
    cache-expiration-minutes = var.cacheExpiration
    extra-lb-zone0 = var.extraLbZone0
    extra-lb-zone1 = var.extraLbZone1
    google-logging-enable = var.enable_cloud_logging ? "1" : "0"
    google-monitoring-enable = var.enable_cloud_monitoring ? "1" : "0"
    ATTACHED_DISKS = join(",", [google_compute_disk.disk1.name, google_compute_disk.disk2.name, google_compute_disk.disk3.name])
    startup-script = <<-EOT
    #!/bin/bash
    echo SUCCESS > /var/log/startup-log.txt
    EOT
  }
}

resource "google_compute_disk" "disk1" {
  name = "${var.goog_cm_deployment_name}-vm-disk-one"
  type = var.disk1_type
  zone = var.zone
  size = var.disk1_size
}

resource "google_compute_disk" "disk2" {
  name = "${var.goog_cm_deployment_name}-vm-disk-xyz"
  type = var.disk2_type
  zone = var.zone
  size = var.disk2_size
}

resource "google_compute_disk" "disk3" {
  name = "${var.goog_cm_deployment_name}-vm-${var.thirdDiskName}"
  type = var.disk3_type
  zone = var.zone
  size = var.disk3_size
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

  attached_disk {
    source      = google_compute_disk.disk1.id
    device_name = google_compute_disk.disk1.name
  }

  attached_disk {
    source      = google_compute_disk.disk2.id
    device_name = google_compute_disk.disk2.name
  }

  attached_disk {
    source      = google_compute_disk.disk3.id
    device_name = google_compute_disk.disk3.name
  }

  scratch_disk {
    interface = "SCSI"
  }

  scratch_disk {
    interface = "SCSI"
  }

  scratch_disk {
    interface = "SCSI"
  }

  can_ip_forward = var.ip_forward

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

  guest_accelerator {
    type = var.accelerator_type
    count = var.accelerator_count
  }

  scheduling {
    // GPUs do not support live migration
    on_host_maintenance = var.accelerator_count > 0 ? "TERMINATE" : "MIGRATE"
  }

  service_account {
    email = "default"
    scopes = compact([
      "https://www.googleapis.com/auth/cloud.useraccounts.readonly",
      "https://www.googleapis.com/auth/devstorage.read_only",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write"
      ,var.enable_compute_api == true ? "https://www.googleapis.com/auth/compute" : null
      ,var.enable_compute_readonly_api == true ? "https://www.googleapis.com/auth/compute.readonly" : null
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

resource "google_compute_firewall" icmp {
  count = var.enable_icmp ? 1 : 0

  name = "${var.goog_cm_deployment_name}-icmp"
  network = element(var.networks, 0)

  allow {
    protocol = "icmp"
  }

  source_ranges =  compact([for range in split(",", var.icmp_source_ranges) : trimspace(range)])

  target_tags = ["${var.goog_cm_deployment_name}-deployment"]
}

resource "random_password" "admin" {
  length = 8
  special = false
}

resource "random_password" "mysql_root" {
  length = 8
  special = false
}

resource "random_password" "this_is_optional" {
  length = 8
  special = false
}
