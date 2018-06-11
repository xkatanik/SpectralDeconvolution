package cz.muni.fi;

import com.google.common.collect.Range;
import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.main.MZmineConfiguration;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.main.impl.MZmineConfigurationImpl;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.CSVExportParameters;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.ExportRowCommonElement;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.ExportRowDataFileElement;
import net.sf.mzmine.modules.peaklistmethods.io.xmlimport.*;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.adap3decompositionV1_5.ADAP3DecompositionV1_5Parameters;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.NetCDFReadTask;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsSelection;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsSelectionType;
import net.sf.mzmine.project.impl.MZmineProjectImpl;
import net.sf.mzmine.project.impl.ProjectManagerImpl;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spectral deconvolution module.
 *
 * @author Kristian Katanik
 */
public class Main {

    private static Boolean id = true;
    private static Boolean mz = true;
    private static Boolean rt = true;
    private static Boolean mainID = true;
    private static Boolean allIDs = true;
    private static Boolean mainIdDetails = true;
    private static Boolean comment = true;
    private static Boolean numberOfDetectedPeaks = true;
    private static Boolean peakStatus = true;
    private static Boolean peakMZ = true;
    private static Boolean peakRT = true;
    private static Boolean peakRTStart = true;
    private static Boolean peakRTEnd = true;
    private static Boolean peakDurationTime = true;
    private static Boolean peakHeight = true;
    private static Boolean peakArea = true;
    private static Boolean peakCharge = true;
    private static Boolean peakDataPoints = true;
    private static Boolean peakFWHM = true;
    private static Boolean peakTailingFactor = true;
    private static Boolean peakAsymmetryFactor = true;
    private static Boolean peakMzMin = true;
    private static Boolean peakMzMax = true;
    private static int exportCommon = 8;
    private static int exportData = 15;

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {

        String inputFileName;
        String rawData;
        String outputFileName;
        Double clusterDistance = 0.01;
        Integer clusterSize = 2;
        Double clusterIntensity = 500.0;
        Boolean findSharedPeaks = false;
        Double edgeToHeightRatio = 0.3;
        Double deltaToHeightRatio = 0.2;
        Double sharpness = 10.0;
        Double shapeSimilarityTolerance = 18.0;
        Boolean mzValueModelPeak = true;
        List<Range<Double>> excludeMzValues = new ArrayList<>();

        //export parameters
        String fieldSeparator = ",";
        String identificationSeparator = ";";
        Boolean quantitationResults = true;


        Options options = setOptions();
        String header = "All options from -qr are output options and are set on true as default. To exclude some row from output file, use specific option."
                + " For example, to exclude row ID, use option -noid.";
        String footer = "Created by Kristian Katanik, version 1.1.";

        if (args.length == 0) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setOptionComparator(null);
            helpFormatter.printHelp("Spectral deconvolution module help.", header, options, footer, true);
            System.exit(1);
            return;
        }

        CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            for (String arg : args) {
                if (arg.equals("-h") || arg.equals("--help")) {
                    HelpFormatter helpFormatter = new HelpFormatter();
                    helpFormatter.setOptionComparator(null);
                    helpFormatter.printHelp("Spectral deconvolution module help.", header, options, footer, true);
                    System.exit(1);
                    return;
                }
            }
            System.err.println("Some of the required parameters or their arguments are missing. Use -h or --help for help.");
            System.exit(1);
            return;
        }

        inputFileName = commandLine.getOptionValue("i");
        outputFileName = commandLine.getOptionValue("o");
        rawData = commandLine.getOptionValue("r");

        if (commandLine.hasOption("cd")) {
            try {
                clusterDistance = Double.parseDouble(commandLine.getOptionValue("cd"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of clusterDistance value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("cs")) {
            try {
                clusterSize = Integer.parseInt(commandLine.getOptionValue("cs"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of clusterSize value. Value has to be number in integer format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("ci")) {
            try {
                clusterIntensity = Double.parseDouble(commandLine.getOptionValue("ci"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of clusterIntensity value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("fsp")) {
            findSharedPeaks = true;
        }
        if (commandLine.hasOption("eth")) {
            try {
                edgeToHeightRatio = Double.parseDouble(commandLine.getOptionValue("eth"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of edgeToHeightRatio value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("dth")) {
            try {
                deltaToHeightRatio = Double.parseDouble(commandLine.getOptionValue("dth"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of deltaToHeightRatio value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("s")) {
            try {
                sharpness = Double.parseDouble(commandLine.getOptionValue("s"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of sharpness value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("sst")) {
            try {
                shapeSimilarityTolerance = Double.parseDouble(commandLine.getOptionValue("sst"));
            } catch (NumberFormatException e) {
                System.err.println("Wrong format of shapeSimilarityTolerance value. Value has to be number in double format.");
                System.exit(1);
                return;
            }
        }
        if (commandLine.hasOption("sm")) {
            mzValueModelPeak = false;
        }
        if (commandLine.hasOption("emz")) {
            String value = commandLine.getOptionValue("emz");
            if(!value.equals("none")){
                String[] values = value.split(":");
                for(String number : values){
                    try {
                        Range<Double> range = Range.closed(Double.parseDouble(number), Double.parseDouble(number));
                        excludeMzValues.add(range);
                    } catch (Exception e) {
                        System.err.println("Wrong format of -excludeMzValues parameter.");
                        System.exit(1);
                        return;
                    }
                }
            }
        }
        if (commandLine.hasOption("fs")) {
            fieldSeparator = commandLine.getOptionValue("fs");
        }
        if (commandLine.hasOption("is")) {
            identificationSeparator = commandLine.getOptionValue("is");
        }
        if (commandLine.hasOption("noqr")) {
            quantitationResults = false;
        }
        if (commandLine.hasOption("noid")) {
            id = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nort")) {
            rt = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nomz")) {
            mz = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nomid")) {
            mainID = false;
            exportCommon--;
        }
        if (commandLine.hasOption("noaid")) {
            allIDs = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nomidd")) {
            mainIdDetails = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nocom")) {
            comment = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nondp")) {
            numberOfDetectedPeaks = false;
            exportCommon--;
        }
        if (commandLine.hasOption("nops")) {
            peakStatus = false;
            exportData--;
        }
        if (commandLine.hasOption("nopdt")) {
            peakDurationTime = false;
            exportData--;
        }
        if (commandLine.hasOption("nopa")) {
            peakArea = false;
            exportData--;
        }
        if (commandLine.hasOption("nopaf")) {
            peakAsymmetryFactor = false;
            exportData--;
        }
        if (commandLine.hasOption("noprt")) {
            peakRT = false;
            exportData--;
        }
        if (commandLine.hasOption("noprte")) {
            peakRTEnd = false;
            exportData--;
        }
        if (commandLine.hasOption("noprts")) {
            peakRTStart = false;
            exportData--;
        }
        if (commandLine.hasOption("nopmz")) {
            peakMZ = false;
            exportData--;
        }
        if (commandLine.hasOption("noph")) {
            peakHeight = false;
            exportData--;
        }
        if (commandLine.hasOption("nopc")) {
            peakCharge = false;
            exportData--;
        }
        if (commandLine.hasOption("nopf")) {
            peakFWHM = false;
            exportData--;
        }
        if (commandLine.hasOption("nopdp")) {
            peakDataPoints = false;
            exportData--;
        }
        if (commandLine.hasOption("noptf")) {
            peakTailingFactor = false;
            exportData--;
        }
        if (commandLine.hasOption("nopmin")) {
            peakMzMin = false;
            exportData--;
        }
        if (commandLine.hasOption("nopmax")) {
            peakMzMax = false;
            exportData--;
        }


        File inputFile;
        try {
            inputFile = new File(inputFileName);
        } catch (Exception e) {
            System.out.println("Unable to load input file.");
            System.exit(1);
            return;
        }

        File rawInputFile;
        try {
            rawInputFile = new File(rawData);
        } catch (Exception e) {
            System.out.println("Unable to load raw file.");
            System.exit(1);
            return;
        }

        File outputFile;
        try {
            outputFile = new File(outputFileName);
        } catch (Exception e) {
            System.out.println("Unable to create/load output file.");
            System.exit(1);
            return;
        }

        if (!inputFile.exists() || inputFile.isDirectory() || !rawInputFile.exists() || rawInputFile.isDirectory()) {
            System.err.println("Unable to load input/raw file.");
            System.exit(1);
            return;
        }

        final MZmineProject mZmineProject = new MZmineProjectImpl();

        //code for raw data
        RawDataFileImpl rawDataFile2;
        try {
            rawDataFile2 = new RawDataFileImpl(rawInputFile.getName());
        } catch (IOException e) {
            System.err.println("Unable to open raw data file.");
            System.exit(1);
            return;
        }

        NetCDFReadTask netCDFReadTask = new NetCDFReadTask(mZmineProject, rawInputFile, rawDataFile2);
        netCDFReadTask.run();
        mZmineProject.addFile(rawDataFile2);


        XMLImportParameters xmlImportParameters = new XMLImportParameters();
        xmlImportParameters.getParameter(XMLImportParameters.filename).setValue(inputFile);
        XMLImportTask xmlImportTask = new XMLImportTask(mZmineProject, xmlImportParameters);
        xmlImportTask.run();


        MZmineConfiguration configuration = new MZmineConfigurationImpl();
        Field configurationField = MZmineCore.class.getDeclaredField("configuration");
        configurationField.setAccessible(true);
        configurationField.set(null, configuration);

        ProjectManagerImpl projectManager = new ProjectManagerImpl();
        Field projectManagerField = MZmineCore.class.getDeclaredField("projectManager");
        projectManagerField.setAccessible(true);
        projectManagerField.set(null, projectManager);
        projectManager.setCurrentProject(mZmineProject);


        PeakListsSelection peakListsSelection = new PeakListsSelection();
        peakListsSelection.setSelectionType(PeakListsSelectionType.ALL_PEAKLISTS);

        ADAP3DecompositionV1_5Parameters adap3DecompositionV1_5Parameters = setADAP3DecompositionParameters(clusterDistance,
                clusterIntensity, clusterSize, sharpness, deltaToHeightRatio, edgeToHeightRatio, shapeSimilarityTolerance, findSharedPeaks,
                mzValueModelPeak, excludeMzValues);

        ADAP3DecompositionV1_5Task task =
                new ADAP3DecompositionV1_5Task(mZmineProject, mZmineProject.getPeakLists()[0], adap3DecompositionV1_5Parameters);
        task.run();

        CSVExportParameters csvExportParameters = setCSVExportParameters(mZmineProject, outputFile, fieldSeparator, identificationSeparator,
                quantitationResults, setRowDataFileElements(), setExportRowCommonElements());

        CSVExportTask csvExportTask = new CSVExportTask(csvExportParameters);
        csvExportTask.run();


    }

    private static Options setOptions() {
        Options options = new Options();
        options.addOption(Option.builder("i").required().hasArg().longOpt("inputFile").desc("[required] Name or path of input file. File name must end with .MPL").build());
        options.addOption(Option.builder("o").required().hasArg().longOpt("outputFile").desc("[required] Name or path of output file. File name must end with .CSV").build());
        options.addOption(Option.builder("r").required().hasArg().longOpt("rawDataFile").desc("[required] Name or path of raw data file from previous step. File name must end with .CDF").build());
        options.addOption(Option.builder("cd").required(false).hasArg().longOpt("clusterDistance").desc("Minimum distance between any two clusters. [default 0.01]").build());
        options.addOption(Option.builder("cs").required(false).hasArg().longOpt("clusterSize").desc("Minimum size of a cluster. [default 2]").build());
        options.addOption(Option.builder("ci").required(false).hasArg().longOpt("clusterIntensity").desc("If the highest peak in a cluster has the intensity below" +
                " Minimum Cluster Intensity,the cluster is removed.[default 500.0]").build());
        options.addOption(Option.builder("fsp").required(false).longOpt("findSharedPeaks").desc("If selected, peaks are marked as Shared" +
                " if they are composed of two or more peaks. [default false]").build());
        options.addOption(Option.builder("eth").required(false).hasArg().longOpt("edgeToHeightRatio").desc("A peak is considered shared if its edge-to-height" +
                " ratio is below this parameter[default 0.3]").build());
        options.addOption(Option.builder("dth").required(false).hasArg().longOpt("deltaToHeightRatio").desc("A peak is considered shared if its delta (difference between the edges)-to-height" +
                " ratio is below this parameter. [default 0.2]").build());
        options.addOption(Option.builder("s").required(false).hasArg().longOpt("sharpness").desc("Minimum sharpness that the model peak can have. [default 10]").build());
        options.addOption(Option.builder("sst").required(false).hasArg().longOpt("shapeSimilarityTolerance").desc("Shape-similarity threshold is used to find similar peaks.(0..90) [default 18]").build());
        options.addOption(Option.builder("sm").required(false).longOpt("sharpnessModel").desc("Criterion to choose a model peak in a cluster: either peak with the highest m/z-value or with the highest sharpness.").build());
        options.addOption(Option.builder("mz").required(false).longOpt("mzModel").desc("Criterion to choose a model peak in a cluster: either peak with the highest m/z-value or with the highest sharpness. [as default]").build());
        options.addOption(Option.builder("emz").required(false).hasArg().longOpt("excludeMzValues").desc("M/z-values to exclude while selecting model peak." +
                " Divide values with colon, etc. 12.0:15.0 [default none]").build());
        options.addOption(Option.builder("fs").required(false).hasArg().longOpt("fieldSeparator").desc("Field separator [default , ]").build());
        options.addOption(Option.builder("is").required(false).hasArg().longOpt("identificationSeparator").desc("Identification separator [default ; ]").build());
        options.addOption(Option.builder("noqr").required(false).longOpt("quantitationResults").desc("Do not export quantitation results and other information").build());
        options.addOption(Option.builder("noid").required(false).desc("Do not export row ID").build());
        options.addOption(Option.builder("nomz").required(false).desc("Do not export row m/z").build());
        options.addOption(Option.builder("nort").required(false).desc("Do not export row retention time").build());
        options.addOption(Option.builder("nomid").required(false).longOpt("mainID").desc("Do not export row identity (main ID)").build());
        options.addOption(Option.builder("noaid").required(false).longOpt("allID").desc("Do not export row identity (all IDs)").build());
        options.addOption(Option.builder("nomidd").required(false).longOpt("mainIdDetails").desc("Do not export row identity (main ID + details)").build());
        options.addOption(Option.builder("nocom").required(false).longOpt("comment").desc("Do not export row comment").build());
        options.addOption(Option.builder("nondp").required(false).longOpt("numberOfDetectedPeaks").desc("Do not export row number of detected peaks").build());
        options.addOption(Option.builder("nops").required(false).longOpt("peakStatus").desc("Do not export Peak status").build());
        options.addOption(Option.builder("nopmz").required(false).longOpt("peakMZ").desc("Do not export Peak m/z").build());
        options.addOption(Option.builder("noprt").required(false).longOpt("peakRT").desc("Do not export Peak RT").build());
        options.addOption(Option.builder("noprts").required(false).longOpt("peakRTStart").desc("Do not export Peak RT start").build());
        options.addOption(Option.builder("noprte").required(false).longOpt("peakRTEnd").desc("Do not export Peak RT end").build());
        options.addOption(Option.builder("nopdt").required(false).longOpt("peakDurationTime").desc("Do not export Peak duration time").build());
        options.addOption(Option.builder("noph").required(false).longOpt("peakHeight").desc("Do not export Peak height").build());
        options.addOption(Option.builder("nopa").required(false).longOpt("peakArea").desc("Do not export Peak area").build());
        options.addOption(Option.builder("nopc").required(false).longOpt("peakCharge").desc("Do not export Peak charge").build());
        options.addOption(Option.builder("nopdp").required(false).longOpt("peakDataPoints").desc("Do not export Peak # data points").build());
        options.addOption(Option.builder("nopf").required(false).longOpt("peakFWHM").desc("Do not export Peak FWHM").build());
        options.addOption(Option.builder("noptf").required(false).longOpt("peakTailingFactor").desc("Do not export Peak tailing factor").build());
        options.addOption(Option.builder("nopaf").required(false).longOpt("peakAssymetryFactor").desc("Do not export Peak assymetry factor").build());
        options.addOption(Option.builder("nopmin").required(false).longOpt("peakMzMin").desc("Do not export Peak m/z min").build());
        options.addOption(Option.builder("nopmax").required(false).longOpt("peakMzMax").desc("Do not export Peak m/z max").build());
        options.addOption(Option.builder("h").required(false).longOpt("help").build());

        return options;

    }

    private static ADAP3DecompositionV1_5Parameters setADAP3DecompositionParameters(Double clusterDistance, Double clusterIntensity,
                                                                                    Integer clusterSize, Double sharpness, Double deltaToHeightRatio,
                                                                                    Double edgeToHeightRatio, Double shapeSimilarityTolerance,
                                                                                    Boolean findSharedPeaks, Boolean mzValueModelPeak, List<Range<Double>> excludeMzValues) {
        ADAP3DecompositionV1_5Parameters parameters = new ADAP3DecompositionV1_5Parameters();
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_DISTANCE).setValue(clusterDistance);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_INTENSITY).setValue(clusterIntensity);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_SIZE).setValue(clusterSize);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_MODEL_SHARPNESS).setValue(sharpness);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.DELTA_TO_HEIGHT_RATIO).setValue(deltaToHeightRatio);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.EDGE_TO_HEIGHT_RATIO).setValue(edgeToHeightRatio);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.SHAPE_SIM_THRESHOLD).setValue(shapeSimilarityTolerance);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.USE_ISSHARED).setValue(findSharedPeaks);
        if (mzValueModelPeak) {
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MODEL_PEAK_CHOICE).setValue("M/z value");
        } else {
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MODEL_PEAK_CHOICE).setValue("Shaprness");
        }

        if (excludeMzValues.isEmpty()) {
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MZ_VALUES).setValue(Collections.<Range<Double>>emptyList());
        } else {
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MZ_VALUES).setValue(excludeMzValues);
        }

        return parameters;
    }

    private static CSVExportParameters setCSVExportParameters(MZmineProject mZmineProject, File outputFile, String fieldSeparator,
                                                              String identificationSeparator, Boolean quantitationResults,
                                                              MultiChoiceParameter<ExportRowDataFileElement> exportDataFileItems,
                                                              MultiChoiceParameter<ExportRowCommonElement> exportCommonItems) {

        PeakListsSelection peakListsSelection = new PeakListsSelection();
        peakListsSelection.setSelectionType(PeakListsSelectionType.ALL_PEAKLISTS);

        PeakListsParameter peakListsParameter = new PeakListsParameter();
        PeakListsSelection peakListsSelection1 = new PeakListsSelection();
        peakListsSelection1.setSpecificPeakLists(new PeakList[]{mZmineProject.getPeakLists()[0]});
        peakListsSelection1.setSelectionType(PeakListsSelectionType.SPECIFIC_PEAKLISTS);
        peakListsParameter.setValue(peakListsSelection1);


        peakListsSelection.setSelectionType(PeakListsSelectionType.SPECIFIC_PEAKLISTS);
        CSVExportParameters csvExportParameters = new CSVExportParameters();
        csvExportParameters.getParameter(CSVExportParameters.filename).setValue(outputFile);
        csvExportParameters.getParameter(CSVExportParameters.peakLists).setValue(peakListsParameter.getValue());
        csvExportParameters.getParameter(CSVExportParameters.fieldSeparator).setValue(fieldSeparator);
        csvExportParameters.getParameter(CSVExportParameters.idSeparator).setValue(identificationSeparator);
        csvExportParameters.getParameter(CSVExportParameters.exportAllPeakInfo).setValue(quantitationResults);
        csvExportParameters.getParameter(CSVExportParameters.exportCommonItems).setValue(exportCommonItems.getValue());
        csvExportParameters.getParameter(CSVExportParameters.exportDataFileItems).setValue(exportDataFileItems.getValue());

        return csvExportParameters;
    }


    private static MultiChoiceParameter<ExportRowCommonElement> setExportRowCommonElements() {

        ExportRowCommonElement[] rowCommonElements = new ExportRowCommonElement[exportCommon];
        int position = 0;
        if (id) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_ID;
            position++;
        }
        if (rt) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_RT;
            position++;
        }
        if (mainID) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY;
            position++;
        }
        if (mainIdDetails) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY_DETAILS;
            position++;
        }
        if (mz) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_MZ;
            position++;
        }
        if (allIDs) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY_ALL;
            position++;
        }
        if (comment) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_COMMENT;
            position++;
        }
        if (numberOfDetectedPeaks) {
            rowCommonElements[position] = ExportRowCommonElement.ROW_PEAK_NUMBER;
        }
        MultiChoiceParameter<ExportRowCommonElement> exportCommonItems = new MultiChoiceParameter<>(
                "Export common elements", "Selection of row's elements to export",
                ExportRowCommonElement.values());

        exportCommonItems.setValue(rowCommonElements);
        return exportCommonItems;
    }

    private static MultiChoiceParameter<ExportRowDataFileElement> setRowDataFileElements() {

        ExportRowDataFileElement[] rowDataFileElements = new ExportRowDataFileElement[exportData];
        int position = 0;

        if (peakStatus) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_STATUS;
            position++;
        }
        if (peakMZ) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZ;
            position++;
        }
        if (peakRT) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT;
            position++;
        }
        if (peakRTStart) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT_START;
            position++;
        }
        if (peakRTEnd) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT_END;
            position++;
        }
        if (peakDurationTime) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_DURATION;
            position++;
        }
        if (peakHeight) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_HEIGHT;
            position++;
        }
        if (peakArea) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_AREA;
            position++;
        }
        if (peakCharge) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_CHARGE;
            position++;
        }
        if (peakDataPoints) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_DATAPOINTS;
            position++;
        }
        if (peakFWHM) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_FWHM;
            position++;
        }
        if (peakTailingFactor) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_TAILINGFACTOR;
            position++;
        }
        if (peakAsymmetryFactor) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_ASYMMETRYFACTOR;
            position++;
        }
        if (peakMzMin) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZMIN;
            position++;
        }
        if (peakMzMax) {
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZMAX;
        }

        MultiChoiceParameter<ExportRowDataFileElement> exportDataFileItems = new MultiChoiceParameter<>(
                "Export data file elements",
                "Selection of peak's elements to export",
                ExportRowDataFileElement.values());

        exportDataFileItems.setValue(rowDataFileElements);
        return exportDataFileItems;
    }
}
