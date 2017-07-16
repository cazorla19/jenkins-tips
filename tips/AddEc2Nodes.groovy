#!/usr/bin/env groovy

// Function to mark slave_label nodes for a particular build job
def MarkNodesForTesting(slave_label, version) {
    def nodes_list = []
    for (slave in hudson.model.Hudson.instance.slaves) {
        def busy_workers = slave.getComputer().countBusy()
        def node_label = slave.getLabelString()
        def node_name = slave.getNodeName()
        // This condition tells us that slave is idle
        if (busy_workers == 0 && node_label == slave_label) {
            slave.setLabelString(version)
            nodes_list.add(node_name)
        }
    }
    return nodes_list
}

// Function to check we have enough nodes for a job execution
// Use Amazon EC2 plugin to add more slaves
def AddNodesForTesting(slave_label, version, requirement) {
    // Check idle slaves first
    def available_nodes = MarkNodesForTesting(slave_label, version)
    def server_name = "http://jenkins.example.com"
    def ec2_cloud_name = 'ec2-myotg-virginia'
    // Add more nodes until we won't have enough
    while (available_nodes.size() < requirement) {
        echo "INFO: Not enough slaves: provisioning one more for now"

        // Provisioning request with George Carlin credentials
        withCredentials([
            usernamePassword(
                credentialsId: 'user_credentials',
                passwordVariable: 'password',
                usernameVariable: 'user'
            )]
        ) {
            node('build') {
                // API call to Amazon EC2 plugin
                sh """
                    curl -u '${user}:${password}' -XPOST --form 'template=${slave_label}' \
                    ${server_name}/cloud/${ec2_cloud_name}/provision
                """.stripIndent()
            }
        }

        // Check if we have enough nodes for this time
        new_node = MarkNodesForTesting(slave_label, version)
        available_nodes = available_nodes.sum(new_node)
    }

    echo "Environment is ready! Waiting 90 seconds to boot all slaves"
    sleep 90
}

// Function to rename node label to the initial state
def CleanupTestNodes(slave_label, version) {
    for (slave in hudson.model.Hudson.instance.slaves) {
        def node_label = slave.getLabelString()
        if (node_label == version) {
            // Set the old tag
            slave.setLabelString(slave_label)
        }
    }
    echo "Cleanup is completed!"
}
