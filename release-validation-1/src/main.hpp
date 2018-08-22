/************************************************************************/
/*  Created by Nick Tyler*/
/*	University Of South Carolina*/
/************************************************************************/

#ifndef MAIN_H_GUARD
#define MAIN_H_GUARD
#include <TFile.h>
#include <vector>
#include "TChain.h"
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

  for (int current_event = 0; current_event < num_of_events; current_event++) {
    chain->GetEntry(current_event);
    if (pid->size() == 0) continue;

    per = ((double)current_event / (double)num_of_events);
    std::cerr << "\t\t" << std::floor(100 * per) << "%\r\r" << std::flush;
    try {
      event_P = TMath::Sqrt((px->at(0) * px->at(0)) + (py->at(0) * py->at(0)) + (pz->at(0) * pz->at(0)));
      mc_P = TMath::Sqrt((mc_px->at(0) * mc_px->at(0)) + (mc_py->at(0) * mc_py->at(0)) + (mc_pz->at(0) * mc_pz->at(0)));
      hist->Fill_Res(px->at(0), py->at(0), pz->at(0), event_P, mc_px->at(0), mc_py->at(0), mc_pz->at(0), mc_P);
    } catch (std::exception &e) {
      total++;
      continue;
    }
  }

  out->cd();
  hist->Write();
  out->Close();
  chain->Reset();

  if (total > 0) std::cout << RED << "Event Errors: " << total << RESET << std::endl;
}

#endif
