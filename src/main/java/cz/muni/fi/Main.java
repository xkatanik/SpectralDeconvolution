package cz.muni.fi;

import com.google.common.collect.Range;
import javafx.beans.binding.ListBinding;
import javafx.collections.ObservableList;
import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.main.MZmineConfiguration;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.main.impl.MZmineConfigurationImpl;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.*;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.ExportRowCommonElement;
import net.sf.mzmine.modules.peaklistmethods.io.csvexport.ExportRowDataFileElement;
import net.sf.mzmine.modules.peaklistmethods.io.xmlimport.XMLImportParameters;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.adap3decompositionV1_5.ADAP3DecompositionV1_5Parameters;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.adap3decompositionV1_5.ADAP3DecompositionV1_5Task;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.NetCDFReadTask;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceParameter;
import net.sf.mzmine.parameters.parametertypes.ranges.ListDoubleRangeParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsSelection;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsSelectionType;
import net.sf.mzmine.project.impl.MZmineProjectImpl;
import net.sf.mzmine.project.impl.ProjectManagerImpl;
import net.sf.mzmine.project.impl.RawDataFileImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Kristian Katanik
 */
public class Main{

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

        Integer i = 0;
        String inputFileName = null;
        String rawData = null;
        String outputFileName = null;
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
        Boolean id = true;
        Boolean mz = true;
        Boolean rt = true;
        Boolean mainID = true;
        Boolean allIDs = true;
        Boolean mainIdDetails = true;
        Boolean comment = true;
        Boolean numberOfDetectedPeaks = true;
        Boolean peakStatus = true;
        Boolean peakMZ = true;
        Boolean peakRT = true;
        Boolean peakRTStart = true;
        Boolean peakRTEnd = true;
        Boolean peakDurationTime = true;
        Boolean peakHeight = true;
        Boolean peakArea = true;
        Boolean peakCharge = true;
        Boolean peakDataPoints = true;
        Boolean peakFWHM = true;
        Boolean peakTailingFactor = true;
        Boolean peakAsymmetryFactor = true;
        Boolean peakMZmin = true;
        Boolean peakMZmax = true;
        Boolean quantitationResults = true;
        String identificationSeparator = ";";

        int exportCommon = 8;
        int exportData = 15;


        while(i < args.length){
            switch (args[i]){
                case "-inputFile":
                    inputFileName = args[i+1];
                    i += 2;
                    break;
                case "-outputFile":
                    outputFileName = args[i+1];
                    i += 2;
                    break;
                case "-rawDataFile":
                    rawData = args[i+1];
                    i += 2;
                    break;
                case "-clusterDistance":
                    try {
                        clusterDistance = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -clusterDistance parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-clusterSize":
                    try {
                        clusterSize = Integer.parseInt(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -clusterSize parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-clusterIntensity":
                    try {
                        clusterIntensity = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -clusterIntensity parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-findSharedPeaks":
                    if(args[i+1].equals("f") || args[i+1].equals("false")){
                        findSharedPeaks = false;
                    } else if (args[i+1].equals("t") || args[i+1].equals("true")){
                        findSharedPeaks = true;
                    } else{
                        System.err.println("Wrong format of -findSharedPeaks parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-edgeToHeightRatio":
                    try {
                        edgeToHeightRatio = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -edgeToHeightRatio parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-deltaToHeightRatio":
                    try {
                        deltaToHeightRatio = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -deltaToHeightRatio parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-sharpness":
                    try {
                        sharpness = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -sharpness parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-shapeSimilarityTolerance":
                    try {
                        shapeSimilarityTolerance = Double.parseDouble(args[i+1]);
                    } catch (Exception e){
                        System.err.println("Wrong format of -shapeSimilarityTolerance parameter.");
                        return;
                    }
                    i += 2;
                    break;
                case "-sharpnessModel":
                    mzValueModelPeak = false;
                    i++;
                    break;
                case "-mzModel":
                    i++;
                    break;
                case "-excludeMzValues":
                    while(i+1 < args.length && !args[i+1].contains("-")) {
                        try {
                            Range<Double> range = Range.closed(Double.parseDouble(args[i + 1]), Double.parseDouble(args[i + 1]));
                            excludeMzValues.add(range);
                        } catch (Exception e) {
                            System.err.println("Wrong format of -excludeMzValues parameter.");
                            return;
                        }
                        i++;
                    }
                    i++;
                    break;

                //Export arguments
                case "-fieldSeparator":
                    fieldSeparator = args[i+1];
                    i += 2;
                    break;
                case "-identificationSeparator":
                    identificationSeparator = args[i+1];
                    i += 2;
                    break;
                case "-id":
                    id = false;
                    i++;
                    exportCommon--;
                    break;
                case "-rt":
                    rt = false;
                    i++;
                    exportCommon--;
                    break;
                case "-mz":
                    mz = false;
                    i++;
                    exportCommon--;
                    break;
                case "-mainID":
                    mainID = false;
                    i++;
                    exportCommon--;
                    break;
                case "-allID":
                    allIDs = false;
                    i++;
                    exportCommon--;
                    break;
                case "-mainIdDetails":
                    mainIdDetails = false;
                    i++;
                    exportCommon--;
                    break;
                case "-comment":
                    comment = false;
                    i++;
                    exportCommon--;
                    break;
                case "-numberOfDetectedPeaks":
                    numberOfDetectedPeaks = false;
                    i++;
                    exportCommon--;
                    break;
                case "-peakStatus":
                    peakStatus = false;
                    i++;
                    exportData--;
                    break;
                case "-peakMZ":
                    peakMZ = false;
                    i++;
                    exportData--;
                    break;
                case "-peakRT":
                    peakRT = false;
                    i++;
                    exportData--;
                    break;
                case "-peakRTStart":
                    peakRTStart = false;
                    i++;
                    exportData--;
                    break;
                case "-peakRTEnd":
                    peakRTEnd = false;
                    i++;
                    exportData--;
                    break;
                case "-peakDurationTime":
                    peakDurationTime = false;
                    i++;
                    exportData--;
                    break;
                case "-peakHeight":
                    peakHeight = false;
                    i++;
                    exportData--;
                    break;
                case "-peakArea":
                    peakArea = false;
                    i++;
                    exportData--;
                    break;
                case "-peakCharge":
                    peakCharge = false;
                    i++;
                    exportData--;
                    break;
                case "-peakDataPoints":
                    peakDataPoints = false;
                    i++;
                    exportData--;
                    break;
                case "-peakFWHM":
                    peakFWHM = false;
                    i++;
                    exportData--;
                    break;
                case "-peakTailingFactor":
                    peakTailingFactor = false;
                    i++;
                    exportData--;
                    break;
                case "-peakAsymmetryFactor":
                    peakAsymmetryFactor = false;
                    i++;
                    exportData--;
                    break;
                case "-peakMZmin":
                    peakMZmin = false;
                    i++;
                    exportData--;
                    break;
                case "-peakMZmax":
                    peakMZmax = false;
                    i++;
                    exportData--;
                    break;
                case "-quantitationResults":
                    quantitationResults = false;
                    i++;
                    break;
                case "-help":
                    System.out.println("Hierarchical Clustering (Spectral deconvolution).\n" +
                            "This method combines peaks into analytes and constructs fragmentation spectrum for each analyte.\n"+
                            "\n" +
                            "Required parameters:\n" +
                            "\t-inputFile | Name or path of input file after ADAP Chromatogram builder, ending with .MPL\n" +
                            "\t-outputFile | Name or path of output file. File name must end with .CSV\n" +
                            "\t-rawDataFile | Name or path of input file after Mass detection, ending with .CDF\n" +
                            "\n" +
                            "Optional parameters:\n" +
                            "\t-clusterDistance | Minimum distance between any two clusters.\n" +
                            "\t\t[default 0.01]\n" +
                            "\t-clusterSize | Minimum size of a cluster.\n" +
                            "\t\t[default 2]\n" +
                            "\t-clusterIntensity | If the highest peak in a cluster has the intensity below\n" +
                            "\t\tMinimum Cluster Intensity,the cluster is removed.\n" +
                            "\t\t[default 500.0]\n" +
                            "\t-findSharedPeaks | If selected, peaks are marked as Shared if they are composed of two or more peaks.\n" +
                            "\t\t[default false]\n" +
                            "\t-edgeToHeightRatio | A peak is considered shared if its edge-to-height ratio is below this parameter\n" +
                            "\t\t[default 0.3]\n" +
                            "\t-deltaToHeightRatio | A peak is considered shared if its delta (difference between the edges)-to-height\n" +
                            "\t\tratio is below this parameter.\n" +
                            "\t\t[default 0.2]\n" +
                            "\t-sharpness | Minimum sharpness that the model peak can have.\n" +
                            "\t\t[default 10]\n" +
                            "\t-shapeSimilarityTolerance | Shape-similarity threshold is used to find similar peaks.(0..90)\n" +
                            "\t\t[default 18]\n" +
                            "\t-sharpnessModel or -mzModel\n" +
                            "\t\tCriterion to choose a model peak in a cluster: either peak with the highest m/z-value or with the highest sharpness.\n" +
                            "\t\t[default from 0.00 to 0.10]\n" +
                            "\t-mzModel | Upper and lower bounds of retention times to be used for setting the wavelet scales.\n" +
                            "\t\tChoose a range that that similar to the range of peak widths expected to be found from the data.\n" +
                            "\t\t[default from 0.00 to 0.10]\n" +
                            "\t-excludeMzValues | M/z-values to exclude while selecting model peak.\n" +
                            "\t\tDivide values with space, etc. \"12.0 15.0\"\n" +
                            "\t\t[default none]\n" +
                            "\n" +
                            "Default value of all following parameters is TRUE, so output file will contain all parameters.\n" +
                            "Use following parameters to exclude them from output file.\n" +
                            "Export parameters:\n" +
                            "\t-fieldSeparator | Field separator [default , ]\n" +
                            "\t-identificationSeparator | Identification separator [default ; ]\n" +
                            "\t-quantitationResults | Export quantitation results and other information\n" +
                            "\t Export common elements:\n" +
                            "\t\t-id | Export row ID\n" +
                            "\t\t-mz | Export row m/z\n" +
                            "\t\t-rt | Export row retention time\n" +
                            "\t\t-mainID | Export row identity (main ID)\n" +
                            "\t\t-allID | Export row identity (all IDs)\n" +
                            "\t\t-mainIdDetails | Export row identity (main ID + details)\n" +
                            "\t\t-comment | Export row comment\n" +
                            "\t\t-numberOfDetectedPeaks | Export row number of detected peaks\n" +
                            "\t Export data file elements:\n" +
                            "\t\t-peakStatus | Peak status\n" +
                            "\t\t-peakMZ | Peak m/z\n" +
                            "\t\t-peakRT | Peak RT\n" +
                            "\t\t-peakRTStart | Peak RT start\n" +
                            "\t\t-peakRTEnd | Peak RT end\n" +
                            "\t\t-peakDurationTime | Peak duration time\n" +
                            "\t\t-peakHeight | Peak height\n" +
                            "\t\t-peakArea | Peak area\n" +
                            "\t\t-peakCharge | Peak charge\n" +
                            "\t\t-peakDataPoints | Peak # data points\n" +
                            "\t\t-peakFWHM | Peak FWHM\n" +
                            "\t\t-peakTailingFactor | Peak tailing factor\n" +
                            "\t\t-peakAsymmetryFactor | Peak assymetry factor\n" +
                            "\t\t-peakMZmin | Peak m/z min\n" +
                            "\t\t-peakMZmax | Peak m/z max\n" +
                            "\n");
                    return;
                default:
                    i++;
                    break;
            }
        }

        //Reading 2 input files

        File inputFile;
        try {
            inputFile = new File(inputFileName);
        } catch (Exception e) {
            System.out.println("Unable to load input file.");
            return;
        }

        File rawInputFile;
        try {
            rawInputFile = new File(rawData);
        } catch (Exception e) {
            System.out.println("Unable to load raw file.");
            return;
        }

        File outputFile;
        try {
            outputFile = new File(outputFileName);
        } catch(Exception e){
            System.out.println("Unable to create/load output file.");
            return;
        }

        if(!inputFile.exists() || inputFile.isDirectory() || !rawInputFile.exists() || rawInputFile.isDirectory()){
            System.err.println("Unable to load input/raw file.");
            return;
        }

        final MZmineProject mZmineProject = new MZmineProjectImpl();
        RawDataFileImpl rawDataFile = null;
        try {
            rawDataFile = new RawDataFileImpl(inputFile.getName());
        } catch (IOException e) {
            System.err.println("Cant load input data file.");
            return;
        }

        //code for raw data
        RawDataFileImpl rawDataFile2 = null;
        try {
            rawDataFile2 = new RawDataFileImpl(rawInputFile.getName());
        } catch (IOException e) {
            System.err.println("Unable to open raw data file.");
            return;
        }


        NetCDFReadTask netCDFReadTask = new NetCDFReadTask(mZmineProject,rawInputFile,rawDataFile2);
        netCDFReadTask.run();
        mZmineProject.addFile(rawDataFile2);


        XMLImportParameters xmlImportParameters = new XMLImportParameters();
        xmlImportParameters.getParameter(XMLImportParameters.filename).setValue(inputFile);
        cz.muni.fi.XMLImportTask xmlImportTask = new cz.muni.fi.XMLImportTask(mZmineProject,xmlImportParameters);
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


        ADAP3DecompositionV1_5Parameters parameters = new ADAP3DecompositionV1_5Parameters();
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_DISTANCE).setValue(clusterDistance);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_INTENSITY).setValue(clusterIntensity);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_CLUSTER_SIZE).setValue(clusterSize);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.MIN_MODEL_SHARPNESS).setValue(sharpness);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.DELTA_TO_HEIGHT_RATIO).setValue(deltaToHeightRatio);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.EDGE_TO_HEIGHT_RATIO).setValue(edgeToHeightRatio);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.SHAPE_SIM_THRESHOLD).setValue(shapeSimilarityTolerance);
        parameters.getParameter(ADAP3DecompositionV1_5Parameters.USE_ISSHARED).setValue(findSharedPeaks);
        if(mzValueModelPeak){
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MODEL_PEAK_CHOICE).setValue("M/z value");
        } else{
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MODEL_PEAK_CHOICE).setValue("Shaprness");
        }

        if(excludeMzValues.isEmpty()){
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MZ_VALUES).setValue(Collections.<Range<Double>>emptyList());
        } else{
            parameters.getParameter(ADAP3DecompositionV1_5Parameters.MZ_VALUES).setValue(excludeMzValues);
        }



        cz.muni.fi.ADAP3DecompositionV1_5Task task =
                new cz.muni.fi.ADAP3DecompositionV1_5Task(mZmineProject,mZmineProject.getPeakLists()[0],parameters);
        task.run();


        PeakListsParameter peakListsParameter = new PeakListsParameter();
        PeakListsSelection peakListsSelection1 = new PeakListsSelection();
        peakListsSelection1.setSpecificPeakLists(new PeakList[]{mZmineProject.getPeakLists()[1]});
        peakListsSelection1.setSelectionType(PeakListsSelectionType.SPECIFIC_PEAKLISTS);
        peakListsParameter.setValue(peakListsSelection1);


        peakListsSelection.setSelectionType(PeakListsSelectionType.SPECIFIC_PEAKLISTS);
        CSVExportParameters csvExportParameters = new CSVExportParameters();
        csvExportParameters.getParameter(CSVExportParameters.filename).setValue(outputFile);
        csvExportParameters.getParameter(CSVExportParameters.peakLists).setValue(peakListsParameter.getValue());
        csvExportParameters.getParameter(CSVExportParameters.fieldSeparator).setValue(fieldSeparator);
        csvExportParameters.getParameter(CSVExportParameters.idSeparator).setValue(identificationSeparator);
        csvExportParameters.getParameter(CSVExportParameters.exportAllPeakInfo).setValue(quantitationResults);

        ExportRowCommonElement[] rowCommonElements = new ExportRowCommonElement[exportCommon];
        int position = 0;
        if(id){
            rowCommonElements[position] = ExportRowCommonElement.ROW_ID;
            position++;
        }
        if(rt){
            rowCommonElements[position] = ExportRowCommonElement.ROW_RT;
            position++;
        }
        if(mainID){
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY;
            position++;
        }
        if(mainIdDetails){
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY_DETAILS;
            position++;
        }
        if(mz){
            rowCommonElements[position] = ExportRowCommonElement.ROW_MZ;
            position++;
        }
        if(allIDs){
            rowCommonElements[position] = ExportRowCommonElement.ROW_IDENTITY_ALL;
            position++;
        }
        if(comment){
            rowCommonElements[position] = ExportRowCommonElement.ROW_COMMENT;
            position++;
        }
        if(numberOfDetectedPeaks){
            rowCommonElements[position] = ExportRowCommonElement.ROW_PEAK_NUMBER;
            position++;
        }
        MultiChoiceParameter<ExportRowCommonElement> exportCommonItems = new MultiChoiceParameter<ExportRowCommonElement>(
                "Export common elements", "Selection of row's elements to export",
                ExportRowCommonElement.values());

        exportCommonItems.setValue(rowCommonElements);

        ExportRowDataFileElement[] rowDataFileElements = new ExportRowDataFileElement[exportData];
        position = 0;

        if(peakStatus){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_STATUS;
            position++;
        }
        if(peakMZ){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZ;
            position++;
        }
        if(peakRT){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT;
            position++;
        }
        if(peakRTStart){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT_START;
            position++;
        }
        if(peakRTEnd){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_RT_END;
            position++;
        }
        if(peakDurationTime){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_DURATION;
            position++;
        }
        if(peakHeight){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_HEIGHT;
            position++;
        }
        if(peakArea){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_AREA;
            position++;
        }
        if(peakCharge){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_CHARGE;
            position++;
        }
        if(peakDataPoints){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_DATAPOINTS;
            position++;
        }
        if(peakFWHM){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_FWHM;
            position++;
        }
        if(peakTailingFactor){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_TAILINGFACTOR;
            position++;
        }
        if(peakAsymmetryFactor){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_ASYMMETRYFACTOR;
            position++;
        }
        if(peakMZmin){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZMIN;
            position++;
        }
        if(peakMZmax){
            rowDataFileElements[position] = ExportRowDataFileElement.PEAK_MZMAX;
            position++;
        }

        MultiChoiceParameter<ExportRowDataFileElement> exportDataFileItems = new MultiChoiceParameter<>(
                "Export data file elements",
                "Selection of peak's elements to export",
                ExportRowDataFileElement.values());

        exportDataFileItems.setValue(rowDataFileElements);


        csvExportParameters.getParameter(CSVExportParameters.exportCommonItems).setValue(exportCommonItems.getValue());
        csvExportParameters.getParameter(CSVExportParameters.exportDataFileItems).setValue(exportDataFileItems.getValue());

        CSVExportTask csvExportTask = new CSVExportTask(csvExportParameters);
        csvExportTask.run();






    }
}
