provider "google" {
  project  = var.project_id
}

module "main" {
  source          = "./modules/main"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.main_instance_count
  zone            = var.zone
}

module "tier2" {
  source          = "./modules/tier2"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier2_instance_count
  zone            = var.zone
}

module "tier3" {
  source          = "./modules/tier3"

  deployment_name = var.goog_cm_deployment_name
  instance_count  = var.tier3_instance_count
  zone            = var.zone
}

