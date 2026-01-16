package com.example.cinema.service.theater;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TheaterStateBroadcaster {
    private final TheaterSyncService theaterSyncService;
    private final SimpMessagingTemplate messagingTemplate;


    public void broadcastState(long scheduleId) {
        var state = theaterSyncService.getState(scheduleId);
        messagingTemplate.convertAndSend("/topic/theaters/" + scheduleId + "/state", state);
    }
}
