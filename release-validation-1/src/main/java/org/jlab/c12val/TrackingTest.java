package org.jlab.c12val;

import java.io.File;
import org.junit.Test;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.jlab.clas.physics.LorentzVector;
import static org.junit.Assert.*;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;


import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;

import org.jlab.clas.physics.Particle;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * Analyze tracking results
 *
 * TODO:  
 * 
 * @author devita
 */
public class TrackingTest {

    static final boolean debug=false;
    
    String  analysisName = "TrackingTest";
    
    IndexedList<DataGroup> dataGroups      = new IndexedList<DataGroup>(1);
    EmbeddedCanvasTabbed   canvasTabbed    = null;
    ArrayList<String>      canvasTabNames  = new ArrayList<String>();
    
    // these correspond to Joseph's two-particle event generater:
    static final int electronSector=1;
    static final int hadronSector=3;

    boolean isForwardTagger=false;
    boolean isCentral=false;

    int fdCharge = 0;

    int nNegTrackEvents = 0;
    int nTwoTrackEvents = 0;
    
    int nEvents = 0;
    
    double ebeam = 10.6;

    @Test
    public static void main(String arg[]){
        
//        System.setProperty("java.awt.headless", "true"); // this should disable the Xwindow requirement
        GStyle.getAxisAttributesX().setTitleFontSize(24);
        GStyle.getAxisAttributesX().setLabelFontSize(18);
        GStyle.getAxisAttributesY().setTitleFontSize(24);
        GStyle.getAxisAttributesY().setLabelFontSize(18);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
//        GStyle.setPalette("kDefault");
        GStyle.getAxisAttributesX().setLabelFontName("Avenir");
        GStyle.getAxisAttributesY().setLabelFontName("Avenir");
        GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
        GStyle.getAxisAttributesX().setTitleFontName("Avenir");
        GStyle.getAxisAttributesY().setTitleFontName("Avenir");
        GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(1);
        GStyle.getH1FAttributes().setOptStat("1111");
        
        TrackingTest ttest = new TrackingTest();
        
        ttest.setAnalysisTabNames("Monte Carlo","Monte Carlo p negative","W", "Vertex","Positive Tracks","Negative Tracks");
        ttest.createHistos();
        
        String resultDir=System.getProperty("RESULTS");
        File dir = new File(resultDir);
        if (!dir.isDirectory()) {
            System.err.println("Cannot find output directory");
            assertEquals(false, true);
        }
        String inname = System.getProperty("INPUTFILE");
        //String fileName=resultDir + "/out_" + inname + ".hipo";
        String fileName="out_" + inname + ".hipo";
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.err.println("Cannot find input file.");
            assertEquals(false, true);
        }

        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            ttest.processEvent(event);
        }
        reader.close();

        JFrame frame = new JFrame("Tracking");
        frame.setSize(1200, 800);
        frame.add(ttest.canvasTabbed);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ttest.analyze();
        ttest.plotHistos();
       
        ttest.printCanvas(resultDir,inname);
        

    }


    private void processEvent(DataEvent event) {

        nEvents++;
        if((nEvents%10000) == 0) System.out.println("Analyzed " + nEvents + " events");
        int run = 0;
        if(event.hasBank("RUN::config")) {
            run = event.getBank("RUN::config").getInt("run", 0);            
        }
        else {
            return;
        }
        if(run>2365 && run<=2597)      ebeam=2.217;
        else if(run>3028 && run<=3105) ebeam=6.424;
        else if(run>3105 && run<=3817) ebeam=10.594;
        else if(run>3817 && run<=3861) ebeam=6.424;
        else if(run>3861)              ebeam=10.594;
        // process event info and save into data group
        Particle partGenNeg = null;
        Particle partGenPos = null;
        Particle partRecNeg = null;
        Particle partRecPos = null;        
        if(event.hasBank("TimeBasedTrkg::TBTracks")==true){
            DataBank  bank = event.getBank("TimeBasedTrkg::TBTracks");
            int rows = bank.rows();
            for(int loop = 0; loop < rows; loop++){
                int pidCode=0;
                if(bank.getByte("q", loop)==-1) pidCode = 11;
                else if(bank.getByte("q", loop)==1) pidCode = 211;
                else pidCode = 22;
                Particle recParticle = new Particle(
                                          pidCode,
                                          bank.getFloat("p0_x", loop),
                                          bank.getFloat("p0_y", loop),
                                          bank.getFloat("p0_z", loop),
                                          bank.getFloat("Vtx0_x", loop),
                                          bank.getFloat("Vtx0_y", loop),
                                          bank.getFloat("Vtx0_z", loop));
                if(bank.getShort("ndf", loop)>0) recParticle.setProperty("chi2", bank.getFloat("chi2", loop)/bank.getShort("ndf", loop));
                if(recParticle.charge()>0) {
                    dataGroups.getItem(2).getH1F("hi_p_pos").fill(recParticle.p());
                    dataGroups.getItem(2).getH1F("hi_theta_pos").fill(Math.toDegrees(recParticle.theta()));
                    dataGroups.getItem(2).getH1F("hi_phi_pos").fill(Math.toDegrees(recParticle.phi()));
                    dataGroups.getItem(2).getH1F("hi_chi2_pos").fill(recParticle.getProperty("chi2"));
                    dataGroups.getItem(2).getH1F("hi_vz_pos").fill(recParticle.vz());
                    dataGroups.getItem(4).getH2F("hi_vz_vs_theta_pos").fill(Math.toDegrees(recParticle.theta()),recParticle.vz());
                    if(recParticle.p()>2.&& Math.toDegrees(recParticle.theta())>10.) {
                        dataGroups.getItem(2).getH1F("hi_vz_pos_cut").fill(recParticle.vz());
                        dataGroups.getItem(4).getH2F("hi_vxy_pos").fill(recParticle.vx(),recParticle.vy());
                    }
                    dataGroups.getItem(2).getH2F("hi_theta_p_pos").fill(recParticle.p(),Math.toDegrees(recParticle.theta()));
                    dataGroups.getItem(2).getH2F("hi_theta_phi_pos").fill(Math.toDegrees(recParticle.phi()),Math.toDegrees(recParticle.theta()));
                    dataGroups.getItem(2).getH2F("hi_chi2_vz_pos").fill(recParticle.vz(),recParticle.getProperty("chi2"));
              }
                else {
                    dataGroups.getItem(1).getH1F("hi_p_neg").fill(recParticle.p());
                    dataGroups.getItem(1).getH1F("hi_theta_neg").fill(Math.toDegrees(recParticle.theta()));
                    dataGroups.getItem(1).getH1F("hi_phi_neg").fill(Math.toDegrees(recParticle.phi()));
                    dataGroups.getItem(1).getH1F("hi_chi2_neg").fill(recParticle.getProperty("chi2"));
                    dataGroups.getItem(1).getH1F("hi_vz_neg").fill(recParticle.vz());
                    dataGroups.getItem(4).getH2F("hi_vz_vs_theta_neg").fill(Math.toDegrees(recParticle.theta()),recParticle.vz());
                    if(recParticle.p()>2.&& Math.toDegrees(recParticle.theta())>15.) {
                        dataGroups.getItem(1).getH1F("hi_vz_neg_cut").fill(recParticle.vz());
                        dataGroups.getItem(4).getH2F("hi_vxy_neg").fill(recParticle.vx(),recParticle.vy());
                    }
                    dataGroups.getItem(1).getH2F("hi_theta_p_neg").fill(recParticle.p(),Math.toDegrees(recParticle.theta()));
                    dataGroups.getItem(1).getH2F("hi_theta_phi_neg").fill(Math.toDegrees(recParticle.phi()),Math.toDegrees(recParticle.theta()));                    
                    dataGroups.getItem(1).getH2F("hi_chi2_vz_neg").fill(recParticle.vz(),recParticle.getProperty("chi2"));
                    LorentzVector virtualPhoton = new LorentzVector(0., 0., ebeam, ebeam);
                    virtualPhoton.sub(recParticle.vector());
                    LorentzVector hadronSystem = new LorentzVector(0., 0., ebeam, 0.9383+ebeam);
                    hadronSystem.sub(recParticle.vector());
                    dataGroups.getItem(5).getH1F("hi_W").fill(hadronSystem.mass());
                    if(Math.toDegrees(recParticle.theta())<15) dataGroups.getItem(5).getH1F("hi_W_cut").fill(hadronSystem.mass());
                    dataGroups.getItem(5).getH2F("hi_W_phi").fill(hadronSystem.mass(),Math.toDegrees(recParticle.phi()));
                }
                if(partRecNeg==null && recParticle.charge()<0) partRecNeg=recParticle;
                if(partRecPos==null && recParticle.charge()>0) partRecPos=recParticle;
            }
        }
        if(event.hasBank("MC::Particle")==true){
            DataBank genBank = event.getBank("MC::Particle");
            int nrows = genBank.rows();
            for(int loop = 0; loop < nrows; loop++) {   
                Particle genPart = new Particle(
                                              genBank.getInt("pid", loop),
                                              genBank.getFloat("px", loop),
                                              genBank.getFloat("py", loop),
                                              genBank.getFloat("pz", loop),
                                              genBank.getFloat("vx", loop),
                                              genBank.getFloat("vy", loop),
                                              genBank.getFloat("vz", loop));
                if(genPart.pid()==11  && partGenNeg==null) partGenNeg=genPart;
                if(genPart.pid()==211 && partGenPos==null) partGenPos=genPart;
                if(partGenNeg != null && partRecNeg != null) {
                    if(testMCpart(partGenNeg,partRecNeg)) {
                        dataGroups.getItem(3).getH1F("hi_dp_neg").fill((partRecNeg.p()-partGenNeg.p())/partGenNeg.p());
                        dataGroups.getItem(3).getH2F("hi_dp_p_neg").fill(partGenNeg.p(),(partRecNeg.p()-partGenNeg.p())/partGenNeg.p());
                        dataGroups.getItem(3).getH2F("hi_dp_theta_neg").fill(Math.toDegrees(partGenNeg.theta()),(partRecNeg.p()-partGenNeg.p())/partGenNeg.p());
                        dataGroups.getItem(3).getH2F("hi_dp_phi_neg").fill(Math.toDegrees(partGenNeg.phi()),(partRecNeg.p()-partGenNeg.p())/partGenNeg.p());
                        dataGroups.getItem(3).getH1F("hi_dtheta_neg").fill(Math.toDegrees(partRecNeg.theta()-partGenNeg.theta()));
                        dataGroups.getItem(3).getH1F("hi_dphi_neg").fill(Math.toDegrees(partRecNeg.phi()-partGenNeg.phi()));
                        dataGroups.getItem(3).getH1F("hi_dvz_neg").fill(partRecNeg.vz()-partGenNeg.vz());
                    }
                }
                if(partGenPos != null && partRecPos != null) {
                    if(testMCpart(partGenPos,partRecPos)) {
                        dataGroups.getItem(3).getH1F("hi_dp_pos").fill((partRecPos.p()-partGenPos.p())/partGenPos.p());
                        dataGroups.getItem(3).getH1F("hi_dtheta_pos").fill(Math.toDegrees(partRecPos.theta()-partGenPos.theta()));
                        dataGroups.getItem(3).getH1F("hi_dphi_pos").fill(Math.toDegrees(partRecPos.phi()-partGenPos.phi()));
                        dataGroups.getItem(3).getH1F("hi_dvz_pos").fill(partRecPos.vz()-partGenPos.vz());
                    }
                }
            }
        }

    }

    
    private void createHistos() {
    
            // negative tracks
        H1F hi_p_neg = new H1F("hi_p_neg", "hi_p_neg", 100, 0.0, 8.0);     
        hi_p_neg.setTitleX("p (GeV)");
        hi_p_neg.setTitleY("Counts");
        H1F hi_theta_neg = new H1F("hi_theta_neg", "hi_theta_neg", 100, 0.0, 40.0); 
        hi_theta_neg.setTitleX("#theta (deg)");
        hi_theta_neg.setTitleY("Counts");
        H1F hi_phi_neg = new H1F("hi_phi_neg", "hi_phi_neg", 100, -180.0, 180.0);   
        hi_phi_neg.setTitleX("#phi (deg)");
        hi_phi_neg.setTitleY("Counts");
        H1F hi_chi2_neg = new H1F("hi_chi2_neg", "hi_chi2_neg", 100, 0.0, 180.0);   
        hi_chi2_neg.setTitleX("#chi2");
        hi_chi2_neg.setTitleY("Counts");
        H1F hi_vz_neg = new H1F("hi_vz_neg", "hi_vz_neg", 100, -15.0, 15.0);   
        hi_vz_neg.setTitleX("Vz (cm)");
        hi_vz_neg.setTitleY("Counts");
        H1F hi_vz_neg_cut = new H1F("hi_vz_neg_cut", "hi_vz_neg_cut", 100, -15.0, 15.0);   
        hi_vz_neg_cut.setTitleX("Vz (cm)");
        hi_vz_neg_cut.setTitleY("Counts");
        hi_vz_neg_cut.setLineColor(2);
        F1D f1_vz_neg = new F1D("f1_vz_neg","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1_vz_neg.setParameter(0, 0);
        f1_vz_neg.setParameter(1, 0);
        f1_vz_neg.setParameter(2, 1.0);
        f1_vz_neg.setLineWidth(2);
        f1_vz_neg.setLineColor(1);
        f1_vz_neg.setOptStat("1111");
        H2F hi_theta_p_neg = new H2F("hi_theta_p_neg", "hi_theta_p_neg", 100, 0.0, 8.0, 100, 0.0, 40.0); 
        hi_theta_p_neg.setTitleX("p (GeV)");
        hi_theta_p_neg.setTitleY("#theta (deg)");        
        H2F hi_theta_phi_neg = new H2F("hi_theta_phi_neg", "hi_theta_phi_neg", 100, -180.0, 180.0, 100, 0.0, 40.0); 
        hi_theta_phi_neg.setTitleX("#phi (deg)");
        hi_theta_phi_neg.setTitleY("#theta (deg)");        
        H2F hi_chi2_vz_neg = new H2F("hi_chi2_vz_neg", "hi_chi2_vz_neg", 100, -15.0, 15.0, 100, 0.0, 180.0);   
        hi_chi2_vz_neg.setTitleX("Vz (cm)");
        hi_chi2_vz_neg.setTitleY("#chi2");
        DataGroup dg_neg = new DataGroup(4,2);
        dg_neg.addDataSet(hi_p_neg, 0);
        dg_neg.addDataSet(hi_theta_neg, 1);
        dg_neg.addDataSet(hi_phi_neg, 2);
        dg_neg.addDataSet(hi_chi2_neg, 3);
        dg_neg.addDataSet(hi_vz_neg, 4);
        dg_neg.addDataSet(hi_vz_neg_cut, 4);
        dg_neg.addDataSet(f1_vz_neg, 4);
        dg_neg.addDataSet(hi_theta_p_neg, 5);
        dg_neg.addDataSet(hi_theta_phi_neg, 6);
        dg_neg.addDataSet(hi_chi2_vz_neg, 7);
        dataGroups.add(dg_neg, 1);
        // positive trakcs
        H1F hi_p_pos = new H1F("hi_p_pos", "hi_p_pos", 100, 0.0, 8.0);     
        hi_p_pos.setTitleX("p (GeV)");
        hi_p_pos.setTitleY("Counts");
        H1F hi_theta_pos = new H1F("hi_theta_pos", "hi_theta_pos", 100, 0.0, 40.0); 
        hi_theta_pos.setTitleX("#theta (deg)");
        hi_theta_pos.setTitleY("Counts");
        H1F hi_phi_pos = new H1F("hi_phi_pos", "hi_phi_pos", 100, -180.0, 180.0);   
        hi_phi_pos.setTitleX("#phi (deg)");
        hi_phi_pos.setTitleY("Counts");
        H1F hi_chi2_pos = new H1F("hi_chi2_pos", "hi_chi2_pos", 100, 0.0, 180.0);   
        hi_chi2_pos.setTitleX("#chi2");
        hi_chi2_pos.setTitleY("Counts");
        H1F hi_vz_pos = new H1F("hi_vz_pos", "hi_vz_pos", 100, -15.0, 15.0);   
        hi_vz_pos.setTitleX("Vz (cm)");
        hi_vz_pos.setTitleY("Counts");
        H1F hi_vz_pos_cut = new H1F("hi_vz_pos_cut", "hi_vz_pos_cut", 100, -15.0, 15.0);   
        hi_vz_pos_cut.setTitleX("Vz (cm)");
        hi_vz_pos_cut.setTitleY("Counts");
        hi_vz_pos_cut.setLineColor(2);
        F1D f1_vz_pos = new F1D("f1_vz_pos","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1_vz_pos.setParameter(0, 0);
        f1_vz_pos.setParameter(1, 0);
        f1_vz_pos.setParameter(2, 1.0);
        f1_vz_pos.setLineWidth(2);
        f1_vz_pos.setLineColor(1);
        f1_vz_pos.setOptStat("1111");
        H2F hi_theta_p_pos = new H2F("hi_theta_p_pos", "hi_theta_p_pos", 100, 0.0, 8.0, 100, 0.0, 40.0); 
        hi_theta_p_pos.setTitleX("p (GeV)");
        hi_theta_p_pos.setTitleY("#theta (deg)");        
        H2F hi_theta_phi_pos = new H2F("hi_theta_phi_pos", "hi_theta_phi_pos", 100, -180.0, 180.0, 100, 0.0, 40.0); 
        hi_theta_phi_pos.setTitleX("#phi (deg)");
        hi_theta_phi_pos.setTitleY("#theta (deg)");  
        H2F hi_chi2_vz_pos = new H2F("hi_chi2_vz_pos", "hi_chi2_vz_pos", 100, -15.0, 15.0, 100, 0.0, 180.0);   
        hi_chi2_vz_pos.setTitleX("Vz (cm)");
        hi_chi2_vz_pos.setTitleY("#chi2");
        DataGroup dg_pos = new DataGroup(4,2);
        dg_pos.addDataSet(hi_p_pos, 0);
        dg_pos.addDataSet(hi_theta_pos, 1);
        dg_pos.addDataSet(hi_phi_pos, 2);
        dg_pos.addDataSet(hi_chi2_pos, 3);
        dg_pos.addDataSet(hi_vz_pos, 4);
        dg_pos.addDataSet(hi_vz_pos_cut, 4);
        dg_pos.addDataSet(f1_vz_pos, 4);
        dg_pos.addDataSet(hi_theta_p_pos, 5);
        dg_pos.addDataSet(hi_theta_phi_pos, 6);
        dg_pos.addDataSet(hi_chi2_vz_pos, 7);
        dataGroups.add(dg_pos, 2);
        // mc comparison
        H1F hi_dp_pos = new H1F("hi_dp_pos", "hi_dp_pos", 100, -0.5, 0.5); 
        hi_dp_pos.setTitleX("#Delta P/P");
        hi_dp_pos.setTitleY("Counts");
        hi_dp_pos.setTitle("Positive Tracks");
        F1D f1_dp_pos = new F1D("f1_dp_pos","[amp]*gaus(x,[mean],[sigma])", -0.5, 0.5);
        f1_dp_pos.setParameter(0, 0);
        f1_dp_pos.setParameter(1, 0);
        f1_dp_pos.setParameter(2, 1.0);
        f1_dp_pos.setLineWidth(2);
        f1_dp_pos.setLineColor(2);
        f1_dp_pos.setOptStat("1111");
        H1F hi_dtheta_pos = new H1F("hi_dtheta_pos","hi_dtheta_pos", 100, -4.0, 4.0); 
        hi_dtheta_pos.setTitleX("#Delta #theta (deg)");
        hi_dtheta_pos.setTitleY("Counts");
        hi_dtheta_pos.setTitle("Positive Tracks");
        F1D f1_dtheta_pos = new F1D("f1_dtheta_pos","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dtheta_pos.setParameter(0, 0);
        f1_dtheta_pos.setParameter(1, 0);
        f1_dtheta_pos.setParameter(2, 1.0);
        f1_dtheta_pos.setLineWidth(2);
        f1_dtheta_pos.setLineColor(2);
        f1_dtheta_pos.setOptStat("1111");        
        H1F hi_dphi_pos = new H1F("hi_dphi_pos", "hi_dphi_pos", 100, -8.0, 8.0); 
        hi_dphi_pos.setTitleX("#Delta #phi (deg)");
        hi_dphi_pos.setTitleY("Counts");
        hi_dphi_pos.setTitle("Positive Tracks");
        F1D f1_dphi_pos = new F1D("f1_dphi_pos","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dphi_pos.setParameter(0, 0);
        f1_dphi_pos.setParameter(1, 0);
        f1_dphi_pos.setParameter(2, 1.0);
        f1_dphi_pos.setLineWidth(2);
        f1_dphi_pos.setLineColor(2);
        f1_dphi_pos.setOptStat("1111");        
        H1F hi_dvz_pos = new H1F("hi_dvz_pos", "hi_dvz_pos", 100, -20.0, 20.0);   
        hi_dvz_pos.setTitleX("#Delta Vz (cm)");
        hi_dvz_pos.setTitleY("Counts");
        hi_dvz_pos.setTitle("Positive Tracks");
        F1D f1_dvz_pos = new F1D("f1_dvz_pos","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dvz_pos.setParameter(0, 0);
        f1_dvz_pos.setParameter(1, 0);
        f1_dvz_pos.setParameter(2, 1.0);
        f1_dvz_pos.setLineWidth(2);
        f1_dvz_pos.setLineColor(2);
        f1_dvz_pos.setOptStat("1111");        
        H1F hi_dp_neg = new H1F("hi_dp_neg", "hi_dp_neg", 100, -0.5, 0.5);
        hi_dp_neg.setTitleX("#Delta P/P");
        hi_dp_neg.setTitleY("Counts");
        hi_dp_neg.setTitle("Negative Tracks");
        F1D f1_dp_neg = new F1D("f1_dp_neg","[amp]*gaus(x,[mean],[sigma])",  -0.5, 0.5);
        f1_dp_neg.setParameter(0, 0);
        f1_dp_neg.setParameter(1, 0);
        f1_dp_neg.setParameter(2, 1.0);
        f1_dp_neg.setLineWidth(2);
        f1_dp_neg.setLineColor(2);
        f1_dp_neg.setOptStat("1111");
        H1F hi_dtheta_neg = new H1F("hi_dtheta_neg","hi_dtheta_neg", 100, -4.0, 4.0); 
        hi_dtheta_neg.setTitleX("#Delta #theta (deg)");
        hi_dtheta_neg.setTitleY("Counts");
        hi_dtheta_neg.setTitle("Negative Tracks");
        F1D f1_dtheta_neg = new F1D("f1_dtheta_neg","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dtheta_neg.setParameter(0, 0);
        f1_dtheta_neg.setParameter(1, 0);
        f1_dtheta_neg.setParameter(2, 1.0);
        f1_dtheta_neg.setLineWidth(2);
        f1_dtheta_neg.setLineColor(2);
        f1_dtheta_neg.setOptStat("1111");
        H1F hi_dphi_neg = new H1F("hi_dphi_neg", "hi_dphi_neg", 100, -8.0, 8.0); 
        hi_dphi_neg.setTitleX("#Delta #phi (deg)");
        hi_dphi_neg.setTitleY("Counts");
        hi_dphi_neg.setTitle("Negative Tracks");
        F1D f1_dphi_neg = new F1D("f1_dphi_neg","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dphi_neg.setParameter(0, 0);
        f1_dphi_neg.setParameter(1, 0);
        f1_dphi_neg.setParameter(2, 1.0);
        f1_dphi_neg.setLineWidth(2);
        f1_dphi_neg.setLineColor(2);
        f1_dphi_neg.setOptStat("1111");
        H1F hi_dvz_neg = new H1F("hi_dvz_neg", "hi_dvz_neg", 100, -20.0, 20.0);   
        hi_dvz_neg.setTitleX("#Delta Vz (cm)");
        hi_dvz_neg.setTitleY("Counts");
        hi_dvz_neg.setTitle("Negative Tracks");
        F1D f1_dvz_neg = new F1D("f1_dvz_neg","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);
        f1_dvz_neg.setParameter(0, 0);
        f1_dvz_neg.setParameter(1, 0);
        f1_dvz_neg.setParameter(2, 1.0);
        f1_dvz_neg.setLineWidth(2);
        f1_dvz_neg.setLineColor(2);
        f1_dvz_neg.setOptStat("1111");
        H2F hi_dp_p_neg = new H2F("hi_dp_p_neg", "hi_dp_p_neg", 16, 0.5, 8.5, 100, -0.2, 0.2);
        hi_dp_p_neg.setTitleX("p");
        hi_dp_p_neg.setTitleY("#Delta p/p");
        hi_dp_p_neg.setTitle("Negative Tracks");
        H2F hi_dp_theta_neg = new H2F("hi_dp_theta_neg", "hi_dp_theta_neg", 30, 5, 35, 100, -0.2, 0.2);
        hi_dp_theta_neg.setTitleX("#theta");
        hi_dp_theta_neg.setTitleY("#Delta p/p");
        hi_dp_theta_neg.setTitle("Negative Tracks");
        H2F hi_dp_phi_neg = new H2F("hi_dp_phi_neg", "hi_dp_phi_neg", 100, -180, 180, 100, -0.2, 0.2);
        hi_dp_phi_neg.setTitleX("#phi");
        hi_dp_phi_neg.setTitleY("#Delta p/p");
        hi_dp_phi_neg.setTitle("Negative Tracks");
        GraphErrors gr_dp_p_neg = new GraphErrors("gr_dp_p_neg");
        gr_dp_p_neg.setTitleX("p");
        gr_dp_p_neg.setTitleY("#Delta p/p");
        gr_dp_p_neg.setTitle("gr_dp_p_neg");
        GraphErrors gr_dp_theta_neg = new GraphErrors("gr_dp_theta_neg");
        gr_dp_theta_neg.setTitleX("#theta");
        gr_dp_theta_neg.setTitleY("#Delta p/p");
        gr_dp_theta_neg.setTitle("gr_dp_p_neg");
        GraphErrors gr_dp_phi_neg = new GraphErrors("gr_dp_phi_neg");
        gr_dp_phi_neg.setTitleX("#phi");
        gr_dp_phi_neg.setTitleY("#Delta p/p");
        gr_dp_phi_neg.setTitle("gr_dp_p_neg");
        DataGroup mc = new DataGroup(4,4);
        mc.addDataSet(hi_dp_pos, 0);
        mc.addDataSet(f1_dp_pos, 0);
        mc.addDataSet(hi_dtheta_pos, 1);
        mc.addDataSet(f1_dtheta_pos, 1);
        mc.addDataSet(hi_dphi_pos, 2);
        mc.addDataSet(f1_dphi_pos, 2);
        mc.addDataSet(hi_dvz_pos, 3);
        mc.addDataSet(f1_dvz_pos, 3);
        mc.addDataSet(hi_dp_neg, 4);
        mc.addDataSet(f1_dp_neg, 4);
        mc.addDataSet(hi_dtheta_neg, 5);
        mc.addDataSet(f1_dtheta_neg, 5);
        mc.addDataSet(hi_dphi_neg, 6);
        mc.addDataSet(f1_dphi_neg, 6);
        mc.addDataSet(hi_dvz_neg, 7);
        mc.addDataSet(f1_dvz_neg, 7);
        mc.addDataSet(hi_dp_p_neg, 8);
        mc.addDataSet(hi_dp_theta_neg, 9);
        mc.addDataSet(hi_dp_phi_neg, 10);
        mc.addDataSet(gr_dp_p_neg, 11);
        mc.addDataSet(gr_dp_theta_neg, 12);
        mc.addDataSet(gr_dp_phi_neg, 13);
        dataGroups.add(mc, 3);
        // vertex
        H2F hi_vxy_pos = new H2F("hi_vxy_pos","hi_vxy_pos",100,-15.,15.,100,-15.,15);
        hi_vxy_pos.setTitleX("Vx (cm)");
        hi_vxy_pos.setTitleY("Vy (cm)");
        H2F hi_vxy_neg = new H2F("hi_vxy_neg","hi_vxy_neg",100,-15.,15.,100,-15.,15);
        hi_vxy_neg.setTitleX("Vx (cm)");
        hi_vxy_neg.setTitleY("Vy (cm)"); 
        H2F hi_vz_vs_theta_pos = new H2F("hi_vz_vs_theta_pos","hi_vz_vs_theta_pos",100, 5.,40.,100,-15.,15);
        hi_vz_vs_theta_pos.setTitleX("#theta (deg)");
        hi_vz_vs_theta_pos.setTitleY("Vz (cm)");
        H2F hi_vz_vs_theta_neg = new H2F("hi_vz_vs_theta_neg","hi_vz_vs_theta_neg",100, 5.,40.,100,-15.,15);
        hi_vz_vs_theta_neg.setTitleX("#theta (deg)");
        hi_vz_vs_theta_neg.setTitleY("Vz (cm)");
        DataGroup vertex = new DataGroup(2,2);
        vertex.addDataSet(hi_vz_vs_theta_pos, 0);
        vertex.addDataSet(hi_vxy_pos, 1);
        vertex.addDataSet(hi_vz_vs_theta_neg, 2);
        vertex.addDataSet(hi_vxy_neg, 3);
        dataGroups.add(vertex, 4);
        // W
        H1F hi_W = new H1F("hi_W","hi_W",100,0., 3.5);
        hi_W.setTitleX("W (GeV)");
        hi_W.setTitleY("Counts");
        H1F hi_W_cut = new H1F("hi_W_cut","hi_W_cut",100,0., 3.5);
        hi_W_cut.setTitleX("W (GeV)");
        hi_W_cut.setTitleY("Counts");
        hi_W_cut.setLineColor(2);
        H2F hi_W_phi = new H2F("hi_W_phi","hi_W_phi",100,0.,3.5, 100, -180, 180);
        hi_W_phi.setTitleX("W (GeV)");
        hi_W_phi.setTitleY("#phi (deg)"); 
        DataGroup W = new DataGroup(2,2);
        W.addDataSet(hi_W, 0);
        W.addDataSet(hi_W_cut, 0);
        W.addDataSet(hi_W_phi, 1);
        dataGroups.add(W, 5);
    }

    private void plotHistos() {
        canvasTabbed.getCanvas("Negative Tracks").divide(4,2);
        canvasTabbed.getCanvas("Negative Tracks").setGridX(false);
        canvasTabbed.getCanvas("Negative Tracks").setGridY(false);
        canvasTabbed.getCanvas("Positive Tracks").divide(4,2);
        canvasTabbed.getCanvas("Positive Tracks").setGridX(false);
        canvasTabbed.getCanvas("Positive Tracks").setGridY(false);
        canvasTabbed.getCanvas("Monte Carlo").divide(4, 2);
        canvasTabbed.getCanvas("Monte Carlo").setGridX(false);
        canvasTabbed.getCanvas("Monte Carlo").setGridY(false);
        canvasTabbed.getCanvas("Monte Carlo p negative").divide(3, 2);
        canvasTabbed.getCanvas("Monte Carlo p negative").setGridX(false);
        canvasTabbed.getCanvas("Monte Carlo p negative").setGridY(false);
        canvasTabbed.getCanvas("Vertex").divide(3, 2);
        canvasTabbed.getCanvas("Vertex").setGridX(false);
        canvasTabbed.getCanvas("Vertex").setGridY(false);
        canvasTabbed.getCanvas("W").divide(2,1);
        canvasTabbed.getCanvas("W").setGridX(false);
        canvasTabbed.getCanvas("W").setGridY(false);
        canvasTabbed.getCanvas("Negative Tracks").cd(0);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_p_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(1);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_theta_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(2);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_phi_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(3);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_chi2_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(4);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_vz_neg"));
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH1F("hi_vz_neg_cut"),"same");
//        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getF1D("f1_vz_neg"),"same");
        canvasTabbed.getCanvas("Negative Tracks").cd(5);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH2F("hi_theta_p_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(6);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH2F("hi_theta_phi_neg"));
        canvasTabbed.getCanvas("Negative Tracks").cd(7);
        canvasTabbed.getCanvas("Negative Tracks").draw(dataGroups.getItem(1).getH2F("hi_chi2_vz_neg"));
        canvasTabbed.getCanvas("Positive Tracks").cd(0);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_p_pos"));
        canvasTabbed.getCanvas("Positive Tracks").cd(1);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_theta_pos"));
        canvasTabbed.getCanvas("Positive Tracks").cd(2);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_phi_pos"));
        canvasTabbed.getCanvas("Positive Tracks").cd(3);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_chi2_pos"));
        canvasTabbed.getCanvas("Positive Tracks").cd(4);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_vz_pos"));
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH1F("hi_vz_pos_cut"),"same");
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getF1D("f1_vz_pos"),"same");
        canvasTabbed.getCanvas("Positive Tracks").cd(5);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH2F("hi_theta_p_pos"));
        canvasTabbed.getCanvas("Positive Tracks").cd(6);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH2F("hi_theta_phi_pos"));        
        canvasTabbed.getCanvas("Positive Tracks").cd(7);
        canvasTabbed.getCanvas("Positive Tracks").draw(dataGroups.getItem(2).getH2F("hi_chi2_vz_pos"));
        canvasTabbed.getCanvas("Monte Carlo").cd(0);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dp_pos"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dp_pos"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(1);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dtheta_pos"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dtheta_pos"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(2);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dphi_pos"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dphi_pos"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(3);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dvz_pos"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dvz_pos"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(4);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dp_neg"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dp_neg"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(5);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dtheta_neg"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dtheta_neg"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(6);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dphi_neg"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dphi_neg"),"same");
        canvasTabbed.getCanvas("Monte Carlo").cd(7);
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getH1F("hi_dvz_neg"));
        canvasTabbed.getCanvas("Monte Carlo").draw(dataGroups.getItem(3).getF1D("f1_dvz_neg"),"same");
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(0);
        canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getH2F("hi_dp_p_neg"));
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(1);
        canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getH2F("hi_dp_theta_neg"));
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(2);
        canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getH2F("hi_dp_phi_neg"));
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(3);
        canvasTabbed.getCanvas("Monte Carlo p negative").getPad(3).getAxisY().setRange(-0.05, 0.05);
        if(dataGroups.getItem(3).getGraph("gr_dp_p_neg").getDataSize(0)>1) canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getGraph("gr_dp_p_neg"));
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(4);
        canvasTabbed.getCanvas("Monte Carlo p negative").getPad(4).getAxisY().setRange(-0.05, 0.05);
        if(dataGroups.getItem(3).getGraph("gr_dp_theta_neg").getDataSize(0)>1) canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getGraph("gr_dp_theta_neg"));
        canvasTabbed.getCanvas("Monte Carlo p negative").cd(5);
        canvasTabbed.getCanvas("Monte Carlo p negative").getPad(5).getAxisY().setRange(-0.05, 0.05);
        if(dataGroups.getItem(3).getGraph("gr_dp_phi_neg").getDataSize(0)>1) canvasTabbed.getCanvas("Monte Carlo p negative").draw(dataGroups.getItem(3).getGraph("gr_dp_phi_neg"));
        canvasTabbed.getCanvas("Vertex").cd(0);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(2).getH1F("hi_vz_pos"));
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(2).getH1F("hi_vz_pos_cut"),"same");
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(2).getF1D("f1_vz_pos"),"same");
        canvasTabbed.getCanvas("Vertex").cd(1);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(4).getH2F("hi_vz_vs_theta_pos"));
        canvasTabbed.getCanvas("Vertex").cd(2);
        canvasTabbed.getCanvas("Vertex").getPad(2).getAxisZ().setLog(true);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(4).getH2F("hi_vxy_pos"));
        canvasTabbed.getCanvas("Vertex").cd(3);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(1).getH1F("hi_vz_neg"));
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(1).getH1F("hi_vz_neg_cut"),"same");
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(1).getF1D("f1_vz_neg"),"same");
        canvasTabbed.getCanvas("Vertex").cd(4);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(4).getH2F("hi_vz_vs_theta_neg"));
        canvasTabbed.getCanvas("Vertex").cd(5);
        canvasTabbed.getCanvas("Vertex").getPad(5).getAxisZ().setLog(true);
        canvasTabbed.getCanvas("Vertex").draw(dataGroups.getItem(4).getH2F("hi_vxy_neg"));
        canvasTabbed.getCanvas("W").cd(0);
        canvasTabbed.getCanvas("W").draw(dataGroups.getItem(5).getH1F("hi_W"));
        canvasTabbed.getCanvas("W").draw(dataGroups.getItem(5).getH1F("hi_W_cut"),"same");
        canvasTabbed.getCanvas("W").cd(1);
        canvasTabbed.getCanvas("W").draw(dataGroups.getItem(5).getH2F("hi_W_phi"));

        
        canvasTabbed.getCanvas("Negative Tracks").update();
        canvasTabbed.getCanvas("Positive Tracks").update();
        canvasTabbed.getCanvas("Monte Carlo").update();
        canvasTabbed.getCanvas("Vertex").update();
        canvasTabbed.getCanvas("W").update();
    }
    
    private void analyze() {
//        System.out.println("Updating TBT");
//        //fitting negative tracks vertex
        this.fitVertex(dataGroups.getItem(1).getH1F("hi_vz_neg_cut"), dataGroups.getItem(1).getF1D("f1_vz_neg"));
        //fitting positive tracks vertex
        this.fitVertex(dataGroups.getItem(2).getH1F("hi_vz_pos_cut"), dataGroups.getItem(2).getF1D("f1_vz_pos"));
        // fitting MC comparisons
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dp_pos"),     dataGroups.getItem(3).getF1D("f1_dp_pos"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dtheta_pos"), dataGroups.getItem(3).getF1D("f1_dtheta_pos"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dphi_pos"),   dataGroups.getItem(3).getF1D("f1_dphi_pos"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dvz_pos"),    dataGroups.getItem(3).getF1D("f1_dvz_pos"));     
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dp_neg"),     dataGroups.getItem(3).getF1D("f1_dp_neg"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dtheta_neg"), dataGroups.getItem(3).getF1D("f1_dtheta_neg"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dphi_neg"),   dataGroups.getItem(3).getF1D("f1_dphi_neg"));
        this.fitMC(dataGroups.getItem(3).getH1F("hi_dvz_neg"),    dataGroups.getItem(3).getF1D("f1_dvz_neg"));     
        this.fitMCSlice(dataGroups.getItem(3).getH2F("hi_dp_p_neg"),dataGroups.getItem(3).getGraph("gr_dp_p_neg"));
        this.fitMCSlice(dataGroups.getItem(3).getH2F("hi_dp_theta_neg"),dataGroups.getItem(3).getGraph("gr_dp_theta_neg"));
        this.fitMCSlice(dataGroups.getItem(3).getH2F("hi_dp_phi_neg"),dataGroups.getItem(3).getGraph("gr_dp_phi_neg"));
    }

    private void fitVertex(H1F hivz, F1D f1vz) {
        double mean  = hivz.getDataX(hivz.getMaximumBin());
        double amp   = hivz.getBinContent(hivz.getMaximumBin());
        double sigma = 1.;
        if(hivz.getEntries()>500) { // first fits 
            sigma = Math.abs(f1vz.getParameter(2));       
        }
        f1vz.setParameter(0, amp);
        f1vz.setParameter(1, mean);
        f1vz.setParameter(2, sigma);
        f1vz.setRange(mean-2.*sigma,mean+2.*sigma);
        DataFitter.fit(f1vz, hivz, "Q"); //No options uses error for sigma 
        hivz.setFunction(null);
    }

    private void fitMC(H1F himc, F1D f1mc) {
        double mean  = himc.getDataX(himc.getMaximumBin());
        double amp   = himc.getBinContent(himc.getMaximumBin());
        double sigma = himc.getRMS()/2;
        f1mc.setParameter(0, amp);
        f1mc.setParameter(1, mean);
        f1mc.setParameter(2, sigma);
        f1mc.setRange(mean-2.*sigma,mean+2.*sigma);
        DataFitter.fit(f1mc, himc, "Q"); //No options uses error for sigma 
        sigma = Math.abs(f1mc.getParameter(2));  
        f1mc.setRange(mean-2.*sigma,mean+2.*sigma);
        DataFitter.fit(f1mc, himc, "Q"); //No options uses error for sigma 
        himc.setFunction(null);
    }
    
    private void fitMCSlice(H2F himc, GraphErrors grmc) {
        grmc.reset();
        ArrayList<H1F> hslice = himc.getSlicesX();
        for(int i=0; i<hslice.size(); i++) {
            double  x = himc.getXAxis().getBinCenter(i);
            double ex = 0;
            double  y = hslice.get(i).getRMS();
            double ey = 0;
            double mean  = hslice.get(i).getDataX(hslice.get(i).getMaximumBin());
            double amp   = hslice.get(i).getBinContent(hslice.get(i).getMaximumBin());
            double sigma = hslice.get(i).getRMS()/2;
            F1D f1 = new F1D("f1slice","[amp]*gaus(x,[mean],[sigma])", hslice.get(i).getDataX(0), hslice.get(i).getDataX(hslice.get(i).getDataSize(1)-1));
            f1.setParameter(0, amp);
            f1.setParameter(1, mean);
            f1.setParameter(2, sigma);
            f1.setRange(mean-2.*sigma,mean+2.*sigma);
            DataFitter.fit(f1, hslice.get(i), "Q"); //No options uses error for sigma 
            if(amp>50) grmc.addPoint(x, f1.getParameter(2), ex, f1.parameter(2).error());
        }

    }
    
    public void setAnalysisTabNames(String... names) {
        for(String name : names) {
            canvasTabNames.add(name);
        }
        canvasTabbed = new EmbeddedCanvasTabbed(names);
    }
    
    public void printCanvas(String dir, String name) {
        // print canvas to files
        for(int tab=0; tab<canvasTabNames.size(); tab++) {
            String fileName = dir + "/" + this.analysisName + "_" + name + "." + tab + ".png";
            System.out.println(fileName);
            canvasTabbed.getCanvas(canvasTabNames.get(tab)).save(fileName);
        }
    }
    
    public boolean testMCpart(Particle mc, Particle rec) {
        if(Math.abs(mc.px()-rec.px())<2.5 &&
           Math.abs(mc.py()-rec.py())<2.5 &&
           Math.abs(mc.pz()-rec.pz())<2.5) return true;
        else return false;
    }
}
