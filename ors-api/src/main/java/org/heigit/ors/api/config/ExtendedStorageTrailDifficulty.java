package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("TrailDifficulty")
public class ExtendedStorageTrailDifficulty extends ExtendedStorage {

    public ExtendedStorageTrailDifficulty() {
    }

    @JsonCreator
    public ExtendedStorageTrailDifficulty(String ignoredEmpty) {
    }
}
