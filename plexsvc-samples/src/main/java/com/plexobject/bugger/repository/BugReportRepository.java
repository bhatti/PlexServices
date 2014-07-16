package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.predicate.Predicate;

public class BugReportRepository {
    private AtomicLong nextId = new AtomicLong();
    private final Map<Long, BugReport> memoryStorage = new HashMap<>();

    public BugReport load(Long id) {
        return memoryStorage.get(id);
    }

    public BugReport save(BugReport bugReport) {
        if (bugReport.getId() == null) {
            bugReport.setId(nextId.incrementAndGet());
        }
        memoryStorage.put(bugReport.getId(), bugReport);
        return bugReport;
    }

    public void delete(Long id) {
        memoryStorage.remove(id);
    }

    public List<BugReport> getAll(Predicate<BugReport> predicate) {
        List<BugReport> matched = new ArrayList<>();
        for (BugReport r : memoryStorage.values()) {
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
