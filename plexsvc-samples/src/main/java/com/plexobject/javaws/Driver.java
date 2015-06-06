package com.plexobject.javaws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class Driver {
    private static final Logger logger = LoggerFactory.getLogger(Driver.class);
    private static final int DEFAULT_PORT = 9193;

    private static class DelegateHandler implements RequestHandler {
        private static final JsonObjectCodec jsonCodec = new JsonObjectCodec();

        private final Object delegate;
        private final Method method;

        private DelegateHandler(Object delegate, Method method) {
            this.delegate = delegate;
            this.method = method;
        }

        @Override
        public void handle(Request request) {
            logger.info("******* Received request " + request + ", for "
                    + method + ", type "
                    + request.getPayload().getClass().getName());
            Object[] args = new Object[method.getParameters().length];
            Class<?> firstArgType = method.getParameters().length > 0 ? method
                    .getParameters()[0].getType() : null;
            if (method.getParameters().length == 1
                    && !firstArgType.isPrimitive()
                    && !Collection.class.isAssignableFrom(firstArgType
                            .getClass())) {
                logger.info("Converting to " + firstArgType.getName());
                args[0] = jsonCodec.decode((String) request.getPayload(),
                        firstArgType, request.getProperties());
            } else if (method.getParameters().length > 0) {
                logger.info("Unable to handle " + request);
            }
            try {
                Object result = method.invoke(delegate, args);
                if (result != null) {
                    request.getResponseDispatcher().send(result);
                }
            } catch (Exception e) {
                logger.error("Failed to invoke " + method + ", for request "
                        + request, e);
                request.getResponseDispatcher().send(e);
            }
        }
    }

    private final ServiceRegistry serviceRegistry;

    public Driver() {
        Properties props = new Properties();
        props.setProperty("HttpPort", String.valueOf(DEFAULT_PORT));
        Configuration config = new Configuration(props);
        RoleAuthorizer roleAuthorizer = null;
        serviceRegistry = new ServiceRegistry(config, roleAuthorizer);
    }

    private static Class<?> getWebServiceInterface(Class<?> serviceClass) {
        Class<?>[] interfaces = serviceClass.getInterfaces();
        for (Class<?> iface : interfaces) {
            WebService webService = iface.getAnnotation(WebService.class);
            if (webService != null) {
                return iface;
            }
        }
        return null;
    }

    private void addHandlers(String... pkgNames) {
        for (String pkgName : pkgNames) {
            logger.info("Parsing " + pkgName + " ...");
            Reflections reflections = new Reflections(pkgName);

            Set<Class<?>> serviceClasses = reflections
                    .getTypesAnnotatedWith(WebService.class);

            for (Class<?> serviceClass : serviceClasses) {
                if (!serviceClass.isInterface()) {
                    Class<?> webService = getWebServiceInterface(serviceClass);
                    if (webService == null) {
                        continue;
                    }
                    Path path = serviceClass.getAnnotation(Path.class);
                    String endpoint = null;
                    if (path == null) {
                        endpoint = webService.getSimpleName();
                    } else {
                        endpoint = path.value();
                    }
                    addHandlers(serviceClass, webService, endpoint);
                }
            }
        }
        serviceRegistry.start();
    }

    private void addHandlers(Class<?> serviceClass, Class<?> webService,
            String endpoint) {
        try {
            Object service = serviceClass.newInstance();
            for (final Method m : serviceClass.getMethods()) {
                try {
                    if (webService
                            .getMethod(m.getName(), m.getParameterTypes()) != null) {
                        addHandler(service, m, endpoint);
                    }
                } catch (NoSuchMethodException e) {
                } catch (Exception e) {
                    logger.error("Failed to add handler for " + m, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to instantiate " + serviceClass.getName(), e);
        }
    }

    private void addHandler(Object service, Method m, String path) {
        WebMethod webMethod = m.getAnnotation(WebMethod.class);
        if (webMethod == null || !webMethod.exclude()) {
            com.plexobject.service.Method httpMethod = m.getName().startsWith(
                    "get")
                    || m.getName().startsWith("read")
                    || m.getName().startsWith("query")
                    || m.getName().startsWith("find") ? com.plexobject.service.Method.GET
                    : com.plexobject.service.Method.POST;
            ServiceConfigDesc desc = new ServiceConfigDesc(Protocol.HTTP,
                    httpMethod, Void.class, CodecType.JSON, "1.0", path, true,
                    new String[0], 1);
            DelegateHandler handler = new DelegateHandler(service, m);
            serviceRegistry.add(handler, desc);
            logger.info("Registering " + service + ", method " + m + ", desc "
                    + desc);
        }
    }

    private void test() throws Exception {
        testCreateBugReports();
    }

    private void testCreateBugReports() throws Exception {
        String request = "{\"projectId\":2, \"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}";

        String resp = post("/bugReports", request);
        System.out.println(resp);
    }

    private void testGetBugReports() throws Exception {
        String request = "{\"projectId\":2, \"title\":\"As an awesome user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}";

        String resp = post("/bugReports", request);
        System.out.println(resp);
    }

    private void testUpdateBugReports() throws Exception {
        String request = "{\"projectId\":2, \"title\":\"As an awesome user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}";

        String resp = post("/bugReports", request);
        System.out.println(resp);
    }
    /*
     *   def test_update_bug_report(self):
    self.authenticate("erica", "pass")
    request = "{\"title\":\"As an awesome user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}"
    json_resp = self.post("/projects/2/bugreports/2", request)
    self.assertTrue(\"createdAt\" in json_resp, json_resp)                                                                                                                                            
    title = json_resp["title"]
    self.assertTrue(title.find("As an awesome user") >= 0, title)

  def test_list_bug_report(self):
    self.authenticate("erica", "pass")
    json_resp = self.get("/bugreports")
    for d in json_resp:
      self.assertTrue(\"createdAt\" in d, d)

  def test_delete_user_with_invalid_role(self):
    self.authenticate("erica", "pass")
    json_resp = self.post("/users/2/delete", "")
    self.assertTrue(json_resp["status"] == 401, json_resp)

  def test_create_delete_user_with_valid_role(self):
    self.authenticate("scott", "pass")
    json_resp = self.post("/users", "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}")
    uid = json_resp["id"]
    json_resp = self.post("/users/%s/delete" % uid, "")
    self.assertTrue(json_resp["deleted"], json_resp)


  def test_assign_bugreport(self):
    self.authenticate("scott", "pass")
    request = "{\"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}"
    json_resp = self.post("/projects/2/bugreports", request)
    bid = json_resp["id"]
    json_resp = self.post("/projects/2/bugreports/%s/assign" % bid, "assignedTo=scott")
    self.assertTrue(json_resp["assignedTo"] == "scott", json_resp)


  def test_add_member_project(self):
    self.authenticate("scott", "pass")
    json_resp = self.post("/projects/2/membership/add", "projectLead=true&assignedTo=scott")
    self.assertTrue(json_resp["projectLead"] == "scott", json_resp)


  def test_remove_member_project(self):
    self.authenticate("scott", "pass")
    json_resp = self.post("/projects/2/membership/remove", "projectLead=true&assignedTo=scott")
    self.assertTrue("projectLead" not in json_resp, json_resp)
    
      def test_list_project_bug_report(self):
    self.authenticate("erica", "pass")
    json_resp = self.get("/projects/2/bugreports")                                                                                                                                                    
    for d in json_resp:
      self.assertTrue(\"createdAt\" in d, d)

  def test_list_projects(self):
    self.authenticate("erica", "pass")
    json_resp = self.get("/projects")
    for d in json_resp:
      self.assertTrue(\"createdAt\" in d, d)

  def test_create_project(self):
    self.authenticate("scott", "pass")
    json_resp = self.post("/projects", "{\"title\":\"To do\",\"description\":\"T  o do Desc\",\"projectCode\":\"todo\",\"projectLead\":\"erica\",\"members\":[\"alex\"]}")
    self.assertTrue(\"createdAt\" in json_resp, json_resp)

  def test_update_project(self):
    self.authenticate("scott", "pass")
    json_resp = self.post("/projects/2", "\"title\":\"Bugger cool\",\"projectLead\":\"alex\",\"members\":[\"erica\"]}")
    self.assertTrue(\"createdAt\" in json_resp, json_resp)


     */

    private static String get(String path, String contents) throws IOException {
        String url = "http://localhost:" + DEFAULT_PORT + path;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        return toString(con);
    }

    private static String post(String path, String contents) throws IOException {
        String url = "http://localhost:" + DEFAULT_PORT + path;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        out.write(contents.getBytes());
        out.flush();
        out.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        return toString(con);
    }

    private static String toString(HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static void main(String[] args) throws Exception {
        Driver d = new Driver();
        d.addHandlers("com.plexobject.javaws");
        d.test();
    }
}
