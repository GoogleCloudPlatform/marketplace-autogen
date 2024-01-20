locals {
  network_interfaces = [ for i, n in var.networks : {
    network     = n,
    subnetwork  = length(var.sub_networks) > i ? element(var.sub_networks, i) : null
    external_ip = length(var.external_ips) > i ? element(var.external_ips, i) : "NONE"
    }
  ]

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
  machine_type = var.machine_type
  boot_disk {
    initialize_params {
      size = var.boot_disk_size
      type = var.boot_disk_type
      image = var.source_image
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
      ,var.enable_cloud_readonly_api == true ? "https://www.googleapis.com/auth/cloud-platform.read-only" : null
      ,var.enable_cloud_api == true ? "https://www.googleapis.com/auth/cloud-platform" : null
      ,var.enable_compute_readonly_api == true ? "https://www.googleapis.com/auth/compute.readonly" : null
      ,var.enable_compute_api == true ? "https://www.googleapis.com/auth/compute" : null
      ,var.enable_source_read_write_api == true ? "https://www.googleapis.com/auth/source.read_write" : null
      ,var.enable_projecthosting_api == true ? "https://www.googleapis.com/auth/projecthosting" : null
    ])
  }
}

resource "google_compute_firewall" tcp_80 {
  count = var.enable_tcp_80 ? 1 : 0

  name = "${var.deployment_name}-main-tcp-80"
  network = element(var.networks, 0)

  allow {
    ports = ["80"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_80_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-main-tier"]
}

resource "google_compute_firewall" tcp_443 {
  count = var.enable_tcp_443 ? 1 : 0

  name = "${var.deployment_name}-main-tcp-443"
  network = element(var.networks, 0)

  allow {
    ports = ["443"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_443_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-main-tier"]
}

resource "google_compute_firewall" icmp {
  count = var.enable_icmp ? 1 : 0

  name = "${var.deployment_name}-main-icmp"
  network = element(var.networks, 0)

  allow {
    protocol = "icmp"
  }

  source_ranges =  compact([for range in split(",", var.icmp_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-main-tier"]
}

resource "google_compute_firewall" tcp_7000-7001 {
  count = var.enable_tcp_7000-7001 ? 1 : 0

  name = "${var.deployment_name}-main-tcp-7000-7001"
  network = element(var.networks, 0)

  allow {
    ports = ["7000-7001"]
    protocol = "tcp"
  }

  source_ranges =  compact([for range in split(",", var.tcp_7000-7001_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-main-tier"]
}

resource "google_compute_firewall" udp {
  count = var.enable_udp ? 1 : 0

  name = "${var.deployment_name}-main-udp"
  network = element(var.networks, 0)

  allow {
    protocol = "udp"
  }

  source_ranges =  compact([for range in split(",", var.udp_source_ranges) : trimspace(range)])

  target_tags = ["${var.deployment_name}-main-tier"]
}
