provider "google" {
  project  = var.project_id
}

module "main" {
  source = "./modules/main"
  instance_count = 3
}

module "tier2" {
  source = "./modules/tier2"
  instance_count = 2
}

module "tier3" {
  source = "./modules/tier3"
  instance_count = 1
}

