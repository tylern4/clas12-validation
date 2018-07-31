#!/usr/bin/env python
from __future__ import print_function
from multiprocessing import Pool, cpu_count
import multiprocessing as mp
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


def make_list(args):
    from datetime import datetime
    time = datetime.now().strftime('%m_%d_%Y-%H%M_')
    l = []
    for i in range(0, args.num):
        l.append(args.output + "sim_" + time + str(i))
    return l


def do_gemc(base):
    cwd = os.getcwd()
    with tempdir() as dirpath:
        with open(dirpath + "/do_sim.sh", "w") as text_file:
            text_file.write(r"""#!/bin/bash
            source /jlab/2.2/ce/jlab.sh 2> /dev/null
            /jlab/clas12Tags/4a.2.4/source/gemc clas12.gcard -USE_GUI=0 $@
            """)

        command = "docker run -v`pwd`:/jlab/workdir --rm -it jeffersonlab/clas12tags:4a.2.4 bash /jlab/workdir/do_sim.sh "
        out = " 1>" + base + ".out"
        err = " 2>" + base + ".err"
        command = command + err + out
        os.system(command)
        #shutil.copy(dirpath + "/out.evio", base + ".evio")


def main():
    # Make argument parser
    parser = argparse.ArgumentParser(description="Full sim analysis")
    parser.add_argument('-c', dest='cores', type=int, nargs='?',
                        help="Number of cores to use if not all the cores", default=0)
    parser.add_argument('-n', dest='num', type=int, nargs='?',
                        help="Number of simulations to do", default=100)
    parser.add_argument('-o', dest='output', type=str, nargs='?',
                        help="Output directory for final root files", default=os.getcwd())
    parser.add_argument('-e', dest='events', type=int, nargs='?',
                        help="Number of events to simulate", default=1000)

    args = parser.parse_args()

    if args.output[-1] != '/':
        args.output = args.output + '/'
    if args.cores == 0 or cpu_count > cpu_count():
        args.cores = cpu_count()
    if args.output[0] != '/':
        args.output = os.getcwd() + args.output

    files = make_list(args)
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
