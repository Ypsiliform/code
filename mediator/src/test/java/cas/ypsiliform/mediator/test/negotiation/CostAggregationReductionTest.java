package cas.ypsiliform.mediator.test.negotiation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cas.ypsiliform.mediator.negotiation.CostAggregationReduction;
import cas.ypsiliform.messages.AgentResponse;

public class CostAggregationReductionTest {
	CostAggregationReduction cut;
	AgentResponse r;

	@Before
	public void setUp() throws Exception {
		cut = new CostAggregationReduction();

		HashMap<Integer, Double> costs = new HashMap<>();
		costs.put(1, 3d);
		costs.put(2, 6d);
		r = new AgentResponse();
		r.setCosts(costs);
	}

	@Test(expected = Exception.class)
	public void nullResponse__Exception() {
		AgentResponse nullResponse = null;

		cut.reduce(nullResponse, null);
	}

	@Test
	public void nullPrevious__oneBucket() {
		Map<Integer, Double> result = cut.reduce(r, null);

		Assert.assertNotNull(result);
		Assert.assertEquals((Double) 3d, (Double) result.get(1));
		Assert.assertEquals((Double) 6d, (Double) result.get(2));
	}

	@Test
	public void emptyPrevious__oneBucket() {
		Map<Integer, Double> result = cut.reduce(r, new Object[0]);

		Assert.assertNotNull(result);
		Assert.assertEquals((Double) 3d, (Double) result.get(1));
		Assert.assertEquals((Double) 6d, (Double) result.get(2));
	}

	@Test
	public void onePrevious() {
		Map<Integer, Double> previous = new HashMap<Integer, Double>();
		previous.put(1, 1d);
		previous.put(2, 2d);

		Map<Integer, Double> result = cut.reduce(r, new Object[] { previous });

		Assert.assertNotNull(result);
		Assert.assertEquals((Double) 4d, (Double) result.get(1));
		Assert.assertEquals((Double) 8d, (Double) result.get(2));
	}

	@Test
	public void twoPrevious() {
		Map<Integer, Double> previous1 = new HashMap<Integer, Double>();
		previous1.put(1, 1d);
		previous1.put(2, 2d);
		
		Map<Integer, Double> previous2 = new HashMap<Integer, Double>();
		previous2.put(1, 2d);
		previous2.put(2, 1d);

		Map<Integer, Double> result = cut.reduce(r, new Object[] { previous1, previous2 });

		Assert.assertNotNull(result);
		Assert.assertEquals((Double) 6d, (Double) result.get(1));
		Assert.assertEquals((Double) 9d, (Double) result.get(2));
	}
}
