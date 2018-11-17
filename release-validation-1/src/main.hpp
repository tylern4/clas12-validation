/************************************************************************/
/*  Created by Nick Tyler*/
/*	University Of South Carolina*/
/************************************************************************/

#ifndef MAIN_H_GUARD
#define MAIN_H_GUARD
#include <TFile.h>
#include <TLorentzVector.h>
#include <vector>
#include "TChain.h"
#include "colors.hpp"
#include "histogram.hpp"

std::vector<int> *pid;
std::vector<float> *px;
std::vector<float> *py;
std::vector<float> *pz;
std::vector<float> *vx;
std::vector<float> *vy;
std::vector<float> *vz;

std::vector<int> *mc_pid;
std::vector<float> *mc_px;
std::vector<float> *mc_py;
std::vector<float> *mc_pz;
std::vector<float> *mc_vx;
std::vector<float> *mc_vy;
std::vector<float> *mc_vz;
std::vector<float> *mc_E;

// mass in GeV/c2
static const double MASS_E = 0.000511;

namespace filehandeler {
void getBranches(TTree *myTree) {
  myTree->SetBranchAddress("REC_Particle_pid", &pid);
  myTree->SetBranchAddress("REC_Particle_px", &px);
  myTree->SetBranchAddress("REC_Particle_py", &py);
  myTree->SetBranchAddress("REC_Particle_pz", &pz);
  myTree->SetBranchAddress("REC_Particle_vx", &vx);
  myTree->SetBranchAddress("REC_Particle_vy", &vy);
  myTree->SetBranchAddress("REC_Particle_vz", &vz);

  myTree->SetBranchAddress("MC_Particle_pid", &mc_pid);
  myTree->SetBranchAddress("MC_Particle_px", &mc_px);
  myTree->SetBranchAddress("MC_Particle_py", &mc_py);
  myTree->SetBranchAddress("MC_Particle_pz", &mc_pz);
  myTree->SetBranchAddress("MC_Particle_vx", &mc_vx);
  myTree->SetBranchAddress("MC_Particle_vy", &mc_vy);
  myTree->SetBranchAddress("MC_Particle_vz", &mc_vz);
  myTree->SetBranchAddress("MC_Lund_E", &mc_E);

  myTree->SetBranchStatus("*", 1);
}

TChain *addFiles(std::string fin) {
  TChain *clas12 = new TChain("clas12", "clas12");
  clas12->Add(fin.c_str());
  return clas12;
}
}  // namespace filehandeler

void datahandeler(std::string fin, std::string fout) {
  TFile *out = new TFile(fout.c_str(), "RECREATE");
  double event_P, mc_P, per;
  int total = 0;

  // Load chain from branch h10
  TChain *chain = filehandeler::addFiles(fin);
  filehandeler::getBranches(chain);

  int num_of_events = (int)chain->GetEntries();
  Histogram *hist = new Histogram();
  TLorentzVector e_beam;
  TLorentzVector e_prime;
  TVector3 e_prime_P;
  TLorentzVector mc_e_prime;
  TVector3 mc_e_prime_P;

  for (int current_event = 0; current_event < num_of_events; current_event++) {
    chain->GetEntry(current_event);
    per = ((1 + (double)current_event) / (double)num_of_events);
    std::cerr << "\t\t" << std::floor(100 * per) << "%\r\r" << std::flush;

    if (pid->size() == 0) continue;

    try {
      // if (pid->at(0) == 11) {
      hist->Fill_Res(px->at(0), py->at(0), pz->at(0), mc_px->at(0), mc_py->at(0), mc_pz->at(0));
      e_beam.SetPxPyPzE(0, 0, mc_E->at(0), mc_E->at(0));
      e_prime_P.SetXYZ(px->at(0), py->at(0), pz->at(0));
      e_prime.SetVectM(e_prime_P, MASS_E);
      mc_e_prime_P.SetXYZ(mc_px->at(0), mc_py->at(0), mc_pz->at(0));
      mc_e_prime.SetVectM(mc_e_prime_P, MASS_E);

      hist->Fill_WQ2(e_beam, e_prime, mc_e_prime);
      //}

    } catch (std::exception &e) {
      total++;
      continue;
    }
  }

  out->cd();
  hist->Write();
  out->Close();
  chain->Reset();

  if (total > 0) std::cout << RED << "Errors: " << total << RESET << std::endl;
}

#endif
