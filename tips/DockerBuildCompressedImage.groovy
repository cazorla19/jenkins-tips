#!/usr/bin/env groovy

def PullImage(server, image) {
    withDockerServer([uri: server]) {
        latest_image = docker.image(image)
        latest_image.pull()
    }
}

// Note: we aslo need to use one more image
// As a build cache
// It may spped up the build of enormous monolithic apps
def BuildImage(server, image, cache, version) {
    withDockerServer([uri: server]) {
        new_image = docker.build(
            image, "--squash --compress --cache-from ${cache} ."
        )
        new_image.tag(version)
    }
}

def docker_server = "unix:///var/run/docker.sock"
def version='test-1'
def image = 'my-docker-registry.com/test-app'
def build_cache = '${image}:build-cache'

stage ('Checkout') {
    checkout scm
}
stage ('Pull the build cache') {
    PullImage(docker_server, build_cache)
}
stage ('Build and test the image') {
    BuildImage(docker_server, image, build_cache, version)
}
