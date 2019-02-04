package aUtiliti;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;

public class DatasetSynInfo {

	private String datasetFolder = "/ConceptDriftDatasets/Synthetic/";
	private static int numFile = 100;

	private static String[] datasetPathArray = new String[10 * numFile];
	private String[] datasetSettingArray = new String[10 * numFile];

	private String[] datasetName = new String[] { "LEDa", "LEDg", "SEAa", "SEAg", "AGRa", "AGRg", "RTG", "RBF", "RBFr",
			"HYP" };
	private String[] datasetClassIndex = new String[] { "25", "25", "4", "4", "10", "10", "11", "11", "11", "11" };
	private Integer[] datasetNumClass = new Integer[] { 10, 10, 2, 2, 2, 2, 2, 5, 5, 2 };
	private int numDataType = datasetName.length;

	public DatasetSynInfo(String _path) {
		datasetFolder = _path;
		for (int i = 0; i < numDataType; i++) {
			for (int j = 0; j < numFile; j++) {
				datasetPathArray[i * numFile + j] = datasetFolder + datasetName[i] + "/" + datasetName[i] + j + ".arff";
				datasetSettingArray[i * numFile + j] = "(ArffFileStream -f " + datasetPathArray[i * numFile + j]
						+ " -c " + datasetClassIndex[i] + ")";
			}
		}
	}

	public DatasetSynInfo() {

		for (int i = 0; i < numDataType; i++) {
			for (int j = 0; j < numFile; j++) {
				datasetPathArray[i * numFile + j] = datasetFolder + datasetName[i] + "/" + datasetName[i] + j + ".arff";
				datasetSettingArray[i * numFile + j] = "(ArffFileStream -f " + datasetPathArray[i * numFile + j]
						+ " -c " + datasetClassIndex[i] + ")";
			}
		}
	}

	public String getDatasetType(int _datasetID) {
		return datasetName[_datasetID / numFile];
	}

	public String getDatasetName(int _datasetID) {
		return datasetName[_datasetID / numFile] + _datasetID % numFile;
	}

	public String getDatasetPath(int _datasetID) {
		return datasetPathArray[_datasetID];
	}

	public int getDatasetNumClass(int _datasetID) {
		return datasetNumClass[_datasetID / numFile];
	}

	public String getDatasetSetting(int _datasetID) {
		return datasetSettingArray[_datasetID];
	}

	public int getNumDataset() {
		return datasetName.length;
	}

	public static Instances loadData(int _dataID) {

		Instances dataset = DatasetSynInfo.loadData(datasetPathArray[_dataID]);
		return dataset;
	}

	public static Instances loadData(String _dataPath) {

		System.out.println("Loading Data From");
		System.out.println("\t" + _dataPath);
		Instances data = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(_dataPath));
			data = new Instances(reader);
			data.setClassIndex(data.numAttributes() - 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Loading Finished");
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

}
