package com.ioc;

import com.exception.BindingNotFoundException;
import com.exception.ConstructorAmbiguityException;
import com.exception.NoSuitableConstructorException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InjectorImpl implements Injector {
    private Map<Class<?>, Provider<?>> providerMap = new HashMap<>();

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        Provider<T> provider = (Provider<T>)providerMap.get(type);
        if (provider != null) provider.getInstance(); // выбросит исключение, если конструктор не удовлетворяет условиям
        return provider;
    }

    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
        Provider<T> provider = () -> {
            try {
                Constructor<?> constructor = getConstructor(impl); // получение нужного конструктора
                Object[] binds = getBinds(constructor); // получение bindings для аргументов конструктора
                return (T) constructor.newInstance(binds);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                ex.printStackTrace();
                return null;
            }
        };

        providerMap.put(base, provider);
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl) {
        Provider<T> provider = new Provider<T>() {
            private T instance = null;

            @Override
            public synchronized T getInstance() {
                if (instance == null) {
                    try {
                        Constructor<?> constructor = getConstructor(impl); // получение нужного конструктора
                        Object[] binds = getBinds(constructor); // получение bindings для аргументов конструктора
                        instance = (T) constructor.newInstance(binds);
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                        ex.printStackTrace();
                    }
                }
                return instance;
            }
        };

        providerMap.put(base, provider);
    }

    Object[] getBinds(Constructor<?> constructor) {
        Class[] parameterTypes = constructor.getParameterTypes();
        Object[] binds = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Provider<?> provider = getProvider(parameterTypes[i]); // получаем провайдеры для аргументов конструктора
            if (provider == null) {
                throw new BindingNotFoundException();
            } else {
                binds[i] = provider.getInstance();
            }
        }
        return binds;
    }

    private Constructor<?> getConstructor(Class<?> class1) {
        List<Constructor<?>> constructorList = Arrays.asList(class1.getConstructors());
        Constructor<?> constructor = getInjectConstructor(constructorList); // ищем конструктор с Inject
        if (constructor == null) { // если не нашли, ищем дефолтный
            constructor = getDefaultConstructor(constructorList);
        }
        return constructor;
    }

    private Constructor<?> getInjectConstructor(List<Constructor<?>> constructorList) throws ConstructorAmbiguityException {
        List<Constructor<?>> injectConstructorList = constructorList.stream().filter(this::checkInjectAnnotation).collect(Collectors.toList());
        if (injectConstructorList.size() > 1) throw new ConstructorAmbiguityException();
        if (injectConstructorList.size() == 1) return injectConstructorList.get(0);
        else return null;
    }

    private boolean checkInjectAnnotation(Constructor<?> constructor) {
        return Arrays.stream(constructor.getDeclaredAnnotations()).anyMatch(x -> x.annotationType().equals(Inject.class));
    }

    private Constructor<?> getDefaultConstructor(List<Constructor<?>> constructorList) throws NoSuitableConstructorException {
        return constructorList.stream().filter(x -> x.getParameterTypes().length == 0).findAny().orElseThrow(NoSuitableConstructorException::new);
    }
}
