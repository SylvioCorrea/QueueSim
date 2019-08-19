public class App {
    public static void main(String[] args) {
        
        //Creates a queue simulator according to the specification passed to the constructor
        QueueSim q1 = new QueueSim(
            1,      //servers
            5,      //Queue capacity
            100000, //Amount of random numbers to be used
            2.0,    //Arrival min
            4.0,    //Arrival max
            3.0,    //Service min
            5.0     //Service max
            );
        
        //Random seeds to be used in the simulation
        long[] randomSeeds = new long[] {1234L, 3451L, 892L, 7765L, 40101L};
        
        //Runs one simulation for each random seed in the array
        q1.runSimulation(randomSeeds);
        //Prints the mean result of all simulations on a file
        q1.printReportMeanOnFile("simulation1.txt");
        
        
        //Below is another simulation using a different queue, with the same random seeds
        QueueSim q2 = new QueueSim(
            2,      //servers
            5,      //Queue capacity
            100000, //Amount of random numbers to be used
            2.0,    //Arrival min
            4.0,    //Arrival max
            3.0,    //Service min
            5.0     //Service max
            );
        
        q2.runSimulation(randomSeeds);
        q2.printReportMeanOnFile("simulation2.txt");
    }
}