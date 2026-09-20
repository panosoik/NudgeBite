package com.panagiotisoikonomou.nudgebite.engine.ema;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public final class RecentEmaAnswers {

    /** Long enough to reach dinner from the walk home, short enough to never reach yesterday evening. */
    public static final Duration LOOKBACK = Duration.ofHours(8);

    private RecentEmaAnswers() {
    }

    @Nullable
    public static DinnerPlan latestDinnerPlan(List<MomentaryAssessmentEvent> assessmentsNewestFirst, LocalDateTime now) {
        LocalDateTime earliest = now.minus(LOOKBACK);
        for (MomentaryAssessmentEvent assessment : assessmentsNewestFirst) {
            boolean withinWindow = !assessment.creationDate.isBefore(earliest) && !assessment.creationDate.isAfter(now);
            if (withinWindow && assessment.dinnerPlanned != null) {
                return assessment.dinnerPlanned;
            }
        }
        return null;
    }
}
