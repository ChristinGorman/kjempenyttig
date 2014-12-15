package no.e.nyttig;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * Groovy has an ?: operator. C# has an ?: operator. I work on java projects and I want one too, God damn it!
 * Inspired by a code base filled with
 *
 * if (a != null && a.getB() != null && a.getB().getC() != null) {
 *     return a.getB().getC().getAddress();
 * }
 *
 * I would like to be able to write:
 * A?:getB()?:getC()?:getAddress();
 *
 * With this class you can't do anything near that nice, but at least you can now write:
 *
 * Elvis.nullSafe(A.class, a).getB().getC().getAddress();
 *
 * Actually I would do a static import (import static no.e.nyttig.Elvis.nullSafe;) so it would look like this:
 * nullSafe(A.class, a).getB().getC().getAddress();
 *
 * WARNING:
 * This only works if all classes (except the last one are non-final and have default constructors)
 * It will also take far more time as it involves making dynamic proxies
 * Oh well.
 * It was fun to write at least
 *
 * @param <T>
 */
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
            if (Modifier.isFinal(klass.getModifiers())) return (T)instance;
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(klass);
            Class clazz = factory.createClass();
            MethodHandler handler = (self, overridden, forwarder, args) -> {
                Object returnval = (instance != null) ? overridden.invoke(instance, args) : null;
                return nullSafe(overridden.getReturnType(), returnval);

            };
            Object proxyInstance = clazz.newInstance();
            ((ProxyObject) proxyInstance).setHandler(handler);
            return (T) proxyInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
