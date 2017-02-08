package cas.ypsiliform.mediator.negotiation;

import java.util.Arrays;
import java.util.Random;

import cas.ypsiliform.Constants;

/**
 * Manages solution proposals in the negotiation process.
 * This class has methods to generate a new proposal, mutate an existing one, and extract the substrings that
 * are to be sent to the agents.
 * @author Michael Müller
 */
public class SolutionProposal {
	private int numberOfAgents;
	private boolean[] bitString;
	
	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	private void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
		this.setBitString(new boolean[numberOfAgents * Constants.Encoding.NUMBER_OF_PERIODS]);
	}
	
	protected boolean[] getBitString() {
		return bitString;
	}

	protected void setBitString(boolean[] bitString) {
		this.bitString = bitString;
	}

	public SolutionProposal(int numberOfAgents) {
		setNumberOfAgents(numberOfAgents);
		initialize();
	}


	protected SolutionProposal(int numberOfAgents, boolean[] bitString) {
		// only internal consumers, should be correct
		assert (numberOfAgents * Constants.Encoding.NUMBER_OF_PERIODS) == bitString.length : "Length of bit string must be equal to numberOfAgents * Constants.Encoding.NUMBER_OF_PERIODS";
		
		this.setNumberOfAgents(numberOfAgents);
		this.setBitString(bitString);
	}
	
	
	/**
	 * Generate a random solution
	 */
	public void initialize() {
		Random rdm = new Random();
		
		for(int i = 0; i < getBitString().length; i++) {
			getBitString()[i] = rdm.nextBoolean();
		}
	}
	
	/**
	 * Return a deep clone of the object that can be mutated without changing the original.
	 */
	public SolutionProposal clone() {
		return new SolutionProposal(this.getNumberOfAgents(), this.getBitString().clone());
	}
	
	/**
	 * Mutate one bit in the solution.
	 * @return Clone of the original solution with one mutation
	 */
	public SolutionProposal mutate() {
		return this.mutate(1);
	}
	/**
	 * Mutate a number of bits in the solution. Does not guarantee an exact number of mutations.
	 * @param numberOfMuatations maximum number of mutations to be made
	 * @return Clone of the original solution with mutations
	 */
	public SolutionProposal mutate(int numberOfMuatations) {
		Random rdm = new Random();
		SolutionProposal clone = this.clone();
		
		// bitflip on random positions
		for(int i = 0; i < numberOfMuatations; i++) {
			int pos = rdm.nextInt(clone.bitString.length);
			clone.bitString[pos] = !clone.bitString[pos];
		}
		
		return clone;
	}
	
	/**
	 * Get the view on the solution for one particular agent
	 * @param agentPos the position of the agent whose view is asked for
	 * @return a copy of the part of the solution that is relevant for one particular agent
	 */
	public boolean[] sliceForAgent(int agentPos) {
		if (agentPos < 0 || agentPos >= getNumberOfAgents()) {
			throw new IndexOutOfBoundsException();
		}
		
		return Arrays.copyOfRange(this.getBitString(),
				agentPos * Constants.Encoding.NUMBER_OF_PERIODS,
				(agentPos + 1) * Constants.Encoding.NUMBER_OF_PERIODS);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SolutionProposal) {
			SolutionProposal other = (SolutionProposal) obj;
			return Arrays.equals(this.bitString, other.bitString);
		} else {
			return super.equals(obj);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		
		for (int i = 0; i < bitString.length; i++) {
			if (bitString[i])
				string.append("1");
			else
				string.append("0");
		}
		
		return string.toString();
	}
}
