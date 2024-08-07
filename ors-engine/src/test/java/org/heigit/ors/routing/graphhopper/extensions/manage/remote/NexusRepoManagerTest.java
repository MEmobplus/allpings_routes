package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.HashSubDirBasedORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NexusRepoManagerTest {

    @Spy
    NexusRepoManager orsGraphRepoManager;
    ORSGraphFileManager orsGraphFileManager;
    private static final String GRAPHS_REPO_BASE_URL = "https://example.com";
    private static final String GRAPHS_REPO_NAME = "test-repo";
    private static final String GRAPHS_REPO_PATH = "some/path/12345";
    private static final String GRAPHS_COVERAGE = "planet";
    private static final String GRAPHS_PROFILE_GROUP = "traffic";
    private static final String GRAPHS_VERSION = "1";
    private static final String VEHICLE = "car";
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private static Path TEMP_DIR;
    private static final long EARLIER_DATE = 1692373111000L;
    private static final long MIDDLE_DATE = 1692373222000L;
    private static final long LATER_DATE = 1692373333000L;

    String vehicleDirAbsPath, hashDirAbsPath;
    File localDir, vehicleDir, hashDir, downloadedGraphInfoV1File, localGraphInfoV1File;


    @BeforeEach
    void setUp() {
        localDir = TEMP_DIR.toFile();
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        vehicleDir = new File(vehicleDirAbsPath);
        vehicleDir.mkdir();
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(vehicleDir);
    }

    void setupORSGraphManager(String hash) {
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);
        hashDir = new File(hashDirAbsPath);
        hashDir.mkdir();

        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsRepoPath(GRAPHS_REPO_PATH)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .setGraphsProfileGroup(GRAPHS_PROFILE_GROUP)
                .setGraphVersion(GRAPHS_VERSION)
                .setGraphsRootPath(localDir.getAbsolutePath())
                .build();

        ORSGraphFolderStrategy orsGraphFolderStrategy = new HashSubDirBasedORSGraphFolderStrategy(engineConfig.getGraphsRootPath(), VEHICLE, hash);
        orsGraphFileManager = new ORSGraphFileManager(engineConfig, VEHICLE, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        //ORSGraphRepoManager is mocked, empty default constructor was called -> set fields here:
        orsGraphRepoManager.setGraphsRepoPath(engineConfig.getGraphsRepoPath());
        orsGraphRepoManager.setGraphsRepoBaseUrl(engineConfig.getGraphsRepoUrl());
        orsGraphRepoManager.setGraphsRepoName(engineConfig.getGraphsRepoName());
        orsGraphRepoManager.setGraphsRepoCoverage(engineConfig.getGraphsExtent());
        orsGraphRepoManager.setGraphsProfileGroup(engineConfig.getGraphsProfileGroup());
        orsGraphRepoManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphRepoManager.setOrsGraphFileManager(orsGraphFileManager);
        orsGraphRepoManager.setRouteProfileName(VEHICLE);

        ORSGraphRepoStrategy repoStrategy = new HashBasedRepoStrategy(hash);
        orsGraphRepoManager.setOrsGraphRepoStrategy(repoStrategy);
    }

    void setupActiveGraphDirectory(String hash, Long osmDateLocal) throws IOException {
        ORSGraphInfoV1 activeGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateLocal));
        localGraphInfoV1File = orsGraphFileManager.getActiveGraphInfoFile();
        new ObjectMapper().writeValue(localGraphInfoV1File, activeGraphInfoV1Object);
    }

    void setupNoRemoteFiles() {
        doReturn(null).when(orsGraphRepoManager).findLatestGraphInfoAsset();
    }

    void simulateFindLatestGraphInfoAsset(String hash, Long osmDateRemote) throws IOException {
        String graphInfoAssetName = orsGraphFileManager.getGraphInfoFileNameInRepository();
        String graphInfoAssetUrl = String.join("/", GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, orsGraphFileManager.getProfileDescriptiveName(), graphInfoAssetName);

        ORSGraphInfoV1 downloadedOrsGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateRemote));
        downloadedGraphInfoV1File = new File(vehicleDirAbsPath + "/" + graphInfoAssetName);
        new ObjectMapper().writeValue(downloadedGraphInfoV1File, downloadedOrsGraphInfoV1Object);

        AssetXO assetXO = new AssetXO();
        assetXO.setDownloadUrl(graphInfoAssetUrl);

        doReturn(assetXO).when(orsGraphRepoManager).findLatestGraphInfoAsset();
        lenient().doNothing().when(orsGraphRepoManager).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDataExists_noRemoteData() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        setupActiveGraphDirectory(hash, EARLIER_DATE);
        setupNoRemoteFiles();

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, never()).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_noLocalData_remoteDataExists() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(2)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_localDate_before_remoteDate() throws IOException {
        String hash = "xyz111";
        setupORSGraphManager(hash);
        setupActiveGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, LATER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(2)).downloadAsset(anyString(), any());
        File localGraphDir = new File(hashDirAbsPath);
        File backupDir = new File(hashDirAbsPath + "_bak");
        assertTrue(localGraphDir.exists());
        assertFalse(backupDir.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_equals_remoteDate() throws IOException {
        String hash = "xyz222";
        setupORSGraphManager(hash);
        setupActiveGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_after_remoteDate() throws IOException {
        String hash = "xyz333";
        setupORSGraphManager(hash);
        setupActiveGraphDirectory(hash, LATER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void filterLatestAsset() {
        String hash = "b6714103ccd4";
        setupORSGraphManager(hash);
        List<AssetXO> items = Arrays.asList(
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202201011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202201011200/b6714103ccd4.yml"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202301011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202301011200/b6714103ccd4.yml"), //this one is expected
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202301011200/wrong.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/b6714103ccd4/202301011200/wrong.yml"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/wrong/202301011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/wrong/202301011200/b6714103ccd4.yml"),
                new AssetXO().path("https://example.com/test-repo/planet/1/wrong/b6714103ccd4/202301011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/wrong/b6714103ccd4/202301011200/b6714103ccd4.yml"),
                new AssetXO().path("https://example.com/test-repo/planet/wrong/car/b6714103ccd4/202301011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/wrong/car/b6714103ccd4/202301011200/b6714103ccd4.yml"),
                new AssetXO().path("https://example.com/test-repo/wrong/1/car/b6714103ccd4/202301011200/b6714103ccd4.ghz"),
                new AssetXO().path("https://example.com/test-repo/wrong/1/car/b6714103ccd4/202301011200/b6714103ccd4.yml")
        );
        AssetXO filtered = orsGraphRepoManager.filterLatestAsset(items);
        assertEquals("https://example.com/test-repo/planet/1/car/b6714103ccd4/202301011200/b6714103ccd4.yml", filtered.getPath());
    }
}