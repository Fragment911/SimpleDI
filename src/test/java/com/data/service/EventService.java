package com.data.service;

import com.data.dao.event.EventDAO;

import javax.inject.Inject;

public class EventService
{
    private final EventDAO dao;

    @Inject
    public EventService(EventDAO dao)
    {
        this.dao = dao;
    }
}
