package com.plexobject.jaxws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.plexobject.bugger.model.Comment;

@WebService
public interface CommentService {
    @WebMethod
    Comment add(Comment comment);

    List<Comment> getComments();
}
