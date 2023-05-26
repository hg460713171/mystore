#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin


# authentication
cd ./mystore-authentication/
docker build -t mystore-authentication .
docker tag mystore-authentication $DOCKER_USERNAME/mystore-authentication
cd ..

# account
cd ./mystore-account/
docker build -t mystore-account .
docker tag mystore-account $DOCKER_USERNAME/mystore-account
cd ..

# warehouse
cd ./mystore-warehouse/
docker build -t mystore-warehouse .
docker tag mystore-warehouse $DOCKER_USERNAME/mystore-warehouse
cd ..

# payment
cd ./mystore-payment/
docker build -t mystore-payment .
docker tag mystore-payment $DOCKER_USERNAME/mystore-payment
cd ..

docker images
docker push $DOCKER_USERNAME/mystore-authentication
docker push $DOCKER_USERNAME/mystore-account
docker push $DOCKER_USERNAME/mystore-warehouse
docker push $DOCKER_USERNAME/mystore-payment
