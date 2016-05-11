package com.plexobject.handler.ws;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.json.NonFilteringJsonCodecWriter;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.BaseServiceClient;
import com.plexobject.service.Interceptor;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class SymbolTest {
    private static ServiceRegistry serviceRegistry;
    private static WSRequestHandlerAdapter requestHandlerAdapter;
    private SymbolServiceClient client = new SymbolServiceClient();

    public static class SymbolData {
        private String symbol;
        private String description;

        public SymbolData() {
        }

        public SymbolData(String symbol, String description) {
            this.symbol = symbol;
            this.description = description;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    @WebService
    public interface SymbolService {
        SymbolData find(String symbol);

        SymbolData[] findAll(String[] symbols);

        SymbolData get(long id);

        SymbolData save(SymbolData data);
    }

    public static class SymbolServiceClient extends BaseServiceClient implements
            SymbolService {
        private static final String URI = "/SymbolService";

        @WebMethod(exclude = true)
        @Override
        public SymbolData find(String symbol) {
            RequestBuilder request = new RequestBuilder("find", symbol);
            try {
                SymbolData data = post(URI, request, SymbolData.class, null);
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @WebMethod(exclude = true)
        @Override
        public SymbolData get(long id) {
            RequestBuilder request = new RequestBuilder("get", id);
            try {
                SymbolData data = post(URI, request, SymbolData.class, null);
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @WebMethod(exclude = true)
        @Override
        public SymbolData save(SymbolData data) {
            RequestBuilder request = new RequestBuilder("save", data);
            try {
                return post(URI, request, SymbolData.class, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @WebMethod(exclude = true)
        @Override
        public SymbolData[] findAll(String[] symbols) {
            RequestBuilder request = new RequestBuilder("findAll", symbols);
            try {
                return post(URI, request, SymbolData[].class, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @WebService
    @Path("/SymbolService")
    public static class SymbolServiceImpl implements SymbolService {
        @Override
        public SymbolData find(String symbol) {
            return new SymbolData(symbol, symbol + "-desc");
        }

        @Override
        public SymbolData get(long id) {
            return new SymbolData(String.valueOf(id), String.valueOf(id)
                    + "-desc");
        }

        @Override
        public SymbolData save(SymbolData data) {
            return data;
        }

        @Override
        public SymbolData[] findAll(String[] symbols) {
            SymbolData[] data = new SymbolData[symbols.length];
            for (int i = 0; i < symbols.length; i++) {
                data[i] = find(symbols[i]);
            }
            return data;
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT,
                String.valueOf(BaseServiceClient.DEFAULT_PORT));
        props.setProperty(Constants.JAXWS_NAMESPACE, "");
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }

        serviceRegistry = new ServiceRegistry(config);
        requestHandlerAdapter = new WSRequestHandlerAdapter(serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }

        final NonFilteringJsonCodecWriter nonFilteringJsonCodecWriter = new NonFilteringJsonCodecWriter();

        serviceRegistry.addRequestInterceptor(new Interceptor<Request>() {
            @Override
            public Request intercept(Request input) {
                input.getCodec().setObjectCodecFilteredWriter(
                        nonFilteringJsonCodecWriter);
                return input;
            }
        });

        serviceRegistry.start();
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceRegistry.stop();
    }

    @Test
    public void testFind() throws Throwable {
        SymbolData data = client.find("AAPL");
        assertEquals("AAPL", data.getSymbol());
    }

    @Test
    public void testFindAll() throws Throwable {
        SymbolData[] data = client.findAll(new String[] { "AAPL", "GOOG" });
        assertEquals(2, data.length);
        assertEquals("AAPL", data[0].getSymbol());
        assertEquals("GOOG", data[1].getSymbol());
    }

    @Test
    public void testGet() throws Throwable {
        SymbolData data = client.get(10);
        assertEquals("10", data.getSymbol());
    }

    @Test
    public void testSave() throws Throwable {
        SymbolData data = new SymbolData("GOOG", "Google");
        SymbolData saved = client.save(data);
        assertEquals(data.getSymbol(), saved.getSymbol());
        assertEquals(data.getDescription(), saved.getDescription());
    }
}
