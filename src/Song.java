public class Song {
	private String fileLocation;
	private int bpm;

	public Song (String fileLocation, int bpm) {
		this.fileLocation = fileLocation;
		this.bpm = bpm;
	}
	
	public String getFileLocation() {
		return fileLocation;
	}
	
	public int getBPM() {
		return bpm;
	}
	
	public String toString() {
		return "file location: " + fileLocation + " bpm: " + bpm;
	}
}
