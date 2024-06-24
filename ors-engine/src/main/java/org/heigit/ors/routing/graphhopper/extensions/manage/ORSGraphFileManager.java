package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Unzipper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ORSGraphFileManager implements ORSGraphFolderStrategy {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphFileManager.class.getName());

    private EngineConfig engineConfig;
    private String routeProfileName;
    private int maxNumberOfGraphBackups;
    private ORSGraphFolderStrategy orsGraphFolderStrategy;

    public ORSGraphFileManager() {
    }

    public ORSGraphFileManager(EngineConfig engineConfig, String routeProfileName, ORSGraphFolderStrategy orsGraphFolderStrategy) {
        this.engineConfig = engineConfig;
        this.routeProfileName = routeProfileName;
        this.orsGraphFolderStrategy = orsGraphFolderStrategy;
        int maxBak = engineConfig.getMaxNumberOfGraphBackups();
        this.maxNumberOfGraphBackups = Math.max(maxBak, 0);
    }

    public void initialize() {
        File vehicleGraphDir = getActiveGraphDirectory();
        if (!vehicleGraphDir.exists()) {
            LOGGER.info("[%s] Creating vehicle graph directory %s".formatted(getProfileDescriptiveName(), getActiveGraphDirName()));
            if (!vehicleGraphDir.mkdirs()) {
                LOGGER.error("[%s] Could not create vehicle graph directory %s".formatted(getProfileDescriptiveName(), getActiveGraphDirName()));
            }
        }
    }

    boolean hasActiveGraph() {
        return isExistingDirectoryWithFiles(getActiveGraphDirectory());
    }

    boolean hasActiveGraphDirectory() {
        return isExistingDirectory(getActiveGraphDirectory());
    }

    boolean hasGraphDownloadFile() {
        return getDownloadedCompressedGraphFile().exists();
    }

    public boolean hasDownloadedExtractedGraph() {
        return isExistingDirectoryWithFiles(getDownloadedExtractedGraphDirectory());
    }

    boolean isExistingDirectory(File directory) {
        return directory.exists() && directory.isDirectory();
    }

    boolean isExistingDirectoryWithFiles(File directory) {
        return isExistingDirectory(directory) && directory.listFiles().length > 0;
    }

    File asIncompleteFile(File file){
        return new File(file.getAbsolutePath() + "." + INCOMPLETE_EXTENSION);
    }

    File asIncompleteDirectory(File directory){
        return new File(directory.getAbsolutePath() + "_" + INCOMPLETE_EXTENSION);
    }

    public boolean isBusy() {
        return asIncompleteFile(getDownloadedCompressedGraphFile()).exists() ||
                asIncompleteFile(getDownloadedGraphInfoFile()).exists() ||
                asIncompleteFile(getDownloadedExtractedGraphDirectory()).exists();
    }

    void cleanupIncompleteFiles() {
        File incompleteDownloadFile = asIncompleteFile(getDownloadedCompressedGraphFile());
        if (incompleteDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted incomplete graph download file from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteDownloadFile.getAbsolutePath()));
            incompleteDownloadFile.delete();
        }

        File graphInfoDownloadFile = getDownloadedGraphInfoFile();
        if (graphInfoDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted graph-info download file from previous application run: %s".formatted(getProfileDescriptiveName(), graphInfoDownloadFile.getAbsolutePath()));
            graphInfoDownloadFile.delete();
        }

        File incompleteGraphInfoDownloadFile = asIncompleteFile(getDownloadedGraphInfoFile());
        if (incompleteGraphInfoDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted incomplete graph download file from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteGraphInfoDownloadFile.getAbsolutePath()));
            incompleteGraphInfoDownloadFile.delete();
        }

        File incompleteExtractionFolder = asIncompleteDirectory(getDownloadedExtractedGraphDirectory());
        if (incompleteExtractionFolder.exists() && incompleteExtractionFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(incompleteExtractionFolder);
                LOGGER.info("[%s] Deleted incomplete graph graph extraction folder from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteExtractionFolder.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("[%s] Could not delete incomplete graph extraction folder from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteExtractionFolder.getAbsolutePath()));
            }
        }
    }

    String createGraphUrlFromGraphInfoUrl(GraphInfo remoteGraphInfo) {
        String url = remoteGraphInfo.getRemoteUrl().toString();
        String urlWithoutExtension = url.substring(0, url.lastIndexOf('.'));
        return urlWithoutExtension + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    void backupExistingGraph() {
        if (!hasActiveGraph()) {
            deleteOldestBackups();
            return;
        }
        File activeGraphDirectory = getActiveGraphDirectory();
        String origAbsPath = activeGraphDirectory.getAbsolutePath();
        String dateString = DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmmss", Locale.getDefault()).format(LocalDateTime.now());
        String newAbsPath = activeGraphDirectory.getAbsolutePath() + "_" + dateString;
        File backupFile = new File(newAbsPath);

        if (backupFile.exists()){
            LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileDescriptiveName(), newAbsPath));
            try {
                FileUtils.deleteDirectory(backupFile);
                backupFile = new File(newAbsPath);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileDescriptiveName(), newAbsPath, e.getMessage()));
            }
        }

        if (activeGraphDirectory.renameTo(backupFile)) {
            LOGGER.info("[%s] Renamed old local graph directory %s to %s".formatted(getProfileDescriptiveName(), origAbsPath, newAbsPath));
        } else {
            LOGGER.error("[%s] Could not backup local graph directory %s to %s".formatted(getProfileDescriptiveName(), origAbsPath, newAbsPath));
        }
        deleteOldestBackups();
    }

    void deleteOldestBackups() {
        List<File> existingBackups = findGraphBackupsSortedByName();
        int numBackupsToDelete = existingBackups.size() - Math.max(maxNumberOfGraphBackups, 0);
        if (numBackupsToDelete < 1) {
            return;
        }
        List<File> backupsToDelete = existingBackups.subList(0, numBackupsToDelete);
        for (File backupFile : backupsToDelete) {
            try {
                LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileDescriptiveName(), backupFile.getAbsolutePath()));
                FileUtils.deleteDirectory(backupFile);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileDescriptiveName(), backupFile.getAbsolutePath(), e.getMessage()));
            }
        }
    }

    List<File> findGraphBackupsSortedByName() {
        File vehicleDir = getProfileGraphsDirectory();
        FilenameFilter filter = new RegexFileFilter("^%s_\\d{4}-\\d{2}-\\d{2}_\\d{6}$".formatted(getActiveGraphDirName()));
        File[] obj = vehicleDir.listFiles(filter);
        if (obj == null)
            return Collections.emptyList();

        return Arrays.asList(Objects.requireNonNull(obj)).stream().sorted(Comparator.comparing(File::getName)).toList();
    }

    GraphInfo getActiveGraphInfo() {
        LOGGER.trace("[%s] Checking active graph info...".formatted(getProfileDescriptiveName()));
        File activeGraphDirectory = getActiveGraphDirectory();

        if (!hasActiveGraph()) {
            LOGGER.trace("[%s] No active graph directory found".formatted(getProfileDescriptiveName()));
            return new GraphInfo().withLocalDirectory(activeGraphDirectory);
        }

        return getGraphInfo(getActiveGraphInfoFile());
    }

    GraphInfo getDownloadedExtractedGraphInfo() {
        LOGGER.trace("[%s] Checking downloaded graph info...".formatted(getProfileDescriptiveName()));
        File downloadedExtractedGraphDirectory = getDownloadedExtractedGraphDirectory();

        if (!hasDownloadedExtractedGraph()) {
            LOGGER.trace("[%s] No downloaded graph directory found".formatted(getProfileDescriptiveName()));
            return new GraphInfo().withLocalDirectory(downloadedExtractedGraphDirectory);
        }

        return getGraphInfo(getDownloadedExtractedGraphInfoFile());
    }

    private GraphInfo getGraphInfo(File graphInfoFile) {
        File graphDirectory = graphInfoFile.getParentFile();
        if (!graphInfoFile.exists() || !graphInfoFile.isFile()) {
            LOGGER.trace("[%s] No graph info file %s found in %s".formatted(getProfileDescriptiveName(), graphInfoFile.getName(), graphInfoFile.getParentFile().getName()));
            return new GraphInfo().withLocalDirectory(graphDirectory);
        }

        ORSGraphInfoV1 graphInfoV1 = readOrsGraphInfoV1(graphInfoFile);
        LOGGER.trace("[%s] Found local graph info with osmDate=%s".formatted(getProfileDescriptiveName(), graphInfoV1.getOsmDate()));
        return new GraphInfo().withLocalDirectory(graphDirectory).withPersistedInfo(graphInfoV1);
    }

    ORSGraphInfoV1 readOrsGraphInfoV1(File graphInfoFile) {
        try {
            return new ObjectMapper(new YAMLFactory())
                    .readValue(graphInfoFile, ORSGraphInfoV1.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeOrsGraphInfoV1(ORSGraphInfoV1 orsGraphInfoV1, File outputFile) {
        try {
            new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                    .writeValue(outputFile, orsGraphInfoV1);
        } catch (IOException e) {
            LOGGER.error("Could not write file {}".formatted(outputFile.getAbsolutePath()));
            throw new RuntimeException(e);
        }
    }

    ORSGraphInfoV1 getDownloadedGraphInfo() {
        LOGGER.trace("[%s] Checking graph info of previous check ...".formatted(getProfileDescriptiveName()));
        File downloadedGraphInfoFile = getDownloadedGraphInfoFile();
        if (downloadedGraphInfoFile.exists()) {
            return readOrsGraphInfoV1(downloadedGraphInfoFile);
        }
        return null;
    }

    public void activateExtractedDownloadedGraph() {
        LOGGER.info("[%s] Activating extracted downloaded graph".formatted(getProfileDescriptiveName()));
        getDownloadedExtractedGraphDirectory().renameTo(getActiveGraphDirectory());
    }

    public void extractDownloadedGraph() {
        File graphDownloadFile = getDownloadedCompressedGraphFile();
        if (!graphDownloadFile.exists()){
            LOGGER.debug("[%s] No downloaded graph to extract".formatted(getProfileDescriptiveName()));
            return;
        }

        String graphDownloadFileAbsPath = graphDownloadFile.getAbsolutePath();
        File targetDirectory = getDownloadedExtractedGraphDirectory();
        String targetDirectoryAbsPath = targetDirectory.getAbsolutePath();
        File extractionDirectory = asIncompleteDirectory(targetDirectory);
        String extractionDirectoryAbsPath = extractionDirectory.getAbsolutePath();

        if (isExistingDirectory(extractionDirectory)){
            LOGGER.debug("[%s] Extraction already started".formatted(getProfileDescriptiveName()));
            return;
        }

        try {
            LOGGER.debug("[%s] Extracting downloaded graph file to %s".formatted(getProfileDescriptiveName(), extractionDirectoryAbsPath));
            long start = System.currentTimeMillis();
            (new Unzipper()).unzip(graphDownloadFileAbsPath, extractionDirectoryAbsPath, true);
            long end = System.currentTimeMillis();

            LOGGER.debug("[%s] Extraction of downloaded graph file finished after %d ms, deleting downloaded graph file %s".formatted(
                    getProfileDescriptiveName(),
                    end-start,
                    graphDownloadFileAbsPath));
            graphDownloadFile.delete();

            LOGGER.debug("[%s] Renaming extraction directory to %s".formatted(
                    getProfileDescriptiveName(),
                    targetDirectoryAbsPath));
            if (targetDirectory.exists()) {
                FileUtils.deleteDirectory(targetDirectory);
            }
            if (!extractionDirectory.renameTo(targetDirectory)) {
                LOGGER.error("[%s] Could not rename extraction directory to %s".formatted(getProfileDescriptiveName(), targetDirectoryAbsPath));
            }

        } catch (IOException ioException) {
            LOGGER.error("[%s] Error during extraction of %s to %s -> %s".formatted(
                    getProfileDescriptiveName(),
                    graphDownloadFileAbsPath,
                    extractionDirectoryAbsPath,
                    targetDirectoryAbsPath));
            throw new RuntimeException("Caught ", ioException);
        }
        LOGGER.info("[%s] Downloaded graph was extracted and will be activated at next restart check or application start".formatted(getProfileDescriptiveName(), extractionDirectoryAbsPath));
    }

    public void writeOrsGraphInfoFileIfNotExists(GraphHopper gh) {
        if (engineConfig.getProfiles()==null)
            return;
        if (engineConfig.getProfiles().length==0)
            return;

        File graphDir = new File(getActiveGraphDirAbsPath());
        File orsGraphInfoFile = getDownloadedGraphInfoFile();
        if (!graphDir.exists() || !graphDir.isDirectory() || !graphDir.canWrite() ) {
            LOGGER.debug("Graph directory %s not existing or not writeable".formatted(orsGraphInfoFile.getName()));
            return;
        }
        if (orsGraphInfoFile.exists()) {
            LOGGER.debug("GraphInfo-File %s already existing".formatted(orsGraphInfoFile.getName()));
            return;
        }
        Optional<RouteProfileConfiguration> routeProfileConfiguration = Arrays.stream(engineConfig.getProfiles()).filter(prconf -> this.routeProfileName.equals(prconf.getName())).findFirst();
        if (routeProfileConfiguration.isEmpty()) {
            LOGGER.debug("Configuration for profile %s does not exist, could not write GraphInfo-File".formatted(this.routeProfileName));
            return;
        }

        ORSGraphInfoV1 orsGraphInfoV1 = new ORSGraphInfoV1(getDateFromGhProperty(gh, "datareader.data.date"));
        orsGraphInfoV1.setImportDate(getDateFromGhProperty(gh, "datareader.import.date"));
        orsGraphInfoV1.setImportDate(getDateFromGhProperty(gh, "datareader.import.date"));
        orsGraphInfoV1.setProfileProperties(routeProfileConfiguration.get().getOrsGraphInfoV1ProfileProperties());

        ORSGraphFileManager.writeOrsGraphInfoV1(orsGraphInfoV1, orsGraphInfoFile);
    }

    Date getDateFromGhProperty(GraphHopper gh, String ghProperty) {
        try {
            String importDateString = gh.getGraphHopperStorage().getProperties().get(ghProperty);
            if (StringUtils.isBlank(importDateString)) {
                return null;
            }
            DateFormat f = Helper.createFormatter();
            return f.parse(importDateString);
        } catch (ParseException e) {}
        return null;
    }


    @Override
    public String getProfileDescriptiveName() {
        return orsGraphFolderStrategy.getProfileDescriptiveName();
    }

    @Override
    public String getGraphInfoFileNameInRepository() {
        return orsGraphFolderStrategy.getGraphInfoFileNameInRepository();
    }

    @Override
    public String getGraphsRootDirName() {
        return orsGraphFolderStrategy.getGraphsRootDirName();
    }

    @Override
    public String getGraphsRootDirAbsPath() {
        return orsGraphFolderStrategy.getGraphsRootDirAbsPath();
    }

    @Override
    public String getProfileGraphsDirName() {
        return orsGraphFolderStrategy.getProfileGraphsDirName();
    }

    @Override
    public String getProfileGraphsDirAbsPath() {
        return orsGraphFolderStrategy.getProfileGraphsDirAbsPath();
    }

    @Override
    public String getActiveGraphDirName() {
        return orsGraphFolderStrategy.getActiveGraphDirName();
    }

    @Override
    public String getActiveGraphDirAbsPath() {
        return orsGraphFolderStrategy.getActiveGraphDirAbsPath();
    }

    @Override
    public String getActiveGraphInfoFileName() {
        return orsGraphFolderStrategy.getActiveGraphInfoFileName();
    }

    @Override
    public String getDownloadedGraphInfoFileName() {
        return orsGraphFolderStrategy.getDownloadedGraphInfoFileName();
    }

    @Override
    public String getDownloadedGraphInfoFileAbsPath() {
        return orsGraphFolderStrategy.getDownloadedGraphInfoFileAbsPath();
    }

    @Override
    public String getDownloadedCompressedGraphFileName() {
        return orsGraphFolderStrategy.getDownloadedCompressedGraphFileName();
    }

    @Override
    public String getDownloadedCompressedGraphFileAbsPath() {
        return orsGraphFolderStrategy.getDownloadedCompressedGraphFileAbsPath();
    }

    @Override
    public String getDownloadedExtractedGraphDirName() {
        return orsGraphFolderStrategy.getDownloadedExtractedGraphDirName();
    }

    @Override
    public String getDownloadedExtractedGraphDirAbsPath() {
        return orsGraphFolderStrategy.getDownloadedExtractedGraphDirAbsPath();
    }

    @Override
    public String getDownloadedExtractedGraphInfoFileName() {
        return orsGraphFolderStrategy.getDownloadedExtractedGraphInfoFileName();
    }
}