package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Deploys WAR to a container.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DeployPublisher extends Notifier implements Serializable {
    private List<ContainerAdapter> adapters;
    public final String contextPath;

    public final String war;
    public final boolean onFailure;

    /**
     * @deprecated
     *      Use {@link #getAdapters()}
     */
    public final ContainerAdapter adapter = null;
    
    @DataBoundConstructor
    public DeployPublisher(List<ContainerAdapter> adapters, String war, String contextPath, boolean onFailure) {
   		this.adapters = adapters;
        this.war = war;
        this.onFailure = onFailure;
        this.contextPath = contextPath;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build.getResult().equals(Result.SUCCESS) || onFailure) {
            for (FilePath warFile : build.getWorkspace().list(this.war)) {
                for (ContainerAdapter adapter : adapters) {
                    if (adapter instanceof PasswordProtectedAdapterCargo) {
                        // protected containers need Job to do credentialId lookup and usage tracking
                        ((PasswordProtectedAdapterCargo) adapter).loadCredentials(build.getParent());
                    }
                    if (!adapter.redeploy(warFile, contextPath, build, launcher, listener)) {
                        build.setResult(Result.FAILURE);
                    }
                }
            }
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    public Object readResolve() {
    	if(adapter != null) {
    		if(adapters == null) {
    			adapters = new ArrayList<ContainerAdapter>();
    		}
    		adapters.add(adapter);
    	}
    	return this;
    }

    /**
	 * Get the value of the adapterWrappers property
	 *
	 * @return The value of adapterWrappers
	 */
	public List<ContainerAdapter> getAdapters() {
		return adapters;
	}

	@Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public String getDisplayName() {
            return Messages.DeployPublisher_DisplayName();
        }

        /**
         * Sort the descriptors so that the order they are displayed is more predictable
         */
        public List<ContainerAdapterDescriptor> getAdaptersDescriptors() {
            List<ContainerAdapterDescriptor> r = new ArrayList<ContainerAdapterDescriptor>(ContainerAdapter.all());
            Collections.sort(r,new Comparator<ContainerAdapterDescriptor>() {
                public int compare(ContainerAdapterDescriptor o1, ContainerAdapterDescriptor o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
            return r;
        }
    }

    private static final long serialVersionUID = 1L;

    @Restricted(NoExternalUse.class)
    @Extension
    public static final class Migrator extends ItemListener {

        @SuppressWarnings("deprecation")
        @Override
        public void onLoaded() {
            List<StandardUsernamePasswordCredentials> generatedCredentials = new ArrayList<StandardUsernamePasswordCredentials>();
            for (AbstractProject<?,?> project : Jenkins.getActiveInstance().getAllItems(AbstractProject.class)) {
                try {
                    DeployPublisher d = project.getPublishersList().get(DeployPublisher.class);
                    if (d == null) {
                        continue;
                    }
                    boolean modified = false;
                    for (ContainerAdapter a : d.getAdapters()) {
                        if (a instanceof PasswordProtectedAdapterCargo) {
                            PasswordProtectedAdapterCargo ppac = (PasswordProtectedAdapterCargo) a;
                            if (ppac.getCredentialsId() == null) {
                                ppac.migrateCredentials(generatedCredentials);
                                modified = true;
                            }
                        }
                    }
                    if (modified) {
                        Logger.getLogger(DeployPublisher.class.getName()).info(
                                String.format("Successfully migrated DeployPublisher in project: %s", project.getName()));
                        project.save();
                    }
                } catch (IOException e) {
                    Logger.getLogger(DeployPublisher.class.getName()).warning("Migration unsuccessful");
                }
            }
        }
    }

}
