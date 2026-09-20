package com.panagiotisoikonomou.nudgebite.engine;

import java.time.LocalDateTime;

public final class SignalSnapshot {

    public final boolean deliveryAppForeground;
    public final LocationZone currentZone;
    public final boolean recentlyLeftWorkplace;
    public final LocalDateTime now;
    public final MealWindow currentMealWindow;
    public final boolean hasMissingFoodCategory;
    public final boolean weeklyReviewAlreadyDelivered;
    public final boolean deliveryAppForegroundYesterday;
    public final boolean hasFewerDeliveriesGoal;
    public final boolean hasOrganizeMealsGoal;
    public final boolean hasBetterChoicesOutGoal;
    public final boolean eatingOutGapAlreadyDeliveredToday;
    public final boolean hasQuickBalancedGoal;
    public final boolean dinnerLoggedToday;
    public final boolean dinnerPlanAnsweredNoRecently;
    public final boolean dinnerNoPlanAlreadyDeliveredToday;

    private SignalSnapshot(Builder builder) {
        this.deliveryAppForeground = builder.deliveryAppForeground;
        this.currentZone = builder.currentZone;
        this.recentlyLeftWorkplace = builder.recentlyLeftWorkplace;
        this.now = builder.now;
        this.currentMealWindow = builder.currentMealWindow;
        this.hasMissingFoodCategory = builder.hasMissingFoodCategory;
        this.weeklyReviewAlreadyDelivered = builder.weeklyReviewAlreadyDelivered;
        this.deliveryAppForegroundYesterday = builder.deliveryAppForegroundYesterday;
        this.hasFewerDeliveriesGoal = builder.hasFewerDeliveriesGoal;
        this.hasOrganizeMealsGoal = builder.hasOrganizeMealsGoal;
        this.hasBetterChoicesOutGoal = builder.hasBetterChoicesOutGoal;
        this.eatingOutGapAlreadyDeliveredToday = builder.eatingOutGapAlreadyDeliveredToday;
        this.hasQuickBalancedGoal = builder.hasQuickBalancedGoal;
        this.dinnerLoggedToday = builder.dinnerLoggedToday;
        this.dinnerPlanAnsweredNoRecently = builder.dinnerPlanAnsweredNoRecently;
        this.dinnerNoPlanAlreadyDeliveredToday = builder.dinnerNoPlanAlreadyDeliveredToday;
    }

    public static Builder builder(LocalDateTime now) {
        return new Builder(now);
    }

    public static final class Builder {

        private final LocalDateTime now;
        private boolean deliveryAppForeground;
        private LocationZone currentZone = LocationZone.UNKNOWN;
        private boolean recentlyLeftWorkplace;
        private MealWindow currentMealWindow = MealWindow.NONE;
        private boolean hasMissingFoodCategory;
        private boolean weeklyReviewAlreadyDelivered;
        private boolean deliveryAppForegroundYesterday;
        private boolean hasFewerDeliveriesGoal;
        private boolean hasOrganizeMealsGoal;
        private boolean hasBetterChoicesOutGoal;
        private boolean eatingOutGapAlreadyDeliveredToday;
        private boolean hasQuickBalancedGoal;
        private boolean dinnerLoggedToday;
        private boolean dinnerPlanAnsweredNoRecently;
        private boolean dinnerNoPlanAlreadyDeliveredToday;

        private Builder(LocalDateTime now) {
            this.now = now;
        }

        public Builder deliveryAppForeground(boolean value) {
            this.deliveryAppForeground = value;
            return this;
        }

        public Builder currentZone(LocationZone value) {
            this.currentZone = value;
            return this;
        }

        public Builder recentlyLeftWorkplace(boolean value) {
            this.recentlyLeftWorkplace = value;
            return this;
        }

        public Builder currentMealWindow(MealWindow value) {
            this.currentMealWindow = value;
            return this;
        }

        public Builder hasMissingFoodCategory(boolean value) {
            this.hasMissingFoodCategory = value;
            return this;
        }

        public Builder weeklyReviewAlreadyDelivered(boolean value) {
            this.weeklyReviewAlreadyDelivered = value;
            return this;
        }

        public Builder deliveryAppForegroundYesterday(boolean value) {
            this.deliveryAppForegroundYesterday = value;
            return this;
        }

        public Builder hasFewerDeliveriesGoal(boolean value) {
            this.hasFewerDeliveriesGoal = value;
            return this;
        }

        public Builder hasOrganizeMealsGoal(boolean value) {
            this.hasOrganizeMealsGoal = value;
            return this;
        }

        public Builder hasBetterChoicesOutGoal(boolean value) {
            this.hasBetterChoicesOutGoal = value;
            return this;
        }

        public Builder eatingOutGapAlreadyDeliveredToday(boolean value) {
            this.eatingOutGapAlreadyDeliveredToday = value;
            return this;
        }

        public Builder hasQuickBalancedGoal(boolean value) {
            this.hasQuickBalancedGoal = value;
            return this;
        }

        public Builder dinnerLoggedToday(boolean value) {
            this.dinnerLoggedToday = value;
            return this;
        }

        public Builder dinnerPlanAnsweredNoRecently(boolean value) {
            this.dinnerPlanAnsweredNoRecently = value;
            return this;
        }

        public Builder dinnerNoPlanAlreadyDeliveredToday(boolean value) {
            this.dinnerNoPlanAlreadyDeliveredToday = value;
            return this;
        }

        public SignalSnapshot build() {
            return new SignalSnapshot(this);
        }
    }
}
