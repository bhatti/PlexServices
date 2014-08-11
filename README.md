#PlexService - REST/Messaging based Micro-Service Framework

##Overview

PlexService is a simple Javaa framework for defining secured micro-services, which can be accessed by HTTP, Websockets or JMS interfaces. PlexService framework provides provides basic support for converting POJO objects into JSON for remote consumption. The developers define service configuration via Java annoations, which allow them to define gateway types, encoding scheme, end-points, roles, etc. PlexService supports role-based security, which are called before accessing underlying services. 

PlexService also provides web-to-jms bridge for accessing services over http or websockets that listen to JMS queues/topics. PlexService uses jetty for serving web services over http or websockets. Finally, PlexService keeps key metrics such as latency, invocations, errors, etc., which are exposed via JMX interface.


##Building
- Download and install <a href="http://www.gradle.org/downloads">Gradle</a>.
- Download and install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html">Java 8</a>.
- Checkout code using 

```
git clone git@github.com:bhatti/PlexService.git
```

- Compile and build jar file using

```
./gradlew jar
```

- Copy and add jar file manually in your application.


##Version
- 0.2 : experimental

##License
- MIT

## Defining Services

### Defining a REST service for creating a user
```java 
@ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, 
    contentType = "application/json")
public class CreateUserService extends AbstractUserService implements
RequestHandler {
  public CreateUserService(UserRepository userRepository) {
    super(userRepository);
  }

  @Override
    public void handle(Request request) {
      User user = request.getPayload();
      user.validate();
      User saved = userRepository.save(user);
      request.getResponseBuilder().send(saved);
    }
}
```


### Defining a Web service over Websockets for creating a user
```java 
@ServiceConfig(gateway = GatewayType.WEBSOCKET, requestClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, 
    contentType = "application/json")
public class CreateUserService extends AbstractUserService implements
RequestHandler {
  public CreateUserService(UserRepository userRepository) {
    super(userRepository);
  }

  @Override
    public void handle(Request request) {
      User user = request.getPayload();
      user.validate();
      User saved = userRepository.save(user);
      request.getResponseBuilder().send(saved);
    }
}
```


### Accessing Websocket services from Javascript
```javascript 
var ws = new WebSocket("ws://127.0.0.1:8181/users");
ws.onopen = function() {
  var req = {"payload":"", "endpoint":"/login", "method":"POST", "username":"scott", "password":"pass"};
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
    @ServiceConfig(gateway = GatewayType.JMS, requestClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "queue:{scope}-create-user-service-queue", 
      method = Method.LISTEN, contentType = "application/json")
    public class CreateUserService extends AbstractUserService implements
    RequestHandler {
    public CreateUserService(UserRepository userRepository) {
    super(userRepository);
    }

    @Override
    public void handle(Request request) {
      User user = request.getPayload();
      user.validate();
      User saved = userRepository.save(user);
      request.getResponseBuilder().send(saved);
    }
    }
```
  The developer can use variables in end-point of queues, which are populated from configurations. For example, you may create scope variable to create different queues by developer-username or environment. PlexService will serialize POJO classes into JSON when delivering messages over JMS.


### Defining a REST service with parameterized URLs
```java 
  @ServiceConfig(gateway = GatewayType.HTTP, requestClass = BugReport.class, 
      rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports", 
      method = Method.POST, contentType = "application/json")
  public class CreateBugReportService extends AbstractBugReportService implements
  RequestHandler {
    public CreateBugReportService(BugReportRepository bugReportRepository,
        UserRepository userRepository) {
      super(bugReportRepository, userRepository);
    }

    @Override
      public void handle(Request request) {
        BugReport report = request.getPayload();
        report.validate();
        BugReport saved = bugReportRepository.save(report);
        request.getResponseBuilder().send(saved);
      }
  }
```

  The http end-point or URL can also store variables, but unlike end-points for
  queues/topics, they are populated using http parameters. For example, projectId
  parameter would be populated from URL in above example. PlexService will serialize POJO 
  classes into JSON when delivering messages over HTTP.

### Defining a REST service for querying users
```java 
  @ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "/users", method = Method.GET, 
      contentType = "application/json")
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
        request.getResponseBuilder().send(users);
      }
  }
```


### Defining a JMS service for querying users
```java 
  @ServiceConfig(gateway = GatewayType.JMS, requestClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "queue:{scope}-query-user-service-queue", 
      method = Method.LISTEN, contentType = "application/json")
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
        request.getResponseBuilder().send(users);
      }
  }
```
  The end-point can contain variables such as scope that are initialized from configuration.

### Defining role-based security
```java 
  public class BuggerRoleAuthorizer implements RoleAuthorizer {
    private final UserRepository userRepository;

    public BuggerRoleAuthorizer(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
      public void authorize(Request request, String[] roles) throws AuthException {
        String sessionId = request.getSessionId();
        User user = userRepository.getUserBySessionId(sessionId);
        if (user == null) {
          throw new AuthException(Constants.SC_UNAUTHORIZED,
              request.getSessionId(), request.getRemoteAddress(),
              "failed to validate session-id");
        }
        for (String role : roles) {
          if (!user.getRoles().contains(role)) {
            throw new AuthException(Constants.SC_UNAUTHORIZED,
                request.getSessionId(), request.getRemoteAddress(),
                "failed to match role");
          }
        }
      }
  }
```


### Registering services and starting service container
```java 
Collection<RequestHandler> services = new HashSet<>();
services.add(new CreateUserService(userRepository));
services.add(new UpdateUserService(userRepository));
services.add(new QueryUserService(userRepository));
services.add(new DeleteUserService(userRepository));
services.add(new LoginService(userRepository));
services.add(new CreateProjectService(projectRepository, userRepository));
services.add(new UpdateProjectService(projectRepository, userRepository));
services.add(new QueryProjectService(projectRepository, userRepository));
services.add(new AddProjectMemberService(projectRepository, userRepository));
services.add(new RemoveProjectMemberService(projectRepository, userRepository));
services.add(new CreateBugReportService(bugreportRepository, userRepository));
services.add(new UpdateBugReportService(bugreportRepository, userRepository));
services.add(new QueryBugReportService(bugreportRepository, userRepository));
services.add(new QueryProjectBugReportService(bugreportRepository, userRepository));

services.add(new AssignBugReportService(bugreportRepository, userRepository));
serviceRegistry = new ServiceRegistry(config, services, new BuggerRoleAuthorizer(userRepository));
serviceRegistry.start();

```


### Creating Http to JMS bridge
```java 
  final String mappingJson = IOUtils.toString(new FileInputStream( args[1]));
Collection<HttpToJmsEntry> entries = new JsonObjectCodec().decode(
    mappingJson, new TypeReference<List<HttpToJmsEntry>>() {
    });
WebToJmsBridge bridge = new WebToJmsBridge(new Configuration(args[0]), entries, GatewayType.HTTP);
bridge.startBridge();
```



### Creating Websocket to JMS bridge
```java 
  final String mappingJson = IOUtils.toString(new FileInputStream( args[1]));
Collection<HttpToJmsEntry> entries = new JsonObjectCodec().decode(
    mappingJson, new TypeReference<List<HttpToJmsEntry>>() {
    });
WebToJmsBridge bridge = new WebToJmsBridge(new Configuration(args[0]), entries, GatewayType.WEBSOCKET);
bridge.startBridge();
```

  Here is JSON configuration for bridge:
```javascript 
  [
  {"contentType":"application/json","path":"/projects/{projectId}/bugreports/{id}/assign","method":"POST",
    "destination":"queue:{scope}-assign-bugreport-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{projectId}/bugreports","method":"GET",
    "destination":"queue:{scope}-query-project-bugreport-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/users","method":"GET",
    "destination":"queue:{scope}-query-user-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects","method":"GET",
    "destination":"queue:{scope}-query-projects-service","timeoutSecs":30},
  {"contentType":"application/json","path":"/bugreports","method":"GET",
    "destination":"queue:{scope}-bugreports-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{id}/membership/add","method":"POST",
    "destination":"queue:{scope}-add-project-member-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{id}/membership/remove","method":"POST",
    "destination":"queue:{scope}-remove-project-member-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{projectId}/bugreports","method":"POST",
    "destination":"queue:{scope}-create-bugreport-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/users","method":"POST",
    "destination":"queue:{scope}-create-user-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects","method":"POST",
    "destination":"queue:{scope}-create-projects-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/users/{id}","method":"POST",
    "destination":"queue:{scope}-update-user-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/users/{id}/delete","method":"POST",
    "destination":"queue:{scope}-delete-user-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{id}","method":"POST",
    "destination":"queue:{scope}-update-project-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/projects/{projectId}/bugreports/{id}","method":"POST",
    "destination":"queue:{scope}-update-bugreport-service-queue","timeoutSecs":30},
  {"contentType":"application/json","path":"/login","method":"POST",
    "destination":"queue:{scope}-login-service-queue","timeoutSecs":30}]
```

## API Doc
      <ul>
      <li><a href="http://bhatti.github.io/PlexService/javadoc/">Java Doc</a>
      </ul>


## Sample Application
      You can view a full-fledged sample application under plexsvc-sample folder for detailed examples of services and various configurations.

## Support or Contact
      Email bhatti AT plexobject DOT com for any questions or suggestions.


