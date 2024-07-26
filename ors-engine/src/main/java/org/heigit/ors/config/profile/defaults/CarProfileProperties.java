package org.heigit.ors.config.profile.defaults;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.EncoderOptionsProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.storages.*;

public class CarProfileProperties extends ProfileProperties {

    public CarProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        getEncoderOptions().setTurnCosts(true);
        getEncoderOptions().setBlockFords(false);
        getEncoderOptions().setUseAcceleration(true);
        getPreparation().setMinNetworkSize(200);
        getPreparation().getMethods().getCh().setEnabled(true);
        getPreparation().getMethods().getCh().setThreads(1);
        getPreparation().getMethods().getCh().setWeightings("fastest");
        getPreparation().getMethods().getLm().setEnabled(false);
        getPreparation().getMethods().getLm().setThreads(1);
        getPreparation().getMethods().getLm().setWeightings("fastest,shortest");
        getPreparation().getMethods().getLm().setLandmarks(16);
        getPreparation().getMethods().getCore().setEnabled(true);
        getPreparation().getMethods().getCore().setThreads("1");
        getPreparation().getMethods().getCore().setWeightings("fastest,shortest");
        getPreparation().getMethods().getCore().setLandmarks(64);
        getPreparation().getMethods().getCore().setLmsets("highways;allow_all");
        getExecution().getMethods().getLm().setActiveLandmarks(6);
        getExecution().getMethods().getCore().setActiveLandmarks(6);
        getExtStorages().put("WayCategory", new ExtendedStorageWayCategory());
        getExtStorages().put("HeavyVehicle", new ExtendedStorageHeavyVehicle());
        getExtStorages().put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
        getExtStorages().put("Tollways", new ExtendedStorageTollways());
        getExtStorages().put("RoadAccessRestrictions", new ExtendedStorageRoadAccessRestrictions());
    }

}
