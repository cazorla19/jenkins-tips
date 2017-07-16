#!/usr/bin/env groovy

// Purpose: Deploy ECS service
def DeployToEcs (cluster, service, spec_file, version, region, health_timeout = 300) {
    // Generate the image version in task definition
    sh "sed -i 's:latest:${version}:g' ${spec_file}"
    withAWS(region: region) {
        // Push the task definition
        sh """
            aws ecs register-task-definition \
            --cli-input-json file://${spec_file} > .definition.json
        """.stripIndent()

        // Get the task definition ID
        def DefStatusJson = readFile(".definition.json")
        def DefStatus = new groovy.json.JsonSlurperClassic().parseText(DefStatusJson)
        def task = DefStatus.taskDefinition
        def revision = task.revision
        def definition = cluster + ":" + revision

        // Create new containers
        sh """
            aws ecs update-service --cluster ${cluster} \
            --service ${service} --task-definition ${definition}
        """.stripIndent()
        // Sleep before status check
        sleep 45

        // Update cluster status until it will be healthy
        timeout(time: health_timeout, unit: 'SECONDS') {
            waitUntil {
                sleep 15
                sh """
                    aws ecs describe-services --services ${service} \
                    --cluster ${cluster} > .status.json
                """.stripIndent()
                // parse output
                def StatusJson = readFile(".status.json")
                def Status = new groovy.json.JsonSlurperClassic().parseText(StatusJson)
                def ServiceStatus = Status.services[0]
                def running = ServiceStatus.get('runningCount')
                def desired = ServiceStatus.get('desiredCount')
                def state = ServiceStatus.get('status')
                //Close the loop if desired tasks count is equal to running
                return running == desired && state == "ACTIVE"
            }
        }
    }
}


stage ("Deploy to ${environment}") {
    aws_region = 'us-east-1'
    cluster = 'test-cluster'
    service = 'test-service'
	// Declare a path of task definition file from repo root
	definition_file = '.servicespec.json'
	// You might pick this parameter from Jenkins job
	docker_container_tag = 'test-1'
    checkout scm
    aws.DeployToEcs(
	    cluster, service, definition_file, docker_container_tag, aws_region
	)
}
