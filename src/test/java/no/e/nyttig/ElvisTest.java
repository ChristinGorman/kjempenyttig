package no.e.nyttig;

import org.junit.Test;
import org.junit.Ignore;

import static no.e.nyttig.Elvis.nullSafe;
import static org.junit.Assert.*;

public class ElvisTest {


    public static class TestClass {
        Nested nested = new Nested();
        public Integer answer() {
            return 42;
        }
        public String withArguments(int one, String two) {
            return String.format("Hello %s-%d", two, one);
        }

        public Nested nestedClass() {
            return nested;
        }
    }

    public static class Nested {
        static final String HELLO = "Hello";
        public String hello() {
            return HELLO;
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

    @Test
    public void should_not_try_to_proxy_final_classes() {
        String hello = "Hello";
        String s = nullSafe(String.class, hello);
        assertSame(hello, s);
    }

    @Test
    public void should_return_final_return_values_without_proxy() {
        TestClass instance = new TestClass();
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertSame(Nested.HELLO, nullSafe.nestedClass().hello());
    }

    @Test
    public void should_return_proxy_for_non_finals() {
        TestClass instance = new TestClass();
        TestClass nullSafe = nullSafe(TestClass.class, instance);
        assertNotSame(instance.nested, nullSafe.nestedClass());
    }


    /**
     * Simple test to show that it takes about 15 times longer to execute this nonsense than the traditional if-else check
     */
    @Test
    @Ignore
    public void speed() {
        long stamp = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            TestClass test = new TestClass();
            if (test != null && test.nestedClass() != null) {
                System.out.print(test.nestedClass().hello().charAt(0));
            }
        }

        System.out.print("\nOld: ");
        System.out.println((System.currentTimeMillis() - stamp));

        stamp = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            System.out.print(nullSafe(TestClass.class, new TestClass()).nestedClass().hello().charAt(0));
        }

        System.out.print("\nProxified: ");
        System.out.println((System.currentTimeMillis() - stamp));
    }
}
