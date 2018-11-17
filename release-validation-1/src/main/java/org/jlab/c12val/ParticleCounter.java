package org.jlab.c12val;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ParticleCounter {

  public ArrayList<Integer> pids, genCounts, recCounts;
  public static final HashMap<Integer, Integer> chargeOfPid = new HashMap<Integer, Integer>();
  static {
    chargeOfPid.put(11, -1);
    chargeOfPid.put(-11, 1);
    chargeOfPid.put(211, 1);
    chargeOfPid.put(-211, -1);
    chargeOfPid.put(2212, 1);
    chargeOfPid.put(321, 1);
    chargeOfPid.put(-321, -1);
    chargeOfPid.put(22, 0);
    chargeOfPid.put(2112, 0);
  }


  public ParticleCounter(int... pids) {
    this.pids = new ArrayList<>();
    for(int pid : pids) this.pids.add(pid);
    this.genCounts = new ArrayList<Integer>(Collections.nCopies(this.pids.size(), 0));
    this.recCounts = new ArrayList<Integer>(Collections.nCopies(this.pids.size(), 0));
  }


  public void processEvent(DataEvent event) {
    DataBank genBank, recBank;
    if(event.hasBank("MC::Particle")) genBank = event.getBank("MC::Particle");
    else genBank = null;
    if(event.hasBank("REC::Particle")) recBank = event.getBank("REC::Particle");
    else recBank = null;
    if(genBank != null) processGenRecBanks(genBank, recBank);
  }


  public void processGenRecBanks(DataBank genBank, DataBank recBank) {
    for(int i = 0; i < genBank.rows(); i++) {
      int pid = genBank.getInt("pid", i);
      if(pids.contains(pid)) {
        int ipid = pids.indexOf(pid);
        genCounts.set(ipid, genCounts.get(ipid) + 1);
        Integer charge = chargeOfPid.get(pid);
        if(recBank != null && recBank.rows() > 0) processRecBank(recBank, pid, charge, genBank.getFloat("px", i), genBank.getFloat("py", i), genBank.getFloat("pz", i));
      }
    }
  }


  public void processRecBank(DataBank recBank, int gpid, Integer gq, double gpx, double gpy, double gpz) {
    for(int i = 0; i < recBank.rows(); i++) {
      byte rq = recBank.getByte("charge", i);
      float rpx = recBank.getFloat("px", i);
      float rpy = recBank.getFloat("py", i);
      float rpz = recBank.getFloat("pz", i);
      int ipid = pids.indexOf(gpid);
      if(gq == null || gq == rq) {
        if(Math.abs(gpx - rpx) < 0.2 && Math.abs(gpy - rpy) < 0.2 && Math.abs(gpz - rpz) < 0.2) recCounts.set(ipid, recCounts.get(ipid) + 1);
      }
    }
  }


  public void printResults() {
    System.out.printf("%-7s %-7s %-7s %-7s %n", "pid", "#gen", "#rec", "#percent");
    for(int i = 0; i < pids.size(); i++) {
        if(genCounts.get(i) >0) {
            System.out.printf("%-7d %-7d %-7d %-2f %n", pids.get(i), genCounts.get(i), recCounts.get(i), 100.0*recCounts.get(i)/genCounts.get(i));
        }
    }
  }


  public static void main(String[] args) {
    ParticleCounter pcounter = new ParticleCounter(11, 211, -211, 2212, 321, 22, 2112);
    HipoDataSource reader = new HipoDataSource();
    reader.open("out_gemcout.hipo");
    while(reader.hasEvent()) {
      DataEvent event = reader.getNextEvent();
      pcounter.processEvent(event);
    }
    pcounter.printResults();
  }

}
