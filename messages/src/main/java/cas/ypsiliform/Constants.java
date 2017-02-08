package cas.ypsiliform;

public interface Constants {
	public interface Encoding {
		public int NUMBER_OF_PERIODS = 12;
	}
	
	public interface Negotiation {
		public int NUMBER_OF_ROUNDS = 1000;
		public int TIMEOUT_PER_ROUND_MS = 5*1000;
	}
}
