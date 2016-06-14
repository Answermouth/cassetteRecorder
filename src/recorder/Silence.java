package recorder;

public class Silence {

    private long start, end;
    
    public Silence() {
        start = -1;
        end = -1;
    }
    
    public void setStart(long start) {
        this.start = start;
    }
    
    public long getStart() {
        return start;
    }
    
    public void setEnd(long end) {
        this.end = end;
    }
    
    public long getEnd() {
        return end;
    }
    
    public String toString() {
        return "start : " + start + " | end : " + end;
    }
}
