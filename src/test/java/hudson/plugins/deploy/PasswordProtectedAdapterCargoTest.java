package hudson.plugins.deploy;

import hudson.util.Scrambler;
import hudson.util.XStream2;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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
    public void testDeserializeOldPlainPassword () {
        String username = "manager";
        String password = "lighthouse";
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><password>"
                + password + "</password><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        PasswordProtectedAdapterCargo adapter = (PasswordProtectedAdapterCargo)xs.fromXML(oldXml);

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

        // adapter returns correct username and password
        assertEquals(username, adapter.getUsername());
        assertEquals(password, adapter.getPassword());
    }

    @Test
    public void testMigrateOldPlainPasswordToSecret () {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><password>"
                + password + "</password><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        String newXml = xs.toXML(xs.fromXML(oldXml)); // deserialize and serialize

        assertEquals("<hudson.plugins.deploy.glassfish.GlassFish3xAdapter>\n  <userName>manager</userName>\n  <passwordEncrypted>",
                newXml.substring(0, 105));
        assertFalse(newXml.contains(password)); // doesn't have plaintext password
        assertFalse(newXml.contains(scrambledPassword)); // doesn't have base64 pass
        assertEquals("</passwordEncrypted>\n  <home>/</home>\n  <hostname></hostname>\n</hudson.plugins.deploy.glassfish.GlassFish3xAdapter>",
                newXml.substring(newXml.length() - 115, newXml.length()));
    }

    @Test
    public void testMigrateOldScrambledPasswordToSecret () {
        String username = "manager";
        String password = "lighthouse";
        String scrambledPassword = Scrambler.scramble(password);
        String oldXml = "<hudson.plugins.deploy.glassfish.GlassFish3xAdapter><userName>" + username + "</userName><passwordScrambled>"
                + scrambledPassword + "</passwordScrambled><home>/</home><hostname></hostname></hudson.plugins.deploy.glassfish.GlassFish3xAdapter>";
        XStream2 xs = new XStream2();

        String newXml = xs.toXML(xs.fromXML(oldXml)); // deserialize and serialize

        assertEquals("<hudson.plugins.deploy.glassfish.GlassFish3xAdapter>\n  <userName>manager</userName>\n  <passwordEncrypted>",
                newXml.substring(0, 105));
        assertFalse(newXml.contains(password)); // doesn't have plaintext password
        assertFalse(newXml.contains(scrambledPassword)); // doesn't have base64 pass
        assertEquals("</passwordEncrypted>\n  <home>/</home>\n  <hostname></hostname>\n</hudson.plugins.deploy.glassfish.GlassFish3xAdapter>",
                newXml.substring(newXml.length() - 115, newXml.length()));
    }
}
