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
    
    private double arrivalMin;
    private double arrivalMax;
    private double serviceMin;
    private double serviceMax;
    
    private double time;
    private double[] stateTime;
    private PriorityQueue<ScheduleEntry> schedule;
    
    private RNG rng;
    
    public QueueSim(int servers, int queueMax,
                    double arrivalMin, double arrivalMax,
                    double serviceMin, double serviceMax, long randomSeed) {
        this.servers = servers;
        this.queueMax = queueMax;
        this.arrivalMin = arrivalMin;
        this.arrivalMax = arrivalMax;
        this.serviceMin = serviceMin;
        this.serviceMax = serviceMax;
        time = 0.0;
        stateTime = new double[queueMax+1];
        schedule = new PriorityQueue<ScheduleEntry>(new SchEntComparator());
        rng = new RNG(randomSeed);
    }
    
    private double arrivalTime() {
        return (arrivalMax-arrivalMin) * rng.next() + arrivalMin;
    }
}