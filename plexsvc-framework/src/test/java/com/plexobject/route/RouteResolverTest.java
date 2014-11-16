package com.plexobject.route;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.route.RouteResolver.Node;

public class RouteResolverTest {
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
            RequestHandler h = requestHandlerPaths
                    .get(td.searchUrl, parameters);
            assertNotNull("Could not find " + td, h);
            for (Map.Entry<String, String> e : td.params.entrySet()) {
                assertEquals("Expecting " + e.getKey() + "==" + e.getValue(),
                        e.getValue(), parameters.get(e.getKey()));
            }
        }
    }

    @Test
    public void testSimplePutGet() {
        Map<String, Object> parameters = new HashMap<>();
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put("/path", h);
        assertEquals(h, requestHandlerPaths.get("/path", parameters));
        assertTrue(requestHandlerPaths.getObjects().contains(h));
    }

    @Test
    public void testSimplePutRemove() {
        Map<String, Object> parameters = new HashMap<>();
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put("/path", h);
        assertEquals(h, requestHandlerPaths.get("/path", parameters));
        assertTrue(requestHandlerPaths.remove("/path"));
        assertNull(requestHandlerPaths.get("/path", parameters));
        assertFalse(requestHandlerPaths.remove("/path"));
    }

    @Test
    public void testParameterizedPutGet() {
        Map<String, Object> parameters = new HashMap<>();
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put(
                "/users/{user_id}/projects/{project_id}/reports/{id}", h);
        assertEquals(h, requestHandlerPaths.get(
                "/users/1/projects/2/reports/3", parameters));
        assertTrue(requestHandlerPaths.getObjects().contains(h));
        assertEquals("3", parameters.get("id"));
        assertEquals("2", parameters.get("project_id"));
        assertEquals("1", parameters.get("user_id"));
    }

    @Test
    public void testWildPutGet() {
        Map<String, Object> parameters = new HashMap<>();
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put("/users/*", h);
        assertEquals(h, requestHandlerPaths.get("/users/1/2", parameters));
        assertNull(requestHandlerPaths.get("/users2/1/2", parameters));
        assertNotNull(requestHandlerPaths.get("/", parameters));
        assertTrue(requestHandlerPaths.getObjects().contains(h));
    }

    @Test
    public void testParameterizedPutRemove() {
        Map<String, Object> parameters = new HashMap<>();
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put(
                "/users/{user_id}/projects/{project_id}/reports/{id}", h);
        assertEquals(h, requestHandlerPaths.get(
                "/users/1/projects/2/reports/3", parameters));
        assertTrue(requestHandlerPaths
                .remove("/users/{user_id}/projects/{project_id}/reports/{id}"));
        assertNull(requestHandlerPaths.get("/users/1/projects/2/reports/3",
                parameters));
        assertFalse(requestHandlerPaths
                .remove("/users/{user_id}/projects/{project_id}/reports/{id}"));
    }

    @Test
    public void testToString() {
        final TestHandler h = new TestHandler("name");
        requestHandlerPaths.put("/path", h);
        assertTrue(requestHandlerPaths.toString(), requestHandlerPaths
                .toString().contains("path"));
    }

    @Test
    public void testNodeHash() {
        Node<Object> node1 = new Node<Object>(null, "path1", 1);
        Node<Object> node2 = new Node<Object>(null, "path2", 2);
        Node<Object> node2a = new Node<Object>(null, "path2", 2);
        assertFalse(node1.hashCode() == node2.hashCode());
        assertEquals(node2.hashCode(), node2a.hashCode());
    }

    @Test
    public void testNodeEquals() {
        Node<Object> node1 = new Node<Object>(null, "path1", 1);
        Node<Object> node2 = new Node<Object>(null, "path2", 2);
        Node<Object> node2a = new Node<Object>(null, "path2", 2);
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals(3));
        assertEquals(node2, node2a);
        assertEquals(node2, node2);
    }

    @Test
    public void testNodeToString() {
        Node<Object> node1 = new Node<Object>(null, "path1", 1);
        assertTrue(node1.toString(), node1.toString().contains("path1"));
    }
}
