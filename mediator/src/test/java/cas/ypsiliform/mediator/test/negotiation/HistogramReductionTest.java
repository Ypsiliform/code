package cas.ypsiliform.mediator.test.negotiation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cas.ypsiliform.mediator.negotiation.HistogramReduction;
import cas.ypsiliform.messages.AgentResponse;

public class HistogramReductionTest {
	HistogramReduction cut;

	@Before
	public void setUp() throws Exception {
		cut = new HistogramReduction();
	}

	@Test(expected = Exception.class)
	public void nullResponse__Exception() {
		AgentResponse nullResponse = null;

		cut.reduce(nullResponse, null);
	}

	@Test
	public void nullPrevious__oneBucket() {
		AgentResponse r = new AgentResponse();
		r.setSelection(6);

		Map<Integer, Integer> result = cut.reduce(r, null);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.containsKey(6));
		Assert.assertEquals(1, (int) result.get(6));
	}

	@Test
	public void emptyPrevious__oneBucket() {
		AgentResponse r = new AgentResponse();
		r.setSelection(6);

		Map<Integer, Integer> result = cut.reduce(r, new Object[0]);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.containsKey(6));
		Assert.assertEquals(1, (int) result.get(6));
	}

	@Test
	public void onePrevious__oneBucket() {
		AgentResponse r = new AgentResponse();
		r.setSelection(6);

		Map<Integer, Integer> previous = new HashMap<Integer, Integer>();
		previous.put(6, 1);

		Map<Integer, Integer> result = cut.reduce(r, new Object[] { previous });

		Assert.assertNotNull(result);
		Assert.assertTrue(result.containsKey(6));
		Assert.assertEquals(2, (int) result.get(6));
	}

	@Test
	public void onePrevious__twoBucket() {
		AgentResponse r = new AgentResponse();
		r.setSelection(6);

		Map<Integer, Integer> previous = new HashMap<Integer, Integer>();
		previous.put(2, 1);

		Map<Integer, Integer> result = cut.reduce(r, new Object[] { previous });

		Assert.assertNotNull(result);
		Assert.assertTrue(result.containsKey(2));
		Assert.assertTrue(result.containsKey(6));
		Assert.assertEquals(1, (int) result.get(2));
		Assert.assertEquals(1, (int) result.get(6));
	}

	@Test
	public void twoPrevious__twoBucket() {
		AgentResponse r = new AgentResponse();
		r.setSelection(6);

		Map<Integer, Integer> previous = new HashMap<Integer, Integer>();
		previous.put(2, 1);
		previous.put(6, 2);

		Map<Integer, Integer> result = cut.reduce(r, new Object[] { previous });

		Assert.assertNotNull(result);
		Assert.assertTrue(result.containsKey(2));
		Assert.assertTrue(result.containsKey(6));
		Assert.assertEquals(1, (int) result.get(2));
		Assert.assertEquals(3, (int) result.get(6));
	}
}
