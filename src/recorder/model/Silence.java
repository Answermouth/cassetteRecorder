package recorder.model;

import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Silence {

    private static int THRESHOLD = 5800; 
    private static int sampleRate = 48000;

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
    
    public long getLength() {
        return end - start;
    }
    
    public String toString() {
        long startMinutes = start/sampleRate/60;
        float startSecondes = (((float)start) /sampleRate)%60;
        
        long endMinutes = end/sampleRate/60;
        float endSecondes = (((float)end) /sampleRate)%60;
        
        return "start : " +startMinutes+":"+startSecondes+ "\t | end : " +endMinutes+":"+endSecondes;
    }
    
    public String toString(String format) {
        if (format.equals("samples"))
            return "start : " + start + " | end : " + end;
        else
            return toString();
    }
    
    public static ArrayList<Silence> getSilences(String filename) {
        boolean silence = false;
        long totalRead = 0;
        ArrayList<Silence> silences = new ArrayList<Silence>();
        try
        {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
            
            System.out.println(ais.getFormat());
            
            int sampleRate = Math.round(ais.getFormat().getSampleRate());
            
            int numBytes = ais.available();
            System.out.println("numbytes: "+numBytes+" | numsamples: "+numBytes/4);
            byte[] buffer = new byte[numBytes];
            int count=0;
            while(count!=-1){
                count=ais.read(buffer, 0, numBytes);
            }
            
            
            long value1 = 0;
            long value2 = 0;

            int byte1, byte2;
            
            SoundLevel level = new SoundLevel();
            
            /*
            level.fill(buffer);
            level.calculateSums();
            level.calculateLevels();
            */
            
            
            long max = 0;
            for (int i = 0; i < buffer.length; i+=4)
            {
                totalRead++;
                byte1 = (java.lang.Math.abs(buffer[i]) & 0xffd);
                byte2 = (java.lang.Math.abs(buffer[i+1]) & 0xffd);
                
                if (byte1 <= 1 && byte2 <= 1) {
                    byte1 = 0;
                    byte2 = 0;
                }
                value1 = (byte1 << 8) + byte2;
               
                byte1 = (java.lang.Math.abs(buffer[i+2]) & 0xffd);
                byte2 = (java.lang.Math.abs(buffer[i+3]) & 0xffd);
               
                if (byte1 <= 2 && byte2 <= 2) {
                    byte1 = 0;
                    byte2 = 0;
                }
               
                value2 = (byte1 << 8) + byte2;
               
                
                level.add(value1, value2);
               
                if (value1 > max)
                    max = value1;
                
                if (value2 > max)
                    max = value2;
                
                if (level.getLevels()[0] < THRESHOLD && level.getLevels()[1] < THRESHOLD && silence == false) {
                    silence = true;
                    silences.add(new Silence());
                    silences.get(silences.size()-1).setStart(totalRead - SoundLevel.getSampleLength());
                    //System.out.println(level.toString());
                }
                
                if (level.getLevels()[0] > THRESHOLD && level.getLevels()[1] > THRESHOLD && silence == true) {
                    silence = false;
                    silences.get(silences.size()-1).setEnd(totalRead - 3*sampleRate/4);
                }                
            }
            
            
            if (silences.get(silences.size()-1).getEnd() == -1)
                silences.get(silences.size()-1).setEnd(totalRead);
            
            return silences;
            
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
