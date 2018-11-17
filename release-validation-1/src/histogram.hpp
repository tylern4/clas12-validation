/************************************************************************/
/*  Created by Nick Tyler*/
/*	University Of South Carolina*/
/************************************************************************/

#ifndef HIST_H_GUARD
#define HIST_H_GUARD
#include <TF1.h>
#include <TH1.h>
#include <TH2.h>
#include <TLorentzVector.h>

class Histogram {
 private:
  int bins = 600;
  double p_min = -6.0;
  double p_max = 12.0;

  TH1D *momentum = new TH1D("ReconMom", "Electron Reconstructed Momentum", bins, p_min, p_max);
  TH1D *momentum_x = new TH1D("ReconMom_x", "Electron Reconstructed Momentum in x", bins, p_min, p_max);
  TH1D *momentum_y = new TH1D("ReconMom_y", "Electron Reconstructed Momentum in y", bins, p_min, p_max);
  TH1D *momentum_z = new TH1D("ReconMom_z", "Electron Reconstructed Momentum in z", bins, p_min, p_max);

  TH2D *mom_rvg_x = new TH2D("Recon_vs_gen_X", "Electron Momentum X", bins, p_min, p_max, bins, p_min, p_max);
  TH2D *mom_rvg_y = new TH2D("Recon_vs_gen_Y", "Electron Momentum Y", bins, p_min, p_max, bins, p_min, p_max);
  TH2D *mom_rvg_z = new TH2D("Recon_vs_gen_Z", "Electron Momentum Z", bins, p_min, p_max, bins, p_min, p_max);

  TH1D *resolution = new TH1D("MomRes", "Electron Momentum Resolution", 50, -2.5, 2.5);
  TH1D *resolution_x = new TH1D("MomRes_x", "Electron Momentum Resolution in x", 50, -1.0, 1.0);
  TH1D *resolution_y = new TH1D("MomRes_y", "Electron Momentum Resolution in y", 50, -1.0, 1.0);
  TH1D *resolution_z = new TH1D("MomRes_z", "Electron Momentum Resolution in z", 50, -1.0, 1.0);

  TH2D *W_vs_Q2 = new TH2D("W_vs_Q2", "W vs Q^{2}", bins, 0, 10.0, bins, 0, 10.0);
  TH2D *mc_W_vs_Q2 = new TH2D("mc_W_vs_Q2", "W vs Q^{2}", bins, 0, 10.0, bins, 0, 10.0);
  TH1D *W = new TH1D("W", "W", bins, 0, 10);
  TH1D *mc_W = new TH1D("mc_W", "W", bins, 0, 10);

 public:
  Histogram();
  ~Histogram();

  void Fill_Res(double px, double py, double pz, double mc_px, double mc_py, double mc_pz);
  void Fill_WQ2(TLorentzVector _beam, TLorentzVector _e, TLorentzVector _mc_e);
  void Write();
};

#endif
