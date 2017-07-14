package hudson.plugins.deploy;

import hudson.model.Job;
import hudson.util.Scrambler;
import hudson.util.Secret;
import org.apache.log4j.Logger;
import org.codehaus.cargo.container.property.RemotePropertySet;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * Creates credentials for the previously stored password.
 *
 * Historical precedent of the multiple password field and why they should not be removed. Using
 * {@link hudson.plugins.deploy.tomcat.Tomcat7xAdapter} as an example, but applies to all.
 *
 * v1.0     Stored password as plain text
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <userName>admin</userName>
 *                  <password>password</password>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
 *
 * v1.9     Used {@link hudson.util.Scrambler} to base64 encode password. readResolve converted plaintext password
 *          to passwordScrambled.
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <userName>admin</userName>
 *                  <passwordScrambled>password</passwordScrambled>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
 *
 * v1.11    Full support of credentials. To be backwards compatible and not break builds converts old configurations
 *          from password or passwordScrambled to passwordEncrypted via {@link hudson.util.Secret}.
 *          <code>
 *              <Tomcat7xAdapter>
 *                  <userName>admin</userName>
 *                  <passwordEncrypted>password</passwordEncrypted>
 *                  <url>http://example.com:8080</url>
 *              </Tomcat7xAdapter>
 *          </code>
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
    public String userName, password, passwordScrambled, passwordEncrypted;
    @Deprecated
    public transient Secret secret;

    private String credentialsId;
    @CheckForNull
    private transient Job job;

    public PasswordProtectedAdapterCargo(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Deprecated
    public PasswordProtectedAdapterCargo(String userName, String password) {
        this.userName = userName;
        this.secret = Secret.fromString(password);
    }

    // lookupCredentials requires a Job
    public void setJob (Job job) {
        this.job = job;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Property(RemotePropertySet.USERNAME)
    public String getUsername() {
        if (credentialsId == null) {
            return userName;
        }
        return ContainerAdapterDescriptor.lookupCredentials(job, credentialsId).getUsername();
    }

    @Property(RemotePropertySet.PASSWORD)
    public String getPassword() {
        if (credentialsId == null) {
            return secret.getPlainText();
        }
        return ContainerAdapterDescriptor.lookupCredentials(job, credentialsId).getPassword().getPlainText();
    }

    /**
     * Makes sure either secret or credentials are being used. Converts passwordEncrypted to secret
     * @return the resolved object
     */
    private Object readResolve() throws IOException {
        if (credentialsId == null) { // not converted to credentials
            Logger.getLogger(PasswordProtectedAdapterCargo.class).warn("Please reconfigure deploy-plugin to use credentials");
            if (passwordEncrypted == null) { // not migrated to secure credential management
                if (passwordScrambled != null) {
                    password = Scrambler.descramble(passwordScrambled);
                }
                secret = Secret.fromString(password);
                passwordEncrypted = secret.getEncryptedValue();
            }
        } else {
            // credentials are being used so make sure no other fields are being serialized
            userName = null;
            passwordEncrypted = null;
        }
        password = null;
        passwordScrambled = null;
        return this;
    }
}
