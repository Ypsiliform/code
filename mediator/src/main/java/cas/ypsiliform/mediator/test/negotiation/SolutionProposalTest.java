package cas.ypsiliform.mediator.test.negotiation;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.negotiation.SolutionProposal;

public class SolutionProposalTest {

	class SolutionProposalTestHelper extends SolutionProposal {

		public SolutionProposalTestHelper(int numberOfAgents) {
			super(numberOfAgents);
		}

		public SolutionProposalTestHelper(int numberOfAgents, boolean[] bitstring) {
			super(numberOfAgents, bitstring);
		}

		public boolean[] getBitstring() {
			return super.getBitString();
		}
	}

	@Test
	public void SolutionProposal__numAgents__lengthOfBitString() {
		SolutionProposalTestHelper cut = new SolutionProposalTestHelper(10);
		// references constant
		Assert.assertEquals(10, cut.getNumberOfAgents());
		Assert.assertEquals(10 * Constants.Encoding.NUMBER_OF_PERIODS, cut.getBitstring().length);
	}

	@Test
	public void Initialize__ChangeBitString() {
		SolutionProposalTestHelper cut = new SolutionProposalTestHelper(10,
				new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS]);
		cut.initialize();

		for (boolean b : cut.getBitstring()) {
			if (b)
				return;
		}
		// all bits were false, which is a 1 in 2^120 chance
		// --> seems like it's not been randomized
		Assert.fail("Bit string was not initialized with random values");
	}

	@Test
	public void Equals__Positive() {
		boolean[] bitString = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString.length; i++) {
			bitString[i] = true;
		}
		
		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString);
		SolutionProposalTestHelper cut2 = new SolutionProposalTestHelper(10, bitString);
		
		Assert.assertEquals(cut1, cut2);
	}

	@Test
	public void Equals__Negative() {
		boolean[] bitString1 = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString1.length; i++) {
			bitString1[i] = true;
		}
		
		boolean[] bitString2 = bitString1.clone();
		bitString2[10] = false;
		
		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString1);
		SolutionProposalTestHelper cut2 = new SolutionProposalTestHelper(10, bitString2);
		
		Assert.assertNotEquals(cut1, cut2);
	}
	
	@Test
	public void Clone__SameBitString() throws Exception {
		boolean[] bitString = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString.length; i++) {
			bitString[i] = true;
		}
		
		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString);

		SolutionProposal cut2 = cut1.clone();
		
		Assert.assertNotSame(cut1, cut2);
		Assert.assertTrue("SolutionProposals are not equal", cut1.equals(cut2));
	}
}
