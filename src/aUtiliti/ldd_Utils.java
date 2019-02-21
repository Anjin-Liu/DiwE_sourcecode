/*
 * Decompiled with CFR 0_123.
 */
package aUtiliti;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddID;
import weka.filters.unsupervised.attribute.Remove;

public class ldd_Utils {
	
	public static Instances idRemove(Instances _insts) {
		Instances noIdInsts = null;
		Remove remove = new Remove();
		remove.setAttributeIndices("1");
		try {
			remove.setInputFormat(_insts);
			noIdInsts = Filter.useFilter(_insts, remove);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return noIdInsts;
	}

	public static Instances idFilter(Instances _insts) {
		Instances idInsts = null;
		AddID instAddidFilter = new AddID();
		try {
			instAddidFilter.setInputFormat(_insts);
			idInsts = Filter.useFilter(_insts, instAddidFilter);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return idInsts;
	}

	public static Instances classRemove(Instances _insts) {
		Instances noIdInsts = null;
		Remove remove = new Remove();
		int classIndex = _insts.classIndex() + 1;
		remove.setAttributeIndices(Integer.toString(classIndex));
		try {
			remove.setInputFormat(_insts);
			noIdInsts = Filter.useFilter(_insts, remove);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return noIdInsts;
	}

	public static KDTree buildKDTree(Instances _insts) {
		KDTree instTree = new KDTree();
		try {
			instTree.getDistanceFunction().setOptions(new String[] { "-D" });
			instTree.setInstances(_insts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instTree;
	}

	public static boolean belongsTo(Instances _base, Instance _inst) {
		for (Instance _i : _base) {
			boolean identicalFlag = true;
			int i = 0;
			while (i < _i.numAttributes()) {
				if (_i.value(i) != _inst.value(i)) {
					identicalFlag = false;
					break;
				}
				++i;
			}
			if (!identicalFlag)
				continue;
			return true;
		}
		return false;
	}

	public static int[][] makeConfusionMatrix(ArrayList<Double> _pr, Instances _testBatch) {
		int cm_size = _testBatch.classAttribute().numValues();
		int correct = 0;
		int[][] cm = new int[cm_size][cm_size];
		int i = 0;
		while (i < cm_size) {
			int j = 0;
			while (j < cm_size) {
				cm[i][j] = 0;
				++j;
			}
			++i;
		}
		i = 0;
		while (i < _testBatch.numInstances()) {
			int n = Double.valueOf(_testBatch.instance(i).classValue()).intValue();
			int m = _pr.get(i).intValue();
			int[] arrn = cm[n];
			int n2 = m;
			arrn[n2] = arrn[n2] + 1;
			if (_testBatch.instance(i).classValue() == _pr.get(i).doubleValue()) {
				++correct;
			}
			++i;
		}
		return cm;
	}

	public static double printAPR(int[][] _cm) {
		int numClass = _cm.length;
		double pAverage = 0.0;
		double rAverage = 0.0;
		double totalCorrect = 0.0;
		double totalInst = 0.0;
		int i = 0;
		while (i < numClass) {
			double pCount = 0.0;
			double rCount = 0.0;
			int j = 0;
			while (j < numClass) {
				pCount += (double) _cm[j][i];
				rCount += (double) _cm[i][j];
				totalInst += (double) _cm[i][j];
				++j;
			}
			System.out.println("  Class " + i + ": Precision: " + (double) _cm[i][i] / pCount + ", Recall: "
					+ (double) _cm[i][i] / rCount);
			pAverage += (double) _cm[i][i] / pCount;
			rAverage += (double) _cm[i][i] / rCount;
			totalCorrect += (double) _cm[i][i];
			++i;
		}
		System.out.println("Accuracy:" + totalCorrect / totalInst + ", AveragePrecision: "
				+ pAverage / (double) numClass + ", AverageRecall: " + rAverage / (double) numClass);
		return totalCorrect / totalInst;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		LinkedList<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return ((Comparable) o1.getValue()).compareTo(o2.getValue());
			}
		});
		LinkedHashMap<K, Comparable> result = new LinkedHashMap<K, Comparable>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), (Comparable) entry.getValue());
		}
		return (Map<K, V>) result;
	}

}
