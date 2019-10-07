import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class QueueSim {
	
	
    private ArrayList<QueueStructure> qs;
    private ArrayList<Long> seeds;
    private int randoms;
    private ArrayList<ScheduleEntry> firstArrivals;
	
    public QueueSim(ArrayList<QueueStructure> qs, ArrayList<Long> seeds, int randoms, ArrayList<ScheduleEntry> firstArrivals) {
    	this.qs = qs;
    	this.seeds = seeds;
    	this.randoms = randoms;
    	this.firstArrivals = firstArrivals;
    }
    
    /* Constructs object from file. */
    public QueueSim(String fileString) throws Exception {
    	parseFile(fileString);
    }
    
	public void runSimulation() {
		/* Creates a new SimulationReport With all fields zeroed
		 * This report will accumulate the results of every simulation.
		 * Begin by creating the list containing, for each queue in the simulation,
		 * the list of time spent on each state of said queue. Since a queue can have
		 * infinite capacity, this list is created containing empty lists
		 * (lists with no times for any state). */
		String[] queueIDs = new String[qs.size()];
		ArrayList<ArrayList<Double>> stateTimes = new ArrayList<>();
		for(int i=0; i<qs.size(); i++) {
			stateTimes.add(new ArrayList<Double>());
			queueIDs[i] = qs.get(i).id;
		}
		SimulationReport res = new SimulationReport(queueIDs, 0.0, stateTimes, new double[qs.size()]);
		
		
		for(long r : seeds) {
			SimulationReport temp = runSimulation(r, randoms);
			//System.out.println(temp.toString());
			res.sumSimulation(temp);
		}
		/* Divide the accumulated results by the number of simulations run to obtain
		 * the average of the results. */
		res.averageResults(seeds.size());
		System.out.printf("Printing average results of %d simulations:\n", seeds.size());
		System.out.println(res.toString());
	}
	
	private SimulationReport runSimulation(long randomSeed, int totalRandoms) {
		
		//Event schedule
		Comparator<ScheduleEntry> schComparator = new ScheduleEntry();
		PriorityQueue<ScheduleEntry> schedule = new PriorityQueue<>(schComparator);
		//First arrivals are offered to the schedule
        for(ScheduleEntry se : firstArrivals) {
        	schedule.offer(se);
        }
               
        RNG rng = new RNG(randomSeed);
        double time = 0;
        
        while(totalRandoms > 0) {
            
            //Process next scheduled event
            ScheduleEntry se = schedule.poll();
            double timeDelta = se.time-time;
            for(QueueStructure q : qs) {
            	q.updateQueueTimes(timeDelta);
            }
            time += timeDelta; //update simulation clock
            
            
            if(se.event == EventEnum.ARRIVAL) {
                QueueStructure dest = se.destination;
            	if(!dest.isFull()) { //Queue can receive the client
                    dest.addClient();
                    if(dest.canServeOnArrival()) { //Queue can serve the client
                        totalRandoms -= scheduleDeparture(schedule, dest, time, rng);
                    }
                } else { //Queue full
                    dest.clientsLost++;
                }
                scheduleArrival(schedule, dest, time, rng);
                totalRandoms--;
            
            
            
            } else if(se.event == EventEnum.PASSAGE) {
            	QueueStructure ori = se.origin;
            	QueueStructure dest = se.destination;
            	ori.removeClient();
                if(ori.canServeOnDeparture()) { //Origin can serve another client.
                	totalRandoms -= scheduleDeparture(schedule, ori, time, rng);
                }
                if(!dest.isFull()) { //Destination can take another client.
                	dest.addClient();
                	if(dest.canServeOnArrival()) { //Destination can serve another client.
                		totalRandoms -= scheduleDeparture(schedule, dest, time, rng);
                	}
            	} else { //Destination full. Client lost.
            		dest.clientsLost++;
            	}
            
            
                
            } else { //It's a departure
            	QueueStructure ori = se.origin;
            	ori.removeClient();
            	if(ori.canServeOnDeparture()) { //Can serve one more client
            		totalRandoms -= scheduleDeparture(schedule, ori, time, rng);
            	}
            }
        }
        
        //Simulation finished. Make report.
        ArrayList<ArrayList<Double>> qTimes = new ArrayList<>();
        double[] clientsLost = new double[qs.size()];
        String[] queueIDs = new String[qs.size()];
        for(int i=0; i<qs.size(); i++) {
        	queueIDs[i] = qs.get(i).id;
        	qTimes.add(qs.get(i).stateTimes);
        	clientsLost[i] = qs.get(i).clientsLost;
        }
        
        SimulationReport sr = new SimulationReport(queueIDs, time, qTimes, clientsLost);
        
        //Reset the state of all queues.
        for(QueueStructure q : qs) {
        	q.resetSimulationVariables();
        }
        
        return sr;
	}
	
	private void scheduleArrival(PriorityQueue<ScheduleEntry> schedule, QueueStructure destination, double time, RNG rng) {
		double randomNumber = rng.next();
		double eventTime = time + (destination.arrivalMax-destination.arrivalMin) * randomNumber + destination.arrivalMin;
		schedule.offer(ScheduleEntry.newArrival(eventTime, destination));
	}
	
	private int scheduleDeparture(PriorityQueue<ScheduleEntry> schedule, QueueStructure origin, double time, RNG rng) {
		//Define event time
		double randomNumber = rng.next();
		int randomsUsed = 1;
		double eventTime = time + (origin.serviceMax-origin.serviceMin) * randomNumber + origin.serviceMin;
		
		QueueStructure dest = null;
		/* If more than one possible destination, roll the probabilities.
		 * This consumes an extra random number. */
		if(origin.destinations.size()>1) {
			double randomProb = rng.next();
			randomsUsed++;
			double prob = 0;
			/* Probability check works like this:
			 * Add p1 to prob. Check if random is lower. If not, add p2 to prob and check again.
			 * If not, add p3 and check again and so on. If at any check the random is lower,
			 * the corresponding destination is chosen.*/
			for(int i = 0; i<origin.destinationProbs.size(); i++) {
				prob += origin.destinationProbs.get(i);
				if(randomProb < prob) {
					dest = origin.destinations.get(i);
					break;
				}
			}
		
		// Else there's only one possible destination
		} else {
			dest = origin.destinations.get(0);
		}
		
		//Generate schedule events accordingly
		if(dest == QueueStructure.EXIT) {//Departure from the system
			schedule.offer(ScheduleEntry.newDeparture(eventTime, origin));
		} else { //Passage from one queue to another
			schedule.offer(ScheduleEntry.newPassage(eventTime, origin, dest));
		}
		
		return randomsUsed;
	}
	
	
	
	
	
	
	//===========================================================
	// File parsing methods
    //===========================================================
	
	private void parseFile(String fileString) throws Exception {
		try {
			qs = new ArrayList<>();
			seeds = new ArrayList<>();
			firstArrivals = new ArrayList<>();
			String data = new String(Files.readAllBytes(Paths.get(fileString)));
			String[] lines = data.split("(\r\n)|\n");
			for(int i=0; i<lines.length; i++) {
				try {
					String s = lines[i];
					if(s.length()==0 || s.charAt(0)=='#') { //Empty or comment line
						continue;
						
					} else if(s.charAt(0)=='q') { //Line defines a queue
						qs.add(createQueue(s));
						
					} else if(s.charAt(0)=='d'){ //Line defines a destination
						int beginning = s.indexOf(':');
						defineDestination(qs, s.substring(beginning+1));
						
					} else if(s.charAt(0)=='s') { //Line defines seeds for the rng
						int beginning = s.indexOf(':');
						defineSeeds(s.substring(beginning+1));
					
					} else if(s.charAt(0)=='r') { //Line defines the amount of randoms to be used
						int beginning = s.indexOf(':');
						randoms = Integer.parseInt(s.substring(beginning+1).trim());
						
					} else if(s.charAt(0)=='f') { //Line defines the first arrivals for the queues
						int beginning = s.indexOf(':');
						defineFirstArrivals(s.substring(beginning+1));
						
					} else { //Sintax error
						throw new Exception(
								"Non empty line contains invalid syntax.");
					}
				} catch(Exception e) {
					throw new Exception(String.format(
							"Exception thrown during file parsing (file line: %d): %s", i+1, e.toString()));
				}
			}
			
			if(firstArrivals.isEmpty()) {
				throw new Exception("No first arrivals were defined for any queue in the system.");
			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	private QueueStructure createQueue(String s) throws Exception {
		
			String[] splitOnColon = s.replaceAll("\\s", "").split(":");
			String[] params = splitOnColon[1].split("/");
			int capacity;
			if(params[1].equals("inf")) {
				capacity = Integer.MAX_VALUE;
			} else {
				capacity = Integer.parseInt(params[1]);
			}
			return new QueueStructure(
					splitOnColon[0], //id
					Integer.parseInt(params[0]), //servers
					capacity,
					Double.parseDouble(params[2]), //arrivalMin
					Double.parseDouble(params[3]), //arrivalMax
					Double.parseDouble(params[4]), //serviceMin					
					Double.parseDouble(params[5]), //serviceMax
					null); //destinations
	}
	
	private void defineDestination(ArrayList<QueueStructure> qs, String s) throws Exception {
		String[] connectedQs = s.split("(->)");
		
		//Defines the origin queue whose destinations are being parsed
		String originName = connectedQs[0].trim();
		QueueStructure origin = findQueue(qs, originName);
		
		//Parse destinations
		String[] destinations = connectedQs[1].split(",");
		//Add each destination with it's corresponding routing probability to the origin object
		for(String d : destinations) {
			QueueStructure dest = null;
			String[] destAndProb = d.split("/");
			String destName = destAndProb[0].trim();
			if(destName.equals("S") || destName.equals("s")) { //Destination is the system exit
				dest = QueueStructure.EXIT;
			} else { //Destination is one of the system's queues
				dest = findQueue(qs, destName);
			}
			origin.destinations.add(dest);
			origin.destinationProbs.add(Double.parseDouble(destAndProb[1]));
		}
		double sum = 0.0;
		for(double p : origin.destinationProbs) {
			sum += p;
		}
		if(sum != 1.0) {
			throw new Exception("The sum of all routing probabilities in a queue must equal 1.");
		}
	}
	
	private QueueStructure findQueue(ArrayList<QueueStructure> qs, String name) throws Exception{
		for(QueueStructure q : qs) {
			if(q.id.equals(name)) return q;
		}
		throw new Exception(String.format(
				"Queue \"%s\" does not exist or wasn't previously defined in input file.", name));
		
	}
	
	private void defineSeeds(String str) {
		String[] longsString = str.replaceAll("\\s", "").split(",");
		for(String s : longsString) {
			seeds.add(Long.parseLong(s));
		}
	}
	
	private void defineFirstArrivals(String str) throws Exception{
		for(String s1 : str.replaceAll("\\s", "").split(",")) {
			String[] queueAndArr = s1.split("/");
			double time = Double.parseDouble(queueAndArr[1]);
			QueueStructure q = findQueue(qs, queueAndArr[0]);
			firstArrivals.add(ScheduleEntry.newArrival(time, q));
		}
	}
}


















