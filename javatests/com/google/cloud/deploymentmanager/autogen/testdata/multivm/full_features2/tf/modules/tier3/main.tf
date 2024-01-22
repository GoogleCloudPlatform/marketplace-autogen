locals {
  network_interfaces = [ for i, n in var.networks : {
    network     = n,
    subnetwork  = length(var.sub_networks) > i ? element(var.sub_networks, i) : null
    external_ip = length(var.external_ips) > i ? element(var.external_ips, i) : "NONE"
    }
  ]
}

resource "google_compute_instance" "instance" {
  count = var.instance_count

  name = "${var.deployment_name}-tier3-vm-${count.index}"
  zone = var.zone
  machine_type = var.machine_type

  tags = ["${var.deployment_name}-deployment", "${var.deployment_name}-tier3-tier"]

  boot_disk {
    device_name = "${var.deployment_name}-tier3-vm-tmpl-${count.index}-boot-disk"

    initialize_params {
      size = var.boot_disk_size
      type = var.boot_disk_type
      image = var.source_image
    }
  }

  metadata = {
    admin-password = var.admin_password
    ghost-db-password = var.ghost_mysql_password
    optional-password = var.this_is_optional_password
    google-logging-enable = var.enable_cloud_logging ? "1" : "0"
    google-monitoring-enable = var.enable_cloud_monitoring ? "1" : "0"
    startup-script = <<-EOT
    #!/bin/bash
    cd /tmp
    echo STARTED >> /tmp/startup_log
    cd -
    EOT
  }

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
    ])
  }
}

resource "google_compute_firewall" tcp_9000 {
  count = var.enable_tcp_9000 ? 1 : 0

  name = "${var.deployment_name}-tier3-tcp-9000"
  network = element(var.networks, 0)

  allow {
    ports = ["9000"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_9000_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-tier3-tier"]
}

resource "google_compute_firewall" udp_2333 {
  count = var.enable_udp_2333 ? 1 : 0

  name = "${var.deployment_name}-tier3-udp-2333"
  network = element(var.networks, 0)

  allow {
    ports = ["2333"]
    protocol = "udp"
  }

  source_ranges =  compact([for range in split(",", var.udp_2333_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-tier3-tier"]
}
