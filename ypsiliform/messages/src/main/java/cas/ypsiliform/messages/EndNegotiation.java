package cas.ypsiliform.messages;

import java.util.Arrays;

public class EndNegotiation extends AbstractMessage {

	private int[] solution;

	public int[] getSolution() {
		return solution;
	}

	public void setSolution(int[] solution) {
		this.solution = solution;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EndNegotiation [solution=");
		builder.append(Arrays.toString(solution));
		builder.append("]");
		return builder.toString();
	}
}
