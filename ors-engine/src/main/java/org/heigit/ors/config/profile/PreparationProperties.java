package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.heigit.ors.config.utils.NonEmptyObjectFilter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreparationProperties {
    @Setter
    @JsonProperty("min_network_size")
    private Integer minNetworkSize;
    @Setter
    @JsonProperty("min_one_way_network_size")
    private Integer minOneWayNetworkSize;
    @JsonProperty("methods")
    @Accessors(chain = true)
    private MethodsProperties methods;

    public PreparationProperties() {
        this.methods = new MethodsProperties();
    }


    @Getter
    @Setter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyObjectFilter.class)
    public static class MethodsProperties {
        private CHProperties ch;
        private LMProperties lm;
        private CoreProperties core;
        private FastIsochroneProperties fastisochrones;

        public MethodsProperties() {
            ch = new CHProperties();
            lm = new LMProperties();
            core = new CoreProperties();
            fastisochrones = new FastIsochroneProperties();
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;
            private String lmsets;

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties {
            @Setter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer maxcellnodes;

            public Boolean isEnabled() {
                return enabled;
            }
        }
    }
}

