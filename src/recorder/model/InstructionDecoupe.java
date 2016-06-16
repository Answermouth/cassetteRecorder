package recorder.model;

import java.util.ArrayList;
import recorder.model.Playlist;

public class InstructionDecoupe{
    
    static int SAMPLE_RATE = 48000; 
    static int MARGIN = SAMPLE_RATE/2;
    
    long debut; // Temps en millisecondes
    long fin; // Temps en millisecondes
    
    Title musique; 
    
    public InstructionDecoupe(Title musique, long debut, long fin){
        this.musique = musique;
        this.debut = debut;
        this.fin = fin;
    }
    
    public String getName() {
        return musique.getName();
    }
    
    public String getArtists() {
        return musique.getArtists();
    }
    
    public String getAlbum() {
        return musique.getAlbum();
    }
    
    public String getDebut() {
        return samplesVersSSMMM(debut);
    }
    
    public static String samplesVersSSMMM(long valeur){
        long millis = valeur/(SAMPLE_RATE/1000);
        long secondes = valeur/SAMPLE_RATE;        
        return secondes+"."+millis;
    }
    
    public String getLength() {
        return samplesVersSSMMM(musique.getLength());
    }
    
    public String toString() {
        return musique.toString() + " | " + debut + " | " + fin;
    }
    
    public static ArrayList<InstructionDecoupe> getInstructionDecoupeSmart(Playlist playlist, ArrayList<Silence> silences) {
        ArrayList<InstructionDecoupe> IDList = new ArrayList<InstructionDecoupe>();
        InstructionDecoupe ID;
        long start = silences.get(0).getEnd();
        long end = silences.get(1).getStart();
        int i = 0;
        
        
        for (Title t : playlist.getTitles()) {            
            if (t.getLength()*SAMPLE_RATE + start > end - MARGIN && t.getLength()*SAMPLE_RATE + start < end + MARGIN) {
                ID = new InstructionDecoupe(t, start, start-end);
                i++;
                if (i+1 < silences.size()) {
                    start = silences.get(i).getEnd();
                    end = silences.get(i+1).getStart();
                }
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
            ID = new InstructionDecoupe(playlist.getTitles().get(i), start, (end-start));
            IDList.add(ID);
        }
                
        return IDList; 
    }
}
