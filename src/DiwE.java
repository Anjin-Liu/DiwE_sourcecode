import aUtiliti.DatasetRealInfo;
import aUtiliti.DatasetSynInfo;
import aUtiliti.ldd_Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sizeof.agent.SizeOfAgent;
import weka.core.Instance;
import weka.core.Instances;

public class DiwE {
	protected static ArrayList<RegionSet> RegionSetList = new ArrayList<RegionSet>();
	public static double[] PhiArray;// = new double[] { 0.025, 0.05, 0.075, 0.1, 0.125, 0.15, 0.175, 0.2, 0.225,
									// 0.25 };
	private static int the_r;// = 10;
	public static int[] MaxDivPhiArray;// = new int[the_r];
	protected static int maxWin = 1000;
	protected static double alpha = 0.01;
	protected static Instances dataset;
	protected static ArrayList<Map<Integer, Double>> tempID_DistMapList;
	protected static int tempClassValue;
	protected static int numClass;
	protected static double[] knnRatioWeight;
	static ArrayList<Double> prediction;
	public static long start;
	public static long end;
	public static double accuracy;
	public static double pre0;
	public static double rec0;
	public static double pre1;
	public static double rec1;
	public static long runTime;
	public static long memoryAverage;

	private static ArrayList<int[]> combinationList = new ArrayList<int[]>();
	private static ArrayList<ArrayList<Double>> expProbList = new ArrayList<ArrayList<Double>>();
	private static ArrayList<ArrayList<Double>> expValuList = new ArrayList<ArrayList<Double>>();
	private static ArrayList<Integer> maxDiversityID = new ArrayList<Integer>();
	public static DatasetSynInfo datasetSyntInfo = new DatasetSynInfo(
			"D:/Anjin/GithubProjects/ConceptDriftDatasets/Synthetic/");
	public static DatasetRealInfo datasetRealInfo = new DatasetRealInfo(
			"D:/Anjin/GithubProjects/ConceptDriftDatasets/RealWorld/Classification/");
	public static int datasetID = 4;
	static {
		tempID_DistMapList = new ArrayList<Map<Integer, Double>>();
		prediction = new ArrayList<Double>();
		pre0 = 0.0;
		rec0 = 0.0;
		pre1 = 0.0;
		rec1 = 0.0;
		memoryAverage = 0;
	}

	public static void main(String[] args) {
		int phiSize = 20;
		double phiMax = 0.5;
		boolean randomSelect = false;
		the_r = 10;
		MaxDivPhiArray = new int[the_r];
		PhiArray = new double[phiSize];
		for (int i = 1; i < phiSize + 1; i++) {
			PhiArray[i - 1] = i * phiMax / phiSize;
		}
		// PhiArray = new double[] {0.05,0.1,0.15,0.2,0.25};
		// PhiArray = new double[]
		// {0.03,0.05,0.04,0.06,0.09,0.1,0.12,0.15,0.18,0.2,0.21,0.25,0.27,0.3,0.35,0.4,0.45};

		knnRatioWeight = new double[PhiArray.length];
		int rnd_seed = 0;

		if (args.length == 3) {
			// System.out.println("Yes");
			// System.exit(0);
			datasetID = Integer.parseInt(args[0]);
			randomSelect = Boolean.parseBoolean(args[1]);
			rnd_seed = Integer.parseInt(args[2]);
			dataset = datasetRealInfo.loadData(datasetID);
		} else if (args.length == 4) {

			datasetID = Integer.parseInt(args[0]);
			randomSelect = Boolean.parseBoolean(args[1]);
			rnd_seed = Integer.parseInt(args[2]);
			dataset = datasetSyntInfo.loadData(datasetID);

		} else {
			System.out.println("Default Settings");
			dataset = datasetRealInfo.loadData(datasetID);

		}

		List<Integer> solution = new ArrayList<>();
		List<Integer> sorted_rnd = new ArrayList<>();
		for (int i = 0; i < PhiArray.length; i++) {
			solution.add(i);
		}

		numClass = dataset.numClasses();
		maxWin = maxWin / numClass;
		long startTime = System.currentTimeMillis();
		int _numKnnRatio = 0;
		while (_numKnnRatio < PhiArray.length) {
			tempID_DistMapList.add(new HashMap());
			tempID_DistMapList.get(_numKnnRatio).put(0, 0.0);
			int _numClass = 0;
			while (_numClass < numClass) {
				RegionSetList.add(new RegionSet(maxWin, PhiArray[_numKnnRatio], alpha));
				++_numClass;
			}
			++_numKnnRatio;
		}
		// prediction.add(dataset.instance(0).classValue());
		prediction.add(0.0);

		updateKNNMap(0, tempID_DistMapList);
		// updateExpDistribution();

		for (int t = 1; t < dataset.numInstances(); t++) {
			// for (int t = 1; t < 10; t++) {
			// memoryAverage += SizeOfAgent.sizeOf(RegionSetList.get(0));
			// memoryAverage += SizeOfAgent.sizeOf(RegionSetList.get(1));
			prediction.add(classifyInstance(t, dataset.instance(t)));

			// updateExpDistribution();
			System.out.println(t + "/" + dataset.numInstances());

			if (randomSelect) {

				Collections.shuffle(solution, new Random(t + rnd_seed));
				MaxDivPhiArray = new int[the_r];
				// for (int i = 0; i < the_r; i++) {
				// sorted_rnd.add(solution.get(i));

				// }
				// Collections.sort(sorted_rnd);
				for (int i = 0; i < the_r; i++) {
					MaxDivPhiArray[i] = solution.get(i);
					System.out.print(MaxDivPhiArray[i] + " ");
				}

				System.out.println("");
			} else {
				if (the_r < PhiArray.length) {
					maxDiversity();
				} else {
					MaxDivPhiArray = new int[the_r];
					for (int i = 0; i < the_r; i++) {
						MaxDivPhiArray[i] = i;
					}
				}
			}
		}

		long endTime = System.currentTimeMillis();
		int[][] cm = ldd_Utils.makeConfusionMatrix(prediction, dataset);
		accuracy = ldd_Utils.printAPR(cm);
		end = System.currentTimeMillis();
		runTime = end - start;
		System.out.println("Running time: " + (endTime - startTime));
		// System.out.println(
		// "Time: " + runTime + ", Memory: " + (double) memoryAverage * 1.0 / 1024.0 /
		// 1024.0 / 1024.0 + " GB");
	}

	private static void updateExpDistribution() {

		int maxDistInfo = 15;
		int maxExpProbSize = 1;
		for (int i = 1; i <= maxDistInfo; i++) {
			maxExpProbSize = maxExpProbSize + i + 1;
		}
		System.out.println(maxExpProbSize);
		if (expProbList.size() == 0) {
			ArrayList<Double> emptyList = new ArrayList<Double>();
			emptyList.add(1.0);
			for (int phi_1 = 0; phi_1 < PhiArray.length - 1; phi_1++) {
				for (int phi_2 = phi_1 + 1; phi_2 < PhiArray.length; phi_2++) {
					expProbList.add(emptyList);
					expValuList.add(emptyList);
				}
			}
		} else if (expProbList.size() > maxExpProbSize) {

		} else {

		}

	}

	protected static void maxDiversity() {

		int p1_region_idx, p2_region_idx;
		double p1, p2, mu;
		double[][] RDDMatrix = new double[PhiArray.length][PhiArray.length];
		double totalWeightDiff = 0;
		double[] RDD;
		// P1 < P2
		for (int p1_idx = 0; p1_idx < PhiArray.length - 1; p1_idx++) {
			for (int p2_idx = p1_idx + 1; p2_idx < PhiArray.length; p2_idx++) {
				p1 = PhiArray[p1_idx];
				p2 = PhiArray[p2_idx];
				// totalInst = 0;
				totalWeightDiff = 0;
				mu = 1.0 / (2 - p2) - 1.0 / (2 - p1);
				for (int y_idx = 0; y_idx < numClass; y_idx++) {
					p1_region_idx = p1_idx * numClass + y_idx;
					p2_region_idx = p2_idx * numClass + y_idx;
					RDD = RDD(RegionSetList.get(p1_region_idx).getKnnWeight(),
							RegionSetList.get(p2_region_idx).getKnnWeight(), mu);

					totalWeightDiff += RDD[0] / RDD[1];
					// totalInst += RegionSetList.get(p1_region_idx).getKnnWeight()
				}
				RDDMatrix[p1_idx][p2_idx] = 1 - totalWeightDiff / numClass;
				RDDMatrix[p2_idx][p1_idx] = 1 - totalWeightDiff / numClass;
//				System.out.println("     " + totalWeightDiff / numClass);

//				if (totalWeightDiff > 0) {
//					mu = 1.0 / (2 - p2) - 1.0 / (2 - p1);
//					RDDMatrix[p1_idx][p2_idx] = Math.abs(totalWeightDiff - mu) / mu;
//					RDDMatrix[p2_idx][p1_idx] = RDDMatrix[p1_idx][p2_idx];
//					//System.out.println(p1 + "-" + p2 + ": " + Math.abs(totalWeightDiff - mu) / mu);
//				} else {
//					RDDMatrix[p1_idx][p2_idx] = 0;
//					RDDMatrix[p2_idx][p1_idx] = RDDMatrix[p1_idx][p2_idx];
//					//System.out.println(p1 + "-" + p2 + ": 0.0");
//				}
			}
		}

		System.out.println(calMaxDivergence(RDDMatrix));
	}

	private static double calMaxDivergence(double[][] RDDMatrix) {

		int n = RDDMatrix.length;
		int r = the_r;
		double[] maxDivergenceEachRow = new double[n];
		int[] arr = new int[n];
		for (int i = 0; i < n; i++) {
			arr[i] = i;
		}
		combinationList.clear();
		printCombination(arr, n, r);

		int maxCombId = 0;
		double maxDivergence = 0;
		double temDivergence = 0;
		int[] comb;
		for (int i = 0; i < combinationList.size(); i++) {
			comb = combinationList.get(i);
			temDivergence = 0;
			for (int row = 0; row < comb.length - 1; row++) {
				for (int column = row + 1; column < comb.length; column++) {
					temDivergence += RDDMatrix[comb[row]][comb[column]];
				}
			}

			if (temDivergence > maxDivergence) {
				maxDivergence = temDivergence;
				maxCombId = i;
			}
		}
		System.out.print("  ");
		MaxDivPhiArray = new int[the_r];
		int counter = 0;
		for (int i : combinationList.get(maxCombId)) {
			MaxDivPhiArray[counter] = i;
			counter++;
			System.out.print(i + " ");
		}
		System.out.print("\n");
		return maxDivergence * 2 / (r * (r - 1));

	}

	static void combinationUtil(int arr[], int data[], int start, int end, int index, int r) {
		if (index == r) {
			combinationList.add(new int[r]);
			for (int j = 0; j < r; j++) {
				combinationList.get(combinationList.size() - 1)[j] = data[j];
			}
			// System.out.print(data[j] + " ");
			// System.out.println("");
			return;
		}
		for (int i = start; i <= end && end - i + 1 >= r - index; i++) {
			data[index] = arr[i];
			combinationUtil(arr, data, i + 1, end, index + 1, r);
		}
	}

	static void printCombination(int arr[], int n, int r) {

		int data[] = new int[r];
		combinationUtil(arr, data, 0, n - 1, 0, r);
	}

	protected static double[] RDD(Map<Integer, Double> p1_map, Map<Integer, Double> p2_map, double _mu) {

		double[] RDD = new double[2];
		for (int i : p1_map.keySet()) {
			if (p2_map.keySet().contains(i)) {
				// RDD[0] = RDD[0] + (p2_map.get(i) - p1_map.get(i) - _mu + 1) / 2;
				RDD[0] = RDD[0] + 1;
				RDD[1]++;
			} else {
				// RDD[0] = RDD[0] + (0 - p1_map.get(i) - _mu + 1) / 2;
				RDD[1]++;
			}
		}
		for (int i : p2_map.keySet()) {
			if (!p1_map.keySet().contains(i)) {
				// RDD[0] = RDD[0] + (p2_map.get(i) - 0 - _mu + 1) / 2;
				RDD[1]++;
			}
		}

		return RDD;
	}

	protected static void updateKNNMap(int _t, ArrayList<Map<Integer, Double>> _idDistMap) {
		tempClassValue = (int) dataset.instance(_t).classValue();
		int numKnnRatio = 0;
		while (numKnnRatio < PhiArray.length) {
			int knnMapList_index = numClass * numKnnRatio + tempClassValue;
			RegionSetList.get(knnMapList_index).insertItem(_t, _idDistMap.get(numKnnRatio));
			++numKnnRatio;
		}
	}

	public static void reset() {
		maxWin = 1000;
		RegionSetList.clear();
		// tempID_DistMapList.clear();
		prediction.clear();

		combinationList.clear();
		expProbList.clear();
		expValuList.clear();
		maxDiversityID.clear();

		tempID_DistMapList = new ArrayList<Map<Integer, Double>>();
		prediction = new ArrayList<Double>();
		pre0 = 0.0;
		rec0 = 0.0;
		pre1 = 0.0;
		rec1 = 0.0;
		accuracy = 0;
		memoryAverage = 0;
	}

	protected static double classifyInstance(int _t, Instance _inst) {
		ArrayList<double[]> voteList = new ArrayList<double[]>();
		ArrayList<ArrayList<Integer>> classIDList = new ArrayList<ArrayList<Integer>>();
		ArrayList distList = new ArrayList();
		ArrayList idDistMap = new ArrayList();
		Map mergedIdDistmap = new HashMap();
		int classifyKvalue = 5;
		int classifyKcounter = 0;
		int[] classifyKnnID = new int[classifyKvalue];
		double[] classifyKnnDist = new double[classifyKvalue];
		// int numKnnRatio = 0;
		for (int numKnnRatio : MaxDivPhiArray) {
			// while (numKnnRatio < PhiArray.length) {
			int knnMapList_index;
			classIDList.clear();
			distList.clear();
			idDistMap.clear();
			mergedIdDistmap.clear();
			int _numClass = 0;
			while (_numClass < numClass) {
				knnMapList_index = numClass * numKnnRatio + _numClass;
				classIDList.add(RegionSetList.get(knnMapList_index).getIDList());
				idDistMap.add(new HashMap());
				Iterator iterator = ((ArrayList) classIDList.get(_numClass)).iterator();
				while (iterator.hasNext()) {
					int _id = (Integer) iterator.next();
					((Map) idDistMap.get(_numClass)).put(_id, calDist(_inst, dataset.instance(_id)));
					mergedIdDistmap.put(_id, (Double) ((Map) idDistMap.get(_numClass)).get(_id));
				}
				if ((double) _numClass == _inst.classValue()) {
					((Map) idDistMap.get(_numClass)).put(_t, 0.0);
					RegionSetList.get(knnMapList_index).insertItem(_t, (Map) idDistMap.get(_numClass));
				}
				++_numClass;
			}
			mergedIdDistmap = ldd_Utils.sortByValue(mergedIdDistmap);
			classifyKcounter = 0;
			Iterator _id = mergedIdDistmap.keySet().iterator();
			while (_id.hasNext()) {
				int kID;
				classifyKnnID[classifyKcounter] = kID = ((Integer) _id.next()).intValue();
				classifyKnnDist[classifyKcounter] = (Double) mergedIdDistmap.get(kID);
				if (++classifyKcounter == classifyKvalue)
					break;
			}
			double[] vote = new double[numClass];
			_numClass = 0;
			while (_numClass < numClass) {
				knnMapList_index = numClass * numKnnRatio + _numClass;
				int k = 0;
				while (k < classifyKnnID.length) {
					double[] arrd = vote;
					int n = _numClass;
					arrd[n] = arrd[n] + RegionSetList.get(knnMapList_index).getWeight(classifyKnnID[k])
							/ (classifyKnnDist[k] + 1.0E-5);
					++k;
				}
				++_numClass;
			}
			voteList.add(vote);
			++numKnnRatio;
		}
		double[] finalVote = new double[numClass];
		for (double[] _v : voteList) {
			int i = 0;
			while (i < _v.length) {
				double[] arrd = finalVote;
				int n = i;
				arrd[n] = arrd[n] + _v[i];
				++i;
			}
		}
		double maxVoteValue = -1.0;
		int maxVoteId = -1;
		int i = 0;
		while (i < finalVote.length) {
			if (maxVoteValue < finalVote[i]) {
				maxVoteValue = finalVote[i];
				maxVoteId = i;
			}
			++i;
		}
		if (maxVoteId == -1) {
			i = 0;
			while (i < finalVote.length) {
				System.out.print(String.valueOf(finalVote[i]) + " , ");
				++i;
			}
			System.out.print(maxVoteValue);
			System.exit(0);
		}
		return maxVoteId;
	}

	public static double calDist(Instance _i1, Instance _i2) {
		double eucDist = 0.0;
		int i = 0;
		while (i < _i1.numAttributes()) {
			if (i != _i1.classIndex()) {
				eucDist += (_i1.value(i) - _i2.value(i)) * (_i1.value(i) - _i2.value(i));
			}
			++i;
		}
		return Math.sqrt(eucDist);
	}
}
