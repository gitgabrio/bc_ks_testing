package net.cardosi.kieserverclient;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

public class MyConfigurationObject {

  //private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
  private static final String URL = "http://localhost:18080/kie-server/rest/server";
  private static final String USER = "kie-server";
  private static final String PASSWORD = "kie-server";

  private static final MarshallingFormat FORMAT = MarshallingFormat.JSON;
//  private static final MarshallingFormat FORMAT = MarshallingFormat.JAXB;

  private static KieServicesConfiguration conf;
  public static KieServicesClient KIE_SERVICES_CLIENT;

  static {
    conf = KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);

    //If you use custom classes, such as Obj.class, add them to the configuration.
   /* Set<Class<?>> extraClassList = new HashSet<Class<?>>();
    extraClassList.add(Obj.class);
    conf.addExtraClasses(extraClassList);*/

    conf.setMarshallingFormat(FORMAT);
    KIE_SERVICES_CLIENT = KieServicesFactory.newKieServicesClient(conf);
  }
}