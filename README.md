#PlexServices - Micro Framework for building high performance and secured services 

##Overview

PlexServices is a light-weight Java framework for defining secured micro-services, which can be accessed by HTTP, Websockets or JMS interfaces. 

## Design Principles 
PlexServices is designed on following design principles:

- Micro framework - PlexServices is only meant for writing web and messaging services and it's not general purpose MVC framework.

- Uniform interface - PlexServices uses uniform interfaces for defining services, which can be configured to be deployed via REST, websocket, JMS or intra-process event bus.

- Minimal Dependencies: PlexServices depends only on a small number of external libraries for XML/JSON serialization.

- Easily Configurable: PlexServices uses DRY principle using annotations for configuring services but allows them to override the properties at run-time.

- Easily deployable: PlexServices framework supports both war files and embeddable Netty server for easily deplying services. It allows you to determine what services should be deployed together at runtime, thus encourages light weight services that can be deployed independently if needed.

- Development Support: Though, you may use different Java processes to deploy services in your production environment, but you can add all of services in a single Java process during development to simplify the deployment process.

- Operational Support: PlexServices provides monitoring, statistics and logging support for ease of operational support.

## Major Features
- PlexServices framework provides support for converting POJO objects into JSON for service consumption. The developers define service configuration via Java annoations, which allow them to define protocols, encoding scheme, end-points, roles, etc. You can also override the configurations at runtime if needed.

- PlexServices framework allows annotations for validating request parameters or attributes of request object.

- PlexServices framework allows request interceptors to define cross cutting logic that is common to all handlers.

- PlexServices supports role-based security, which are enforced before accessing underlying services. PlexServices provides simple interfaces for providing security rules for access to the services.

- PlexServices also provides bridge for forwarding web requests to JMS based services for accessing services over http or websockets. For example, you may use JMS for all internal services and then create a bridge to expose them through HTTP or websocket interfaces.

- For intra-process communication, PlexServices provides event-bus, which uses same interfaces as other services. In order to decouple your services from any external protocols, you may deploy all services to event-bus and then create event-bus to JMS bridge for external communication.

- PlexServices keeps key metrics such as latency, invocations, errors, etc., which are exposed via JMX interface. It also supports integration with StatsD, which can be enabled via configuration.

- PlexServices provides support for using finite state machines in building services.

- PlexServices supports both war files and Netty 4.0+ for hosting web services and you can deploy both http and websocket services to the same server.

- PlexServices also supports reactive messaging services using JMS APIs and support a number of messageing middlewares such as ActiveMQ, SwiftMQ, etc. 

- PlexServices allows you to import existing JaxWS/JaxRS annotations based services and expose them as REST or POX services. 

- PlexServices allows you to auto-deploy services by specifying package names of services, it deploys all services automatically that implement ServiceConfig annotation.

- PlexServices allows you to filter response JSON fields by passing comma-delimited list of field names from the response object.

- PlexServices allows you to call multiple services (batch) in parallel at once when using JaxRS based JSON requests



##Building
- Download and install <a href="http://www.gradle.org/downloads">Gradle</a>.
- Download and install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html">Java 8</a>.
- Checkout code using 

```
git clone git@github.com:bhatti/PlexServices.git
```

- Compile and build jar file using

```
cd plexsvc-framework
./gradlew jar
```

- Copy and add jar file (build/libs/plexsvc-framework-*.jar) manually in your application.



##Dependencies
- Netty 4.0 for standalone services otherwise web container such as Jetty or Tomcat
- Fast JSON  2.4
- XStream 1.4
- JMS API 1.1

##Version
- 1.7.x

##License
- MIT

## Defining Services

PlexServices uses Netty server as embedded web server to host web services by default and you can easily build REST services as follows:

### Defining a REST service for creating a user
```java 
@ServiceConfig(protocol = Protocol.HTTP, contentsClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = RequestMethod.POST, 
    codec = CodecType.JSON)
@RequiredFields({ @Field(name = "username") })
public class CreateUserService extends AbstractUserService implements
RequestHandler {
  public CreateUserService(UserRepository userRepository) {
    super(userRepository);
  }

  @Override
    public void handle(Request request) {
      User user = request.getContentsAs();
      User saved = userRepository.save(user);
      request.getResponse().setContents(saved);
    }
}


```
You can invoke the service with HTTP request, e.g.
```bash 
curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/users" 
  -d "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
```

Here is a sample python client for accessing these services
```python
resp = requests.post('http://localhost:8181/login', data={'password': password, 'username': username})
json_resp = json.loads(resp.text)
```

### Accepting client specific encoding
The service clients can optionally send Accept header to request response in XML, JSON or any other supported encoding scheme. By default, service returns response in same encoding as codec's type. For example,
```bash 
curl -H "Accept: application/json" http://localhost:8080/plexsvc-samples/array 
```
will return response in JSON format, whereas 
```bash 
curl -H "Accept: application/xml" http://localhost:8080/plexsvc-samples/array
```
will return response in XML format


### Defining a Web service over Websockets for creating a user 
PlexServices supports both war files and embedded Netty server for hosting webservices, however websockets is only supported under Netty server, 
which is default setting.

```java 
@ServiceConfig(protocol = Protocol.WEBSOCKET, contentsClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = RequestMethod.POST, 
    codec = CodecType.JSON)
@RequiredFields({ @Field(name = "username") })
public class CreateUserService extends AbstractUserService implements
RequestHandler {
  public CreateUserService(UserRepository userRepository) {
    super(userRepository);
  }

  @Override
    public void handle(Request request) {
      User user = request.getContentsAs();
      User saved = userRepository.save(user);
      request.getResponse().setContents(saved);
    }
}
```

Note that we use URL format for endpoints for websockets, but it can be in any format as long it's unique for a service.

### Accessing Websocket services from Javascript
```javascript 
var ws = new WebSocket("ws://127.0.0.1:8181/ws");
ws.onopen = function() {
  var req = {"contents":"", "endpoint":"/login", "method":"POST", 
    "username":"scott", "password":"pass"};
  ws.send(JSON.stringify(req));
};

ws.onmessage = function (evt) {
  alert("Message: " + evt.data);
};

ws.onclose = function() {
};

ws.onerror = function(err) {
};
```

### Defining a JMS service for creating a user
```java 
@ServiceConfig(protocol = Protocol.JMS, contentsClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "queue://{scope}-create-user-service-queue", 
      method = RequestMethod.MESSAGE, 
      concurrency = 10,
      codec = CodecType.JSON)
@RequiredFields({ @Field(name = "username") })
public class CreateUserService extends AbstractUserService implements RequestHandler {
    public CreateUserService(UserRepository userRepository) {
      super(userRepository);
    }

    @Override
    public void handle(Request request) {
      User user = request.getContentsAs();
      User saved = userRepository.save(user);
      request.getResponse().setContents(saved);
    }
}
```

The developer can use variables in end-point of queues, which are populated from configurations. For example, you may create scope variable to create different queues by developer-username or environment. PlexServices will serialize POJO classes into JSON when delivering messages over JMS.
Note: concurrency parameter specifies number of concurrent consumers that would listen for the incoming messages.


### Defining a REST service with parameterized URLs
```java 
@ServiceConfig(protocol = Protocol.HTTP, contentsClass = BugReport.class, 
      rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports", 
      method = RequestMethod.POST, 
      codec = CodecType.JSON)
@RequiredFields({ @Field(name = "bugNumber"),
        @Field(name = "projectId"), @Field(name = "priority")
        })
public class CreateBugReportService extends AbstractBugReportService implements RequestHandler {
    public CreateBugReportService(BugReportRepository bugReportRepository,
        UserRepository userRepository) {
      super(bugReportRepository, userRepository);
    }

    @Override
      public void handle(Request request) {
        BugReport report = request.getContentsAs();
        BugReport saved = bugReportRepository.save(report);
        request.getResponse().setContents(saved);
      }
}
```

The http end-point or URL can also store variables, but unlike end-points for
queues/topics, they are populated using http parameters. For example, projectId
parameter would be populated from URL in above example. PlexServices will serialize POJO 
classes into JSON when delivering messages over HTTP.

### Defining a Websocket based service to create bug-report 
```java 
@ServiceConfig(protocol = Protocol.WEBSOCKET, contentsClass = BugReport.class, 
      rolesAllowed = "Employee", endpoint = "queue://{scope}-create-bugreport-service-queue", 
      method = RequestMethod.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "bugNumber"),
        @Field(name = "projectId"), @Field(name = "priority")
        })
public class CreateBugReportService extends AbstractBugReportService implements
        RequestHandler {
    public CreateBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        BugReport report = request.getContentsAs();
        BugReport saved = bugReportRepository.save(report);
        request.getResponse().setContents(saved);
    }

}
```

For websocket based services, the parameters are passed explicitly by consumer. 
PlexServices automatically passes any json parameters sent as part of request, which are consumed by the service.


### Consuming Websocket based service for creating bug-report 
```javascript
 
var ws = new WebSocket("ws://127.0.0.1:8181/ws");
ws.onopen = function() {
  var req = {"contents":{"title":"my title", "description":"my description","bugNumber":"story-201", 
    "assignedTo":"mike", "developedBy":"mike"},"PlexSessionID":"4", 
      "endpoint":"/projects/2/bugreports/2/assign", "method":"POST"};
  ws.send(JSON.stringify(req));
};

ws.onmessage = function (evt) {
  alert("Message: " + evt.data);
};

ws.onclose = function() {
};

ws.onerror = function(err) {
};
```

For websocket based services, the parameters are passed explicitly by consumer. 
PlexServices automatically passes any json parameters sent as part of request, which are consumed by the service.

### Defining a REST service for querying users
```java 
@ServiceConfig(protocol = Protocol.HTTP, contentsClass = User.class, 
  rolesAllowed = "Administrator", endpoint = "/users", method = RequestMethod.GET, 
  codec = CodecType.JSON)
public class QueryUserService extends AbstractUserService implements
RequestHandler {
public QueryUserService(UserRepository userRepository) {
  super(userRepository);
}
@Override
  public void handle(Request request) {
    Collection<User> users = userRepository.getAll(new Predicate<User>() {
        @Override
        public boolean accept(User u) {
            return true;
        }
        });
    request.getResponse().setContents(users);
  }
}
```


### Defining a JMS service for querying users
```java 
@ServiceConfig(protocol = Protocol.JMS, contentsClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "queue://{scope}-query-user-service-queue", 
      method = RequestMethod.MESSAGE, 
      codec = CodecType.JSON)
public class QueryUserService extends AbstractUserService implements RequestHandler {
    public QueryUserService(UserRepository userRepository) {
      super(userRepository);
    }
    @Override
      public void handle(Request request) {
        Collection<User> users = userRepository.getAll(new Predicate<User>() {
            @Override
            public boolean accept(User u) {
                return true;
            }
            });
        request.getResponse().setContents(users);
      }
}
```
  The end-point can contain variables such as scope that are initialized from configuration.


### Input Validation
PlexServices provides flexible annotations for validating input parameters or attributes of incoming rquest, e.g.
```java 
@RequiredFields({
        @Field(name = "username", minLength = 6, maxLength = 12),
        @Field(name = "password", minLength = 8, maxLength = 20),
        @Field(name = "email", minLength = 6, maxLength = 100, regex = ".*@.*"),
        @Field(name = "zipcode", minLength = 5, maxLength = 5, regex = "^\\d{5}$"), })
```
Above example describes rules for validating username, password, email and zipcode. You can specify min/max size of data fields or use regex to verify the data.

 
### Overriding service configuration at runtime and deploying same service via different protocols
In addition to defining service configurations via annotations, you can also override them at runtime and deploy same service via multiple protocols, e.g.
```java 
@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/ping", method = RequestMethod.GET, codec = CodecType.JSON)
public class PingService implements RequestHandler {
  @Override
  public void handle(Request request) {
    String data = request.getProperty("data");
    request.getResponse().setContents(data);
  }
}

And then at runtime, override configuration, e.g.
...
    ServiceRegistry serviceRegistry = new ServiceRegistry(config);
    PingService pingService = new PingService();
    serviceRegistry.addRequestHandler(
                    pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(RequestMethod.MESSAGE)
                            .setEndpoint("queue://ping")
                            .setProtocol(Protocol.JMS)
                            .build());
    serviceRegistry.addRequestHandler(
                    pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(RequestMethod.MESSAGE)
                            .setProtocol(Protocol.WEBSOCKET)
                            .build());
    serviceRegistry.addRequestHandler(pingService,
                    ServiceConfigDesc.builder(pingService)
                            .setMethod(RequestMethod.GET).setProtocol(Protocol.HTTP)
                            .build());

    serviceRegistry.start();
```

Alternatively, you can also deploy a service via JMS protocol and then use web-to-jms bridge to expose the service via HTTP/Websocket protocols.

### Creating a static file server
Though, PlexServices framework is meant for REST or messaging based services,
but here is an example of creating a simple static file server:

```java 
@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/static/*", method = RequestMethod.GET, codec = CodecType.TEXT)
public class StaticFileServer implements RequestHandler {
    private File webFolder;

    public StaticFileServer(String webdir) throws IOException {
        this.webFolder = new File(webdir);
        if (!webFolder.exists()) {
            throw new FileNotFoundException(webdir + " does not exist");
        }
    }

    @Override
    public void handle(Request request) {
        String path = request.getEndpoint().replaceAll("^.static.", "");
        try {
            if (new File(path).isAbsolute()) {
                throw new IOException("Absolute path '" + path
                        + "' not allowed");
            }
            final String canonicalDirPath = webFolder.getCanonicalPath()
                    + File.separator;
            final File filePath = new File(webFolder, path);

            if (!filePath.getCanonicalPath().startsWith(canonicalDirPath)) {
                request.getResponse().setContents(
                        new IOException("Relative path '" + path
                                + "' not allowed"));
            }
            String extension = filePath.getName().substring(
                    filePath.getName().lastIndexOf('.'));
            String contentType = contentType = Files.probeContentType(filePath.toPath());
            if (contentType != null) {
                request.getResponse().setProperty(
                        HttpResponse.CONTENT_TYPE, contentType);
            }
            //
            request.getResponse().setContents(
                    new String(Files.readAllBytes(Paths.get(filePath
                            .toURI()))));
        } catch (IOException e) {
            request.getResponse().setContents(e);
        }
    }
}

```

The end-point can contain variables such as scope that are initialized from configuration.
You can send both text files or binary files. For example, you can call request.getResponse().setContents() method with String parameter to send back text files or byte[] parameter to send back binary files.

### Defining role-based security
```java 
public class BuggerSecurityAuthorizer implements SecurityAuthorizer {
    private final UserRepository userRepository;

    public BuggerSecurityAuthorizer(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
      public void authorize(Request request, String[] roles) throws AuthException {
        String sessionId = request.getSessionId();
        User user = userRepository.getUserBySessionId(sessionId);
        if (user == null) {
          throw new AuthException("authError", "failed to validate session-id");
        }
        for (String role : roles) {
          if (!user.getRoles().contains(role)) {
            throw new AuthException("authError", "failed to match role");
          }
        }
      }
}
```


### Adding interceptors for handling incoming requests 
You can add interceptors for raw-input/raw-output (stringified XML/JSON) as well as interceptors for request/response objects to execute cross cutting logic, e.g.

```java  
serviceRegistry.addInputInterceptor(new Interceptor<BaseRequest<Object>>() {
  @Override
  public BaseRequest<Object> intercept(BaseRequest<Object> input) {
      logger.info("INPUT: " + input);
      return input;
  }
});

serviceRegistry.addOutputInterceptor(new Interceptor<BaseRequest<Object>>() {
  @Override
  public BaseRequest<Object> intercept(BaseRequest<Object> output) {
      logger.info("OUTPUT: " + output);
      return output;
  }
});

serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
  @Override
  public Request intercept(Request input) {
      logger.info("INPUT PAYLOAD: " + input);
      return input;
  }
});

serviceRegistry.addResponseInterceptor(new Interceptor<Response>() {
  @Override
  public Response intercept(Response output) {
      logger.info("OUTPUT PAYLOAD: " + output);
      return output;
  }
});
```


### Filtering JSON Response fields 
You can filter fields by passing comma-delimited list of field names from the JSON response object, e.g.

```java  
ObjectCodecFactory.getInstance().getObjectCodec(CodecType.JSON)
                .setCodecConfigurer(new FilteringJsonCodecConfigurer());

serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
    @Override
    public Request intercept(Request request) {
        if (request
                .hasProperty(FilteringJsonCodecWriter.DEFAULT_FILTERED_NAMES_PARAM)) {
            request.getCodec()
                    .setObjectCodecFilteredWriter(
                            new FilteringJsonCodecWriter(
                                    request,
                                    FilteringJsonCodecWriter.DEFAULT_FILTERED_NAMES_PARAM));
        } else {
            request.getCodec().setObjectCodecFilteredWriter(
                    new NonFilteringJsonCodecWriter());
        }

        return request;
    }
});
```
For example, if your service returns a JSON response of {"getByMyClassResponse":{"id": 485, "name": "my name", "description": "my description"}} and you call the service
as /myservice?filteredFieldNames=id,name then you will only receive {"getByMyClassResponse":{"id": 485,"name": "my name"}}. This can be useful for low bandwidth mobile devices when you are calling an existing service that returns a lot of unnecessary data.



### Creating Http or Websocket bridge for JMS services
Here is how you can setup bridge between HTTP/Websocket and JMS based services. 
```java 
  Configuration config = new Configuration(configFile);
  Collection<WebToJmsEntry> entries = WebToJmsBridge.load(new File(mappingFile));
  ServiceRegistry serviceRegistry = new ServiceRegistry(config);
  serviceRegistry.setWebToJmsEntries(entries);
  serviceRegistry.start();
```
Note that with above configuration, you can access your services either with HTTP or Websocket



  Here is sample JSON configuration for bridge:
```javascript 
  [
  {"codecType":"JSON","endpoint":"/projects/{projectId}/bugreports/{id}/assign","method":"POST",
    "destination":"queue://{scope}-assign-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{projectId}/bugreports","method":"GET",
    "destination":"queue://{scope}-query-project-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/users","method":"GET",
    "destination":"queue://{scope}-query-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects","method":"GET",
    "destination":"queue://{scope}-query-projects-service","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/bugreports","method":"GET",
    "destination":"queue://{scope}-bugreports-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{id}/membership/add","method":"POST",
    "destination":"queue://{scope}-add-project-member-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{id}/membership/remove","method":"POST",
    "destination":"queue://{scope}-remove-project-member-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{projectId}/bugreports","method":"POST",
    "destination":"queue://{scope}-create-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/users","method":"POST",
    "destination":"queue://{scope}-create-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects","method":"POST",
    "destination":"queue://{scope}-create-projects-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/users/{id}","method":"POST",
    "destination":"queue://{scope}-update-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/users/{id}/delete","method":"POST",
    "destination":"queue://{scope}-delete-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{id}","method":"POST",
    "destination":"queue://{scope}-update-project-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/projects/{projectId}/bugreports/{id}","method":"POST",
    "destination":"queue://{scope}-update-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/login","method":"POST",
    "destination":"queue://{scope}-login-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"/logs","method":"POST",
    "destination":"queue://{scope}-log-service-queue","asynchronous":true},
  {"codecType":"JSON","endpoint":"query-project-bugreport-ws","method":"MESSAGE",
    "destination":"queue://{scope}-query-project-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"query-user-ws","method":"MESSAGE",
    "destination":"queue://{scope}-query-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"projects-ws","method":"MESSAGE",
    "destination":"queue://{scope}-query-projects-service","timeoutSecs":30},
  {"codecType":"JSON","endpoint":"bugreports-ws","method":"MESSAGE",
    "destination":"queue://{scope}-bugreports-service-queue","timeoutSecs":30}]
```
Note that Method types of GET/POST will use HTTP based bridge and method type
of MESSAGE will use Websocket based bridge.

The web bridge supports both synchronous and asynchronous requests. When the
configuration defines asynchronous flag as true then message is sent to JMS
but, it does not wait for response. When asynchronous flag is false (by
default), then message is sent to JMS and the web server waits for the response
from the JMS handler. If it doesn't receive the message within timeout then an
error is returned to the web client.

### Configuring HTTP ports in configuration
Here is how you can specify HTTP ports and default websocket path in the properties file:
```bash 
http.port=8181
http.websocketUri=/ws
```
In above example, we are using ActiveMQ as JMS server


### Configuring JMS provider in configuration
Here is how you can specify JMS server in properties file, which is passed
to the runtime.
```bash
JMSContextFactory=org.apache.activemq.jndi.ActiveMQInitialContextFactory
JMSProviderUrl=tcp://localhost:61616
JMSConnectionFactoryLookup=ConnectionFactory
```
In above example, we are using ActiveMQ as JMS server


### Configuring JMS container in configuration
PlexServices comes with simple JMS container but you can replace it with Spring or other JMS frameworks by defining configuration, e.g.:
```bash
jms.containerFactory=com.plexobject.bugger.jms.SpringJMSContainerFactory
```

In above example, we are defining factory to use spring container. You can then define factory as:
```java 
public class SpringJMSContainerFactory implements JMSContainerFactory {
    @Override
    public JMSContainer create(Configuration config) {
        return new SpringJMSContainer(config);
    }
}
```
The samples folder include an example of SpringJMSContainer that you can use.
PlexServices didn't include it in the framework to remove dependency on specific
version of Spring with PlexServices.


### EventBus for intra-process communication
PlexServices uses EventBus for publishing or subscribing messages within the same process. You can define services with protocol of Protocol.EVENT_BUS and add it to service-registry similar to other services, e.g.

```java 
@ServiceConfig(protocol = Protocol.EVENT_BUS, contentsClass = Course.class, endpoint = "courses", method = RequestMethod.MESSAGE)
public static class SaveHandler implements RequestHandler {
    @Override
    public void handle(Request request) {
        Course course = request.getContentsAs();
        courses.put(course.getId(), course);
        request.getResponse().setContents(course);
    }
}

...
serviceRegistry.addRequestHandler(new SaveHandler());
...

```
You can also use EventBus directly without service registry, e.g.
```java 
EventBus eb = new EventBusImpl();
// publishing a request
Request req = Request.builder().setContents("test").build();
eb.publish("test-channel", req);

// subscribing to receive requests
eb.subscribe("test-channel", new RequestHandler() {
   @Override
   public void handle(Request request) {
       logger.info("Received " + request);
   }
}, null);
```

You can optionally pass predicate parameter with subscribe so that you only receive messages that are accepted by your predicate.


### Connecting EventBus to JMS for external communication
Similar to web-to-jms bridge, PlexServices provides event-bus-to-jms bridge, which allows you convert messages from JMS queue/topic into request objects and receive them via event-bus. Likewise, you can setup outgoing bridge to send messages that are published to event bus be forwarded to JMS queues/topics. The bridge also performs encoding similar to JMS or web services, e.g.
```java  
Configuration config = new Configuration(args[0]);
Collection<EventBusToJmsEntry> entries = EventBusToJmsBridge.load(new File(args[1]));
EventBusToJmsBridge.run(config, entries);

```

Here is a sample json file that describes mapping:
```javascript
[{"codecType":"JSON","type":"JMS_TO_EB_CHANNEL", "source":"queue://{scope}-query-user-service-queue",
"target":"query-user-channel", "requestType":"com.plexobject.bugger.model.User"}, 
{"codecType":"JSON","type":"EB_CHANNEL_TO_JMS", "source":"create-user",
"target":"queue://{scope}-assign-bugreport-service-queue","requestType":
"com.plexobject.bugger.model.User"}]
```

### JaxWS/JaxRS annotations support
PlexServices allows you to import existing JaxWS based services and export them as services to be deployed with web server or JMS server. For example, let's assume you have an existing service such as:
```java 
import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;


@WebService
@Path("/courses")
public class CourseServiceImpl implements CourseService {
    private Map<String, Course> courses = new HashMap<>();

    @Override
    @POST
    public Course save(Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    @Override
    @GET
    public Course get(@QueryParam("courseId") Long courseId) {
        Course c = courses.get(String.valueOf(courseId));
        if (c == null) {
            throw new IllegalArgumentException("course not found for "
                    + courseId + ", local " + courses.keySet());
        }
        return c;
    }
    @Override
    @GET
    @Path("/query")
    public List<Course> query(Map<String, Object> criteria) {
        . . .
        return list;
    }

    @Override
    @GET
    public void find(@FormParam("id") Long id, @DefaultValue("all") @FormParam("type") String type) {
        . . .
    }

    @Override
    @Path("/path/{path1}/{path2}")
    @GET
    public Course getWithId(@PathParam("path1"), String @PathParam("path2")) {
    }


    @Override
    @GET
    public void getFile(@FormParam("name") String name, Request request) {
        File webFolder = new File("./src/test/resources");
        try {
            final File filePath = new File(webFolder, name);
            //
            byte[] contents = TestWebUtils.toBytes(new FileInputStream(
                    filePath));
            request.getResponse().setCodecType(CodecType.SERVICE_SPECIFIC);
            request.getResponse().setContents(contents);
            request.getResponse().setHeader(HttpResponse.CONTENT_TYPE,
                    "application/pdf");
            request.getResponse().setHeader(HttpResponse.CONTENT_LENGTH,
                    contents.length);
        } catch (IOException e) {
            request.getResponse().setContents(e);
        }
    }
}

```
You can also use JaxRS's annotations such as GET/POST to specify HTTP methods and QueryParam/FormParam to send query or form parameters. You can use DefaultValue for specifying default form/query parameter and use PathParam to extract parameter from URL path. Note that you can optionally define Path at method level so that methods are invoked for specific URLs. If Path annotations are defined at method level, it will add class-level path, e.g. if in above example "/courses" is defined at class level and "/query" is defined at method level for query so when you call query API, you would use "/courses/query" when invoking to the API. You can also have Request parameter as one of the argument and take full control on what kind of data that you are sending back, e.g. in above example getFile method returns PDF file from the service API.

You can convert the JaxWS service into RequestHandler as follows:

```java 
Configuration config = ...
SecurityAuthorizer securityAuthorizer = ...
serviceRegistry = new ServiceRegistry(config);
serviceRegistry.setSecurityAuthorizer(securityAuthorizer);
WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(config);
Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws");
for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers.entrySet()) {
  serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
}
serviceRegistry.start();
```
Above code looks for classes that implement WebService and createFromPackages
returns RequestHandlers. If you have an existing service object then you can
use create method instead.


### Invoking Multiple Requests with JaxWS/JaxRS annotations 
PlexServices allows you to call multiple services when using JaxWS/JaxRS based requests, e.g. if you are invoking your service with JSON as:
```javascript 
{"service1":{"service1-param":"value"}}
``` 
and
```javascript 
{"service2":{"service2-param":"value"}}
```
You can invoke both services with one request such as:
```javascript 
[{"service1":{"service1-param":"value"}},{"service2":{"service2-param":"value"}}]
``` 

You will then receive response as an array as well, e.g.
```javascript 
[{"service1Response":{"service1-resp":"value"}},{"service2Response":{"service2-resp":"value"}}]
```

This batching of requests can improve performance if client such as mobile app needs to call multiple services. 
Note that all of the services must use same HTTP verb, e.g. all services must be either GET or POST.
Also, if one of the service fails, you will receive errors for that service and normal response from other services, e.g. if you call two services such as:
```javascript 
[{"save":{"id":"1449902","name":"Java"}},{"errorService":""}]]
```
And errorServie fails, you will receive:
```javascript 
[{"saveResponse":{"id":"1449902","name":"Java"}},{"errorServiceResponse":{"errors":[{"errorType":"IOException","message":"IO error"}]}}]
```


### Finite State Machine
PlexServices provides helper classes to implement finite state machine. For example, here is how you can implement FSM for Android application lifecycle:

![Android Lifecycle](http://upload.wikimedia.org/wikipedia/en/f/f6/Android_application_life_cycle.png)

```java 
final TransitionMappings mappings = new TransitionMappings();
mappings.register(new TransitionMapping("Init", "onCreate", "Created"));
mappings.register(new TransitionMapping("Created", "onStart", "Started"));
mappings.register(new TransitionMapping("Started", "onResume", "Resumed"));
mappings.register(new TransitionMapping("Resumed", "onPause", "Paused"));
mappings.register(new TransitionMapping("Paused", "onResume", "Resumed"));
mappings.register(new TransitionMapping("Paused", "onStop", "Stopped"));
mappings.register(new TransitionMapping("Stopped", "onRestart", "Started"));
mappings.register(new TransitionMapping("Stopped", "onDestroy", "Destroyed"));
FSM instance = new FSM(State.of("Init"), mappings, null);
assertEquals("Created", instance.nextStateOnEvent("onCreate", null) .getName());
assertEquals("Started", instance.nextStateOnEvent("onStart", null) .getName());
assertEquals("Resumed", instance.nextStateOnEvent("onResume", null) .getName());
assertEquals("Paused", instance.nextStateOnEvent("onPause", null) .getName());
assertEquals("Resumed", instance.nextStateOnEvent("onResume", null) .getName());
assertEquals("Paused", instance.nextStateOnEvent("onPause", null) .getName());
assertEquals("Stopped", instance.nextStateOnEvent("onStop", null) .getName());
assertEquals("Started", instance.nextStateOnEvent("onRestart", null) .getName());
assertEquals("Resumed", instance.nextStateOnEvent("onResume", null) .getName());
assertEquals("Paused", instance.nextStateOnEvent("onPause", null) .getName());
assertEquals("Stopped", instance.nextStateOnEvent("onStop", null) .getName());
assertEquals("Destroyed", instance.nextStateOnEvent("onDestroy", null) .getName());
```


### JMX Monitoring
PlexServices provides monitoring and management through JMX. For example, you
can start/stop services or view statistics, e.g. 
![JMX Support](http://bhatti.github.io/PlexServices/jmx.png)



### Registering services and starting service container 
PlexServices allows you to specify the services that you want to deploy in
a container and start the container using service-registry, e.g.
```java 
Configuration config = new Configuration(args[0]);
serviceRegistry = new ServiceRegistry(config);
serviceRegistry.setSecurityAuthorizer(new BuggerSecurityAuthorizer(userRepository));
serviceRegistry.addRequestHandler(new CreateUserService(userRepository));
serviceRegistry.addRequestHandler(new UpdateUserService(userRepository));
serviceRegistry.addRequestHandler(new QueryUserService(userRepository));
serviceRegistry.addRequestHandler(new DeleteUserService(userRepository));
serviceRegistry.addRequestHandler(new LoginService(userRepository));
serviceRegistry.addRequestHandler(new CreateProjectService(projectRepository, userRepository));
serviceRegistry.addRequestHandler(new UpdateProjectService(projectRepository, userRepository));
serviceRegistry.addRequestHandler(new QueryProjectService(projectRepository, userRepository));
serviceRegistry.addRequestHandler(new AddProjectMemberService(projectRepository, userRepository));
serviceRegistry.addRequestHandler(new RemoveProjectMemberService(projectRepository, userRepository));
serviceRegistry.addRequestHandler(new CreateBugReportService(bugreportRepository, userRepository));
serviceRegistry.addRequestHandler(new UpdateBugReportService(bugreportRepository, userRepository));
serviceRegistry.addRequestHandler(new QueryBugReportService(bugreportRepository, userRepository));
serviceRegistry.addRequestHandler(new QueryProjectBugReportService(bugreportRepository, userRepository));
serviceRegistry.addRequestHandler(new AssignBugReportService(bugreportRepository, userRepository));
serviceRegistry.start();

```
You will be able to view all of the services in JMX console at runtime.


### Building War file
PlexServices uses embedded Netty server by default for hosting web services but here is you can deploy inside a war file using any J2EE compatible container such as Tomcat, Jetty, JBoss, etc.

Define a class to add your services, e.g.
```java 
public class Deployer implements ServiceRegistryLifecycleAware {
    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        PingService pingService = new PingService();
        ReverseService reverseService = new ReverseService();
        SimpleService simpleService = new SimpleService();
        serviceRegistry.addRequestHandler(pingService);
        serviceRegistry.addRequestHandler(reverseService);
        serviceRegistry.addRequestHandler(simpleService);
    }
    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }
}
```

Then add servlet mapping to the web.xml, e.g.
```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>

<web-app xmlns="http://java.sun.com/xml/ns/j2ee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
    version="2.4">
    <display-name>PlexServices Sample Application</display-name>
    <servlet>
        <servlet-name>plexservice</servlet-name>
        <servlet-class>com.plexobject.http.servlet.WebRequestHandlerServlet</servlet-class>
        <init-param>
            <param-name>plexserviceConfigResourcePath</param-name> 
            <param-value>/myweb.properties</param-value> 
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>plexservice</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
</web-app>  
```

You can define additional properties in myweb.properties declared above such
as:
```bash
service.registryCallbackClass=com.plexobject.basic.Main 
```
or if you wish to auto-deploy all services that implement ServiceConfig then you can use com.plexobject.deploy.AutoDeployer , e.g.
```bash
service.registryCallbackClass=com.plexobject.deploy.AutoDeployer
```

Optionally, you can add class name for the security authorizer, e.g.
```bash 
service.securityAuthorizerClass=com.plexobject.ping.MyAuthorizer
```


PlexServices comes with examples that you can use to deploy using
```bash
cd plexsvc-samples
./gradlew jettyRun
```

### Auto-Deploying
In addition to specifying services manually for deployment, PlexServices provides support to scan all services 
in your application package that implement ServiceConfig annotation and deploy them, e.g.
```bash
java com.plexobject.deploy.AutoDeployer bugger.properties
```
You need to specify package name of your services in the properties file, e.g.
```bash
service.autoDeployPackages=com.plexobject.stock
```

Your services must have default constructor for this option to work. You can specify multiple packages separated by comma if needed.

### Adding Streaming Quotes Service over Websockets 
Here is a small example of creating a streaming quote server that sends real-time
quote quotes over the websockets.


```java 
@ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/quotes", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "symbol"),
        @Field(name = "action") })
public class QuoteServer implements RequestHandler {
    public enum Action {
        SUBSCRIBE, UNSUBSCRIBE
    }

    static final Logger log = LoggerFactory.getLogger(QuoteServer.class);

    private QuoteStreamer quoteStreamer = new QuoteStreamer();

    @Override
    public void handle(Request request) {
        String symbol = request.getProperty("symbol");
        String actionVal = request.getProperty("action");
        Action action = Action.valueOf(actionVal.toUpperCase());
        if (action == Action.SUBSCRIBE) {
            quoteStreamer.add(symbol, request);
        } else {
            quoteStreamer.remove(symbol, request);
        }
    }

    public static void main(String[] args) throws Exception {
        new AutoDeployer().deploy(args[0]);
        Thread.currentThread().join();
    }
}


```
Here is the streaming server that pushes the updates to web clients:
```java 
public class QuoteStreamer extends TimerTask {
    private int delay = 1000;
    private Map<String, Collection<Request>> subscribers = 
      new ConcurrentHashMap<>();
    private QuoteCache quoteCache = new QuoteCache();
    private final Timer timer = new Timer(true);

    public QuoteStreamer() {
        timer.schedule(this, delay, delay);
    }

    public void add(String symbol, Request request) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<Request> requests = subscribers
                    .get(symbol);
            if (requests == null) {
                requests = new HashSet<Request>();
                subscribers.put(symbol, requests);
            }
            requests.add(request);
        }
    }

    public void remove(String symbol, Request request) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<Request> requests = subscribers
                    .get(symbol);
            if (requests != null) {
                requests.remove(request);
            }
        }
    }

    @Override
    public void run() {
        for (Map.Entry<String, Collection<Request>> e : subscribers
                .entrySet()) {
            Quote q = quoteCache.getLatestQuote(e.getKey());
            Collection<Request> requests = new ArrayList<>(
                    e.getValue());
            for (Request r : requests) {
                try {
                    r.getResponse().setContents(q);
                    r.sendResponse();
                } catch (Exception ex) {
                    remove(e.getKey(), d);
                }
            }
        }
    }
}
```


Here is a javascript client that subscribes to the streaming quotes:
```javascript
   <script>
      var ws = new WebSocket("ws://127.0.0.1:8181/ws");
      ws.onopen = function() {
      };
      var lasts = {};
      ws.onmessage = function (evt) {
        //console.log(evt.data);
        var quote = JSON.parse(evt.data).contents;
        var d = new Date(quote.timestamp);
        $('#time').text(d.toString());
        $('#company').text(quote.company);
        $('#last').text(quote.last.toFixed(2));
        var prev = lasts[quote.company];
        if (prev != undefined) {
          var change = quote.last - prev;
          if (change >= 0) {
            $('#change').css({'background-color':'green'});
          } else {
            $('#change').css({'background-color':'red'});
          }
          $('#change').text(change.toFixed(2));
        } else {
          $('#change').text('N/A');
        }
        lasts[quote.company] = quote.last;
      };

      ws.onclose = function() {
      };

      ws.onerror = function(err) {
      };
      function send(payload) {
        $('#input').text(payload);
        ws.send(payload);
      }
      $(document).ready(function() {
        $("#subscribe").click(function() {
          var symbol = $("#symbol").val();
          var req = {"endpoint":"/quotes", "symbol":symbol, "action":"subscribe"};
          send(JSON.stringify(req));
        });
      });
      $(document).ready(function() {
        $("#unsubscribe").click(function() {
          var symbol = $("#symbol").val();                                                                                            
          var req = {"endpoint":"/quotes", "symbol":symbol, "action":"unsubscribe"};
          send(JSON.stringify(req));
        });
      });
   <script>
```

Here is the html form that displays quotes:
```html
  <body>
    <form>
      Symbol:<input type="text" id="symbol" value="AAPL" size="4" />
      <input type="button" id="subscribe" value="Subscribe"/>
      <input type="button" id="unsubscribe" value="Unsubscribe"/>
    </form>

    <br>

    <table id="quotes" class="quote" width="600" border="2" cellpadding="0" cellspacing="3">
      <thead>
        <tr>
          <th>Time</th>
          <th>Company</th>
          <th>Last</th>
          <th>Change</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td id="time"></td>
          <td id="company"></td>
          <td id="last"></td>
          <td id="change"></td>
        </tr>
      </tbody>
    </table>
  </body>
```

## API Doc
[Java Doc](http://bhatti.github.io/PlexServices/javadoc/)


## Sample Applications
      You can view sample applications under plexsvc-sample folder for detailed examples of services and various configurations.

## Support or Contact
      Email bhatti AT plexobject DOT com for any questions or suggestions.

