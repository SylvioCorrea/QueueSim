import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class QueueSim {
    
    //=================================================================
    //          Support Inner classes
    //=================================================================
    private static enum Event {
        ARRIVAL, DEPARTURE
    }
    
    private static class ScheduleEntry {
        Event event;
        double time;
        public ScheduleEntry(Event event, double time) {
            this.event = event;
            this.time = time;
        }
    }
    
    //Used to place ScheduleEntry objects in order in a priority queue
    private static class SchEntComparator implements Comparator<ScheduleEntry> {
        public int compare(ScheduleEntry se1, ScheduleEntry se2) {
            if(se1.time < se2.time)
                return -1;
            if(se1.time > se2.time)
                return 1;
            return 0;
        }
    }
    
    //Saves results of one simulation
    private static class SimulationReport {
        double totalSimulationTime;
        double[] stateTime;
        double clientLoss; //Used a double for cases where the report
                           //saves a mean of different results
                           
        public SimulationReport(double t, double[] st, double cl) {
            totalSimulationTime = t;
            stateTime = st;
            clientLoss = cl;
        }
        
        public static SimulationReport meanResults(List<SimulationReport> reportList) {
            double meanTime = 0.0;
            int nOfStates = reportList.get(0).stateTime.length;
            double[] meanStateTime = new double[nOfStates];
            double meanLoss = 0.0;
            for (SimulationReport report : reportList) {
                meanTime += report.totalSimulationTime;
                meanLoss += report.clientLoss;
                for(int j=0; j<report.stateTime.length; j++) {
                    meanStateTime[j] += report.stateTime[j];
                }
            }
            
            meanTime = meanTime/reportList.size();
            meanLoss = meanLoss/reportList.size();
            for(int i=0; i<meanStateTime.length; i++) {
                meanStateTime[i] = meanStateTime[i]/reportList.size();
            }
            return new SimulationReport(meanTime, meanStateTime, meanLoss);
        }
        
        public String toString() {
            StringBuilder res = new StringBuilder(500);
            for(int i=0; i<stateTime.length; i++) {
                double result = stateTime[i]/totalSimulationTime;
                res.append(String.format(
                           "State: %d  time: %.2f  probability: %.2f%%\n",
                           i, stateTime[i], 100*stateTime[i]/totalSimulationTime));
            }
            res.append(String.format("Clients Lost: %.2f\n", clientLoss));
            res.append(String.format("Total time: %.2f", totalSimulationTime));
            return res.toString();
        }
    }
    //=================================================================
    //=================================================================
    
    
    private int servers;
    private int queueMax;
    private int randoms; //Amount of random numbers to be used
    
    private double arrivalMin;
    private double arrivalMax;
    private double serviceMin;
    private double serviceMax;
    
    private double time;
    private int currentQueueSize;
    private double[] stateTime;
    private int clientLoss;
    private PriorityQueue<ScheduleEntry> schedule;
    
    private ArrayList<SimulationReport> reportList;
    
    private RNG rng;
    
    public QueueSim(int servers, int queueMax, int randoms,
                    double arrivalMin, double arrivalMax,
                    double serviceMin, double serviceMax) {
        this.servers = servers;
        this.queueMax = queueMax;
        this.randoms = randoms;
        this.arrivalMin = arrivalMin;
        this.arrivalMax = arrivalMax;
        this.serviceMin = serviceMin;
        this.serviceMax = serviceMax;
        time = 0.0;
        currentQueueSize = 0;
        clientLoss = 0;
        reportList = new ArrayList<SimulationReport>();
        stateTime = new double[queueMax+1];
        schedule = new PriorityQueue<ScheduleEntry>(new SchEntComparator());
    }
    
    private void saveReport() {
        reportList.add(new SimulationReport(time, stateTime.clone(), clientLoss));
    }
    
    private void resetSimulation() {
        time = 0.0;
        stateTime = new double[queueMax+1];
        time = 0.0;
        currentQueueSize = 0;
        clientLoss = 0;
        stateTime = new double[queueMax+1];
        schedule.clear();
    }
    
    private double genArrivalTime() {
        return time + (arrivalMax-arrivalMin) * rng.next() + arrivalMin;
    }
    
    private double genDepartureTime() {
        return time + (serviceMax-serviceMin) * rng.next() + serviceMin;
    }
    
    private void scheduleArrival() {
        schedule.offer(new ScheduleEntry(Event.ARRIVAL, genArrivalTime()));
    }
    
    private void scheduleDeparture() {
        schedule.offer(new ScheduleEntry(Event.DEPARTURE, genDepartureTime()));
    }
    
    public void runSimulation(long[] randomSeeds) {
        for(int i=0; i<randomSeeds.length; i++) {
            runSimulation(randomSeeds[i]);
        }
    }
    
    public void runSimulation(long randomSeed) {
        //First arrival is hard coded
        schedule.offer(new ScheduleEntry(Event.ARRIVAL, 3.0));
        
        rng = new RNG(randomSeed);
        int remainingRandoms = randoms;
        
        while(remainingRandoms > 0) {
            
            //Process next scheduled event
            ScheduleEntry se = schedule.poll();
            double timeDelta = se.time-time;
            stateTime[currentQueueSize] += timeDelta;
            time += timeDelta;
            
            
            if(se.event == Event.ARRIVAL) {
                if(currentQueueSize < queueMax) {
                    currentQueueSize++;
                    if(currentQueueSize<=servers) {
                        scheduleDeparture();
                        remainingRandoms--;
                    }
                } else {
                    clientLoss++;
                }
                scheduleArrival();
                remainingRandoms--;
            
            } else { //Event is a departure
                currentQueueSize--;
                if(currentQueueSize >= servers) {
                    scheduleDeparture();
                    remainingRandoms--;
                }
            }
        }
        
        saveReport();
        resetSimulation();
    }
    
    public void printReportMean() {
        SimulationReport report = SimulationReport.meanResults(reportList);
        System.out.printf("Queue model: G/G/%d/%d\n", servers, queueMax);
        System.out.println("Simulation Results:");
        System.out.println("Showing mean results of " + reportList.size() +
                           " simulations");
        System.out.println(report.toString());
    }
    
    public void printReportMeanOnFile(String file) {
        SimulationReport report = SimulationReport.meanResults(reportList);
        BufferedWriter bwriter;
        try {
            bwriter = new BufferedWriter(new FileWriter(file));
            bwriter.write(String.format("Queue model: G/G/%d/%d\n", servers, queueMax));
            bwriter.write("Simulation Results:\n");
            bwriter.write("Showing mean results of " + reportList.size() +
                          " simulations\n");
            bwriter.write(report.toString());
            bwriter.close();
            System.out.println("Simulation results printed on file \""+file+"\".");
        
        } catch(IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}












