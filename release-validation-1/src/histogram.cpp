/**************************************/
/*																		*/
/*  Created by Nick Tyler             */
/*	University Of South Carolina      */
/**************************************/
#include "TCanvas.h"
#include "histogram.hpp"

Histogram::Histogram() {}

Histogram::~Histogram() {}

void Histogram::Fill_Res(double _px, double _py, double _pz, double _P, double _mc_px, double _mc_py, double _mc_pz,
                         double _mc_P) {
  momentum->Fill(_P);
  momentum_x->Fill(_px);
  momentum_y->Fill(_py);
  momentum_z->Fill(_pz);

  mom_rvg_x->Fill(_mc_px, _px);
  mom_rvg_y->Fill(_mc_py, _py);
  mom_rvg_z->Fill(_mc_pz, _pz);

  resolution->Fill(_P - _mc_P);
  resolution_x->Fill(_px - _mc_px);
  resolution_y->Fill(_py - _mc_py);
  resolution_z->Fill(_pz - _mc_pz);
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

  resolution->SetXTitle("momentum (GeV)");
  resolution->Fit("gaus", "QM+");
  resolution->Write();
  resolution_x->SetXTitle("momentum (GeV)");
  resolution_x->Fit("gaus", "QM+");
  resolution_x->Write();
  resolution_y->SetXTitle("momentum (GeV)");
  resolution_y->Fit("gaus", "QM+");
  resolution_y->Write();
  resolution_z->SetXTitle("momentum (GeV)");
  resolution_z->Fit("gaus", "QM+");
  resolution_z->Write();
}
