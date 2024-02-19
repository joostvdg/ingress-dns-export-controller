# Configuration
IMAGE_REPOSITORY := your-repository/your-image-name
IMAGE_TAG := latest
HELM_CHART_DIR := helm/ingress-dns-export-controller
HELM_CHART_REPO := your-helm-chart-repository
HELM_CHART_NAME := ingress-dns-export-controller
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
	helm package $(HELM_CHART_DIR) --destination $(HELM_CHART_DIR)/releases

.PHONY: publish
publish:
	helm repo add myrepo $(HELM_CHART_REPO)
	helm push $(HELM_CHART_DIR)/releases/$(HELM_CHART_NAME)-*.tgz myrepo

# Maven Spring Boot commands
.PHONY: run-dev
run-dev:
	mvn spring-boot:run -Dspring-boot.run.profiles=$(SPRING_BOOT_DEV_PROFILE)

# Utility commands
.PHONY: clean
clean:
	-rm -rf $(HELM_CHART_DIR)/releases
