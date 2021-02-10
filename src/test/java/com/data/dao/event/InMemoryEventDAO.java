package com.data.dao.event;

import com.data.domain.Entity;

import java.util.Collection;
import java.util.Collections;

public class InMemoryEventDAO implements EventDAO
{
    @Override
    public Collection<Entity> getEvents()
    {
        return Collections.emptyList();
    }
}
