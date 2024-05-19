import java.util.Random;

public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;



	public Autoassociator(CourseArray courses) {
		weights = new int[courses.length()][courses.length()];

		for (int i = 1; i < courses.length(); i++) {
			for (int j = 1; j < courses.length(); j++) {
				if (i != j && courses.elements[i].clashesWith.contains(courses.elements[j]))
					weights[i][j] = -1;
				else
					weights[i][j] = 0;
			}
		}

		trainingCapacity = courses.length() - 1;
	}

	public int getTrainingCapacity() {
		return trainingCapacity;
	}

	public void training(int[] pattern) {
		int n = pattern.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					weights[i][j] += pattern[i] * pattern[j];
				}
			}
		}
	}

	public int unitUpdate(int neurons[]) {
		int n = neurons.length;
		Random rand = new Random();
		int i = rand.nextInt(n);
		int sum = 0;
		for (int j = 0; j < n; j++) {
			sum += weights[i][j] * neurons[j];
		}
		neurons[i] = (sum >= 0) ? 1 : -1;
		return i;
	}

	public void unitUpdate(int[] neurons, int index) {
		int sum = 0;
		for (int j = 0; j < neurons.length; j++) {
			sum += weights[index][j] * neurons[j];
		}
		neurons[index] = (sum >= 0) ? 1 : -1;
	}

	public void chainUpdate(int[] neurons, int steps) {
		for (int s = 0; s < steps; s++) {
			unitUpdate(neurons);
		}
	}

	public void fullUpdate(int neurons[])  {
		boolean stable;
		do {
			stable = true;
			for (int i = 0; i < neurons.length; i++) {
				int oldValue = neurons[i];
				unitUpdate(neurons, i);
				if (neurons[i] != oldValue) {
					stable = false;
				}
			}
		} while (!stable);
	}
}