package hudson.plugins.deploy;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.Scrambler;
import hudson.util.XStream2;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Confirms that old adapters are serialized and deserialized correctly
 *
 * @author Alex Johnson
 */
public class PasswordProtectedAdapterCargoTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testDeserializeOldPlainPassword () throws Exception {
        String username = "manager";
        String password = "lighthouse";
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><password>"
                + password + "</password><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        adapter.migrateCredentials(new ArrayList<StandardUsernamePasswordCredentials>());
        adapter.loadCredentials(j.createFreeStyleProject()); // temp project

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    public void testDeserializeOldScrambledPassword () throws Exception {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        adapter.migrateCredentials(new ArrayList<StandardUsernamePasswordCredentials>());
        adapter.loadCredentials(j.createFreeStyleProject()); // temp project

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    public void testMigrateOldPlainPassword () throws Exception {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><password>"
                + password + "</password><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        adapter.migrateCredentials(new ArrayList<StandardUsernamePasswordCredentials>());
        String newXml = xs.toXML(adapter);

        assertEquals("<hudson.plugins.deploy.glassfish.GlassFish3xAdapter>\n  <credentialsId>",
                newXml.substring(0, 70));
        assertFalse(newXml.contains(password)); // doesn't have plaintext password
        assertFalse(newXml.contains(scrambledPassword)); // doesn't have base64 pass
        assertEquals("</credentialsId>\n  <home>/</home>\n  <hostname></hostname>\n</hudson.plugins.deploy.glassfish.GlassFish3xAdapter>",
                newXml.substring(newXml.length() - 111, newXml.length()));
    }

    @Test
    public void testMigrateOldScrambledPassword () throws Exception {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);
        adapter.migrateCredentials(new ArrayList<StandardUsernamePasswordCredentials>());
        String newXml = xs.toXML(adapter);

        assertEquals("<hudson.plugins.deploy.glassfish.GlassFish3xAdapter>\n  <credentialsId>",
                newXml.substring(0, 70));
        assertFalse(newXml.contains(password)); // doesn't have plaintext password
        assertFalse(newXml.contains(scrambledPassword)); // doesn't have base64 pass
        assertEquals("</credentialsId>\n  <home>/</home>\n  <hostname></hostname>\n</hudson.plugins.deploy.glassfish.GlassFish3xAdapter>",
                newXml.substring(newXml.length() - 111, newXml.length()));
    }

    @Test
    public void testMatchGeneratedCredentials () throws Exception {

        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);

        List<StandardUsernamePasswordCredentials> prev = new ArrayList<StandardUsernamePasswordCredentials>();
        prev.add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,"id_0","", username, "tennis"));
        prev.add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,"id_1","", "supervisor", password));
        prev.add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,"id_2","", username, password));

        String xml0 = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        String xml1 = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter0 = (PasswordProtectedAdapterCargo)xs.fromXML(xml0);
        adapter0.migrateCredentials(prev);
        PasswordProtectedAdapterCargo adapter1 = (PasswordProtectedAdapterCargo)xs.fromXML(xml1);
        adapter1.migrateCredentials(prev);

        assertEquals("id_2", adapter0.getCredentialsId());
        assertEquals("id_2", adapter1.getCredentialsId());
        assertEquals(3, prev.size());
    }
}
