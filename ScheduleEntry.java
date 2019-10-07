import java.util.Comparator;

/* Used to schedule events during simulation. */
public class ScheduleEntry implements Comparator<ScheduleEntry> {
    
	EventEnum event;
    double time;
    QueueStructure origin;
	QueueStructure destination;
	
	/* Arrivals require destination != null
	 * Departures require origin != null
	 * Passages require both origin and destination != null */
	public ScheduleEntry(EventEnum event, double time, QueueStructure origin, QueueStructure destination) {
		this.event = event;
		this.time = time;
		this.origin = origin;
		this.destination = destination;
	}
	
	public ScheduleEntry() {
		
	}
	
	public static ScheduleEntry newArrival(double time, QueueStructure destination) {
		return new ScheduleEntry(EventEnum.ARRIVAL, time, null, destination);
	}
	public static ScheduleEntry newDeparture(double time, QueueStructure origin) {
		return new ScheduleEntry(EventEnum.DEPARTURE, time, origin, QueueStructure.EXIT);
	}
	public static ScheduleEntry newPassage(double time, QueueStructure origin, QueueStructure destination) {
		return new ScheduleEntry(EventEnum.PASSAGE, time, origin, destination);
	}
	
	public int compare(ScheduleEntry se1, ScheduleEntry se2) {
        if(se1.time < se2.time)
            return -1;
        if(se1.time > se2.time)
            return 1;
        return 0;
    }
	
	public ScheduleEntry clone() {
		return new ScheduleEntry(event, time, origin, destination);
	}
}
//===========================================================