package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Util;
import hudson.model.Job;
import hudson.util.Scrambler;
import jenkins.model.Jenkins;
import org.codehaus.cargo.container.property.RemotePropertySet;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * Creates credentials for the previously stored password.
 *
 * Historical precedent of the multiple password fields and why they should not be removed. Using
 * {@link hudson.plugins.deploy.tomcat.Tomcat7xAdapter} as an example, but applies to all.
 *
 * v1.0     Stored password as plain text
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <userName>admin</userName>
 *                  <password>pw</password>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
 *
 * v1.9     Used {@link hudson.util.Scrambler} to base64 encode password. readResolve converted plaintext password
 *          to passwordScrambled.
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <userName>admin</userName>
 *                  <passwordScrambled>cHcNCg==</passwordScrambled>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
 *
 * v1.11    Full support of credentials. To be backwards compatible and not break builds converts old configurations
 *          from password or passwordScrambled to credentials.
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <credentialsId>aDjnKd4j-s66fnF53-2dmAS7PkqD4</credentialsId>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
 *
 * @author Alex Johnson
 * @author Kohsuke Kawaguchi
 */
public abstract class PasswordProtectedAdapterCargo extends DefaultCargoContainerAdapterImpl {
    @Deprecated // backward compatibility
    public String userName, password, passwordScrambled;

    @CheckForNull
    private String credentialsId;
    @CheckForNull
    private transient StandardUsernamePasswordCredentials credentials;

    public PasswordProtectedAdapterCargo(String credentialsId) {
        this.credentialsId = Util.fixEmpty(credentialsId);
        credentials = ContainerAdapterDescriptor.lookupCredentials(null, credentialsId);
    }

    @Deprecated
    public PasswordProtectedAdapterCargo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void trackCredentials(Job job) {
        credentials = ContainerAdapterDescriptor.lookupCredentials(job, credentialsId);
        CredentialsProvider.track(job, credentials);
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Property(RemotePropertySet.USERNAME)
    public String getUsername() {
        return credentials.getUsername();
    }

    @Property(RemotePropertySet.PASSWORD)
    public String getPassword() {
        return credentials.getPassword().getPlainText();
    }

    /**
     * Migrates to credentials.
     */
    public void migrateCredentials() throws IOException {
        if (credentialsId == null) {
            if (passwordScrambled != null) {
                password = Scrambler.descramble(passwordScrambled);
            }

            credentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                    null, "Deploy credentials for " + getContainerId(), userName, password);
            CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next().addCredentials(Domain.global(), credentials);

            credentialsId = credentials.getId();
            userName = null;
            password = null;
            passwordScrambled = null;
        }
    }
}
