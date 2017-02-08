package cas.ypsiliform.mediator.test.negotiation;

import static org.junit.Assert.*;

import java.util.Arrays;

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
	public void solutionProposal__numAgents__lengthOfBitString() {
		SolutionProposalTestHelper cut = new SolutionProposalTestHelper(10);
		// references constant
		Assert.assertEquals(10, cut.getNumberOfAgents());
		Assert.assertEquals(10 * Constants.Encoding.NUMBER_OF_PERIODS, cut.getBitstring().length);
	}

	@Test
	public void initialize__ChangeBitString() {
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
	public void equals__Positive() {
		boolean[] bitString = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString.length; i++) {
			bitString[i] = true;
		}
		
		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString);
		SolutionProposalTestHelper cut2 = new SolutionProposalTestHelper(10, bitString);
		
		Assert.assertEquals(cut1, cut2);
	}

	@Test
	public void equals__Negative() {
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
	public void clone__SameBitString() throws Exception {
		boolean[] bitString = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString.length; i++) {
			bitString[i] = true;
		}
		
		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString);

		SolutionProposal cut2 = cut1.clone();
		
		Assert.assertNotSame(cut1, cut2);
		Assert.assertTrue("SolutionProposals are not equal", cut1.equals(cut2));
	}
	
	@Test
	public void mutate__NotEquals() throws Exception {
		boolean[] bitString = new boolean[10 * Constants.Encoding.NUMBER_OF_PERIODS];
		for(int i = 0; i < bitString.length; i++) {
			bitString[i] = true;
		}

		SolutionProposalTestHelper cut1 = new SolutionProposalTestHelper(10, bitString);
		
		SolutionProposal cut2 = cut1.mutate();

		Assert.assertNotSame(cut1, cut2);
		Assert.assertFalse("SolutionProposals are equal", cut1.equals(cut2));
	}
	
	@Test
	public void sliceForAgent__LengthEqualsPeriods() throws Exception {
		SolutionProposal cut = new SolutionProposal(10);
		
		Assert.assertEquals(Constants.Encoding.NUMBER_OF_PERIODS, cut.sliceForAgent(0).length);
		Assert.assertEquals(Constants.Encoding.NUMBER_OF_PERIODS, cut.sliceForAgent(9).length);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void sliceForAgent__NegativeIndex() throws Exception {
		SolutionProposal cut = new SolutionProposal(10);
		
		Assert.assertEquals(Constants.Encoding.NUMBER_OF_PERIODS, cut.sliceForAgent(-1).length);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void sliceForAgent__TooHighIndex() throws Exception {
		SolutionProposal cut = new SolutionProposal(10);
		
		Assert.assertEquals(Constants.Encoding.NUMBER_OF_PERIODS, cut.sliceForAgent(10).length);
	}
	
	@Test
	public void mutate___DifferentBitString() throws Exception {
		SolutionProposalTestHelper cut = new SolutionProposalTestHelper(2,
				new boolean[2 * Constants.Encoding.NUMBER_OF_PERIODS]);
		
		// currently the entire bit string is "false"
		Assert.assertArrayEquals(cut.sliceForAgent(0), cut.sliceForAgent(1));
		
		SolutionProposal mutatedCut = cut.mutate();
		// one mutation changed one bit
		
		Assert.assertFalse("After mutation the bit strings should no longer match.", Arrays.equals(mutatedCut.sliceForAgent(0), mutatedCut.sliceForAgent(1)));
	}
	
	@Test
	public void toString__() throws Exception {
		SolutionProposalTestHelper cut = new SolutionProposalTestHelper(2,
				new boolean[2 * Constants.Encoding.NUMBER_OF_PERIODS]);
		
		// currently the entire bit string is "false"
		Assert.assertEquals("000000000000000000000000", cut.toString());
		
		SolutionProposal mutatedCut = cut.mutate();
		// one mutation changed one bit
		
		Assert.assertTrue("String should contain at least one 1", mutatedCut.toString().contains("1"));
	}
}
