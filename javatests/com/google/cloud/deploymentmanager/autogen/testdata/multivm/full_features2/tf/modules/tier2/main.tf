resource "google_compute_instance" "instance" {
  count = var.instance_count
  name = "${var.deployment_name}-tier2-vm-${count.index}"
  zone = var.zone
  machine_type = "e2-medium"
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian12"
    }
  }
  network_interface {
    network = "default"
  }
}
