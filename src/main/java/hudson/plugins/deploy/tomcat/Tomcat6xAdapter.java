package hudson.plugins.deploy.tomcat;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.deploy.ContainerAdapterDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Tomcat 6.x
 *
 * @author Kohsuke Kawaguchi
 */
public class Tomcat6xAdapter extends TomcatAdapter {

    @DataBoundConstructor
    public Tomcat6xAdapter(String url, String credentialsId, Run run) {
        super(url, credentialsId);
    }

    @Deprecated
    public Tomcat6xAdapter(String url, String userName, String password) {
        super(url, userName, password);
    }

    public String getContainerId() {
        return "tomcat6x";
    }
    
    @Extension
    public static final class DescriptorImpl extends ContainerAdapterDescriptor {
        public String getDisplayName() {
            return "Tomcat 6.x";
        }
    }
}

