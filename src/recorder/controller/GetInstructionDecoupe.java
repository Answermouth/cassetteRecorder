package recorder.controller;

import java.util.ArrayList;

import recorder.model.InstructionDecoupe;
import recorder.model.Playlist;
import recorder.model.Silence;
import recorder.model.Title;

import recorder.RecorderConstants;

public class GetInstructionDecoupe {
    
    public static ArrayList<InstructionDecoupe> getInstructionDecoupeSmart(Playlist playlist, ArrayList<Silence> silences) {
        ArrayList<InstructionDecoupe> IDList = new ArrayList<InstructionDecoupe>();
        InstructionDecoupe ID;
        long start = silences.get(0).getEnd();
        long end = silences.get(1).getStart();
        int i = 0;
        
        
        for (Title t : playlist.getTitles()) {            
            if (t.getLength()*RecorderConstants.SAMPLE_RATE + start > end - RecorderConstants.MARGIN && 
                t.getLength()*RecorderConstants.SAMPLE_RATE + start < end + RecorderConstants.MARGIN) {
                ID = new InstructionDecoupe(t, playlist.getSide(), start, start-end);
                i++;
                if (i+1 < silences.size()) {
                    start = silences.get(i).getEnd();
                    end = silences.get(i+1).getStart();
                }
                IDList.add(ID);
            } else {
                i++;
                if (i+1 < silences.size())
                    end = silences.get(i+1).getStart();
            }
        }
        
        for (InstructionDecoupe id : IDList)
            System.out.println(id.toString());
        
        return IDList; 
    }
    
    public static ArrayList<InstructionDecoupe> getInstructionDecoupeStupid(Playlist playlist, ArrayList<Silence> silences) {
        ArrayList<InstructionDecoupe> IDList = new ArrayList<InstructionDecoupe>();
        InstructionDecoupe ID;
        long start;
        long end;
        
        for (int i = 0; i < playlist.getTitles().size(); i++ ) {            
            start = silences.get(i).getEnd();
            end = silences.get(i+1).getStart();
            ID = new InstructionDecoupe(playlist.getTitles().get(i), playlist.getSide(), start-RecorderConstants.MARGIN, (end-start)+RecorderConstants.MARGIN*2);
            IDList.add(ID);
        }
                
        return IDList; 
    }
}
