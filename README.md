#PlexService - HTTP/Messaging based Micro-Service Framework

##Overview

PlexService provides framework for defining secured micro-services, which can be accessed by HTTP or JMS. PlexService framework provides easy conversion of POJO objects into JSON when accessing services remotely. The developers define configurations via annoations, which allow them to define gateway types, encoding scheme, end-points, roles, etc. PlexService provides secured access using role based security. PlexService also provides http-to-jms bridge for accessing services over http that listen to JMS queues/topics. PlexService uses jetty for hosting http/rest services.



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
 - 0.1 : experimental
 
##License
 - MIT

## Defining REST Services

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
HttpToJmsBridge bridge = new HttpToJmsBridge(new Configuration(args[0]), entries);
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

## Sample Application
You can view a full-fledged sample application under plexsvc-sample folder for detailed examples of services and various configurations.

## Support or Contact
  Email bhatti AT plexobject DOT com for any questions or suggestions.


