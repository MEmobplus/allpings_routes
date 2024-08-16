package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;

import java.nio.file.Path;

public class RepoManagerTestHelper {


    public static EngineProperties createEngineProperties(Path localGraphsRootPath,
                                                          Path graphManagementRepositoryPath,
                                                          String graphManagementRepositoryUrl,
                                                          String graphManagementRepositoryName,
                                                          String graphManagementRepositoryProfileGroup,
                                                          String graphManagementGraphExtent,
                                                          String profileName, int graphManagementMaxBackups) {

        EngineProperties engineProperties = new EngineProperties();
        engineProperties.initProfilesMap();
        engineProperties.setGraphsRootPath(localGraphsRootPath);
        engineProperties.getProfiles().get(profileName).setEnabled(true);

        GraphManagementProperties graphManagementProperties = engineProperties.getGraphManagement();
        graphManagementProperties.setRepositoryUrl(graphManagementRepositoryUrl);
        if (graphManagementRepositoryPath != null) {
            graphManagementProperties.setRepositoryPath(graphManagementRepositoryPath.toFile().toString());
        }
        graphManagementProperties.setRepositoryName(graphManagementRepositoryName);
        graphManagementProperties.setRepositoryProfileGroup(graphManagementRepositoryProfileGroup);
        graphManagementProperties.setGraphExtent(graphManagementGraphExtent);
        graphManagementProperties.setMaxBackups(graphManagementMaxBackups);

        return engineProperties;
    }


}
