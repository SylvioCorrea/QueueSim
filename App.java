public class App {
    public static void main(String[] args) {
        QueueSim q = new QueueSim(1, 5, 25, 3.0, 6.0, 4.0, 8.0, 12345L);
        q.runSimulation();
    }
}