package com.plexobject.handler.ws;

import javax.jws.WebService;

@WebService
public interface MyService {
    MyClass getById(Long id);

    MyClass getByParam(Long id);

    MyClass getByMyClass(MyClass c);
}