/**************************************/
/*																		*/
/*  Created by Nick Tyler             */
/*	University Of South Carolina      */
/**************************************/
#include "TCanvas.h"
#include "histogram.hpp"

static const double MASS_P = 0.93827203;

Histogram::Histogram() {}

Histogram::~Histogram() {}

void Histogram::Fill_Res(double _px, double _py, double _pz, double _mc_px, double _mc_py, double _mc_pz) {
  double _P = TMath::Sqrt((_px * _px) + (_py * _py) + (_pz * _pz));
  double _mc_P = TMath::Sqrt((_mc_px * _mc_px) + (_mc_py * _mc_py) + (_mc_pz * _mc_pz));

  momentum->Fill(_P);
  momentum_x->Fill(_px);
  momentum_y->Fill(_py);
  momentum_z->Fill(_pz);

  mom_rvg_x->Fill(_mc_px, _px);
  mom_rvg_y->Fill(_mc_py, _py);
  mom_rvg_z->Fill(_mc_pz, _pz);

  resolution->Fill((_P - _mc_P) / _P);
  resolution_x->Fill((_px - _mc_px) / _px);
  resolution_y->Fill((_py - _mc_py) / _py);
  resolution_z->Fill((_pz - _mc_pz) / _pz);
}

void Histogram::Fill_WQ2(TLorentzVector _beam, TLorentzVector _e, TLorentzVector _mc_e) {
  TLorentzVector _p_mu;
  TVector3 _p_mu_3(0, 0, 0);
  TLorentzVector _q_mu = (_beam - _e);
  _p_mu.SetVectM(_p_mu_3, MASS_P);
  double _e_W = (_p_mu + _q_mu).Mag();
  double _e_Q2 = -_q_mu.Mag2();

  TLorentzVector _mc_q_mu = (_beam - _mc_e);
  double _mc_e_W = (_p_mu + _mc_q_mu).Mag();
  double _mc_e_Q2 = -_mc_q_mu.Mag2();

  W_vs_Q2->Fill(_e_W, _e_Q2);
  mc_W_vs_Q2->Fill(_mc_e_W, _mc_e_Q2);
  W->Fill(_e_W);
  mc_W->Fill(_mc_e_W);
}

void Histogram::Write() {
  TCanvas c1("c1");
  momentum->SetXTitle("momentum (GeV)");
  momentum->Write();
  momentum_x->SetXTitle("momentum (GeV)");
  momentum_x->Write();
  momentum_y->SetXTitle("momentum (GeV)");
  momentum_y->Write();
  momentum_z->SetXTitle("momentum (GeV)");
  momentum_z->Write();

  mom_rvg_x->SetXTitle("Gen X (GeV)");
  mom_rvg_x->SetYTitle("Rec X (GeV)");
  mom_rvg_x->SetOption("COLZ");
  mom_rvg_x->Write();
  mom_rvg_y->SetXTitle("Gen Y (GeV)");
  mom_rvg_y->SetYTitle("Rec Y (GeV)");
  mom_rvg_y->SetOption("COLZ");
  mom_rvg_y->Write();
  mom_rvg_z->SetXTitle("Gen Z (GeV)");
  mom_rvg_z->SetYTitle("Rec Z (GeV)");
  mom_rvg_z->SetOption("COLZ");
  mom_rvg_z->Write();

  resolution->SetXTitle("#Delta P/P");
  resolution->Fit("gaus", "QM+");
  resolution->Write();
  resolution_x->SetXTitle("#Delta Px/Px");
  resolution_x->Fit("gaus", "QM+");
  resolution_x->Write();
  resolution_y->SetXTitle("#Delta Py/Py");
  resolution_y->Fit("gaus", "QM+");
  resolution_y->Write();
  resolution_z->SetXTitle("#Delta Pz/Pz");
  resolution_z->Fit("gaus", "QM+");
  resolution_z->Write();

  W_vs_Q2->SetXTitle("#Delta P/P");
  mc_W_vs_Q2->SetXTitle("#Delta P/P");
  W->SetXTitle("#Delta P/P");
  mc_W->SetXTitle("#Delta P/P");
  W_vs_Q2->SetYTitle("#Delta P/P");
  mc_W_vs_Q2->SetYTitle("#Delta P/P");
  W->SetYTitle("#Delta P/P");
  mc_W->SetYTitle("#Delta P/P");
  W_vs_Q2->Write();
  mc_W_vs_Q2->Write();
  W->Write();
  mc_W->Write();
}
