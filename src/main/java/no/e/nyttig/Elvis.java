package no.e.nyttig;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.Method;

public class Elvis<T> {
    private Class<T> klass;

    public Elvis(Class<T> klass) {
        this.klass = klass;
    }

    public static <T> T nullSafe(Class<T> klass, final T instance){
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(klass);
            Class clazz = factory.createClass();
            MethodHandler handler = new MethodHandler() {
                @Override
                public Object invoke(Object self, Method overridden, Method forwarder,
                                     Object[] args) throws Throwable {
                    if (instance == null) return null;
                    return overridden.invoke(instance, args);
                }
            };
            Object proxyInstance = clazz.newInstance();
            ((ProxyObject) proxyInstance).setHandler(handler);
            return (T) proxyInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
