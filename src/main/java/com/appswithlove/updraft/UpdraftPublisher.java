package com.appswithlove.updraft;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UpdraftPublisher extends Builder implements SimpleBuildStep {

    private final String url;
    private final String path;
    private static final String CHOICE_OF_SHELL = "/bin/bash";


    @DataBoundConstructor
    public UpdraftPublisher(String url, String path) {
        this.url = url;
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Process process = null;
        BufferedReader inputBufferedReader = null;
        BufferedReader errorBufferedReader = null;
        try {
            String script = generateScript(filePath);
            process = runScript(script);

            // INPUT
            InputStreamReader processInputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
            //Creating a BufferedReader object
            inputBufferedReader = new BufferedReader(processInputStreamReader);
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = inputBufferedReader.readLine()) != null) {
                listener.getLogger().println("UPLOADING FILE to UPDRAFT...");
                sb.append(str);
            }
            if (!sb.toString().equals("")) {
                listener.getLogger().println(sb.toString());
                listener.getLogger().println("----------------");
            }

            // When we get back success, the file was uploaded successfully
            boolean isOk = sb.toString().contains("\"success\":\"ok\"");
            if (isOk) return;

            // ERROR: In this case, we have an error and the file could not be uploaded
            InputStreamReader errorInputStreamReader = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8);
            errorBufferedReader = new BufferedReader(errorInputStreamReader);

            StringBuilder errorSb = new StringBuilder();

            String errorStr;
            while ((errorStr = errorBufferedReader.readLine()) != null) {
                errorSb.append(errorStr);
            }

            if (!errorSb.toString().equals("")) {
                listener.getLogger().println("ERROR INFORMATION:");
                listener.getLogger().println(errorSb.toString());
            }

            throw new InterruptedException("Couldn't upload your file to updraft. Please check the error information above.");

        } catch (Throwable cause) {
            listener.getLogger().println(cause);
            run.setResult(Result.FAILURE);
        } finally {
            try {
                if (inputBufferedReader != null) inputBufferedReader.close();
                if (errorBufferedReader != null) errorBufferedReader.close();
            } catch (IOException e) {
                System.out.println("Failed to close streams");
            }
        }
    }

    private Process runScript(String script) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(new String[]{CHOICE_OF_SHELL, "-c", script});
    }

    private String generateScript(FilePath filePath) {
        String loop = "for file in $(ls " + filePath.child(path) + ");";
        loop += "do ";
        String curlRequest = loop + "curl -X PUT" +
                " -F app=@$file" +
                " -F build_type=Jenkins" +
                " " + url + " --http1.1;";
        return curlRequest + "done;";
    }

    @Symbol("updraftPublish")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.UpdraftPublisher_DescriptorImpl_DisplayName();
        }
    }

}
