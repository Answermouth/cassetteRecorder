package recorder.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import recorder.model.InstructionDecoupe;
import recorder.model.Silence;
import recorder.model.Playlist;

public class bashWriter {
    static String PATH = "E:/Test/";
    
    public static void main(String[] args) {
        
        String source = "E:/Test/RecordAudio.wav";
        
        //new AudioWaveformCreator(source, "E:/Test/test.png");
        
        ArrayList<Silence> silences = Silence.getSilences(source);
        
        Playlist playlist = getPlaylist.playlistForm();
        
        ArrayList<InstructionDecoupe> IDList = InstructionDecoupe.getInstructionDecoupeStupid(playlist, silences);
        
        ArrayList<String> commands = dumpInstructionsDecoupe(IDList, "E:/Test/bash.bat", source);
        
        try {
            for (InstructionDecoupe id : IDList) {
                boolean successful;
                File artistPath = new File(PATH+id.getArtists());
                File albumPath = new File(PATH+id.getArtists()+"/"+id.getAlbum());
                
                if (!artistPath.exists()) {
                    successful = artistPath.mkdir();
                    if (successful)
                        System.out.println("Artist added to library");
                    else 
                        System.out.println("Unable to add artist to library");
                } else {
                    System.out.println("Artist already in library");
                }
                
                if (!albumPath.exists()) {
                    successful = albumPath.mkdir();
                    if (successful)
                        System.out.println("Album added to library");
                    else 
                        System.out.println("Unable to add album to library");
                } else {
                    System.out.println("Album already in library");
                }
                    
            }
            
            for (String cmd : commands)
                Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> dumpInstructionsDecoupe(ArrayList<InstructionDecoupe> liste, String nom_fichier, String source){
        try {
            ArrayList<String> textTotal = new ArrayList<String>();
            PrintStream ps = new PrintStream(new File(nom_fichier));
            InstructionDecoupe id;
            for(int i = 0; i< liste.size(); i++){
                id = liste.get(i);
                // Colonnes : fichier wave source, durée théorique de la musiquen durée réelle, temps de début, temps de fin, nom de la usique, nom de l'artiste, nom de l'album
                // ffmpeg -ss <temps_debut ss.mmm> -t <duree ss.mmm> -i <fichier_source> -codec:a libmp3lame -qscale:a 0 <fichier dest>
                String text = "ffmpeg.exe -ss "+
                        id.getDebut()+
                        " -t "+
                        id.getLength()+
                        " -i \""+
                        source+
                        "\" -id3v2_version 3"+
                        " -metadata track=\"" + (i+1) +
                        "\"" +
                        " -metadata title=\"" + id.getName() +
                        "\"" +
                        " -metadata artist=\"" + id.getArtists() +
                        "\"" +
                        " -metadata album=\"" + id.getAlbum() +
                        "\"" +
                        " -codec:a libmp3lame -qscale:a 0 "+
                        "\""+
                        PATH+
                        id.getArtists()+"/"+
                        id.getAlbum()+"/"+
                        id.getArtists()+
                        " - "+
                        id.getName()+
                        ".mp3\"";
                //System.out.println(text);
                textTotal.add(text);
                ps.println(text);
            }
            ps.close();
            return textTotal;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
