package recorder;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;

public class test {
    private static int THRESHOLD = 2000; 
    
	public static long getUnsignedInt(int x) {
	    return x & 0x00000000ffffffffL;
	}
		
	public static void main(String[] args) {
	    boolean silence = false;
	    long totalRead = 0;
	    ArrayList<Silence> silences = new ArrayList<Silence>();
		try
        {
            //AudioInputStream ais = AudioSystem.getAudioInputStream(new File("E:/Test/RecordAudio.wav"));
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File("E:/Test/silence.wav"));
			//AudioInputStream ais = AudioSystem.getAudioInputStream(new File("E:/Test/silence2.wav"));
			
			System.out.println(ais.getFormat());
			
            int numBytes = ais.available();
            System.out.println("numbytes: "+numBytes);
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
                
                //System.out.println("value1: "+value1+" | "+Long.toBinaryString(value1));
                //System.out.println("value2: "+value2+" | "+Long.toBinaryString(value2));
               
                if (value1 > max)
                    max = value1;
                
                if (value2 > max)
                    max = value2;
                
                if (level.getLevels()[0] < THRESHOLD && level.getLevels()[1] < THRESHOLD && silence == false) {
                    silence = true;
                    silences.add(new Silence());
                    silences.get(silences.size()-1).setStart(totalRead-SoundLevel.getSampleLength());
                }
                
                if (level.getLevels()[0] > THRESHOLD && level.getLevels()[1] > THRESHOLD && silence == true) {
                    silence = false;
                    silences.get(silences.size()-1).setEnd(totalRead);
                }
                
                //System.out.println(level.getLevels()[0]);
            }
            System.out.println(max);
            
            for (Silence s: silences)
                System.out.println(s);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }	
}

