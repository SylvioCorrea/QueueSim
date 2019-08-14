import java.util.PriorityQueue;
import java.util.Comparator;

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
    
    private static class SchEntComparator implements Comparator<ScheduleEntry> {
        public int compare(ScheduleEntry se1, ScheduleEntry se2) {
            if(se1.time < se2.time)
                return -1;
            if(se1.time > se2.time)
                return 1;
            return 0;
        }
    }
    //=================================================================
    
    
    
    private int servers;
    private int queueMax;
    private int simulationLength;
    
    private double arrivalMin;
    private double arrivalMax;
    private double serviceMin;
    private double serviceMax;
    
    private double time;
    private int currentQueueSize;
    private double[] stateTime;
    private PriorityQueue<ScheduleEntry> schedule;
    
    private RNG rng;
    
    public QueueSim(int servers, int queueMax, int simulationLength,
                    double arrivalMin, double arrivalMax,
                    double serviceMin, double serviceMax, long randomSeed) {
        this.servers = servers;
        this.queueMax = queueMax;
        this.simulationLength = simulationLength;
        this.arrivalMin = arrivalMin;
        this.arrivalMax = arrivalMax;
        this.serviceMin = serviceMin;
        this.serviceMax = serviceMax;
        time = 0.0;
        currentQueueSize = 0;
        stateTime = new double[queueMax+1];
        schedule = new PriorityQueue<ScheduleEntry>(new SchEntComparator());
        rng = new RNG(randomSeed);
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
    
    public void runSimulation() {
        
        scheduleArrival();
        
        for(int i=0; i<simulationLength; i++) {
            
            ScheduleEntry se = schedule.poll();
            double timeDelta = se.time-time;
            stateTime[currentQueueSize] += timeDelta;
            time += timeDelta;
            
            if(se.event == Event.ARRIVAL) {
                if(currentQueueSize < queueMax) {
                    currentQueueSize++;
                    if(currentQueueSize<=servers)
                        scheduleDeparture();
                }
                scheduleArrival();
            } else {
                currentQueueSize--;
                if(currentQueueSize >= 1) {
                    scheduleDeparture();
                }
            }
        }
        
        System.out.println("Simulation Results:");
        for(int i=0; i<queueMax+1; i++) {
            double result = stateTime[i]/time;
            System.out.printf("State: %d  time: %.2f  probability: %.2f\n", i, stateTime[i], stateTime[i]/time);
        }
        System.out.printf("Total time: %.2f\n", time);
        
    }
}












