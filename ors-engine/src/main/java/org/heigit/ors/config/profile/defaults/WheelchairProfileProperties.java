package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.storages.ExtendedStorageOsmId;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.config.profile.storages.ExtendedStorageWaySurfaceType;
import org.heigit.ors.config.profile.storages.ExtendedStorageWheelchair;

public class WheelchairProfileProperties extends ProfileProperties {
    public WheelchairProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.WHEELCHAIR);
        getEncoderOptions().setBlockFords(false);
        setMaximumSnappingRadius(50);
        getExtStorages().put("WayCategory", new ExtendedStorageWayCategory());
        getExtStorages().put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
        getExtStorages().put("Wheelchair", new ExtendedStorageWheelchair());
        getExtStorages().put("OsmId", new ExtendedStorageOsmId());

    }
}
