import java.util.ArrayList;

/* Records results of one simulation. These results can be combined into average results. */
class SimulationReport {
    
	String[] queueIDs;
	double totalSimulationTime;
    /* This list holds, for each queue in the simulation, a list of
     * the times it spent on each of it's respective states. */
    ArrayList<ArrayList<Double>> stateTimes;
    
    //Used a double for cases where the report is an average of different results:
    double[] clientsLost;
                       
    public SimulationReport(String[] queueIDs, double t, ArrayList<ArrayList<Double>> st, double[] cl) {
        this.queueIDs = queueIDs;
    	totalSimulationTime = t;
        stateTimes = st;
        clientsLost = cl;
    }
    
    public String toString() {
        StringBuilder res = new StringBuilder(500);
        for(int i=0; i<stateTimes.size(); i++) {
        	ArrayList<Double> qTimes = stateTimes.get(i);
            res.append("Queue "+queueIDs[i]+":\n");
        	for(int j=0; j<qTimes.size(); j++) {
        		res.append(String.format(
                           "State: %d  time: %.2f  probability: %.2f%%\n",
                           j, qTimes.get(j), 100*qTimes.get(j)/totalSimulationTime));
        	}
        	res.append(String.format("Clients Lost: %.2f\n---------------------------\n", clientsLost[i]));
        }
        
        res.append(String.format("Total simulation time: %.2f", totalSimulationTime));
        return res.toString();
    }
    
    /* Sums the results of the second report on the first report. */
    public void sumSimulation(SimulationReport r) {
    	totalSimulationTime += r.totalSimulationTime;
    	
    	// For each queue simulated...
    	for(int i=0; i<stateTimes.size(); i++) {
    		// sum the number of clients lost
    		clientsLost[i] += r.clientsLost[i];
    		
    		/* and for each state of that queue, sum the time spent in both simulations.
    		 * Since queues can have infinite capacity, states may differ.
    		 * This must taken into account: */
    		ArrayList<Double> qTimes1 = stateTimes.get(i);
    		ArrayList<Double> qTimes2 = r.stateTimes.get(i);
    		matchArraySizes(qTimes1, qTimes2); //Make list sizes equal
    		for(int j=0; j<qTimes1.size(); j++) {
    			qTimes1.set(j, qTimes1.get(j) + qTimes2.get(j));
    		}
    	}
    }
    
    /* Divides the fields of the report by n. */
    public void averageResults(int n) {
    	totalSimulationTime /= n;
    	for(int i=0; i<stateTimes.size(); i++) {
    		clientsLost[i] /= n;
    		ArrayList<Double> stateTime = stateTimes.get(i);
    		for(int j=0; j<stateTime.size(); j++) {
    			stateTime.set(j, stateTime.get(j)/n);
    		}
    	}
    }
    
    /* Equalizes the size of two arrays by filling the smaller array with zeroes. */
    private void matchArraySizes(ArrayList<Double> arr1, ArrayList<Double> arr2) {
    	while(arr1.size()<arr2.size())
			arr1.add(0.0);
    	while(arr2.size()<arr1.size())
			arr2.add(0.0);
    	
    }
}





