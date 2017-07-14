package hudson.plugins.deploy.tomcat;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.StreamBuildListener;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.tomcat.Tomcat8xRemoteContainer;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author frekele
 */
public class Tomcat8xAdapterTest {

    private Tomcat8xAdapter adapter;
    private static final String url = "http://localhost:8080";
    private static final String configuredUrl = "http://localhost:8080/manager/text";
    private static final String urlVariable = "URL";
    private static final String username = "usernm";
    private static final String usernameVariable = "user";
    private static final String password = "password";
    private static final String variableStart = "${";
    private static final String variableEnd = "}";
    
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup() throws Exception {
        UsernamePasswordCredentialsImpl c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "test", "sample", username, password);
        CredentialsProvider.lookupStores(jenkinsRule.jenkins).iterator().next().addCredentials(Domain.global(), c);

        adapter = new  Tomcat8xAdapter(url, c.getId());
    }

    @Test
    public void testContainerId() {
        Assert.assertEquals(adapter.getContainerId(), new Tomcat8xRemoteContainer(null).getId());            
    }

    @Test
    public void testConfigure() {
        Assert.assertEquals(url, adapter.url);
        Assert.assertEquals(username, adapter.getUsername());
        Assert.assertEquals(password, adapter.getPassword());
    }
    
    private String getVariable(String variableName) {
    	return variableStart + variableName + variableEnd;
    }
}
