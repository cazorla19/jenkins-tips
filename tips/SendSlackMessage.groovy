#!/usr/bin/env groovy

// Works well with Github organizations
def SendBuildMessage (
    application, mode, channel, repository = null, organization = 'MyOrg'
) {
    if (repository == null) {
        repository = application
    }
    server_name = 'https://jenkins.myorg.com'
    // Print the link to blue ocean UI
    build_url="${server_name}/blue/organizations/jenkins/${organization}%2F${repository}/detail/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/pipeline"
    if (mode == "failure") {
        slack_message = """
            Build failed! Application: ${application}.
            Branch: ${env.BRANCH_NAME}. Build: ${env.BUILD_NUMBER}.
            Build URL: ${build_url}
            """.stripIndent()
        color = "danger"
    }
    else if (mode == "success") {
        slack_message = """
            Build succeeded! Application: ${application}.
            Branch: ${env.BRANCH_NAME}. Build: ${env.BUILD_NUMBER}.
            Build URL: ${build_url}
            """.stripIndent()
        color = "good"
    }
    else {
        sh "exit 0"
    }
    // Finally send the message to Slack
    slackSend color: color, message: slack_message, channel: channel
}

stage ("Send slack notification") {
    def slack_channel = "#test-service-builds"
    def application = 'test'
    slack.SendDeploymentMessage(application, "success", slack_channel)
}
