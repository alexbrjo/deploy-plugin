package hudson.plugins.deploy.tomcat;

import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.deploy.ContainerAdapterDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Tomcat 4.x.
 *
 * @author Kohsuke Kawaguchi
 */
public class Tomcat4xAdapter extends TomcatAdapter {

    @DataBoundConstructor
    public Tomcat4xAdapter(String url, String credentialsId) {
        super(url, credentialsId);
    }

    @Deprecated
    public Tomcat4xAdapter(String url, String userName, String password) {
        super(url, userName, password);
    }

    public String getContainerId() {
        return "tomcat4x";
    }

    @Extension
    public static final class DescriptorImpl extends ContainerAdapterDescriptor {
        public String getDisplayName() {
            return "Tomcat 4.x";
        }
    }
}
