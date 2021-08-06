provider "aws" {
  region = "ap-southeast-2"
}

terraform {
  backend "s3" {
    bucket = "terraform.ruchij.com"
    key = "dynamic-dns.tfstate"
    region = "ap-southeast-2"
  }

  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 3.0"
    }
  }
}