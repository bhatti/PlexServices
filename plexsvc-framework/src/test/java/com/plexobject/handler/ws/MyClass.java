package com.plexobject.handler.ws;

public class MyClass {
    private Long id;
    private String name;
    private String description;

    MyClass() {

    }

    public MyClass(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MyClass [id=" + id + ", name=" + name + ", description="
                + description + "]";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
