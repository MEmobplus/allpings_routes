package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesHiking extends ProfileProperties {
    public DefaultProfilePropertiesHiking() {
        this(false);
    }

    public DefaultProfilePropertiesHiking(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.FOOT_HIKING);
    }
}
