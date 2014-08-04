package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.predicate.Predicate;

public class BugReportRepository {
    private AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, BugReport> memoryStorage = new HashMap<>();

    public BugReport load(Long id) {
        return memoryStorage.get(id);
    }

    public BugReport save(BugReport bugReport) {
        if (bugReport.getId() == null) {
            bugReport.setId(nextId.incrementAndGet());
        } else {
            BugReport old = memoryStorage.get(bugReport.getId());
            if (old == null) {
                throw new RuntimeException("Not found " + bugReport);
            }
            if (bugReport.getTitle() != null) {
                old.setTitle(bugReport.getTitle());
            }
            if (bugReport.getDescription() != null) {
                old.setDescription(bugReport.getDescription());
            }
            if (bugReport.getResolutionDate() != null) {
                old.setResolutionDate(bugReport.getResolutionDate());
            }
            if (bugReport.getEstimatedResolutionDate() != null) {
                old.setEstimatedResolutionDate(bugReport
                        .getEstimatedResolutionDate());
            }
            if (bugReport.getAssignedTo() != null) {
                old.setAssignedTo(bugReport.getAssignedTo());
            }
            if (bugReport.getDevelopedBy() != null) {
                old.setDevelopedBy(bugReport.getDevelopedBy());
            }
            if (bugReport.getState() != null) {
                old.setState(bugReport.getState());
            }
            if (bugReport.getPriority() != null) {
                old.setPriority(bugReport.getPriority());
            }
            bugReport = old;
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
