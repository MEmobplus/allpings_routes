package org.heigit.ors.routing.graphhopper.extensions.manage;

public interface ORSGraphRepoStrategy {

    String getRepoCompressedGraphFileName();

    String getRepoGraphInfoFileName();

    String getAssetFilterPattern(String repository, String coverage, String graphVersion, String profileGroup, String profileName, String fileName);
}
