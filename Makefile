\
# AsaNa developer shortcuts.
#
# Run `make help` to see what's available. Backend targets assume you're on
# a machine with PHP, Composer, and Docker set up per CLAUDE.md; Android
# targets assume Android Studio's JDK is at the path below (adjust
# JAVA_HOME if yours lives elsewhere).

BACKEND := backend
ANDROID := android
JAVA_HOME_ANDROID := /c/Program Files/Android/Android Studio/jbr

.DEFAULT_GOAL := help

.PHONY: help up down restart logs install migrate fresh seed tinker serve test pint dev \
        android-build android-install android-clean android-test

help: ## Show this list of commands
	@grep -E '^[a-zA-Z_-]+:.*## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

## --- Backend (Laravel) ---

up: ## Start Postgres in Docker
	cd $(BACKEND) && docker compose up -d

down: ## Stop Postgres
	cd $(BACKEND) && docker compose down

restart: down up ## Restart Postgres

logs: ## Tail the Postgres container's logs
	cd $(BACKEND) && docker compose logs -f

install: ## Install PHP dependencies
	cd $(BACKEND) && composer install

migrate: ## Run pending migrations
	cd $(BACKEND) && php artisan migrate

fresh: ## Drop all tables and reseed from scratch
	cd $(BACKEND) && php artisan migrate:fresh --seed

seed: ## Run the seeders (without resetting the schema)
	cd $(BACKEND) && php artisan db:seed

tinker: ## Open an interactive REPL against the app
	cd $(BACKEND) && php artisan tinker

serve: ## Run the Laravel dev server at http://127.0.0.1:8000
	cd $(BACKEND) && php artisan serve

test: ## Run the backend test suite
	cd $(BACKEND) && ./vendor/bin/pest

pint: ## Fix backend code style
	cd $(BACKEND) && ./vendor/bin/pint

dev: up ## One-shot: start Postgres, wait for it, then migrate + seed
	@echo "Waiting for Postgres..."
	@cd $(BACKEND) && until docker compose exec -T postgres pg_isready -U asana >/dev/null 2>&1; do sleep 1; done
	$(MAKE) fresh

## --- Android ---

android-build: ## Build the debug APK
	cd $(ANDROID) && JAVA_HOME="$(JAVA_HOME_ANDROID)" ./gradlew assembleDebug

android-install: ## Build and install the debug APK on a connected device/emulator
	cd $(ANDROID) && JAVA_HOME="$(JAVA_HOME_ANDROID)" ./gradlew installDebug

android-test: ## Run Android unit tests
	cd $(ANDROID) && JAVA_HOME="$(JAVA_HOME_ANDROID)" ./gradlew test

android-clean: ## Clean the Android build output
	cd $(ANDROID) && JAVA_HOME="$(JAVA_HOME_ANDROID)" ./gradlew clean
