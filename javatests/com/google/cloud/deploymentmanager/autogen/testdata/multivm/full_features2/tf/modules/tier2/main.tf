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

  name = "${var.deployment_name}-tier2-vm-${count.index}"
  zone = var.zone
  machine_type = var.machine_type

  tags = ["${var.deployment_name}-deployment", "${var.deployment_name}-tier2-tier"]

  boot_disk {
    device_name = "${var.deployment_name}-tier2-vm-tmpl-${count.index}-boot-disk"

    initialize_params {
      size = var.boot_disk_size
      type = var.boot_disk_type
      image = var.source_image
    }
  }

  dynamic "scratch_disk" {
    for_each = range(var.tier2LocalSSDs)
    content {
      interface = "SCSI"
    }
  }

  metadata = {
    admin-password = var.admin_password
    ghost-db-password = var.ghost_mysql_password
    optional-password = var.this_is_optional_password
    main-0 = "${var.deployment_name}-main-vm-0"
    main-1 = "${var.deployment_name}-main-vm-${(var.main_instance_count - 1)}"
    domain-name = var.domain
    show-conditionals = title(var.showConditionals)
    condition-to-generate-password = title(var.generateOptionalPassword)
    image-caching = var.imageCaching
    image-compression = title(var.imageCompression)
    image-sizing = title(var.imageSizing)
    extra-lb-zone0 = var.extraLbZone0
    extra-lb-zone1 = var.extraLbZone1
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

resource "google_compute_firewall" tcp_9878 {
  count = var.enable_tcp_9878 ? 1 : 0

  name = "${var.deployment_name}-tier2-tcp-9878"
  network = element(var.networks, 0)

  allow {
    ports = ["9878"]
    protocol = "tcp"
  }

  source_tags = ["${var.deployment_name}-tier2-tier"]

  target_tags = ["${var.deployment_name}-tier2-tier"]
}

resource "google_compute_firewall" udp_2555 {
  count = var.enable_udp_2555 ? 1 : 0

  name = "${var.deployment_name}-tier2-udp-2555"
  network = element(var.networks, 0)

  allow {
    ports = ["2555"]
    protocol = "udp"
  }

  source_tags = ["${var.deployment_name}-deployment"]

  target_tags = ["${var.deployment_name}-tier2-tier"]
}
