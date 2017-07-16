#!/usr/bin/env groovy

// Let's say we want to trigger deployment job
// Only from master branch
if ("${env.BRANCH_NAME}" == "master") {
    try {
        stage ('Wait for a user decision') {
            timeout(time: 1, unit: 'HOURS') {
                input 'Deploy to Production?'
            }
        }
    }
    // Don't finish the build with error if we ignored a deployment
    catch(error) {
        stage('Graceful exit') {
            environment = 'abort'
            return
        }
    }
    if ("${environment}" != 'abort') {
        environment = 'production'
        // WARNING! It's just a fictional functions which don't exist in Jenkins
        stage("Trigger deployment to ${environment}") {
            deployment.TriggerDeployment(application, environment, version)
            deployment.EchoDeploymentInfo(application, environment)
        }
    }
}
