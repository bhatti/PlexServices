package com.plexobject.basic;

import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.BasicConfigurator;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.service.Interceptor;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.ServiceRegistryLifecycleAware;
import com.plexobject.handler.ws.WSRequestHandlerAdapter;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/log", method = RequestMethod.GET, codec = CodecType.JSON, concurrency = 10)
public class LogService implements RequestHandler, ServiceRegistryLifecycleAware {
    private static final Logger log = Logger.getLogger(LogService.class);

    public LogService() {
        log.info("Log Service Started");
    }

    @Override
    public void handle(Request request) {
        String application = request.getStringProperty("application");
        String level = request.getStringProperty("level");
        String message = request.getStringProperty("message");
        log.info(application + "|" + level + "|" + message);
        request.getResponse().setContents("ok");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java " + LogService.class.getName() + " properties-file");
            System.exit(1);
        }
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        Configuration config = new Configuration(args[0]);

        ServiceRegistry serviceRegistry = new ServiceRegistry(config);
        WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.basic");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            System.out.println("Adding " + e);
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        serviceRegistry.addRequestHandler(new LogService());
        serviceRegistry.start();
        Thread.currentThread().join();
    }

    @Override
    public void onStarted(ServiceRegistry serviceRegistry) {
        LogService logService = new LogService();

        serviceRegistry.addRequestHandler(logService);
    }

    @Override
    public void onStopped(ServiceRegistry serviceRegistry) {
    }
}
