package com.plexobject.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlexService allows http url with parameters. This class provides lookup
 * methods for matching url with parameters.
 * 
 * @author shahzad bhatti
 *
 * @param <T>
 */
public class RouteResolver<T> {
    static class Node<T> {
        private final String pathFragment;
        private final String parameter;
        private final int level;
        private final boolean wild;
        private Node<T> parent;
        private final Map<String, Node<T>> children = new HashMap<>();
        private T object;

        Node(Node<T> parent, String pathFragment, int level) {
            this.parent = parent;
            this.pathFragment = pathFragment;
            this.level = level;
            if (pathFragment.startsWith("{")) {
                parameter = pathFragment.replaceAll("[{}]", "");
                wild = false;
            } else if (pathFragment.startsWith("*")) {
                parameter = pathFragment.replaceAll("\\*.*", "");
                wild = true;
            } else {
                parameter = null;
                wild = false;
            }
        }

        private boolean isParameterPath() {
            return parameter != null;
        }

        private Node<T> add(String[] fragments, int index, T object) {
            if (fragments.length <= index) {
                this.object = object;
                return this;
            }
            Node<T> child = children.get(fragments[index]);
            if (child == null) {
                child = new Node<T>(this, fragments[index], index);
                children.put(fragments[index], child);
            }
            return child.add(fragments, index + 1, object);
        }

        private Node<T> find(String[] fragments, int index,
                Map<String, Object> parameters) {
            if (wild) {
                return this;
            }
            if (parent == null
                    || isParameterPath()
                    || (index < fragments.length && pathFragment
                            .equals(fragments[index]))) {
                if (isParameterPath() && index < fragments.length) {
                    parameters.put(parameter, fragments[index]);
                }
                if (object != null && fragments.length - 1 == level) {
                    return this;
                }
                for (Node<T> child : children.values()) {
                    Node<T> matched = child.find(fragments, index + 1,
                            parameters);
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
            result = prime * result + pathFragment.hashCode();
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Node<T> other = (Node<T>) obj;
            return pathFragment.equals(other.pathFragment)
                    && level == other.level;
        }

        @Override
        public String toString() {
            return pathFragment + "=> " + object + ", level " + level;
        }

        public void toString(StringBuilder sb) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            String handler = object != null ? object.getClass().getSimpleName()
                    : "<null>";
            sb.append(pathFragment + " => " + handler + ", level " + level
                    + "\n");

            for (Node<T> child : children.values()) {
                child.toString(sb);
            }
        }
    }

    private final Map<String, T> servicesByPath = new ConcurrentHashMap<>();
    private Node<T> root = new Node<T>(null, "/", 0);

    public void put(String path, T object) {
        Objects.requireNonNull(path, "null path");
        Objects.requireNonNull(object, "null object");
        path = path.trim();
        servicesByPath.put(path, object);
        String[] fragments = path.split("/");
        root.add(fragments, 1, object);
    }

    public T get(String path, Map<String, Object> parameters) {
        path = path.trim();
        Node<T> node = getNode(path, parameters);
        return node != null ? node.object : null;
    }

    public boolean remove(String path) {
        Node<T> node = getNode(path, new HashMap<String, Object>());
        if (node != null) {
            servicesByPath.remove(path);
            return node.parent.children.remove(node.pathFragment) != null;
        }
        return false;
    }

    public Collection<T> getObjects() {
        return new ArrayList<>(servicesByPath.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        root.toString(sb);
        return sb.toString();
    }

    private Node<T> getNode(String path, Map<String, Object> parameters) {
        Objects.requireNonNull(path, "null path");

        String[] fragments = path.split("/");
        if (fragments.length == 0) {
            return root;
        }
        if (fragments.length < 2) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        Node<T> parent = root.children.get(fragments[1]);
        if (parent == null) {
            return null;
        }
        return parent.find(fragments, 1, parameters);
    }
}