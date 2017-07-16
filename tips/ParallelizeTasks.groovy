#!/usr/bin/env groovy

// Purpose: show up how to parallelize jobs
// Example: testing job
stage ('Fast testing') {
    def version = 'test-1'
    def new_image = 'my-docker-reg.com/test-app:${version}'
    parallel api_v1_functional: {
        node('build') {
            def type = "api-v1-functional"
            def test_reports_location = "functional/ApiV1/resources"
            test(new_image, type, test_reports_location)
        }
    },
    api_functional: {
        node('build') {
            def type = "api-functional"
            def test_reports_location = "functional/Api/resources"
            test(new_image, type, test_reports_location)
        }
    },
    unit_testing: {
        node('build') {
            def type = "unit"
            def test_reports_location = "unit/resources"
            test(new_image, type, test_reports_location)
        }
    },
    webapi: {
        node('build') {
            def type = "web-api-functional"
            def test_reports_location = "functional/WebApi/resources"
            test(new_image, type, test_reports_location)
        }
    },
    // Stop all builds if one was failed
    failFast: true
}
