import java.io.*;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.jlab.io.hipo.*;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.coda.hipo.*;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.decode.CLASDecoder;
import org.apache.commons.lang3.StringUtils;

public class SkimRaw2TrackEvents {

	public static void main(String[] args) throws IOException {

		// create a list of desired event numbers
		BufferedReader txtReader = new BufferedReader(new FileReader("twoTrackEvents_809.txt"), 200);
		
		HashMap<Integer, ArrayList<Integer>> eventNumbersForFileNumbers = new HashMap<Integer, ArrayList<Integer>>();
		
		String line;
		while ((line = txtReader.readLine()) != null) {
			String[] row = line.split(Pattern.quote(" "));
			int fileNumber = Integer.parseInt(StringUtils.substringBetween(row[0], "809_", ".hipo"));
			int eventNumber = Integer.parseInt(row[2]);
			if(!eventNumbersForFileNumbers.containsKey(fileNumber)) eventNumbersForFileNumbers.put(fileNumber, new ArrayList<>());
			eventNumbersForFileNumbers.get(fileNumber).add(eventNumber);
		}
		
		txtReader.close();
		System.out.println(eventNumbersForFileNumbers);
		

		// create decoder and output skim file
		CLASDecoder decoder = new CLASDecoder(false);
		EvioDataSync writer = new EvioDataSync("twoTrackEvents_809_raw.evio");
		
		// loop over input events
		int eventCount = 0;
		
		for(Entry<Integer, ArrayList<Integer>> entry : eventNumbersForFileNumbers.entrySet()) {
			int fileNumber = entry.getKey();
			ArrayList<Integer> eventNumbers = entry.getValue();
			EvioSource reader = new EvioSource();
			reader.open(String.format("/cache/clas12/kpp/data/clas_000809.evio.%d", fileNumber));
		
			System.out.println("starting file " + fileNumber);
		
			while(reader.hasEvent()) {
				eventCount++;
				EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
				DataEvent decodedEvent = decoder.getDataEvent(event);
				DataBank header = decoder.createHeaderBank(decodedEvent, -1, -1, (float) 123.4, (float) 123.4);
				decodedEvent.appendBanks(header);
				int thisEventNumber = decodedEvent.getBank("RUN::config").getInt("event", 0);
		
				if(eventNumbers.contains(thisEventNumber)) {
					System.out.println("YES! " + eventCount + " " + thisEventNumber);
					writer.writeEvent(event);
				}
		
				if(eventCount%10000 == 0) System.out.println("   processed " + eventCount + " events.");
			}
		
			reader.close();
		}
		
		writer.close();
	
	}
}
