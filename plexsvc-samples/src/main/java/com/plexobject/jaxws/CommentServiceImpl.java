package com.plexobject.jaxws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Comment;
import com.plexobject.predicate.Predicate;

@WebService
@Path("/comments")
public class CommentServiceImpl implements CommentService {
    @Override
    @WebMethod
    public Comment add(Comment comment) {
        BugReport report = SharedRepository.bugReportRepository.load(comment
                .getBugId());
        report.getComments().add(comment);
        SharedRepository.bugReportRepository.save(report);
        return comment;
    }

    @Override
    public List<Comment> getComments() {
        final List<Comment> comments = new ArrayList<>();
        Collection<BugReport> reports = SharedRepository.bugReportRepository
                .getAll(new Predicate<BugReport>() {
                    @Override
                    public boolean accept(BugReport report) {
                        return true;
                    }
                });
        for (BugReport r : reports) {
            comments.addAll(r.getComments());
        }
        return comments;
    }
}
