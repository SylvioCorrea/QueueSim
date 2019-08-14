public class RNG {
    private long a;
    private long c;
    private long m;
    private long seed;
    
    private long x;
    
    //Parameters based on java implementation according to wikipedia.
    public RNG(long seed) {
        a = Long.parseUnsignedLong("25214903917");
        c = 11L;
        m = 1L<<48;
        this.seed = seed;
        x = seed;
    }
    
    //Return range: [0, 1)
    public double next() {
        x = Long.remainderUnsigned(a*x+c , m);
        //System.out.println(x);
        double res = x/(double)m;
        //System.out.println(res);
        //System.out.println();
        return res;
    }
}