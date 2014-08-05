package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.Comment;
import com.plexobject.predicate.Predicate;

public class CommentRepository {
    private AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, Comment> memoryStorage = new ConcurrentHashMap<>();

    public Comment load(Long id) {
        return memoryStorage.get(id);
    }

    public Comment save(Comment Comment) {
        if (Comment.getId() == null) {
            Comment.setId(nextId.incrementAndGet());
        }
        memoryStorage.put(Comment.getId(), Comment);
        return Comment;
    }

    public void delete(Long id) {
        memoryStorage.remove(id);
    }

    public List<Comment> getAll(Predicate<Comment> predicate) {
        List<Comment> matched = new ArrayList<>();
        for (Comment r : memoryStorage.values()) {
            if (predicate == null || predicate.accept(r)) {
                matched.add(r);
            }
        }
        return matched;
    }

    public int count() {
        return memoryStorage.size();
    }

}
