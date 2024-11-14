# Configuration Properties

The configuration properties are organized in a hierarchical structure,
with the following ones at top level.
Since openrouteservice is based on spring,
all
common [spring properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
can be set.
The most important ones are:

* [Server Properties](server.md)
* [Logging Properties](logging.md)

Other openrouteservice specific properties are organized in the `ors` object:

* [ors.endpoints](endpoints/index.md): Settings required at runtime to process API requests.
* [ors.engine](engine/index.md): Settings required at graph-build time during startup, and settings
  regarding [profiles](engine/profiles/) run on the instance.
* [ors.cors](cors/index.md): Cross-origin resource sharing settings.
* [ors.messages](messages/index.md): System messages that can be sent with API responses following simple rules.

::: tip
In this documentation we use the dot notation for the properties.
Note, that the same properties can be defined in [different notations](how-to-set-properties.md#different-notations).
:::

## Minimal Configuration

At the very least, openrouteservice needs the configuration to contain at least one
enabled [profile](engine/profiles/profiles.md) and the
reference to an [OSM data file](../data.md#osm-data) to run properly. Therefore, the minimal valid configuration
would be, e.g.:

```yaml
ors:
    engine:
        profile_default:
            build:
                source_file: ./osm_file.pbf
        profiles:
            driving-car:
                enabled: true
```

The two properties can be set in different ways, see [How to set Properties](how-to-set-properties.md)