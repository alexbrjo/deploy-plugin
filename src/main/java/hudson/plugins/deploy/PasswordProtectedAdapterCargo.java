package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Job;
import hudson.util.Scrambler;
import jenkins.model.Jenkins;
import org.codehaus.cargo.container.property.RemotePropertySet;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class PasswordProtectedAdapterCargo extends DefaultCargoContainerAdapterImpl {
    @Deprecated // backward compatibility
    public String userName, password, passwordScrambled, passwordEncrypted;

    private String credentialsId;
    private transient Job job;

    public PasswordProtectedAdapterCargo(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setJob (Job job) {
        this.job = job;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Property(RemotePropertySet.USERNAME)
    public String getUsername() {
        return ContainerAdapterDescriptor.lookupCredentials(job, credentialsId).getUsername();
    }

    @Property(RemotePropertySet.PASSWORD)
    public String getPassword() {
        return ContainerAdapterDescriptor.lookupCredentials(job, credentialsId).getPassword().getPlainText();
    }

    /*
     * Creates credentials for the previously stored password
     */
    private Object readResolve() throws IOException {
        // not converted
        if (credentialsId == null) {
            if (passwordScrambled != null) {
                password = Scrambler.descramble(passwordScrambled);
            }

            // TODO remove and replace with secret
            UsernamePasswordCredentialsImpl c =
                    new UsernamePasswordCredentialsImpl(
                            CredentialsScope.GLOBAL, "auto-converted-" + userName.hashCode(),
                            "Deploy-plugin plain text converted credentials", userName, password);
            CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next().addCredentials(Domain.global(), c);

            userName = null;
            password = null;
            passwordScrambled = null;
            credentialsId = c.getId();
        }

        return this;
    }
}
