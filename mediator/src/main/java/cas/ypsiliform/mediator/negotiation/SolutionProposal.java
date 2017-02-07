package cas.ypsiliform.mediator.negotiation;

import java.util.Arrays;
import java.util.Random;

import cas.ypsiliform.Constants;

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
		assert (numberOfAgents * Constants.Encoding.NUMBER_OF_PERIODS) == bitString.length : "Length of bit string must be equal to numberOfAgents * Constants.Encoding.NUMBER_OF_PERIODS";
		
		this.setNumberOfAgents(numberOfAgents);
		this.setBitString(bitString);
	}
	
	
	public void initialize() {
		Random rdm = new Random();
		
		for(int i = 0; i < getBitString().length; i++) {
			getBitString()[i] = rdm.nextBoolean();
		}
	}
	
	public SolutionProposal clone() {
		return new SolutionProposal(this.getNumberOfAgents(), this.getBitString().clone());
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
}
