import java.util.ArrayList;

public class QueueStructure {
	
	/* Static variable representing a destination from a queue to the outside of the system.
	 * See destinations ArrayList below.*/
	public static final QueueStructure EXIT = new QueueStructure("EXIT", -1,-1,-1,-1,-1,-1,null); 
	
	
	public String id;
    public int servers;
    public int capacity;
    
    //Time intervals for events
    public double arrivalMin;
    public double arrivalMax;
    public double serviceMin;
    public double serviceMax;
    
    // A queue may have one or more destinations for departure
    public ArrayList<QueueStructure> destinations;
    // Each destination has it's corresponding routing probability
    public ArrayList<Double> destinationProbs;
    
    //------------------------------------------------------------
    // Simulation state variables
    //------------------------------------------------------------
    /* Because the capacity of the queue can be infinite, the list containing
     * the time spent on each state cannot be defined before runtime. It might grow
     * with each client arrival. Therefore I've decided to keep currentQueueSize private
     * and provide methods to modify it.*/
    private int currentQueueSize;
    // List that keeps track of the amount of time the queue spent in each given state:
    public ArrayList<Double> stateTimes;
    public int clientsLost;
    
	public QueueStructure(String id, int servers, int capacity, double arrivalMin, double arrivalMax, double serviceMin,
			double serviceMax, ArrayList<QueueStructure> destinations) {
		this.id = id;
		this.servers = servers;
		this.capacity = capacity;
		this.arrivalMin = arrivalMin;
		this.arrivalMax = arrivalMax;
		this.serviceMin = serviceMin;
		this.serviceMax = serviceMax;
		if(destinations==null) {
			this.destinations = new ArrayList<>();
		} else {
			this.destinations = destinations;
		}
		this.destinationProbs = new ArrayList<>();
		resetSimulationVariables();
	}
	
	public boolean isFull() {
		return currentQueueSize>=capacity;
	}
	
	public boolean canServeOnArrival() {
		return currentQueueSize <= servers;
	}
	
	public boolean canServeOnDeparture() {
		return currentQueueSize >= servers;
	}
	
	/* If client can enter the queue: increments currentQueueSize, adds another state to stateTime if necessary
	 * and returns true.
	 * Otherwise: increments clientsLost and returns false. */
	public void addClient() {
		currentQueueSize++;
		/* If this is the first time the queue has entered this state, add the
		 * state to the list of state times. */
		if(stateTimes.size()<currentQueueSize+1) {
			stateTimes.add(0.0);
		}
	}
	
	public void removeClient() {
		currentQueueSize--;
	}
	
	public void updateQueueTimes(double timeDelta) {
		stateTimes.set(currentQueueSize, stateTimes.get(currentQueueSize) + timeDelta);
	}
	
	public void resetSimulationVariables() {
		currentQueueSize = 0;
		stateTimes = new ArrayList<>();
		stateTimes.add(0.0); //Add time for the beginning state 0.
		clientsLost = 0;
	}
	
	public String toString() {
		return String.format(
				"Servers:%d\nCapacity:%d\nArrivals:%.1f to %.1f\nService:%.1f to %.1f\nTransfers clients to another queue:%b\n",
				servers, capacity, arrivalMin, arrivalMax, serviceMin, serviceMax, (destinations!=null));
	}
	
	
}