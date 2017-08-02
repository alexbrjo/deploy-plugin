package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.AbstractProject;
import hudson.plugins.deploy.glassfish.GlassFish3xAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.assertEquals;

/**
 * Confirms that old adapters are serialized and deserialized correctly
 *
 * @author Alex Johnson
 */
public class PasswordProtectedAdapterCargoTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    // these need to match what is configured in the @LocalData resource .zip
    private String username = "manager";
    private String password = "lighthouse";

    @Test
    @LocalData
    public void testMigrateOldPLainPassword () throws Exception {
        AbstractProject project = (AbstractProject) j.getInstance().getItem("plainPassword");
        DeployPublisher deployer = (DeployPublisher) project.getPublishersList().get(DeployPublisher.class);

        GlassFish3xAdapter adapter = (GlassFish3xAdapter) deployer.getAdapters().get(0);
        adapter.loadCredentials(project);

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    @LocalData
    public void testMigrateOldScrambledPassword () throws Exception {
        AbstractProject project = (AbstractProject) j.getInstance().getItem("scrambledPassword");
        DeployPublisher deployer = (DeployPublisher) project.getPublishersList().get(DeployPublisher.class);

        GlassFish3xAdapter adapter = (GlassFish3xAdapter) deployer.getAdapters().get(0);
        adapter.loadCredentials(project);

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    @LocalData
    public void testMatchGeneratedCredentials () throws Exception {
        // create 2 projects and first build
        AbstractProject project1 = (AbstractProject) j.getInstance().getItem("plainPassword");
        project1.scheduleBuild2(0).get();
        AbstractProject project2 = (AbstractProject) j.getInstance().getItem("scrambledPassword");
        project2.scheduleBuild2(0).get();

        assertEquals(extractCredentials(project1), extractCredentials(project2));
    }

    private StandardUsernamePasswordCredentials extractCredentials(AbstractProject project) {
        DeployPublisher publisher = (DeployPublisher) project.getPublishersList().get(DeployPublisher.class);
        String id = ((PasswordProtectedAdapterCargo) publisher.getAdapters().get(0)).getCredentialsId();
        return CredentialsProvider.findCredentialById(id,
                StandardUsernamePasswordCredentials.class, project.getFirstBuild());
    }
}
