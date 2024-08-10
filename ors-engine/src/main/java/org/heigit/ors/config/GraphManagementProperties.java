package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class GraphManagementProperties {

    @JsonProperty("graph_version")
    private String graphVersion;

    @JsonProperty("graph_extent")
    private String graphExtent;

    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("repository_path")
    private String repositoryPath;

    @JsonProperty("repository_profile_group")
    private String repositoryProfileGroup;

    @JsonProperty("download_schedule")
    private String downloadSchedule;

    @JsonProperty("activation_schedule")
    private String activationSchedule;

    @JsonProperty("max_backups")
    private Integer maxBackups;

}
