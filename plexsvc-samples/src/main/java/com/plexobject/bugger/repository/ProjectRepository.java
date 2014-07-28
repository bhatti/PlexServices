package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.Project;
import com.plexobject.predicate.Predicate;

public class ProjectRepository {
    private AtomicLong nextId = new AtomicLong();
    private final Map<Long, Project> memoryStorage = new HashMap<>();

    public Project load(Long id) {
        return memoryStorage.get(id);
    }

    public Project save(Project project) {
        if (project.getId() == null) {
            project.setId(nextId.incrementAndGet());
        }
        memoryStorage.put(project.getId(), project);
        return project;
    }

    public void delete(Long id) {
        memoryStorage.remove(id);
    }

    public List<Project> getAll() {
        return new ArrayList<>(memoryStorage.values());
    }

    public List<Project> getAll(Predicate<Project> predicate) {
        List<Project> matched = new ArrayList<>();
        for (Project p : memoryStorage.values()) {
            if (predicate == null || predicate.accept(p)) {
                matched.add(p);
            }
        }
        return matched;
    }

    public int count() {
        return memoryStorage.size();
    }
}
