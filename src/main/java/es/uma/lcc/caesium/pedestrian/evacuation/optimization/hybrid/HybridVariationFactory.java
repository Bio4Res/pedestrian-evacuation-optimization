package es.uma.lcc.caesium.pedestrian.evacuation.optimization.hybrid;

import java.util.List;

import es.uma.lcc.caesium.ea.operator.variation.VariationOperator;
import es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy.GreedyVariationFactory;

/**
 * Extend the base factory with the greedy initialization operator
 * @author ccottap
 * @version 1.0
 */
public class HybridVariationFactory extends GreedyVariationFactory {

	@Override
	public VariationOperator create (String name, List<String> pars) {
		VariationOperator op = null;
		
		switch (name.toUpperCase()) {
		case "GREEDYRECOMBINATION":
			op = new EvacuationProblemGreedyRecombination(pars);
			break;
		
		default:
			op = super.create(name, pars);
		}
		
		return op;
	}

}
