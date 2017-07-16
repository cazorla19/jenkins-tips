#!/usr/bin/env groovy

if ("${env.BRANCH_NAME}" == "master") {
    try {
        stage('Wait for a user decision') {
            // Generate the list of environments
            def environments = ['qa', 'staging']
            def env_list = environments.join('\n')
            // Insert production environment only on master branch
            if ("${env.BRANCH_NAME}" == "master") {
                env_list = 'production\n' + env_list
            }

            timeout(time: 1, unit: 'HOURS') {
                environment = input (
                    message: 'Where do you want to deploy this version?',
                    parameters: [
                        choice(
                            // Nasty bug, please, use string instead of array
                            // https://issues.jenkins-ci.org/browse/JENKINS-34590
                            choices: env_list,
                            description: '',
                            name: 'environment'
                        )
                    ]
                )
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
        // WARNING! It's just a fictional functions which don't exist in Jenkins
        stage("Trigger deployment to ${environment}") {
            deployment.TriggerDeployment(application, environment, version)
            deployment.EchoDeploymentInfo(application, environment)
        }
    }
}
