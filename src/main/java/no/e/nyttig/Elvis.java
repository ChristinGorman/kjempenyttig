package no.e.nyttig;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Elvis<T> {
    private Class<T> klass;

    public Elvis(Class<T> klass) {
        this.klass = klass;
    }

    public T nullSafe(T instance) {
        return nullSafe(klass, instance);
    }

    public static <T> T nullSafe(Class<T> klass, final Object instance){
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(klass);
            Class clazz = factory.createClass();
            MethodHandler handler = new MethodHandler() {
                @Override
                public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
                    Object returnval = (instance != null) ? overridden.invoke(instance, args) : null;
                    if  (Modifier.isFinal(overridden.getReturnType().getModifiers())) {
                        return returnval;
                    }else {
                        return nullSafe(overridden.getReturnType(), returnval);
                    }
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
