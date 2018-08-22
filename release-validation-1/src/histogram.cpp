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

  momentum_x_y->Fill(_px, _py);
  momentum_x_z->Fill(_px, _pz);
  momentum_y_z->Fill(_py, _pz);

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

  momentum_x_y->SetXTitle("momentum X (GeV)");
  momentum_x_y->SetYTitle("momentum Y (GeV)");
  momentum_x_y->SetOption("COLZ");
  momentum_x_y->Write();
  momentum_x_z->SetXTitle("momentum X (GeV)");
  momentum_x_z->SetYTitle("momentum Z (GeV)");
  momentum_x_z->SetOption("COLZ");
  momentum_x_z->Write();
  momentum_y_z->SetXTitle("momentum Y (GeV)");
  momentum_y_z->SetYTitle("momentum Z (GeV)");
  momentum_y_z->SetOption("COLZ");
  momentum_y_z->Write();

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
