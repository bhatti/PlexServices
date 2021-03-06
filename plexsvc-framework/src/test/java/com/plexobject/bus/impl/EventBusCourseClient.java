package com.plexobject.bus.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import com.plexobject.bus.EventBus;
import com.plexobject.domain.Promise;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.EventBusRequest;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.school.Course;
import com.plexobject.school.Student;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class EventBusCourseClient {
    private final EventBus eventBus;

    public EventBusCourseClient(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Future<Course> save(Course course) throws Exception {
        final Promise<Course> promise = new Promise<>();
        String replyChannel = EventBusCourseService.COURSES_SAVE_REPLY
                + System.nanoTime();
        eventBus.subscribe(replyChannel, new RequestHandler() {
            @Override
            public void handle(Request request) {
                Course saved = request.getContentsAs();
                promise.complete(saved);
            }
        }, null);
        eventBus.publish(EventBusCourseService.COURSES_SAVE,
                request(replyChannel, course));
        return promise;
    }

    public Future<List<Course>> enroll(Collection<Student> students)
            throws Exception {
        String replyChannel = EventBusCourseService.COURSES_ENROLL_REPLY
                + System.nanoTime();

        final Promise<List<Course>> promise = new Promise<>();
        eventBus.subscribe(replyChannel, new RequestHandler() {
            @Override
            public void handle(Request request) {
                List<Course> result = request.getContentsAs();
                promise.complete(result);
            }
        }, null);
        eventBus.publish(EventBusCourseService.COURSES_ENROLL,
                request(replyChannel, students));
        return promise;
    }

    public Future<List<Course>> getCoursesForStudentId(String studentId)
            throws Exception {
        String replyChannel = EventBusCourseService.GET_COURSES_FOR_STUDENT_ID_REPLY
                + System.nanoTime();

        final Promise<List<Course>> promise = new Promise<>();
        eventBus.subscribe(replyChannel, new RequestHandler() {
            @Override
            public void handle(Request request) {
                List<Course> result = request.getContentsAs();
                promise.complete(result);
            }
        }, null);
        eventBus.publish(EventBusCourseService.GET_COURSES_FOR_STUDENT_ID,
                request(replyChannel, studentId));
        return promise;

    }

    public Future<Course> get(String courseId) throws Exception {
        String replyChannel = EventBusCourseService.GET_COURSE_REPLY
                + System.nanoTime();

        final Promise<Course> promise = new Promise<>();
        eventBus.subscribe(replyChannel, new RequestHandler() {
            @Override
            public void handle(Request request) {
                Course saved = request.getContentsAs();
                promise.complete(saved);
            }
        }, null);
        eventBus.publish(EventBusCourseService.GET_COURSE,
                request(replyChannel, courseId));
        return promise;
    }

    public Future<String> error() throws Exception {
        String replyChannel = EventBusCourseService.COURSE_ERROR_REPLY
                + System.nanoTime();

        final Promise<String> promise = new Promise<>();
        eventBus.subscribe(replyChannel, new RequestHandler() {
            @Override
            public void handle(Request request) {
                if (request.getContentsAs() instanceof String) {
                    String result = request.getContentsAs();
                    promise.complete(result);
                } else if (request.getContentsAs() instanceof Exception) {
                    Exception result = request.getContentsAs();
                    promise.completeExceptionally(result);
                } else {
                    promise.completeExceptionally(new IllegalArgumentException(
                            "Unexpected type " + request.getContentsAs()));
                }
            }
        }, null);
        eventBus.publish(EventBusCourseService.COURSE_ERROR,
                request(replyChannel, null));
        return promise;
    }

    Request request(String replyChannel, Object payload) {
        AbstractResponseDispatcher dispatcher = new EventBusResponseDispatcher(
                eventBus, replyChannel);
        return EventBusRequest.builder().setProtocol(Protocol.EVENT_BUS)
                .setCodecType(CodecType.SERVICE_SPECIFIC).setMethod(RequestMethod.MESSAGE)
                .setResponseDispatcher(dispatcher)
                .setReplyEndpoint(replyChannel).setContents(payload).build();
    }
}
