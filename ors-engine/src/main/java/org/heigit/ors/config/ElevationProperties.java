package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ElevationProperties {
    private Boolean preprocessed = false;
    @JsonProperty("data_access")
    private DataAccessEnum dataAccess = DataAccessEnum.MMAP;
    @JsonProperty("cache_clear")
    private Boolean cacheClear = false;
    @JsonProperty("provider")
    private String provider = "multi";
    @JsonProperty("cache_path")
    @JsonSerialize(using = PathSerializer.class)
    private Path cachePath = Paths.get("elevation_cache");
}