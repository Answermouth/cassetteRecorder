package recorder.model;

public class Title {
    private String name, artists, album;
    private long length;
    
    public Title(String name, String artists, String album, String length) {
        this.name = name;
        this.artists = artists;
        this.album = album;
        
        this.length = timeStringToLong(length);
    }
    
    public Title(String name, String artists, String album, long length) {
        this.name = name;
        this.artists = artists;
        this.album = album;
        
        this.length = length;
    }
    
    public long timeStringToLong(String timeString) {
        return (Integer.parseInt(timeString.substring(3)) + Integer.parseInt(timeString.substring(0, 2))*60) * 48000;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getArtists() {
        return artists;
    }
    
    public void setArtists(String artists) {
        this.artists = artists;
    }
    
    public String getAlbum() {
        return album;
    }
    
    public void setAlbum(String album) {
        this.album = album;
    }
    
    public long getLength() {
        return length;
    }
    
    public void setLength(String length) {
        this.length = timeStringToLong(length);
    }
    
    public void setLength(long length) {
        this.length = length;
    }
    
    public String toString() {
        long minutes = length/60;
        long secondes = length%60;
        return "title : "+name+" | artists: "+artists+" | album: "+album+" | length: "+minutes+":"+secondes;
    }
}
