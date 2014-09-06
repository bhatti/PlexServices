#PlexService - REST/Messaging based Micro-Service Framework

##Overview

PlexService is a simple Java framework for defining secured micro-services, which can be accessed by HTTP, Websockets or JMS interfaces. PlexService framework provides provides basic support for converting POJO objects into JSON for service consumption. The developers define service configuration via Java annoations, which allow them to define gateway types, encoding scheme, end-points, roles, etc. 

PlexService supports role-based security, which are enforced before accessing underlying services. 

PlexService also provides web-to-jms bridge for accessing services over http or websockets that listen to JMS queues/topics. 

PlexService keeps key metrics such as latency, invocations, errors, etc., which are exposed via JMX interface. It also supports integration with StatsD, which can be enabled via configuration.

PlexService supports jetty 9.0+ and Netty 4.0+ for hosting web services and you can use http or websockets transport for your services. 

PlexService also supports JMS compatible messageing middlewares such as ActiveMQ, SwiftMQ, etc. 


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
- 0.3

##License
- MIT

## Defining Services

### Defining a REST service for creating a user
```java 
@ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, 
    codec = CodecType.JSON)
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
You can invoke the service with HTTP request, e.g.
```bash 
curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/users" -d "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
```

### Defining a Web service over Websockets for creating a user
```java 
@ServiceConfig(gateway = GatewayType.WEBSOCKET, requestClass = User.class, 
    rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, 
    codec = CodecType.JSON)
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

Note that we use URL format for endpoints for websockets, but it can be in any format as long it's unique for a service.

### Accessing Websocket services from Javascript
```javascript 
var ws = new WebSocket("ws://127.0.0.1:8181/ws");
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
      method = Method.MESSAGE, 
      codec = CodecType.JSON)
public class CreateUserService extends AbstractUserService implements RequestHandler {
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
      method = Method.POST, 
      codec = CodecType.JSON)
public class CreateBugReportService extends AbstractBugReportService implements RequestHandler {
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

### Defining a Websocket based service to create bug-report 
```java 
@ServiceConfig(gateway = GatewayType.WEBSOCKET, requestClass = BugReport.class, 
      rolesAllowed = "Employee", endpoint = "queue:{scope}-create-bugreport-service-queue", 
      method = Method.MESSAGE, codec = CodecType.JSON)
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

  For websocket based services, the parameters are passed explicitly by consumer. 
PlexService automatically passes any json parameters sent as part of request, which are consumed by the service.


### Consuming Websocket based service for creating bug-report 
```javascript
 
var ws = new WebSocket("ws://127.0.0.1:8181/ws");
ws.onopen = function() {
  var req = {"payload":{"title":"my title", "description":"my description","bugNumber":"story-201", 
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
PlexService automatically passes any json parameters sent as part of request, which are consumed by the service.

### Defining a REST service for querying users
```java 
  @ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "/users", method = Method.GET, 
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
        request.getResponseBuilder().send(users);
      }
  }
```


### Defining a JMS service for querying users
```java 
@ServiceConfig(gateway = GatewayType.JMS, requestClass = User.class, 
      rolesAllowed = "Administrator", endpoint = "queue:{scope}-query-user-service-queue", 
      method = Method.MESSAGE, 
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
  {"codecType":"JSON","path":"/projects/{projectId}/bugreports/{id}/assign","method":"POST",
    "destination":"queue:{scope}-assign-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{projectId}/bugreports","method":"GET",
    "destination":"queue:{scope}-query-project-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/users","method":"GET",
    "destination":"queue:{scope}-query-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects","method":"GET",
    "destination":"queue:{scope}-query-projects-service","timeoutSecs":30},
  {"codecType":"JSON","path":"/bugreports","method":"GET",
    "destination":"queue:{scope}-bugreports-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{id}/membership/add","method":"POST",
    "destination":"queue:{scope}-add-project-member-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{id}/membership/remove","method":"POST",
    "destination":"queue:{scope}-remove-project-member-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{projectId}/bugreports","method":"POST",
    "destination":"queue:{scope}-create-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/users","method":"POST",
    "destination":"queue:{scope}-create-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects","method":"POST",
    "destination":"queue:{scope}-create-projects-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/users/{id}","method":"POST",
    "destination":"queue:{scope}-update-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/users/{id}/delete","method":"POST",
    "destination":"queue:{scope}-delete-user-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{id}","method":"POST",
    "destination":"queue:{scope}-update-project-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/projects/{projectId}/bugreports/{id}","method":"POST",
    "destination":"queue:{scope}-update-bugreport-service-queue","timeoutSecs":30},
  {"codecType":"JSON","path":"/login","method":"POST",
    "destination":"queue:{scope}-login-service-queue","timeoutSecs":30}]
```

### Configuring JMS provider in configuration
Here is how you can specify JMS container in properties file, which is passed
to the runtime.
```bash
jms.contextFactory=org.apache.activemq.jndi.ActiveMQInitialContextFactory
jms.providerUrl=tcp://localhost:61616
jms.connectionFactoryLookup=ConnectionFactory
```
In above example, we are using ActiveMQ as JMS container 


### Configuring HTTP container in configuration
Here is how you can specify container for your web services in properties file:
to the runtime.
```bash
http.webServiceContainer=JETTY 
```
In above example, we are using Jetty embedded server for hosting web services.
We can switch to Netty embedded server by changing configuration file
as follows:
```bash
http.webServiceContainer=NETTY 
```


### EventBus for intra-process communication
Here is an example of using EventBus publishing or subscribing messages within the same process:
```java 
EventBus eb = new EventBusImpl();
// publishing a request
Request req = Request.builder().setPayload("test").build();
eb.publish("test-channel", req);

// subscribing to receive requests
eb.subscribe("test-channel", new RequestHandler() {
   @Override
   public void handle(Request request) {
       System.out.println("Received " + request);
   }
}, null);
```

You can optionally pass predicate parameter with subscribe so that you only receive messages that are accepted by your predicate.


### Connecting EventBus to JMS for external communication
Similar to web-to-jms bridge, PlexService provides event-bus-to-jms bridge, which allows you convert messages from JMS queue/topic into request objects and receive them via event-bus. Likewise, you can setup outgoing bridge to send messages that are published to event bus be forwarded to JMS queues/topics. The bridge also performs encoding similar to JMS or web services, e.g.
```java  
final String mappingJson = IOUtils
       .toString(new FileInputStream(args[1]));
Collection<EventBusToJmsEntry> entries = new JsonObjectCodec().decode(
       mappingJson, new TypeReference<List<EventBusToJmsEntry>>() {
       });
Configuration config = new Configuration(args[0]);
EventBus eb = new EventBusImpl();
EventBusToJmsBridge bridge = new EventBusToJmsBridge(config, entries, eb);
bridge.startBridge();
```

Here is a sample json file that describes mapping:
```javascript
[{"codecType":"JSON","type":"JMS_TO_EB_CHANNEL", "source":"queue:{scope}-query-user-service-queue",
"target":"query-user-channel", "requestType":"com.plexobject.bugger.model.User"}, 
{"codecType":"JSON","type":"EB_CHANNEL_TO_JMS", "source":"create-user",
"target":"queue:{scope}-assign-bugreport-service-queue","requestType":
"com.plexobject.bugger.model.User"}]
```


### Adding Streaming Quotes Service over Websockets 
Here is an example of creating a streaming quote server that sends real-time
quote quotes over the websockets.


```java 
@ServiceConfig(gateway = GatewayType.WEBSOCKET, requestClass = Void.class, 
  endpoint = "/quotes", method = Method.MESSAGE, codec = CodecType.JSON)
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
        log.info("Received " + request);
        ValidationException
                .builder()
                .assertNonNull(symbol, "undefined_symbol", "symbol",
                        "symbol not specified")
                .assertNonNull(actionVal, "undefined_action", "action",
                        "action not specified").end();
        Action action = Action.valueOf(actionVal.toUpperCase());
        if (action == Action.SUBSCRIBE) {
            quoteStreamer.add(symbol, request.getResponseBuilder());
        } else {
            quoteStreamer.remove(symbol, request.getResponseBuilder());
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration(args[0]);
        QuoteServer service = new QuoteServer();
        Collection<RequestHandler> services = new ArrayList<>();
        services.add(new QuoteServer());
        //
        ServiceRegistry serviceRegistry = new ServiceRegistry(config, services, null);
        serviceRegistry.start();
        Thread.currentThread().join();
    }
}

```
Here is the streaing server that pushes the updates to web clients:
```java 
public class QuoteStreamer extends TimerTask {
    private int delay = 1000;
    private Map<String, Collection<ResponseDispatcher>> subscribers = 
      new ConcurrentHashMap<>();
    private QuoteCache quoteCache = new QuoteCache();
    private final Timer timer = new Timer(true);

    public QuoteStreamer() {
        timer.schedule(this, delay, delay);
    }

    public void add(String symbol, ResponseDispatcher dispatcher) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<ResponseDispatcher> dispatchers = subscribers
                    .get(symbol);
            if (dispatchers == null) {
                dispatchers = new HashSet<ResponseDispatcher>();
                subscribers.put(symbol, dispatchers);
            }
            dispatchers.add(dispatcher);
        }
    }

    public void remove(String symbol, ResponseDispatcher dispatcher) {
        symbol = symbol.toUpperCase();
        synchronized (symbol.intern()) {
            Collection<ResponseDispatcher> dispatchers = subscribers
                    .get(symbol);
            if (dispatchers != null) {
                dispatchers.remove(dispatcher);
            }
        }
    }

    @Override
    public void run() {
        for (Map.Entry<String, Collection<ResponseDispatcher>> e : subscribers
                .entrySet()) {
            Quote q = quoteCache.getLatestQuote(e.getKey());
            Collection<ResponseDispatcher> dispatchers = new ArrayList<>(
                    e.getValue());
            for (ResponseDispatcher d : dispatchers) {
                try {
                    d.send(q);
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
        var quote = JSON.parse(evt.data).payload;
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
[Java Doc](http://bhatti.github.io/PlexService/javadoc/)


## Sample Application
      You can view a full-fledged sample application under plexsvc-sample folder for detailed examples of services and various configurations.

## Support or Contact
      Email bhatti AT plexobject DOT com for any questions or suggestions.


