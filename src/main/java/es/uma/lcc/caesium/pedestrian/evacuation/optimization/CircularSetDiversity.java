package es.uma.lcc.caesium.pedestrian.evacuation.optimization;

import java.util.List;

import es.uma.lcc.caesium.ea.base.Genotype;
import es.uma.lcc.caesium.ea.base.Individual;
import es.uma.lcc.caesium.ea.statistics.DiversityMeasure;

/**
 * Computes diversity in the population by measuring the average distance of
 * individuals, considered as sets of real-valued variables. The distance between 
 * a set A and and set B is sum_{x in A} min_{y in B} |x-y|, but the range
 * of values is considered circular, so |x-y| is to be interpreted as 
 * min (|x-y|, R-|x-y|) where R is the range of the variables.
 * @author ccottap
 * @version 1.0
 *
 */
public class CircularSetDiversity implements DiversityMeasure {
	/**
	 * range of variables
	 */
	private double range;
	
	/**
	 * Creates the diversity estimator given the range of variables
	 * @param range the range of variables
	 */
	public CircularSetDiversity(double range) {
		this.range = range;
	}
	
	@Override
	public double apply(List<Individual> pop) {
		int n = pop.get(0).getGenome().length();
		int mu = pop.size();
		double[][] matrix = new double[mu][n];
		
		for (int i=0; i<mu; i++) {
			Genotype gi = pop.get(i).getGenome();
			for (int k=0; k<n; k++)
				matrix[i][k] = (double)gi.getGene(k);
		}
		
		double totalDist = 0.0;
		for (int i=0; i<mu; i++ ) {
			for (int j=0; j<mu; j++) {
				if (i != j) {
					for (int ki=0; ki<n; ki++) {
						double d = Math.abs(matrix[i][ki] - matrix[j][0]);
						double best = Math.min(d,  range - d);
						for (int kj=1; kj<n; kj++) {
							d = Math.abs(matrix[i][ki] - matrix[j][kj]);
							double cand = Math.min(d,  range - d);
							if (cand < best)
								best = cand;
						}
						totalDist += best;
					}
				}
			}
		}
		
		totalDist /= (mu*(mu-1));
		
		return totalDist;
	}

}
