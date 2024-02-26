# Configuration
LOCAL_VERSION = $(shell git describe --tags --always)
PACKAGE_VERSION ?= "0.1.0-$(LOCAL_VERSION)"
IMAGE_REPOSITORY := harbor.home.lab/homelab/ingress-dns-export-controller
IMAGE_TAG := "$(PACKAGE_VERSION)"
HELM_CHART_DIR := helm/ingress-dns-export-controller
HELM_CHART_NAME := ingress-dns-export-controller
HELM_CHART_RELEASE_DIR := helm/releases
HELM_CHART_VERSION := "$(PACKAGE_VERSION)"
BUILDER_NAME := mybuilder
PLATFORMS := linux/amd64,linux/arm64
SPRING_BOOT_DEV_PROFILE := dev

# Set the default make target
.PHONY: all
all: build push package publish

# Docker commands
.PHONY: create-builder
create-builder:
	docker buildx create --name $(BUILDER_NAME) --use

.PHONY: start-builder
start-builder:
	docker buildx inspect --bootstrap

.PHONY: build
build:
	docker buildx build --platform $(PLATFORMS) -t $(IMAGE_REPOSITORY):$(IMAGE_TAG) --push .

# Helm commands
.PHONY: package
package:
	helm package ${HELM_CHART_DIR} \
		--app-version ${PACKAGE_VERSION} \
		--version ${HELM_CHART_VERSION} \
		--destination ${HELM_CHART_RELEASE_DIR}

.PHONY: publish
publish: package
	helm push \
		--ca-file=/home/joostvdg/projects/homelab/certs/ca.pem \
		${HELM_CHART_RELEASE_DIR}/${HELM_CHART_NAME}-${HELM_CHART_VERSION}.tgz \
		"oci://harbor.home.lab:443/charts"

# Maven Spring Boot commands
.PHONY: run-dev
run-dev:
	mvn spring-boot:run -Dspring-boot.run.profiles=$(SPRING_BOOT_DEV_PROFILE)

.PHONY: helm-validate
helm-validate:
	helm lint ./helm/ingress-dns-export-controller
	polaris audit --helm-chart ./helm/ingress-dns-export-controller --helm-values ./helm/ingress-dns-export-controller/example-values.yaml --format=pretty

.PHONY: helm-template
helm-template:
	helm template ingress-dns-export-controller ./helm/ingress-dns-export-controller --values ./helm/ingress-dns-export-controller/example-values.yaml | yq