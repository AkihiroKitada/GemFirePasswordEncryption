package io.pivotal.akitada;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by akitada on 2018/03/12.
 */
public class TestClient {
  private static final String regionName = "SecurityTestRegion";

  public static void main(String[] argv)  throws Exception {
    System.setProperty("gemfirePropertyFile", "gfclient.properties");

    ClientCache ccache = new ClientCacheFactory()
        .set("name", "TestClient")
        .set("cache-xml-file", "client.xml")
        .set("log-level","config")
        .create();

    Region<String, String> region = ccache.getRegion(regionName);
    System.out.println(region.getFullPath() + " region is created in cache. ");

    region.put("key","value");
    System.out.println("Put something into the region : (key,value) = (key," + region.get("key") + ")");

    System.out.println("--- push any key to stop this client");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    bufferedReader.readLine();

    ccache.close();
  }
}
