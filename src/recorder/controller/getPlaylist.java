package recorder.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import recorder.model.Playlist;
import recorder.model.Title;

public class getPlaylist {
    public static Playlist playlistForm() {
        Playlist playlist = new Playlist();
        
        Title title = new Title("I Never Talk To Stangers", "Tom Waits", "Anthology of Tom Waits", "03:36");
        playlist.addTitle(title);
        
        title = new Title("Somewhere(From West Side Story)", "Tom Waits", "Anthology of Tom Waits", "03:48");
        playlist.addTitle(title);
        
        title = new Title("Burma Shave", "Tom Waits", "Anthology of Tom Waits", "06:30");
        playlist.addTitle(title);
        
        return playlist;
    }
    
    public static Playlist load(String filename) {
        Playlist playlist = new Playlist();
        
        File playlistCsv;
        BufferedReader reader;
        Scanner scanner;
        String line, name, artists, album, length;
        
        try {
            //on essaye d'ouvrir les flux
            playlistCsv = new File(filename);
            reader = new BufferedReader(new FileReader(playlistCsv));
            
            reader.readLine();
            line = reader.readLine();
            
            while (line != null) {
                // tant que l'on a pas atteind la fin d'un des 2 fichiers

                scanner = new Scanner(line);
                scanner.useDelimiter(",");
                
                name = scanner.next();
                artists = scanner.next();
                album = scanner.next();
                length = scanner.next();
                
                playlist.addTitle(new Title(name, artists, album, length));
                
                scanner.close();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            //throw e;
        }
        
        return playlist;
    }
}
