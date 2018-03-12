package io.pivotal.akitada;

import org.apache.geode.LogWriter;
import org.apache.geode.management.internal.security.ResourceConstants;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by akitada on 2018/03/12.
 */
public class EncryptedPasswordSecurityManager implements SecurityManager {

    static Logger logger = LogManager.getLogger(EncryptedPasswordSecurityManager.class);

    public static final String KEYSTORE_PASSWORD = "security-keystore-password";
    public static final String KEYSTORE_NAME = "security-keystore-name";

    private static String keystoreName = null;
    private static String keystorePassword = null;

    protected LogWriter securitylog;

    protected LogWriter systemlog;

    // IV length must be 16 bytes long.
    private static final String INIT_VECTOR = "__myInitVector__";

    private static boolean testValidName(String userName) {
        return (userName.startsWith("user") || userName.startsWith("reader")
            || userName.startsWith("writer") || userName.equals("admin")
            || userName.equals("root") || userName.equals("administrator"));
    }

    @Override
    public void init(Properties securityProps) {
        this.keystoreName = securityProps.getProperty(KEYSTORE_NAME);
        this.keystorePassword = securityProps.getProperty(KEYSTORE_PASSWORD);
    }

    @Override
    public Object authenticate(Properties credentials) throws AuthenticationFailedException {
        String userName = credentials.getProperty(ResourceConstants.USER_NAME);
        String password;

        try {
            password = decryptPassword(credentials.getProperty(ResourceConstants.PASSWORD));
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Failed to decrypt the encrypted password :" + ex.getMessage() + "\n" + getExceptionString(ex));
        }
        if (userName.equals(password) && testValidName(userName)) {
            return userName;
        } else {
            throw new AuthenticationFailedException("EncryptedPasswordSecurityManager: wrong user name/password");
        }
    }

    @Override
    public boolean authorize(final Object principal, final ResourcePermission context) {
        // do nothing in this example
        return true;
    }

    @Override
    public void close() {
    }

    public static void main(String[] args) {
        EncryptedPasswordSecurityManager epa = new EncryptedPasswordSecurityManager();
        try {
            Properties properties = new Properties();
            // need to add path for gemfire.properties into CLASSPATH environment variable
            InputStream istream = EncryptedPasswordSecurityManager.class.getResourceAsStream("/gemfire.properties");
            properties.load(istream);
            epa.init(properties);
            System.out.println("Encrypted password for '" + args[0] + "' is '" + epa.encryptPassword(args[0]) + "'.");
        } catch (Exception ex) {
            System.err.println(
                "Need to specify password to be encrypted. \nUsage: java io.pivotal.akitada.EncryptedPasswordSecurityManager [password to be encrypted]\nReason for this error:\n"
                    + epa.getExceptionString(ex));
        }
    }

    private String encryptPassword(String password) throws Exception {
        return cipherPassword(password, true);
    }

    private String decryptPassword(String encryptedPassword) throws Exception {
        return cipherPassword(encryptedPassword, false);
    }

    private String cipherPassword(String str, boolean encryption) throws Exception {
        InputStream
            keystoreStream = Files.newInputStream(Paths.get(System.getProperty("user.home") + "/.keystore"));
        KeyStore keystore = KeyStore.getInstance("JCEKS");
        keystore.load(keystoreStream, keystorePassword.toCharArray());
        Key key = keystore.getKey(keystoreName, keystorePassword.toCharArray());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(encryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key, new IvParameterSpec(INIT_VECTOR.getBytes()));

        String retVal = null;
        if (encryption) {
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            retVal = new String(Base64.getEncoder().encode(encrypted), StandardCharsets.UTF_8);
        } else {
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8)));
            retVal = new String(decrypted, StandardCharsets.UTF_8);
        }

        return retVal;
    }

    private String getExceptionString(Exception ex) {
        String exceptionString = null;
        StackTraceElement[] stack = ex.getStackTrace();
        exceptionString = ex.getClass().getName() + ": " + ex.getMessage() + "\n";
        for (StackTraceElement ste : stack) {
            exceptionString = exceptionString + "\tat " + ste + "\n";
        }
        return exceptionString;
    }
}
