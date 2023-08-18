
provider "google" {
  project = var.project_id
}

locals {
  network_interfaces_map = { for i, n in var.networks : n => {
    network     = n,
    subnetwork  = element(var.sub_networks, i)
    external_ip = element(var.external_ips, i)
    }
  }
}

resource "google_compute_instance" "instance" {
  name = "${var.goog_cm_deployment_name}-vm"
  machine_type = var.machine_type
  zone = var.zone

  boot_disk {
    initialize_params {
      size = var.boot_disk_size
      type = var.boot_disk_type
      image = var.source_image
    }
  }

  dynamic "network_interface" {
    for_each = local.network_interfaces_map
    content {
      network = network_interface.key
      subnetwork = network_interface.value.subnetwork

      dynamic "access_config" {
        for_each = network_interface.value.external_ip == "NONE" ? [] : [1]
        content {
          nat_ip = network_interface.value.external_ip == "EPHEMERAL" ? null : network_interface.value.external_ip
        }
      }
    }
  }
}
