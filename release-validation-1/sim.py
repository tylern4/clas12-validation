#!/usr/bin/env python
from __future__ import print_function
from multiprocessing import Pool, cpu_count
import multiprocessing as mp
from datetime import datetime
import argparse
import sys
import glob
import contextlib
import os
import shutil
import tempfile


@contextlib.contextmanager
def cd(newdir, cleanup=lambda: True):
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)
        cleanup()


@contextlib.contextmanager
def tempdir():
    dirpath = tempfile.mkdtemp()

    def cleanup():
        shutil.rmtree(dirpath)
    with cd(dirpath, cleanup):
        yield dirpath


def make_names(output_dir, input_file, num):
    time = datetime.now().strftime('%m_%d_%Y-%H%M_')
    input_file = input_file[:-4]
    l = []
    for i in range(0, num):
        l.append(output_dir + input_file + "_" + time + str(i))
    return l


def split_lund(file_name, files):
    import re
    num_events = 0
    total_lines = 0
    with open(file_name, "r") as text_file:
        lines = text_file.readlines()
        for line in lines:
            if line.startswith("        "):
                num_events += 1

        out = 0
        tot = 0
        f2 = open(files[out] + ".dat", "w")
        for line_num, line in enumerate(lines):
            if(tot >= num_events / len(files)):
                tot = 0
                if(f2):
                    f2.close()
                out += 1
                if(out < len(files)):
                    f2 = open(files[out] + ".dat", "w")
                else:
                    break

            if line.startswith("        "):
                tot += 1
                num_e = int(re.findall(r'\d+', line)[0]) + 1
                for x in range(num_e):
                    f2.write(lines[line_num + x])
        f2.close()


def do_gemc(base):
    base = os.getcwd() + "/" + base if base[0] != '/' else base
    with tempdir() as dir_temp:
        shutil.copy(base + ".dat", dir_temp + "/input.dat")
        with open(dir_temp + "/do_sim.sh", "w") as text_file:
            text_file.write("#!/bin/bash \n")
            text_file.write("source /jlab/2.2/ce/jlab.sh \n")
            text_file.write(
                "/jlab/clas12Tags/4a.2.4/source/gemc /jlab/workdir/clas12.gcard -USE_GUI=0 -OUTPUT=\"evio, /jlab/workdir/shared/out.evio\" -INPUT_GEN_FILE=\"LUND, /jlab/workdir/shared/input.dat\" \n\n")
            text_file.write("echo \"Gemc Exit Code: \" $? \n")
            text_file.write("\n")

        command = "docker run -v`pwd`:/jlab/workdir/shared --rm -it jeffersonlab/clas12tags:4a.2.4 bash /jlab/workdir/shared/do_sim.sh"
        out = " 1>" + base + ".out"
        err = " 2>" + base + ".err"
        exit_code = os.system(command + out + err)
        shutil.copy(dir_temp + "/out.evio", base + ".evio")


def main():
    # Make argument parser
    parser = argparse.ArgumentParser(description="Full sim analysis")
    parser.add_argument('-c', dest='cores', type=int, nargs='?',
                        help="Number of cores to use for simulation if not all the cores", default=0)
    parser.add_argument('-o', dest='output_dir', type=str, nargs='?',
                        help="Output directory for final files", default='.')
    parser.add_argument('-i', dest='events', type=str, nargs='?',
                        help="Input event file to run gemc over", default="11gev_sidis_500.dat")
    parser.add_argument('-mc', dest='mc', action='store_true',
                        help="Use gemc MC to generate electrons in a sector")
    parser.set_defaults(mc=False)
    parser.add_argument('-E', dest='energy', type=float, nargs='?',
                        help="Energy for gemc MC to generate electrons at", default=4.5)
    parser.add_argument('-T', dest='theta', type=float, nargs='?',
                        help="Theta angle for gemc MC to generate electrons at", default=20)
    parser.add_argument('-P', dest='phi', type=float, nargs='?',
                        help="phi angle for gemc MC to generate electrons at", default=20)

    args = parser.parse_args()

    args.output_dir = args.output_dir + \
        '/' if args.output_dir[-1] != '/' else args.output_dir

    args.cores = cpu_count() if args.cores == 0 or args.cores > cpu_count() else args.cores

    files = make_names(args.output_dir, args.events, args.cores)
    split_lund(args.events, files)
    pool = Pool(processes=args.cores)
    pool.imap_unordered(do_gemc, files)
    pool.close()
    pool.join()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nExiting")
        sys.exit()
