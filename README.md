# Updraft - Jenkins Publisher Plugin

This plugin allows to upload built binary files (.apk/.ipa) directly to Updraft. 

In order to use this plugin you need to do the following steps.

1. Find "Updraft Android/iOS Publisher" in the Jenkins Plugin Directory and install the latest version
2. Create a build project / use an existing and open the "Configure" menu of that project.
3. Add the "Updraft Android/iOS Publisher" as a "Build Step", after your App file is ready.
4. Enter Updraft Url (can be found on Updraft as part of the curl-command)
5. Enter the relative path to App file, starting from your applications main directory (e.g. app/build/outputs/apk/staging/release/mybuiltapp.apk). You are able to use asterisk (*) to target your app file (e.g. .../*.apk)  
6. Run the build. 

If there are any errors (such as that the App file could not be found or the url was incorrect), the plugin should tell you what you need to do.