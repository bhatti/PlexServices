package com.plexobject.validation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RequiredFieldValidatorTest {
    @RequiredFields({
            @Field(name = "username", minLength = 6, maxLength = 12),
            @Field(name = "password", minLength = 8, maxLength = 20),
            @Field(name = "email", minLength = 6, maxLength = 100, regex = ".*@.*"),
            @Field(name = "zipcode", minLength = 5, maxLength = 5, regex = "^\\d{5}$"), })
    private static class WithAnnotations {
    }

    @RequiredFields({})
    private static class WithEmptyAnnotations {
    }

    private static class WithoutAnnotations {
    }

    public static class Properties {
        private String username;
        private String password;
        private String email;
        private int zipcode;

        public Properties() {
        }

        public Properties(String username, String password, String email,
                int zipcode) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.zipcode = zipcode;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getZipcode() {
            return zipcode;
        }

        public void setZipcode(int zipcode) {
            this.zipcode = zipcode;
        }
    }

    private final RequiredFieldValidator instance = new RequiredFieldValidator();

    @Test
    public void testNullObject() {
        instance.validate(null, new HashMap<String, Object>());
    }

    @Test
    public void testMapWithoutAnnotations() {
        instance.validate(new WithoutAnnotations(),
                new HashMap<String, Object>());
    }

    @Test
    public void testMapWithEmptyAnnotations() {
        instance.validate(new WithEmptyAnnotations(),
                new HashMap<String, Object>());
    }

    @Test(expected = ValidationException.class)
    public void testMapWithAnnotationsAndNullFields() {
        instance.validate(new WithAnnotations(), null);
    }

    @Test(expected = ValidationException.class)
    public void testMapWithAnnotationsAndEmptyMap() {
        instance.validate(new WithAnnotations(), new HashMap<String, Object>());
    }

    @Test(expected = ValidationException.class)
    public void testMapNullUsername() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("password", "password");
        fields.put("email", "bhatti@plexobject.com");
        fields.put("zipcode", 98059);
        instance.validate(new WithAnnotations(), fields);
    }

    @Test(expected = ValidationException.class)
    public void testMapEmptyUsername() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("username", "");
        fields.put("password", "password");
        fields.put("email", "bhatti@plexobject.com");
        fields.put("zipcode", 98059);
        instance.validate(new WithAnnotations(), fields);
    }

    @Test(expected = ValidationException.class)
    public void testMapLongUsername() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("username",
                "sbhattisbhattisbhattisbhattisbhattisbhattisbhattisbhatti");
        fields.put("password", "password");
        fields.put("email", "bhatti@plexobject.com");
        fields.put("zipcode", 98059);
        instance.validate(new WithAnnotations(), fields);
    }

    @Test(expected = ValidationException.class)
    public void testMapBadEMail() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("username", "sbhatti");
        fields.put("password", "password");
        fields.put("email", "bhatti");
        fields.put("zipcode", 98059);
        instance.validate(new WithAnnotations(), fields);
    }

    @Test(expected = ValidationException.class)
    public void testMapBadZipcode() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("username", "sbhatti");
        fields.put("password", "password");
        fields.put("email", "bhatti@plexobject.com");
        fields.put("zipcode", "abc");
        instance.validate(new WithAnnotations(), fields);
    }

    @Test
    public void testValidMap() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("username", "sbhatti");
        fields.put("password", "password");
        fields.put("email", "bhatti@plexobject.com");
        fields.put("zipcode", 98059);
        instance.validate(new WithAnnotations(), fields);
    }

    @Test
    public void testObjectWithoutAnnotations() {
        instance.validate(new WithoutAnnotations(), new Properties());
    }

    @Test
    public void testObjectWithEmptyAnnotations() {
        instance.validate(new WithEmptyAnnotations(), new Properties());
    }

    @Test(expected = ValidationException.class)
    public void testObjectWithAnnotationsAndEmptyMap() {
        instance.validate(new WithAnnotations(), new Properties());
    }

    @Test(expected = ValidationException.class)
    public void testObjectWithNullUsername() {
        Properties properties = new Properties(null, "password",
                "bhatti@plexobject", 98059);
        instance.validate(new WithAnnotations(), properties);
    }

    @Test(expected = ValidationException.class)
    public void testObjectWithEmptyUsername() {
        Properties properties = new Properties("", "password",
                "bhatti@plexobject", 98059);
        instance.validate(new WithAnnotations(), properties);
    }

    @Test(expected = ValidationException.class)
    public void testLongUsername() {
        Properties properties = new Properties(
                "sbhattisbhattisbhattisbhattisbhattisbhattisbhattisbhatti",
                "password", "bhatti@plexobject", 98059);
        instance.validate(new WithAnnotations(), properties);
    }

    @Test(expected = ValidationException.class)
    public void testBadEMail() {
        Properties properties = new Properties("sbhatti", "password", "bhatti",
                98059);
        instance.validate(new WithAnnotations(), properties);
    }

    @Test(expected = ValidationException.class)
    public void testBadZipcode() {
        Properties properties = new Properties("sbhatti", "password",
                "bhatti@plexobject.com", 980);
        instance.validate(new WithAnnotations(), properties);
    }

    @Test
    public void testValidObject() {
        Properties properties = new Properties("sbhatti", "password",
                "bhatti@plexobject.com", 98059);
        instance.validate(new WithAnnotations(), properties);
    }
}
