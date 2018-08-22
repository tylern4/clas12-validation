#!/usr/bin/env python
from __future__ import print_function
from datetime import datetime
import argparse
import numpy as np
import sys
import os


def gen_lund(args):
    header = "           2  1.  1.  0 -1 0.0   0.0000  0.0000   0.0000   0.0000"
    event1 = "    1  -1  2  11  0  0  0.0000  0.0000  {:.4f}  {:.4f}  0.0005  {:.4f}  {:.4f}  {:.4f}"
    event2 = "    2   0  1  11  0  0  {:.4f}  {:.4f}  {:.4f}  {:.4f}  0.0005  {:.4f}  {:.4f}  {:.4f}"
    energy = args.energy
    theta = args.theta
    phi = args.phi

    with open(args.output_file, "w") as output:
        for evt in range(0, args.num):
            output.write(header)
            output.write("\n")

            if(args.sigma > 0):
                energy = args.energy + \
                    float(np.random.normal(0.0, args.sigma, 1))
                theta = args.theta + \
                    float(np.random.normal(0.0, args.sigma, 1))
                phi = args.phi + float(np.random.normal(0.0, args.sigma, 1))

            px = energy * np.sin(theta) * np.cos(phi)
            py = energy * np.sin(theta) * np.sin(phi)
            pz = energy * np.cos(theta)
            vx = 0.0
            vy = 0.0
            vz = 0.0
            E = np.sqrt(px * px + py * py + pz * pz)
            output.write(event1.format(E, E, vx, vy, vz))
            output.write("\n")
            output.write(event2.format(px, py, pz, E, vx, vy, vz))
            output.write("\n")
        output.close()


def main():
    # Make argument parser
    parser = argparse.ArgumentParser(
        description="Basic generator for electrons")
    parser.add_argument('-o', dest='output_file', type=str, nargs='?',
                        help="Output filename", default='electron_gen.dat')
    parser.add_argument('-E', dest='energy', type=float, nargs='?',
                        help="Energy to generate electrons at", default=4.5)
    parser.add_argument('-T', dest='theta', type=float, nargs='?',
                        help="Theta angle to generate electrons at", default=20)
    parser.add_argument('-P', dest='phi', type=float, nargs='?',
                        help="phi angle to generate electrons at", default=20)
    parser.add_argument('-s', dest='sigma', type=float, nargs='?',
                        help="Generate electron with noise", default=0.0)
    parser.add_argument('-N', dest='num', type=int, nargs='?',
                        help="Number of electrons to generate", default=200)
    args = parser.parse_args()

    gen_lund(args)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nExiting")
        sys.exit()
