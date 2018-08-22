/************************************************************************/
/*  Created by Nick Tyler*/
/*	University Of South Carolina*/
/************************************************************************/

#ifndef HIST_H_GUARD
#define HIST_H_GUARD
#include "TF1.h"
#include "TH1.h"
#include "TH2.h"

class Histogram {
 private:
  int bins = 100;
  double p_min = 0.0;
  double p_max = 5.0;

  TH1D *momentum = new TH1D("ReconMom", "Reconstructed Momentum", bins, p_min, p_max);
  TH1D *momentum_x = new TH1D("ReconMom_x", "Reconstructed Momentum in x", bins, p_min, p_max);
  TH1D *momentum_y = new TH1D("ReconMom_y", "Reconstructed Momentum in y", bins, p_min, p_max);
  TH1D *momentum_z = new TH1D("ReconMom_z", "Reconstructed Momentum in z", bins, p_min, p_max);

  TH2D *momentum_x_y =
      new TH2D("ReconMom_XvsY", "Reconstructed Momentum X vs Y", bins, p_min, p_max, bins, p_min, p_max);
  TH2D *momentum_x_z =
      new TH2D("ReconMom_XvsZ", "Reconstructed Momentum X vs Z", bins, p_min, p_max, bins, p_min, p_max);
  TH2D *momentum_y_z =
      new TH2D("ReconMom_YvsZ", "Reconstructed Momentum Y vs Z", bins, p_min, p_max, bins, p_min, p_max);

  TH1D *resolution = new TH1D("MomRes", "Momentum Resolution", 50, -2.5, 2.5);
  TH1D *resolution_x = new TH1D("MomRes_x", "Momentum Resolution in x", 50, -1.0, 1.0);
  TH1D *resolution_y = new TH1D("MomRes_y", "Momentum Resolution in y", 50, -1.0, 1.0);
  TH1D *resolution_z = new TH1D("MomRes_z", "Momentum Resolution in z", 50, -1.0, 1.0);

 public:
  Histogram();
  ~Histogram();

  void Fill_Res(double px, double py, double pz, double P, double mc_px, double mc_py, double mc_pz, double mc_P);
  void Write();
};

#endif
