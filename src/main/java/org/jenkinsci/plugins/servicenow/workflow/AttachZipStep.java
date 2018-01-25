package org.jenkinsci.plugins.servicenow.workflow;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import jenkins.plugins.http_request.ResponseHandle;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class AttachZipStep extends AbstractServiceNowStep {

    @DataBoundConstructor
    public AttachZipStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, ServiceNowItem serviceNowItem) {
        super(serviceNowConfiguration, credentialsId, serviceNowItem);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "serviceNow_attachZip";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {

        private transient AttachZipStep step;

        Execution(@Nonnull StepContext context, @Nonnull AttachZipStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseContentSupplier run() throws Exception {
            ServiceNowExecution exec = ServiceNowExecution.from(step, getProject());

            try (InputStream zipStream = getContext().get(FilePath.class).child(step.getServiceNowItem().getFilename()).read()) {
                CloseableHttpResponse response = exec.attachZip(zipStream);
                return new ResponseContentSupplier(ResponseHandle.STRING, response);
            }
        }

        Item getProject() throws IOException, InterruptedException {
            return getContext().get(Run.class).getParent();
        }

        private static final long serialVersionUID = 1L;

    }

}
