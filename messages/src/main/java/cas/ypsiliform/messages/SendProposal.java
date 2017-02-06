package cas.ypsiliform.messages;

import java.util.Arrays;

public class SendProposal extends AbstractMessage {

	private int[] newProposal;
	private int[] referenceProposal;
	private int totalRounds;
	private int remainingRounds;

	public int[] getNewProposal() {
		return newProposal;
	}

	public void setNewProposal(int[] newProposal) {
		this.newProposal = newProposal;
	}

	public int[] getReferenceProposal() {
		return referenceProposal;
	}

	public void setReferenceProposal(int[] referenceProposal) {
		this.referenceProposal = referenceProposal;
	}

	public int getTotalRounds() {
		return totalRounds;
	}

	public void setTotalRounds(int totalRounds) {
		this.totalRounds = totalRounds;
	}

	public int getRemainingRounds() {
		return remainingRounds;
	}

	public void setRemainingRounds(int remainingRounds) {
		this.remainingRounds = remainingRounds;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SendProposal [newProposal=");
		builder.append(Arrays.toString(newProposal));
		builder.append(", referenceProposal=");
		builder.append(Arrays.toString(referenceProposal));
		builder.append(", totalRounds=");
		builder.append(totalRounds);
		builder.append(", remainingRounds=");
		builder.append(remainingRounds);
		builder.append("]");
		return builder.toString();
	}
}
