package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("HillIndex")
@EqualsAndHashCode(callSuper = true)
public class ExtendedStorageHillIndex extends ExtendedStorage {
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    public ExtendedStorageHillIndex() {
    }

    @JsonCreator
    public ExtendedStorageHillIndex(String ignoredEmpty) {
    }

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageHillIndex storage) {
            if (this.getMaximumSlope() == null) {
                this.setMaximumSlope(storage.maximumSlope);
            } else {
                if (storage.getMaximumSlope() != null && overwrite) {
                    this.setMaximumSlope(storage.getMaximumSlope());
                }
            }
        }
    }
}
