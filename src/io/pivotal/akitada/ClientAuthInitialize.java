package io.pivotal.akitada;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.management.internal.security.ResourceConstants;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import java.util.Properties;

/**
 * Created by akitada on 2018/03/12.
 */
public class ClientAuthInitialize implements AuthInitialize {

    protected LogWriter systemlog;

    protected LogWriter securitylog;

    @Override
    public void init() {
        init(systemlog, securitylog);
    }

    @Override
    public void init(LogWriter systemlog, LogWriter securitylog) throws AuthenticationFailedException {
        // this method is deprecated but need to implement as of 03/2018
    }

    @Override
    public Properties getCredentials(Properties props) throws AuthenticationFailedException {
        // this method is deprecated but need to implement as of 06/2017
        return getClientCredentials(props);
    }

    @Override
    public Properties getCredentials(Properties props, DistributedMember server, boolean b) throws AuthenticationFailedException {
        return getClientCredentials(props);
    }

    private Properties getClientCredentials(Properties props) throws AuthenticationFailedException {
        Properties newProps = new Properties();
        String userName = props.getProperty(ResourceConstants.USER_NAME);
        if (userName == null) {
            throw new AuthenticationFailedException(
                "ClientAuthInitialize: user name property [" + ResourceConstants.USER_NAME + "] not set.");
        }
        newProps.setProperty(ResourceConstants.USER_NAME, userName);
        String password = props.getProperty(ResourceConstants.PASSWORD);
        // If password is not provided then use empty string as the password.
        if (password == null) {
            password = "";
        }
        newProps.setProperty(ResourceConstants.PASSWORD, password);
        return newProps;
    }

    @Override
    public void close() {
    }
}
