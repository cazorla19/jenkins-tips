#!/usr/bin/env groovy


// Purpose: Dynamically generate list of stable builds
// for deployment projects using ActiveChoice plugin
// Works well for Github organization folder plugin jobs
def RenderProjectBuilds (repo) {
    import hudson.model.*

    def items = Hudson.instance.allItems

    // "back to normal" means "stable after failure"
    // "?" means "in progress"
    assumed_states = ["stable", "?", "back to normal"]
    release_list = []

    items.each { item ->

        if (item instanceof Job) {

            def builds = item.getBuilds()

            builds.each { build ->
                def status = build.getBuildStatusSummary().message
                def build_string = build.toString()
                if (build_string.startsWith(repo) && assumed_states.contains(status)) {
                    // Cut all characters before branch name first
                    // Replace the space and hash between branch name and build number to the dash
                    // That way we generate release number
                    release = build_string.substring(build_string.lastIndexOf("/") + 1).replace(" #", "-")
                    release_list.add(release)
                }
            }
        }
    }

    // Append the tag 'latest' just in case
    release_list.add('latest')
    return release_list
}

def github_repo = 'MyOrg/myproject'
def stable_builds = RenderProjectBuilds(github_repo)
