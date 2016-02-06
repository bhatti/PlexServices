package com.plexobject.handler.ws.performance;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ws.WSRequestHandlerAdapter;
import com.plexobject.http.TestWebUtils;
import com.plexobject.service.Interceptor;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class WSPerformanceTest {
    private static final int HTTP_PORT = 8325;

    private static ServiceRegistry serviceRegistry;
    private static Account[] accounts = { new Account(1001, "CX2001"),
            new Account(2001, "DX1001"), new Account(3001, "EX3001") };
    private static Security[] securities = {
            new Security(1, "AAPL", "Apple", SecurityType.STOCK),
            new Security(1, "GOOG", "Google", SecurityType.OPTION),
            new Security(1, "CSCO", "Cisco", SecurityType.FUTURE),
            new Security(1, "MSFT", "Microsoft", SecurityType.STOCK) };

    //
    @BeforeClass
    public static void setUp() throws Exception {
        RequestBuilder.filtering = false;
        Properties props = new Properties();
        props.setProperty(Constants.HTTP_PORT, String.valueOf(HTTP_PORT));
        Configuration config = new Configuration(props);
        if (config.getBoolean("logTest")) {
            BasicConfigurator.configure();
            LogManager.getRootLogger().setLevel(Level.INFO);
        }
        serviceRegistry = new ServiceRegistry(config);
        WSRequestHandlerAdapter requestHandlerAdapter = new WSRequestHandlerAdapter(
                serviceRegistry);
        Map<ServiceConfigDesc, RequestHandler> handlers = requestHandlerAdapter
                .createFromPackages("com.plexobject.handler.ws.performance");
        for (Map.Entry<ServiceConfigDesc, RequestHandler> e : handlers
                .entrySet()) {
            serviceRegistry.addRequestHandler(e.getKey(), e.getValue());
        }
        //
        serviceRegistry
                .addInputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> input) {
                        // System.out.println("####INPUT: " +
                        // input.getContents());
                        return input;
                    }
                });
        serviceRegistry
                .addOutputInterceptor(new Interceptor<BasePayload<Object>>() {
                    @Override
                    public BasePayload<Object> intercept(
                            BasePayload<Object> output) {
                        // System.out.println("####OUTPUT: " +
                        // output.getContents());
                        return output;
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
    public void createGetTest() throws Exception {
        long started = System.currentTimeMillis();
        for (int n = 0; n < 1000; n++) {
            Order order = create();
            RequestBuilder request = new RequestBuilder("create", order);
            TestWebUtils.post("http://localhost:" + HTTP_PORT + "/orders",
                    request.encode());
            TestWebUtils.get("http://localhost:" + HTTP_PORT
                    + "/orders?orderId=" + order.orderId);
        }
        System.out.println("createGetTest took "
                + (System.currentTimeMillis() - started));
    }

    @Test
    public void createGetByAccountTest() throws Exception {
        long started = System.currentTimeMillis();
        for (int n = 0; n < 100; n++) {
            Account account = getRandomAccount();
            for (int i = 0; i < 10; i++) {
                Order order = create();
                order.account = account;
                RequestBuilder request = new RequestBuilder("create", order);
                TestWebUtils.post("http://localhost:" + HTTP_PORT + "/orders",
                        request.encode());
            }

            TestWebUtils.get("http://localhost:" + HTTP_PORT
                    + "/orders/account?accountId=" + account.accountId);
        }
        System.out.println("createGetByAccountTest took "
                + (System.currentTimeMillis() - started));

    }

    private static Order create() {
        Order order = new Order(getRandomSecurity(), getRandomAccount(), rand(
                100, 200), rand(1, 10), PriceType.MARKET);
        int maxLegs = rand(5, 20).intValue();
        for (int i = 0; i < maxLegs; i++) {
            order.orderLegs.add(new OrderLeg(OrderSide.BUY, rand(100, 200),
                    rand(1, 10)));
        }
        return order;
    }

    private static Account getRandomAccount() {
        int ndx = (int) Math.abs(System.currentTimeMillis() % accounts.length);
        return accounts[ndx];
    }

    private static Security getRandomSecurity() {
        int ndx = (int) Math
                .abs(System.currentTimeMillis() % securities.length);
        return securities[ndx];
    }

    private static BigDecimal rand(double min, double max) {
        double num = Math.abs(min + (Math.random() * ((max - min) + 1)));
        return new BigDecimal(Double.toString(num));
    }
}
