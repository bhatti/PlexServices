package com.plexobject.service.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.RequestHandler;

public class RequestHandlerPaths {
    private static class Node {
        private final String pathFragment;
        private final String parameter;
        private final int level;
        private Node parent;
        private final Map<String, Node> children = new HashMap<>();
        private RequestHandler requestHandler;

        private Node(Node parent, String pathFragment, int level) {
            this.parent = parent;
            this.pathFragment = pathFragment;
            this.level = level;
            if (pathFragment.startsWith("{")) {
                parameter = pathFragment.replaceAll("[{}]", "");
            } else {
                parameter = null;
            }
        }

        private boolean isParameterPath() {
            return parameter != null;
        }

        private Node add(String[] fragments, int index,
                RequestHandler requestHandler) {
            if (fragments.length <= index) {
                this.requestHandler = requestHandler;
                return this;
            }
            Node child = children.get(fragments[index]);
            if (child == null) {
                child = new Node(this, fragments[index], index);
                children.put(fragments[index], child);
            }
            return child.add(fragments, index + 1, requestHandler);
        }

        private Node find(String[] fragments, int index,
                Map<String, Object> parameters) {
            if (parent == null
                    || isParameterPath()
                    || (index < fragments.length && pathFragment
                            .equals(fragments[index]))) {
                if (isParameterPath() && index < fragments.length) {
                    parameters.put(parameter, fragments[index]);
                }
                if (requestHandler != null && fragments.length - 1 == level) {
                    return this;
                }
                for (Node child : children.values()) {
                    Node matched = child.find(fragments, index + 1, parameters);
                    if (matched != null) {
                        return matched;
                    }
                }
            }
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((pathFragment == null) ? 0 : pathFragment.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Node other = (Node) obj;
            if (pathFragment == null) {
                if (other.pathFragment != null)
                    return false;
            } else if (!pathFragment.equals(other.pathFragment))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return pathFragment + "=> " + requestHandler + ", level " + level;
        }

        public void toString(StringBuilder sb) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            String handler = requestHandler != null ? requestHandler.getClass()
                    .getSimpleName() : "<null>";
            handler = requestHandler != null ? requestHandler.toString()
                    : "<null>";
            sb.append(pathFragment + " => " + handler + ", level " + level
                    + "\n");

            for (Node child : children.values()) {
                child.toString(sb);
            }
        }
    }

    private final Map<String, RequestHandler> servicesByPath = new ConcurrentHashMap<>();
    private Node root = new Node(null, "/", 0);

    public void addHandler(String path, RequestHandler requestHandler) {
        Objects.requireNonNull(path, "null path");
        Objects.requireNonNull(requestHandler, "null requestHandler");
        servicesByPath.put(path, requestHandler);
        String[] fragments = path.split("/");
        root.add(fragments, 1, requestHandler);
    }

    public RequestHandler getHandler(String path, Map<String, Object> parameters) {
        Node node = getNode(path, parameters);
        return node != null ? node.requestHandler : null;
    }

    public void removeHandler(String path) {
        Node node = getNode(path, new HashMap<String, Object>());
        if (node != null) {
            node.parent.children.remove(node);
        }
    }

    public Collection<RequestHandler> getRequestHandlers() {
        return new ArrayList<>(servicesByPath.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        root.toString(sb);
        return sb.toString();
    }

    private Node getNode(String path, Map<String, Object> parameters) {
        Objects.requireNonNull(path, "null path");

        String[] fragments = path.split("/");
        if (fragments.length == 0) {
            return root;
        }
        Node parent = root.children.get(fragments[1]);
        if (parent == null) {
            return null;
        }
        return parent.find(fragments, 1, parameters);
    }

}
