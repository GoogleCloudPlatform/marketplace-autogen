provider "google" {
  project  = var.project_id
}

module "main" {
  source = "./modules/main"

  instance_count = var.main_instance_count
}

module "tier2" {
  source = "./modules/tier2"

  instance_count = var.tier2_instance_count
}

module "tier3" {
  source = "./modules/tier3"

  instance_count = var.tier3_instance_count
}

