package com;

import com.data.dao.event.EventDAO;
import com.data.dao.event.InMemoryEventDAO;
import com.data.dao.profile.InMemoryProfileDAO;
import com.data.dao.profile.ProfileDAO;
import com.data.service.EventService;
import com.data.service.InjectAmbiguityService;
import com.data.service.NoSuitableConstructorService;
import com.exception.BindingNotFoundException;
import com.exception.ConstructorAmbiguityException;
import com.exception.NoSuitableConstructorException;
import com.ioc.Injector;
import com.ioc.InjectorImpl;
import com.ioc.Provider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest
{
    @Test
    void testExistingBinding()
    {
        Injector injector = new InjectorImpl();
        injector.bind(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertNotNull(daoProvider);
        assertNotNull(daoProvider.getInstance());

        assertSame(InMemoryEventDAO.class, daoProvider.getInstance().getClass());
    }

    @Test
    void testNonExistingBinding()
    {
        Injector injector = new InjectorImpl();
        assertNull(injector.getProvider(EventDAO.class));
    }

    @Test
    void testUniqBinding()
    {
        Injector injector = new InjectorImpl();
        injector.bind(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertTrue(daoProvider.getInstance() != daoProvider.getInstance());
    }

    @Test
    void testSingletonBinding()
    {
        Injector injector = new InjectorImpl();
        injector.bindSingleton(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertTrue(daoProvider.getInstance() == daoProvider.getInstance());
    }

    @Test
    void testInjection()
    {
        Injector injector = new InjectorImpl();
        injector.bindSingleton(EventDAO.class, InMemoryEventDAO.class);
        injector.bindSingleton(EventService.class, EventService.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);
        Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

        EventService service = serviceProvider.getInstance();

        EventDAO expectedDao = daoProvider.getInstance();
        EventDAO injectedDao;

        try
        {
            Field daoField = EventService.class.getDeclaredField("dao");
            daoField.setAccessible(true);
            injectedDao = (EventDAO) daoField.get(service);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        assertTrue(expectedDao == injectedDao);
    }

    @Test
    void testConstructorAmbiguityException()
    {
        assertThrows(ConstructorAmbiguityException.class, () -> {
           Injector injector = new InjectorImpl();
           injector.bind(EventDAO.class, InMemoryEventDAO.class);
           injector.bind(ProfileDAO.class, InMemoryProfileDAO.class);
           injector.bind(InjectAmbiguityService.class, InjectAmbiguityService.class);

           Provider<InjectAmbiguityService> serviceProvider = injector.getProvider(InjectAmbiguityService.class);

            // In case of correct implementation the following statement is unreachable
            assertTrue(serviceProvider != null);
        });
    }

    @Test
    void testNoSuitableConstructorException()
    {
        assertThrows(NoSuitableConstructorException.class, () -> {
            Injector injector = new InjectorImpl();
            injector.bind(NoSuitableConstructorService.class, NoSuitableConstructorService.class);

            Provider<NoSuitableConstructorService> serviceProvider = injector.getProvider(NoSuitableConstructorService.class);

            // In case of a correct implementation the following statement is unreachable
            assertTrue(serviceProvider != null);
        });
    }

    @Test
    void testBindingNotFoundException()
    {
        assertThrows(BindingNotFoundException.class, () -> {
            Injector injector = new InjectorImpl();
            injector.bind(EventService.class, EventService.class);

            Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

            // In case of a correct implementation the following statement is unreachable
            assertTrue(serviceProvider != null);
        });
    }
}
