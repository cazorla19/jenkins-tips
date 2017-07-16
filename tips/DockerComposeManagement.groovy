#!/usr/bin/env groovy

// Manage Docker-compose on all build nodes
// Also I want to show how to generate parallel jobs
def ManageComposeEnvironment(slave, version, mode) {

    // Define key-value jobs storage
    def branches = [:]

    // Get available nodes first
    def nodes_list = []
    for (slave in hudson.model.Hudson.instance.slaves) {
        def node_label = slave.getLabelString()
        def node_name = slave.getNodeName()
        if (node_label == slave) {
            nodes_list.add(node_name)
        }
    }

    // Init or kill Compose Environment next
    for (int i=0; i<nodes_list.size(); ++i) {
        def nodeName = nodes_list[i];
        // Within each branch we put the pipeline code we want to execute
        branches[nodeName] = {
            node(nodeName) {
                checkout scm
                // Replase version in specification
                sh "sed 's/latest/${version}/g' docker-compose.template > docker-compose.yml"
                if (mode == 'init') {
                    sh "docker-compose pull --parallel"
                    sh "docker-compose up -d"
                    // Wait for a full initialization
                    sleep 15
                }
                else if (mode == 'kill') {
                    sh "docker-compose kill"
                }
            }
        }
    }
    // Now we trigger all branches in parallel
    parallel branches
}

def slave_label = 'test'
// You can generate this variable at Jenkins
def version = 'test-1'
stage ("Init Docker-Compose environment") {
    mode = 'init'
    ManageComposeEnvironment(slave_label, version, mode)
}
stage ("Kill Docker-Compose environment") {
    mode = 'kill'
    ManageComposeEnvironment(slave_label, version, mode)
}
