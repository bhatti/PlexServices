package com.plexobject.handler.ws.params;

import javax.jws.WebService;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;

@WebService
@Path("/myservice")
public class MyServiceImpl implements MyService {
    @Override
    public MyClass getById(Long id) {
        return new MyClass(id, "getById", "my description for getById");
    }

    @Override
    public MyClass getByParam(@FormParam("id") Long id) {
        return new MyClass(id, "getByParam", "my description for getByParam");
    }

    @Override
    public MyClass getByMyClass(MyClass c) {
        return c;
    }
}