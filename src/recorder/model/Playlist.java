package recorder.model;

import java.util.ArrayList;

public class Playlist {
    
    private ArrayList<Title> titles;
    private int side;
    
    public Playlist(int side) {
        this.side = side;
        titles = new ArrayList<Title>();
    }
    
    public void addTitle(Title title) {
        titles.add(title);
    }
    
    public ArrayList<Title> getTitles() {
        return titles;
    }
    
    public int length() {
        return titles.size();
    }
    
    public int getSide() {
        return side;
    }
    
    public void setSide(int side) {
        this.side = side;
    }
    
    public String toString() {
        String text = "";
        
        for (Title t : titles) {
            text += t.toString()+"\n";
        }
        
        return text;
    }    
}
