package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class CarProfileProperties extends ProfileProperties {

    public CarProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_CAR);
    }
}
