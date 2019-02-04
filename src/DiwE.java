import aUtiliti.DatasetRealInfo;
import aUtiliti.DatasetSynInfo;
import aUtiliti.ldd_Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import sizeof.agent.SizeOfAgent;
import weka.core.Instance;
import weka.core.Instances;

public class DiwE {
	protected static ArrayList<RegionSet> knnMapList = new ArrayList<RegionSet>();
	public static double[] knnRatioArray = new double[] { 0.05, 0.1, 0.25 };
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

	// public static DatasetSynInfo datasetInfo = new
	// DatasetSynInfo("D:/Anjin/GithubProjects/ConceptDriftDatasets/Synthetic/");
	public static DatasetRealInfo datasetInfo = new DatasetRealInfo(
			"D:/Anjin/GithubProjects/ConceptDriftDatasets/RealWorld/Classification/");
	public static int datasetID = 0;
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

		knnRatioWeight = new double[knnRatioArray.length];
		if (args.length != 1) {
			System.out.println("Default Settings");
		} else {
			datasetID = Integer.parseInt(args[0]);
		}
		dataset = datasetInfo.loadData(datasetID);
		numClass = dataset.numClasses();
		maxWin = maxWin / numClass;
		long startTime = System.currentTimeMillis();
		int _numKnnRatio = 0;
		while (_numKnnRatio < knnRatioArray.length) {
			tempID_DistMapList.add(new HashMap());
			tempID_DistMapList.get(_numKnnRatio).put(0, 0.0);
			int _numClass = 0;
			while (_numClass < numClass) {
				knnMapList.add(new RegionSet(maxWin, knnRatioArray[_numKnnRatio], alpha));
				++_numClass;
			}
			++_numKnnRatio;
		}
		prediction.add(dataset.instance(0).classValue());

		updateKNNMap(0, tempID_DistMapList);

		double averageBuffSize = 0;

		for (int t = 1; t < dataset.numInstances(); t++) {
			memoryAverage += SizeOfAgent.sizeOf(knnMapList.get(0));
			memoryAverage += SizeOfAgent.sizeOf(knnMapList.get(1));
			prediction.add(classifyInstance(t, dataset.instance(t)));

//			averageBuffSize = 0;
//			for (int bInd = 0; bInd < knnMapList.size(); bInd++) {
//				averageBuffSize += knnMapList.get(bInd).getSize();
//			}
			System.out.println(String.valueOf(t) + "/" + dataset.numInstances());
		}

		long endTime = System.currentTimeMillis();
		int[][] cm = ldd_Utils.makeConfusionMatrix(prediction, dataset);
		accuracy = ldd_Utils.printAPR(cm);
		end = System.currentTimeMillis();
		runTime = end - start;
		System.out.println("Running time: " + (endTime - startTime));
		System.out.println(
				"Time: " + runTime + ", Memory: " + (double) memoryAverage * 1.0 / 1024.0 / 1024.0 / 1024.0 + " GB");
	}

	protected static void updateKNNMap(int _t, ArrayList<Map<Integer, Double>> _idDistMap) {
		tempClassValue = (int) dataset.instance(_t).classValue();
		int numKnnRatio = 0;
		while (numKnnRatio < knnRatioArray.length) {
			int knnMapList_index = numClass * numKnnRatio + tempClassValue;
			knnMapList.get(knnMapList_index).insertItem(_t, _idDistMap.get(numKnnRatio));
			++numKnnRatio;
		}
	}

	public static void reset() {
		knnMapList.clear();
		tempID_DistMapList.clear();
		prediction.clear();
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
		int numKnnRatio = 0;
		while (numKnnRatio < knnRatioArray.length) {
			int knnMapList_index;
			classIDList.clear();
			distList.clear();
			idDistMap.clear();
			mergedIdDistmap.clear();
			int _numClass = 0;
			while (_numClass < numClass) {
				knnMapList_index = numClass * numKnnRatio + _numClass;
				classIDList.add(knnMapList.get(knnMapList_index).getIDList());
				idDistMap.add(new HashMap());
				Iterator iterator = ((ArrayList) classIDList.get(_numClass)).iterator();
				while (iterator.hasNext()) {
					int _id = (Integer) iterator.next();
					((Map) idDistMap.get(_numClass)).put(_id, calDist(_inst, dataset.instance(_id)));
					mergedIdDistmap.put(_id, (Double) ((Map) idDistMap.get(_numClass)).get(_id));
				}
				if ((double) _numClass == _inst.classValue()) {
					((Map) idDistMap.get(_numClass)).put(_t, 0.0);
					knnMapList.get(knnMapList_index).insertItem(_t, (Map) idDistMap.get(_numClass));
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
					arrd[n] = arrd[n] + knnMapList.get(knnMapList_index).getWeight(classifyKnnID[k])
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
