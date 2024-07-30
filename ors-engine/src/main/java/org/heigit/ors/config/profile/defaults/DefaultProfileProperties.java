package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfileProperties extends ProfileProperties {

    public DefaultProfileProperties() {
        this(false);
    }

    public DefaultProfileProperties(Boolean setDefaults) {
        super(setDefaults);
        if (setDefaults) {
            this.setEncoderName(EncoderNameEnum.UNKNOWN);
            setEncoderOptions(new DefaultEncoderOptionsProperties());
            setEnabled(false);
            setElevation(true);
            setElevationSmoothing(true);
            setEncoderFlagsSize(8);
            setInstructions(true);
            setOptimize(false);
            setTraffic(false);
            setInterpolateBridgesAndTunnels(true);
            setForceTurnCosts(false);
            setLocationIndexResolution(500);
            setLocationIndexSearchIterations(4);
            setMaximumDistance(100000d);
            setMaximumDistanceDynamicWeights(100000d);
            setMaximumDistanceAvoidAreas(100000d);
            setMaximumDistanceAlternativeRoutes(100000d);
            setMaximumDistanceRoundTripRoutes(100000d);
            setMaximumSpeedLowerBound(80d);
            setMaximumWayPoints(50);
            setMaximumSnappingRadius(400);
            setMaximumVisitedNodes(1000000);
        }
    }
}
