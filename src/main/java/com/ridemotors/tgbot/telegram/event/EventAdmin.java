package com.ridemotors.tgbot.telegram.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventAdmin extends Event {

    private final Logger log = LoggerFactory.getLogger(EventAdmin.class);

}
