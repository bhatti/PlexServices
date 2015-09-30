package com.plexobject.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.school.Address;
import com.plexobject.school.Course;
import com.plexobject.school.Student;

public class ReflectUtilsTest {
    public static class TestService {
        public List<Object> printStudent(Student s) {
            return Arrays.asList(s, new Course("1001", "Python"));
        }

        public void nop() {
        }

        public Course getCourse(String id) {
            return new Course(id, "Ruby");
        }

        public Student getStudent(Long id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentShort(short id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentInt(int id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentLong(long id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentByte(byte id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentChar(char id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentFloat(float id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public Student getStudentDouble(double id) {
            return new Student(String.valueOf(id), "Jack");
        }

        public List<Object> printCourse(Course c) {
            return Arrays.asList(c, new Student("1002", "Bill"));
        }

        public Collection<Course> printCourses(Collection<Course> c) {
            return c;
        }

        public Collection<Student> printStudents(Map<String, Student> s) {
            return s.values();
        }
    }

    private static final JsonObjectCodec CODEC = new JsonObjectCodec();
    private TestService service = new TestService();

    @Before
    public void setup() {
    }

    @Test
    public void testGetAnnotatedClasses() {
        Collection<Class<?>> classes = ReflectUtils.getAnnotatedClasses(
                WebService.class, "com.plexobject.handler.ws", " ");
        assertTrue(classes.size() > 0);
    }

    @Test
    public void testJsonSimple() throws Exception {
        Student student1 = buildStudent();
        String json1 = CODEC.encode(student1);
        Student student2 = CODEC.decode(json1, Student.class, null);
        String json2 = CODEC.encode(student2);
        assertEquals(json1, json2);
        assertEquals(student1, student2);
    }

    @Test
    public void testCodecJsonList() throws Exception {
        Collection<Course> courses1 = buildCourses();
        String json1 = CODEC.encode(courses1);
        Collection<Course> courses2 = CODEC.decode(json1,
                new TypeReference<List<Course>>() {
                });
        String json2 = CODEC.encode(courses2);
        assertEquals(json1, json2);
        assertEquals(courses1, courses2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodeJsonList() throws Exception {
        Collection<Course> courses1 = buildCourses();
        String json1 = CODEC.encode(courses1);
        Method m = TestService.class
                .getMethod("printCourses", Collection.class);
        Class<?> klass = m.getParameterTypes()[0];
        Type pKlass = m.getGenericParameterTypes()[0];
        Collection<Course> courses2 = (Collection<Course>) ReflectUtils.decode(
                json1, klass, pKlass, CODEC);
        String json2 = CODEC.encode(courses2);
        assertEquals(json1, json2);
        assertEquals(courses1, courses2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodeMethodParams() throws Exception {
        Student s = buildStudent();
        Method m = TestService.class.getMethod("printStudent", Student.class);
        String payload = CODEC.encode(s);

        Object[] args = ReflectUtils.decode(m, new HashMap<String, Object>(),
                null, payload, CODEC);
        List<Object> result = (List<Object>) m.invoke(service, args);
        assertEquals(s, result.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodeMethodParamsWithCollection() throws Exception {
        Collection<Course> c = buildCourses();
        Method m = TestService.class
                .getMethod("printCourses", Collection.class);
        String payload = CODEC.encode(c);
        Object[] args = ReflectUtils.decode(m, new HashMap<String, Object>(),
                null, payload, CODEC);
        System.out.println(" args " + Arrays.toString(args));
        List<Object> result = (List<Object>) m.invoke(service, args);
        assertEquals(c, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodeMethodParamsWithMap() throws Exception {
        Map<String, Student> students = buildStudentsMap();
        Method m = TestService.class.getMethod("printStudents", Map.class);
        String payload = CODEC.encode(students);
        Object[] args = ReflectUtils.decode(m, new HashMap<String, Object>(),
                null, payload, CODEC);
        Collection<Object> result = (Collection<Object>) m
                .invoke(service, args);
        assertEquals(students.size(), result.size());
        for (Student s : students.values()) {
            assertTrue(result.contains(s));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodeMethodWithVoid() throws Exception {
        Method m = TestService.class.getMethod("nop");
        String payload = null;
        Object arg = ReflectUtils.decode(payload, Void.class, null, CODEC);
        assertNull(arg);
        Object result = (Collection<Object>) m.invoke(service, new Object[0]);
        assertNull(result);
    }

    @Test
    public void testDecodeMethodWithString() throws Exception {
        Method m = TestService.class.getMethod("getCourse", String.class);
        String payload = "100";
        Course c1 = new Course(payload, "Ruby");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(payload, arg);
        Course c2 = (Course) m.invoke(service, new Object[] { arg });
        assertEquals(c1, c2);
    }

    @Test
    public void testDecodeMethodWithLong() throws Exception {
        Method m = TestService.class.getMethod("getStudent", Long.class);
        String payload = "100";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Long.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    @Test
    public void testDecodeMethodWithShort() throws Exception {
        Method m = TestService.class.getMethod("getStudentShort", Short.TYPE);
        String payload = "100";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Short.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    @Test
    public void testDecodeMethodWithInt() throws Exception {
        Method m = TestService.class.getMethod("getStudentInt", Integer.TYPE);
        String payload = "100";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Integer.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    public Student getStudentLong(long id) {
        return new Student(String.valueOf(id), "Jack");
    }

    @Test
    public void testDecodeMethodWithlong() throws Exception {
        Method m = TestService.class.getMethod("getStudentLong", Long.TYPE);
        String payload = "100";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Long.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    @Test
    public void testDecodeMethodWithByte() throws Exception {
        Method m = TestService.class.getMethod("getStudentByte", Byte.TYPE);
        String payload = "100";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Byte.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    public Student getStudentChar(char id) {
        return new Student(String.valueOf(id), "Jack");
    }

    @Test
    public void testDecodeMethodWithChar() throws Exception {
        Method m = TestService.class
                .getMethod("getStudentChar", Character.TYPE);
        String payload = "1";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(new Character(payload.charAt(0)), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    @Test
    public void testDecodeMethodWithFloat() throws Exception {
        Method m = TestService.class.getMethod("getStudentFloat", Float.TYPE);
        String payload = "100.0";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Float.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    @Test
    public void testDecodeMethodWithDouble() throws Exception {
        Method m = TestService.class.getMethod("getStudentDouble", Double.TYPE);
        String payload = "100.0";
        Student s1 = new Student(payload, "Jack");
        Object arg = ReflectUtils.decode(payload, m.getParameterTypes()[0],
                null, CODEC);
        assertEquals(Double.valueOf(payload), arg);
        Student s2 = (Student) m.invoke(service, new Object[] { arg });
        assertEquals(s1, s2);
    }

    private static List<Course> buildCourses() throws Exception {
        return Arrays.asList(buildCourse(), buildCourse());
    }

    private static Map<String, Student> buildStudentsMap() throws Exception {
        Map<String, Student> students = new LinkedHashMap<>();
        Student s1 = buildStudent();
        Student s2 = buildStudent();
        students.put(s1.getId(), s1);
        students.put(s2.getId(), s2);
        return students;
    }

    private static Student buildStudent() throws Exception {
        Thread.sleep(1);

        long time = System.currentTimeMillis() / 1000000;
        Student s = new Student(String.valueOf(time), time % 2 == 0 ? "Ken"
                : "Chris");
        s.getAddresses().add(new Address("100 main", "Seattle", "98101"));
        s.getCourseIds().add(String.valueOf(time + 1));
        s.getCourseIds().add(String.valueOf(time + 2));
        return s;
    }

    private static Course buildCourse() throws Exception {
        Thread.sleep(1);

        long time = System.currentTimeMillis() / 1000000;
        Course c = new Course(String.valueOf(time), time % 2 == 0 ? "Java"
                : "C++");
        c.getStudentIds().add(String.valueOf(time + 1));
        c.getStudentIds().add(String.valueOf(time + 2));
        c.getAddresses().add(new Address("100 main", "Seattle", "98101"));

        return c;
    }
}
