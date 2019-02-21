
/*
 * Decompiled with CFR 0_123.
 */
import aUtiliti.ldd_Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegionSet {
	private Map<Integer, Map<Integer, Double>> knnMap = new HashMap<Integer, Map<Integer, Double>>();
	private Map<Integer, Double> knnWeight = new HashMap<Integer, Double>();
	private int winMax = 1000;
	private double knnRatio = 0.1;
	private double alpha = 0.01;

	public RegionSet(int _winMax, double _knnRatio, double _alpha) {
		this.winMax = _winMax;
		this.knnRatio = _knnRatio;
		this.alpha = _alpha;
	}

	public void insertItem(int _id, Map<Integer, Double> _idDistMap) {

		ArrayList<Integer> removeList = new ArrayList<Integer>();
		int knnCounter = 0;
		double kDist = 0.0;
		for (int _i : _idDistMap.keySet()) {

			if (_i == _id)
				continue;
			this.knnMap.get(_i).put(_id, _idDistMap.get(_i));

			if (this.knnRatio * (double) this.knnMap.size() <= 10.0)
				continue;
			Map<Integer, Double> tempSortedIdDistMap = ldd_Utils.sortByValue(this.knnMap.get(_i));
			knnCounter = 0;

			for (double _kDist : tempSortedIdDistMap.values()) {
				if ((double) (++knnCounter) < this.knnRatio * (double) this.knnMap.size())
					continue;
				kDist = _kDist;
				break;
			}
			if (_idDistMap.get(_i) < kDist) {
				this.knnWeight.put(_i, 1.0);
				continue;
			}
			this.knnWeight.put(_i, this.knnWeight.get(_i) * (1.0 - this.knnRatio));
		}
		this.knnMap.put(_id, _idDistMap);
		this.knnWeight.put(_id, 1.0);

		for (int _i : this.knnWeight.keySet()) {
			if (this.knnWeight.get(_i) >= this.alpha)
				continue;
			removeList.add(_i);
		}

		for (int _i : removeList) {
			this.removeItem(_i);
		}
		if (this.knnMap.size() >= this.winMax) {
			double minWeightVal = Double.MAX_VALUE;
			int minWeightID = -1;

			for (int _i2 : this.knnWeight.keySet()) {
				if (this.knnWeight.get(_i2) >= minWeightVal)
					continue;
				minWeightVal = this.knnWeight.get(_i2);
				minWeightID = _i2;
			}
			this.removeItem(minWeightID);
		}
	}

	public void removeItem(int _id) {
		this.knnMap.remove(_id);
		this.knnWeight.remove(_id);
		Iterator<Integer> iterator = this.knnMap.keySet().iterator();
		while (iterator.hasNext()) {
			int _i = iterator.next();
			this.knnMap.get(_i).remove(_id);
		}
	}

	public ArrayList<Integer> getIDList() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> iterator = this.knnMap.keySet().iterator();
		while (iterator.hasNext()) {
			int _id = iterator.next();
			result.add(_id);
		}
		return result;
	}

	public int[] getIDArray() {
		int[] idArray = new int[this.knnMap.keySet().size()];
		int counter = 0;
		Iterator<Integer> iterator = this.knnMap.keySet().iterator();
		while (iterator.hasNext()) {
			int id;
			idArray[counter] = id = iterator.next().intValue();
			++counter;
		}
		return idArray;
	}

	public double getWeight(int _i) {
		if (this.knnWeight.get(_i) == null) {
			return 0.0;
		}
		return this.knnWeight.get(_i);
	}

	public Map<Integer, Double> getKnnWeight() {
		return this.knnWeight;
	}

	public double getSize() {
		return this.knnWeight.size();
	}
}
