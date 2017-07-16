#!/usr/bin/env groovy

def TriggerDeployment (job, environment, release) {
    // Here Deployments is just a Jenkins folder for jobs
    // You can patternize however you need at this variable
    def job_name = "/Deployments/${job}/${environment}"
    build (
        job: job_name,
        parameters: [
            string(
                name: 'release', value: release
            )
        ],
        wait: false
    )
}

// Provides a link to the Deployment job in Blue Ocean interface
def EchoDeploymentInfo (job, environment) {
    server_name = "jenkins.example.com"
    url = "https://${server_name}/blue/organizations/jenkins/Deployments%2F${job}%2F${environment}/activity"
    echo """
        Deploy to ${environment} started.
        Proceed the URL to track the process:
        ${url}
    """.stripIndent()
}

def application = "test"
def environment= 'qa'
// You can get this parameter from Jenkins
def version='test-1'

stage("Trigger deployment to ${environment}") {
    deployment.TriggerDeployment(application, environment, version)
    deployment.EchoDeploymentInfo(application, environment)
}
