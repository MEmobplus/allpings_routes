package org.heigit.ors.config.defaults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

import java.nio.file.Path;

@JsonIgnoreProperties({"ext_storages"})
@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesPublicTransport extends ProfileProperties {
    public DefaultProfilePropertiesPublicTransport() {
        this(false);
    }

    public DefaultProfilePropertiesPublicTransport(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.PUBLIC_TRANSPORT);

        if (setDefaults) {
            this.setEncoderName(EncoderNameEnum.PUBLIC_TRANSPORT);
            setEnabled(false);
            setElevation(true);
            setMaximumVisitedNodes(1000000);
            setGtfsFile(Path.of(""));
            DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
            setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
        }
    }
}
