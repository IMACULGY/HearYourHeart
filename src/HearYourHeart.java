import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class HearYourHeart {

	public static void main(String[] args)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		File f = new File("songs.txt");
		Scanner scanLine = new Scanner(f);
		Scanner reader;

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

		LocalDateTime now = LocalDateTime.now().minusMinutes(15);
		LocalDateTime oldNow = LocalDateTime.now().minusMinutes(16);
		String time = dtf.format(now);
		String oldTime = dtf.format(oldNow);

		// for making api requests
		String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMkQ5VjgiLCJzdWIiOiI3OFdHOVgiLCJpc3MiOiJGaXRiaXQiLCJ0eXAiOiJhY2Nlc3NfdG9rZW4iLCJzY29wZXMiOiJyc29jIHJzZXQgcmFjdCBybG9jIHJ3ZWkgcmhyIHJwcm8gcm51dCByc2xlIiwiZXhwIjoxNTQ4NTgzMjc1LCJpYXQiOjE1NDc5Nzg0NzV9.nsNqUHyT12a093JDAT7CzQLqWHsQ16S_MABgrzyFIzQ";
		GenericUrl url = new GenericUrl("https://api.fitbit.com/1/user/78WG9X/activities/heart/date/today/1d/1sec/time/"
				+ oldTime + "/" + time + ".json");
		JsonFactory parse = new JacksonFactory();
		HttpTransport transport = new NetHttpTransport();

		ArrayList<Song> songs = new ArrayList<Song>();

		String input;
		while (scanLine.hasNextLine()) {
			input = scanLine.nextLine();
			reader = new Scanner(input);
			String loc = reader.next();
			int bpm = reader.nextInt();
			songs.add(new Song(loc, bpm));
		}

		for (Song s : songs)
			System.out.println(s);

		now = LocalDateTime.now().minusMinutes(15);
		oldNow = LocalDateTime.now().minusMinutes(16);
		time = dtf.format(now);
		oldTime = dtf.format(oldNow);

		url = new GenericUrl("https://api.fitbit.com/1/user/78WG9X/activities/heart/date/today/1d/1sec/time/" + oldTime
				+ "/" + time + ".json");
		HttpResponse request = executeGet(transport, parse, accessToken, url);
		String heartInfo = request.parseAsString();
		System.out.println(heartInfo);
		System.out.println(heartInfo.indexOf("value")); // 370
		String heartRateStr = "";
		for (int i = heartInfo.indexOf("value") + 8; heartInfo.charAt(i) >= '0' && heartInfo.charAt(i) <= '9'
				|| heartInfo.charAt(i) == '.'; i++)
			heartRateStr += heartInfo.charAt(i);
		System.out.println(heartRateStr);
		int targetBPM = (int) Double.parseDouble(heartRateStr);
		System.out.println(targetBPM);

		System.out.println(heartInfo);

		while (true) {
			int minDiff;
			int minIndex = 0;
			minDiff = 300;
			for (int i = 0; i < songs.size(); i++) {

				if (Math.abs(songs.get(i).getBPM() - targetBPM) < minDiff) {
					minDiff = Math.abs(songs.get(i).getBPM() - targetBPM);
					minIndex = i;
				}
			}
			playSong(songs.get(minIndex));

			now = LocalDateTime.now().minusMinutes(15);
			oldNow = LocalDateTime.now().minusMinutes(16);
			time = dtf.format(now);
			oldTime = dtf.format(oldNow);

			url = new GenericUrl("https://api.fitbit.com/1/user/78WG9X/activities/heart/date/today/1d/1sec/time/"
					+ oldTime + "/" + time + ".json");
			request = executeGet(transport, parse, accessToken, url);
			heartInfo = request.parseAsString();
			System.out.println(heartInfo.indexOf("value")); // 370
			heartRateStr = "";
			for (int i = 370; heartInfo.charAt(i) >= '0' && heartInfo.charAt(i) <= '9'
					|| heartInfo.charAt(i) == '.'; i++)
				heartRateStr += heartInfo.charAt(i);
			System.out.println(heartRateStr);
			targetBPM = (int) Double.parseDouble(heartRateStr);
			System.out.println(targetBPM);

			System.out.println(heartInfo);

		}

	}

	public static void playSong(Song s)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(new File(s.getFileLocation()).getAbsoluteFile());
		Clip clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		clip.start();

		while (true) {
			Thread.sleep(5000);
			if (!clip.isRunning())
				break;

		}
		System.out.println("The song has finished!!");
	}

	public static HttpResponse executeGet(HttpTransport transport, JsonFactory jsonFactory, String accessToken,
			GenericUrl url) throws IOException {
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
				.setAccessToken(accessToken);
		HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
		return requestFactory.buildGetRequest(url).execute();
	}
}
