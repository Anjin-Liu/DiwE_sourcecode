import java.util.ArrayList;
import java.util.Arrays;

public class Rnd_Ensemble {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//diwe_synt_exp(1);
		// diwe_real_exp();
		 diwe_rand_exp("2");
	}

	private static void diwe_synt_exp(int _datasetID) {
		DiwE diwe_exp = new DiwE();
		int numrun = 15;
		int datasetID = _datasetID;
		ArrayList<Double> accList = new ArrayList<Double>();
		for (int i = 0; i < numrun; i++) {
			System.out.println("======" + i + "======");
			// diwe_exp = new DiwE();
			diwe_exp.reset();
			diwe_exp.main(new String[] { (datasetID * 100 + i) + "", "true", i * 1000 + i + "", "Syn" });
			accList.add(diwe_exp.accuracy);
		}

		double acc_mean = getMean(accList);
		double acc_std = getStd(accList, acc_mean);

		System.out.println(numrun + " random test on dataset " + datasetID + ", acc " + acc_mean + ", std " + acc_std);
	}

	private static void diwe_real_exp() {
		DiwE diwe_exp = new DiwE();
		String[] datasetID_array = new String[] { "6" };
		// String[] datasetID_array = new String[] { "3", "4", "5", "6" };

		ArrayList<Double> accList = new ArrayList<Double>();

		for (int i = 0; i < datasetID_array.length; i++) {
			// diwe_exp = new DiwE();
			diwe_exp.reset();
			diwe_exp.main(new String[] { datasetID_array[i], "false", i + "" });
			accList.add(diwe_exp.accuracy);
		}

		System.out.println("diwe_real_exp " + Arrays.toString(datasetID_array));
		for (int i = 0; i < accList.size(); i++) {
			System.out.println("dataset, " + datasetID_array[i] + ", acc, " + accList.get(i));
		}
	}

	private static void diwe_rand_exp(String _datasetID) {
		DiwE diwe_exp = new DiwE();
		int numrun = 15;
		String datasetID = _datasetID;
		ArrayList<Double> accList = new ArrayList<Double>();
		for (int i = 0; i < numrun; i++) {
			System.out.println("======" + i + "======");
			// diwe_exp = new DiwE();
			diwe_exp.reset();
			diwe_exp.main(new String[] { datasetID, "true", i * 1000 + i + "" });
			accList.add(diwe_exp.accuracy);
		}

		double acc_mean = getMean(accList);
		double acc_std = getStd(accList, acc_mean);

		System.out.println(numrun + " random test on dataset " + datasetID + ", acc " + acc_mean + ", std " + acc_std);
	}

	private static Double getMean(ArrayList<Double> _aList) {

		Double mean = 0.0;
		for (Double v : _aList) {
			mean += v;
		}
		return mean / _aList.size();
	}

	private static Double getStd(ArrayList<Double> _aList, double _mean) {

		Double std = 0.0;
		for (Double v : _aList) {
			std += (v - _mean) * (v - _mean);
		}

		return Math.sqrt(std / (_aList.size() - 1));
	}

}
