.PHONY: all, docker, run

all:
	@echo "Running sh gradlew docker"
	@sh gradlew docker
	@echo "Running docker-compose up -d"
	@docker-compose up -d

docker:
	@echo "Running sh gradlew docker"
	@sh gradlew docker

run:
	@echo "Running docker-compose up -d"
	@docker-compose up -d