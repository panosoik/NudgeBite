package com.panagiotisoikonomou.nudgebite.engine;

import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;

import java.util.List;

public final class InterventionHistory {

    public final List<EatingOccasionEvent> todaysEvents;
    public final List<UserResponse> recentResponsesOldestFirst;

    public InterventionHistory(List<EatingOccasionEvent> todaysEvents, List<UserResponse> recentResponsesOldestFirst) {
        this.todaysEvents = todaysEvents;
        this.recentResponsesOldestFirst = recentResponsesOldestFirst;
    }
}
