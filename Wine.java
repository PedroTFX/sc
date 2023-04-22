import java.io.Serializable;
import java.util.ArrayList;

public class Wine implements Serializable {
	String name;
	String base64Image;
	String extension;
	private ArrayList<Integer> evals;

	Wine(String name, String base64Image, String extension) {
		this.name = name;
		this.base64Image = base64Image;
		this.extension = extension;
		evals = new ArrayList<Integer>();
	}

	Wine(String name, String base64Image, String extension, ArrayList<Integer> evals) {
		this(name, base64Image, extension);
		for (Integer integer : evals) {
			this.evals.add(integer);
		}
	}


	public double averageEvaluation() {
		if(evals.size() == 0) {
			return -1;
		}

		double sum = 0;
		for (int e : evals) {
			sum += e;
		}
		return sum / evals.size();
	}
}
