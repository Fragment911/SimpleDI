package com.data.dao.event;

import com.data.domain.Entity;

import java.util.Collection;

public interface EventDAO
{
    Collection<Entity> getEvents();
}
