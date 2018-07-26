import org.jlab.io.hipo.*;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.hipo.*;
import org.jlab.clas.physics.Particle;

int eventCount = 0;

for(int k = 0; k < args.length; k++)
{
	HipoDataSource reader = new HipoDataSource();
	reader.open(args[k]);

	System.out.println("starting file " + k + " (" + args[k] + ")...");

	while(reader.hasEvent())
	{
		eventCount++;
		HipoDataEvent event = reader.getNextEvent();

		if(event.hasBank("RUN::config") && event.hasBank("REC::Particle") && event.getBank("REC::Particle").rows() >= 2) {
			DataBank partBank = event.getBank("REC::Particle");
			DataBank configBank = event.getBank("RUN::config");
			int runN = configBank.getInt("run", 0);
			int eventN = configBank.getInt("event", 0);
			boolean foundElectron = false;
			boolean foundChargedTrack = false;
			for(int j = 0; j < partBank.rows(); j++) {
				int pid = partBank.getInt("pid", j);
				byte charge = partBank.getByte("charge", j);
				if(pid == 11) foundElectron = true;
				else if(charge != 0) foundChargedTrack = true;
			}
			if(foundElectron && foundChargedTrack) {
				System.err.println(args[k] + " " + runN + " " + eventN);
				System.out.println(args[k] + " " + runN + " " + eventN);
			}
		}

		if(eventCount%10000 == 0) {
			System.out.println("   processed " + eventCount + " events.");
		}
	}
}
