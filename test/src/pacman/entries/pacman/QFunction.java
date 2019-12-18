package pacman.entries.pacman;

import pacman.utils.DataFile;

/**
 * A linear function of the feature values.
 */
public class QFunction {

	public double[] weights; // Weight vector
	private double bias; // For a constant feature
	
	private double[] eligibility; // Traces
	private double ebias; // For a constant feature
	
	/** Start with everything at zero. */
	public QFunction(FeatureSet prototype) {
		//System.out.println("Size "+prototype.size());
		weights = new double[prototype.size()];
		eligibility = new double[prototype.size()];
	}

	/** Load initial settings from a file. */
	public QFunction(FeatureSet prototype, String filename) {
		this(prototype);
		
		DataFile file = new DataFile(filename);
		bias = Double.parseDouble(file.nextLine());
		int i =0;
		while(i<weights.length)
		{
			double line =  Double.parseDouble(file.nextLine());
			if(line != 0)
			{
				weights[i] = line;
				i++;
			}
			
		}

		file.close();
	}

	/** Estimate the Q-value given the features for an action. */
	public double evaluate(FeatureSet features) {
		double sum = bias;
		for (int i=0; i<weights.length; i++)
			sum += (features.get(i) * weights[i]); 
		//System.out.println("eval "+sum);
		return sum;
	}

	/** Gradient-descent weight update - without eligibility traces. */
	public void updateWeights(double update, FeatureSet features) {
		for (int i=0; i<weights.length; i++)
			weights[i] += (update * features.get(i) );
		bias += update;
	}
	double lambda = 0.001;
	/** Gradient-descent weight update - with eligibility traces. */
	public void updateWeights(double update) {

		for (int i=0; i<weights.length; i++) {
			
			weights[i] += (update * eligibility[i]);
			//System.out.println("after " + weights[i]);
		bias += (update * ebias);
		}
	}
	
	/** Zero out the eligibility traces. */
	public void clearTraces() {
		for (int i=0; i<eligibility.length; i++)
			eligibility[i] = 0;
		ebias = 0;
	}
	
	/** Decrease the eligibility traces. */
	public void decayTraces(double update) {
		for (int i=0; i<eligibility.length; i++) {
			//System.out.println("trace "+eligibility[i]);
			eligibility[i] *= update;
			//System.out.println("decayed "+eligibility[i]);
		}
		ebias *= update;
	}
	
	/** Increase the eligibility traces. */
	public void addTraces(FeatureSet features) {
		
		for (int i=0; i<eligibility.length; i++) {
			eligibility[i] += features.get(i);
			//System.out.println("trace "+eligibility[i]);
			}
		ebias++;
	}
	
	/** Save to a file. */
	public void save(String filename) {
		DataFile file = new DataFile(filename);
		file.clear();
		file.append(bias+"\n");
		
		int padding = 0;		
		// We have reduced depth for this one? So pad it
		// TODO: SANMIT -- HARDCODED HACK!!
		padding = 7 - weights.length;

		
		for (int w = 0; w < weights.length; w++){			
			if (4 - padding == w){
				for (int p = 0; p < padding;  p++){
					file.append("0\n");
				}
			}
			file.append(weights[w]+"\n");
		}
		file.close();
		//System.out.println("Weights: " + weights.length);
	}
	
	public double euclideanDistanceTo(QFunction qfunc){
		
		double distance = 0;
/*
		distance = (this.bias / qfunc.bias);
		if (distance > 1)
			distance = 1 / distance;
*/		
		distance += Math.pow(this.bias - qfunc.bias, 2);
		for (int i = 0; i < this.weights.length; i++){
			distance += Math.pow(this.weights[i] - qfunc.weights[i], 2);
		}
		return Math.sqrt(distance);
	}
	
}
