package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Scrambler;
import hudson.util.XStream2;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class PasswordProtectedAdapterCargoTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testDeserializeOldPlainPassword () {
        String username = "manager";
        String password = "lighthouse";
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><password>"
                + password + "</password><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        StandardUsernamePasswordCredentials c = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class).get(0);

        // credentials were created with the right username and password
        assertEquals(username, c.getUsername());
        assertEquals(password, c.getPassword().getPlainText());

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    public void testDeserializeOldScrambledPassword () {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        StandardUsernamePasswordCredentials c = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class).get(0);

        // credentials were created with the right username and password
        assertEquals(username, c.getUsername());
        assertEquals(password, c.getPassword().getPlainText());

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());

    }
}
