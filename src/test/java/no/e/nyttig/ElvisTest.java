package no.e.nyttig;

import org.junit.Test;

import static no.e.nyttig.Elvis.nullSafe;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ElvisTest {

    public static class TestClass {
        public Integer answer() {
            return 42;
        }
        public String withArguments(int one, String two) {
            return String.format("Hello %s-%d", two, one);
        }

        public Nested nestedClass() {
            return new Nested();
        }
    }

    public static class Nested {
        public String hello() {
            return "Hello";
        }
    }

    @Test
    public void should_return_null_instance() {
        TestClass instance = null;
        assertNull(new Elvis<>(TestClass.class).nullSafe(instance).answer());
    }

    @Test
    public void should_return_real_value_instance() {
        TestClass instance = new TestClass();
        assertEquals(42, new Elvis<>(TestClass.class).nullSafe(instance).answer().intValue());
    }

    @Test
    public void should_return_null_static() {
        TestClass instance = null;
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertNull(nullSafe.answer());
    }

    @Test
    public void should_return_real_value_static() {
        TestClass instance = new TestClass();
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertEquals(42, nullSafe.answer().intValue());
    }

    @Test
    public void arguments_are_taken_into_account() {
        TestClass instance = new TestClass();
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertEquals("Hello you-2", nullSafe.withArguments(2,"you"));
    }

    @Test
    public void returns_null_with_arguments() {
        TestClass instance = null;
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertNull(nullSafe.withArguments(2, "you"));
    }

    @Test
    public void should_return_null_on_nested_values() {
        TestClass instance = null;
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertNull(nullSafe.nestedClass().hello());
    }

    @Test
    public void should_return_real_value_on_nested_values() {
        TestClass instance = new TestClass();
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertEquals("Hello", nullSafe.nestedClass().hello());
    }

}
