package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UpdraftPublisher extends Recorder {

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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        //Get the environment
        final EnvVars env = build.getEnvironment(listener);

        //To change body of generated methods, choose Tools | Templates.
        Runtime runtime = Runtime.getRuntime();
        Process process = null;

        try {
            String script = generateScript(env);
            process = runScript(runtime, script);

            // INPUT
            InputStreamReader isReader = new InputStreamReader(process.getInputStream());
            //Creating a BufferedReader object
            BufferedReader reader = new BufferedReader(isReader);
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                listener.getLogger().println("UPLOADING FILE to UPDRAFT...");
                sb.append(str);
            }
            if (!sb.toString().equals("")) {
                listener.getLogger().println(sb.toString());
                listener.getLogger().println("----------------");
            }

            // When we get back success, the file was uploaded successfully
            boolean isOk = sb.toString().contains("\"success\":\"ok\"");
            if (isOk) return true;

            // ERROR
            InputStreamReader eReader = new InputStreamReader(process.getErrorStream());
            BufferedReader errorReader = new BufferedReader(eReader);
            StringBuilder errorSb = new StringBuilder();
            String errorStr;
            while ((errorStr = errorReader.readLine()) != null) {
                errorSb.append(errorStr);
            }
            if (!errorSb.toString().equals("")) {
                listener.getLogger().println("ERROR INFORMATION:");
                listener.getLogger().println(errorSb.toString());
            }

            // Whenever success state of Updraft is "ok", the build was uploaded
            return false;

        } catch (Throwable cause) {
            listener.getLogger().println(process);
        }
        return true;
    }

    private Process runScript(Runtime runtime, String script) throws IOException {
        Process process = runtime.exec(new String[]{CHOICE_OF_SHELL, "-c", script});
        return process;
    }

    private String generateScript(EnvVars env) {
        String loop = "for file in $(ls " + env.expand(path) + ");";
        loop += "do ";
        String expandedName = env.expand(path);

        String curlRequest = loop + "curl -X PUT" +
                " -F app=@$file" +
                " -F build_type=Jenkins" +
                " " + url + " --http1.1;";
        String loopDone = curlRequest + "done;";
        return loopDone;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.UpdraftPublisher_DescriptorImpl_errors_missingName());
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.UpdraftPublisher_DescriptorImpl_DisplayName();
        }

    }

}
