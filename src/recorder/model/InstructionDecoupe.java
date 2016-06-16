package recorder.model;

import recorder.RecorderConstants;

public class InstructionDecoupe{
    
    long debut; // Temps en millisecondes
    long length; // Temps en millisecondes
    int side;
    
    Title musique; 
    
    public InstructionDecoupe(Title musique, int side, long debut, long length){
        this.musique = musique;
        this.debut = debut;
        this.length = length;
        this.side = side;
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
        long millis = valeur/(RecorderConstants.SAMPLE_RATE/1000);
        long secondes = valeur/RecorderConstants.SAMPLE_RATE;        
        return secondes+"."+millis;
    }
    
    public String getLength() {
        return samplesVersSSMMM(length);
    }
    
    public int getSide() {
        return side;
    }
    
    public String toString() {
        return musique.toString() + " | " + debut + " | " + length;
    }
}
