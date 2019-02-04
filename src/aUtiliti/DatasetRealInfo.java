package aUtiliti;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;

public class DatasetRealInfo {

	private String datasetFolder = "/ConceptDriftDatasets/Synthetic/";
	private static int numFile = 1;

	private static String[] datasetPathArray = new String[10 * numFile];
	private String[] datasetSettingArray = new String[10 * numFile];

	private String[] datasetName = new String[] { "elecNorm", "weather", "spam_corpus_x2_feature_selected", "usenet1_2/usenet1", "usenet1_2/usenet2", "airline",
			"covtype" };
	private String[] datasetClassIndex = new String[] { "9", "9", "501", "100", "100", "7","54" };
	private Integer[] datasetNumClass = new Integer[] { 2, 2, 2, 2, 2, 2, 7 };
	private int numDataType = datasetName.length;

	public DatasetRealInfo(String _path) {
		datasetFolder = _path;
		for (int i = 0; i < numDataType; i++) {
			for (int j = 0; j < numFile; j++) {
				datasetPathArray[i * numFile + j] = datasetFolder + datasetName[i] + "/" + datasetName[i]  + ".arff";
				datasetSettingArray[i * numFile + j] = "(ArffFileStream -f " + datasetPathArray[i * numFile + j]
						+ " -c " + datasetClassIndex[i] + ")";
			}
		}
	}

	public DatasetRealInfo() {

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

		Instances dataset = DatasetRealInfo.loadData(datasetPathArray[_dataID]);
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
