package recorder;

public class SoundLevel {
    static int SAMPLE_LENGTH = 96000;
    private int nbrValues; 
    private long[][] values;
    private long[] levels;
    private long[] sums;
    private int end, start;
    
    public static int getSampleLength() {
        return SAMPLE_LENGTH;
    }
    
    public SoundLevel() {
        nbrValues = 0;
        start = 0;
        end = 0;
        values = new long[SAMPLE_LENGTH][2];
        sums = new long[2];
        levels = new long[2];
        sums[0] = sums[1] = 0;
        levels[0] = levels[1] = Long.MAX_VALUE;
    }
    
    public void fill(byte[] valuesByte) {
        try {
            int byte1, byte2;
            for (int i=0; i < values.length; i+=1) {
                byte1 = (java.lang.Math.abs(valuesByte[i*4]) & 0xffd);
                byte2 = (java.lang.Math.abs(valuesByte[i*4+1]) & 0xffd);
                
                if (byte1 <= 1 && byte2 <= 1) {
                    byte1 = 0;
                    byte2 = 0;
                }
                values[i][0] = (byte1 << 8) + byte2;
                
                byte1 = (java.lang.Math.abs(valuesByte[i*4+2]) & 0xffd);
                byte2 = (java.lang.Math.abs(valuesByte[i*4+3]) & 0xffd);
                
                if (byte1 <= 1 && byte2 <= 1) {
                    byte1 = 0;
                    byte2 = 0;
                }
                values[i][1] = (byte1 << 8) + byte2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void calculateSums() {
        sums[0] = sums[1] = 0;
        for (long[] i : values) {
            sums[0] += i[0];
            sums[1] += i[1];
        }
    }
    
    public void updateSums(long old1, long old2) {
        sums[0] = sums[0] + values[end][0] - old1;
        sums[1] = sums[1] + values[end][1] - old2;
    }
    
    public void calculateLevels() {
        levels[0] = sums[0] / SAMPLE_LENGTH;
        levels[1] = sums[1] / SAMPLE_LENGTH;
    }
    
    
    public void add(long value1, long value2) {
        //System.out.println("add");
        if (nbrValues == SAMPLE_LENGTH) {
            long old1 = values[start][0];
            long old2 = values[start][1];
            
            end = start;
            start = (end+1) % SAMPLE_LENGTH;
            
            values[end][0] = value1;
            values[end][1] = value2;
            
            updateSums(old1, old2);
            calculateLevels();
        } else {
            values[end][0] = value1;
            values[end][1] = value2;
            end++;
            nbrValues++;
            
            if (nbrValues == SAMPLE_LENGTH) {
                System.out.println("full");
                end--;
                calculateSums();
                calculateLevels();
                System.out.println(sums[0]+" | "+sums[1]);
                System.out.println(levels[0]+" | "+levels[1]);
                System.out.println("full");
                
            }
        }
    }
    
    public long[] getLevels() {
        return levels;
    }
}
