# GemFire SecurityManager sample to apply encrypted password

The sample SecurityManager to apply encrypted password for Pivotal GemFire 9.0 or later and Apache Geode - tested with GemFire 9.3 running on JDK 1.8.0_152.

Please note that password encryption is not secured at all although it may prevent the "password leakage." If the encrypted password itself is leaked, you give a certain degree of control to the malicious thrid party as a result. You have to manage access permission properly for the encrypted password itself in the reality. So the security level is not so differenct compared with plane test password, actually.

## Preparation

You need to update JCE jars, whichi is used for decryption/encryption. Otherwise, you may see the following exception if trying to execute SecurityManager modules.

```
java.security.InvalidKeyException: Illegal key size
```

Please download JCE modules from the following site and put them at $JAVA_HOME/

http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

You need to create a jks store with using keytool command like the following.

```
$ keytool -genseckey -storetype jceks -keyalg AES -keysize 256 -alias mykey -storepass gemauth -keypass gemauth
```

And also, of course, you need to install Pivotal GemFire 9.0 or later, or Apache Geode.

## Steps to run the sample SecurityManager

You need to modify the following configuration file under "resources" directory.

* client.xml
* gemfire.properties
* gfclient.properties

Mainly, you will modify IP addresses according to your environment, first of all.

Next, you have to create an encrypted password. The sample SecurityManager authenticates the following users and passwords for them are as same as users' name (i.e., if the user is "admin", then the password is "admin.")

* user
* reader
* writer
* admin
* root
* administrator

You can create encrypted password by executing SecurityManager directly with adding "resources" directly for above configuration files in CLASSPATH - in this case, let's use "admin" user.

```
$ java io.pivotal.akitada.EncryptedPasswordSecurityManager admin
```

Then, copy the encrypted password for "admin" user and paste it as a value for ''security-password'' parameter for gemfire.properties and gfclient.properties.

Next, please modify the following start up script for a locator and a cache server, according to your environment.

* startLocator.sh
* startServer.sh

At least, you modify IP and port number with startLocator.sh. And also, you have to modify classpath setting for both scripts to add the path to the above ''resources'' directories to refer to those configuration files.

Then, you can start the locator and the cache server. If you see AuthenticationFailedExceptions, please review your configuration - mainly, confirm whether the encrypted password is correct or not.

After confirmed that the locator and the cache server start fine, let's connect to the cluster from gfsh. If you try to connect to the locator, you may be asked to input user name and password. In this case, please input "admin" as the user name and the encrypted password for "admin" user. You can also specify thme from command line like the following.

```
gfsh>connect --locator=172.16.227.2[55221] --user=admin --password=6sN6YjzA4yJWC9DzoQdxHw==
```

At last, let's connect from a client application to the cluster. In this case, you need to create a credential via your custom class implementing AuthInitialize. In this example, ClientAuthInitialize class is provided. Basically, you won't modify ClientAuthInitialize class. Then, execute "TestClient" class with adding ''resources'' directories, ClientAuthInitialize class and GemFire/Geode jars to CLASSPATH.