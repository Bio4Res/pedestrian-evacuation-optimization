package es.uma.lcc.caesium.pedestrian.evacuation.optimization.greedy;

import java.util.List;

import es.uma.lcc.caesium.ea.operator.variation.VariationFactory;
import es.uma.lcc.caesium.ea.operator.variation.VariationOperator;

/**
 * Extend the base factory with the greedy initialization operator
 * @author ccottap
 * @version 1.0
 */
public class GreedyVariationFactory extends VariationFactory {

	@Override
	public VariationOperator create (String name, List<String> pars) {
		VariationOperator op = null;
		
		switch (name.toUpperCase()) {
		case "GREEDYINITIALIZATION":
			op = new EvacuationProblemGreedyInitialization(pars);
			break;
		
		case "GREEDYMUTATION":
			op = new EvacuationProblemGreedyMutation(pars);
			break;

		default:
			op = super.create(name, pars);
		}
		
		return op;
	}

}
