package com.plexobject.service.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.route.RouteResolver;

public class RequestHandlerPathsTest {
    private static class TestHandler implements RequestHandler {
        private String name;

        private TestHandler(String name) {
            this.name = name;
        }

        @Override
        public void handle(Request request) {

        }

        public String toString() {
            return name;
        }

    }

    static class TestData {
        final String handlerUrl;
        final String handlerName;
        final String searchUrl;
        final Map<String, String> params = new HashMap<>();

        public TestData(String handlerUrl, String handlerName,
                String searchUrl, String... params) {
            this.handlerUrl = handlerUrl;
            this.handlerName = handlerName;
            this.searchUrl = searchUrl;
            for (int i = 0; i < params.length; i += 2) {
                this.params.put(params[i], params[i + 1]);
            }
        }

        @Override
        public String toString() {
            return "TestData [handlerUrl=" + handlerUrl + ", handlerName="
                    + handlerName + ", searchUrl=" + searchUrl + ", params="
                    + params + "]";
        }
    }

    RouteResolver<RequestHandler> requestHandlerPaths = new RouteResolver<>();

    private static TestData[] TEST_DATA = {
            new TestData("/", "Root", "/"),
            new TestData("/users", "Users", "/users"),
            new TestData("/users/{id}", "User with id", "/users/10", "id", "10"),
            new TestData("/users/{uid}/roles/{roleId}",
                    "User with uid and roleId", "/users/10/roles/20", "uid",
                    "10", "roleId", "20"),
            new TestData("/users/{uid}/roles/{roleId}/assign",
                    "User with uid and roleId assign",
                    "/users/10/roles/20/assign", "uid", "10", "roleId", "20"),
            new TestData("/bugreports", "Bug reports", "/bugreports"),
            new TestData("/projects", "Projects", "/projects"),
            new TestData("/projects/{id}", "Project with id", "/projects/50",
                    "id", "50"),
            new TestData("/projects/{projectId}/bugreports/{id}",
                    "Project and Bugreport with ids",
                    "/projects/15/bugreports/30", "projectId", "15", "id", "30"),
            new TestData("/projects/{projectId}/bugreports/{id}/assign",
                    "Project bugreport assign with ids",
                    "/projects/15/bugreports/30/assign", "projectId", "15",
                    "id", "30") };

    @Before
    public void setUp() throws Exception {
        for (TestData td : TEST_DATA) {
            requestHandlerPaths.put(td.handlerUrl, new TestHandler(
                    td.handlerName));
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetHandler() {
        Map<String, Object> parameters = new HashMap<>();
        assertNull(requestHandlerPaths.get("/xxx", parameters));
        for (TestData td : TEST_DATA) {
            RequestHandler h = requestHandlerPaths.get(td.searchUrl,
                    parameters);
            assertNotNull("Could not find " + td, h);
            for (Map.Entry<String, String> e : td.params.entrySet()) {
                assertEquals("Expecting " + e.getKey() + "==" + e.getValue(),
                        e.getValue(), parameters.get(e.getKey()));
            }
        }
    }
}
