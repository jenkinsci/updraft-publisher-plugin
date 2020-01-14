# Updraft - Jenkins Publisher Plugin

This plugin allows to upload built binary files (.apk/.ipa) directly to Updraft. 

In order to use this plugin you need to do the following steps.

1. Find "Updraft Android/iOS Publisher" in the Jenkins Plugin Directory and install the latest version
2. Create a build project / use an existing and open the "Configure" menu of that project.
3. Add the "Updraft Android/iOS Publisher" as a "Post-build Action"
4. Enter Updraft Url (can be found on Updraft as part of the curl-command)
5. Enter Path to App file (e.g. $WORKSPACE/app/build/outputs/apk/staging/release/mybuiltapp.apk). You can use $WORKSPACE to link directly to the ci-folder of that project.
6. Run the build. 

If there are any errors (such as that the App file could not be found or the url was incorrect), the plugin should tell you what you need to do.