#!/usr/bin/python

from distutils.core import setup
from distutils.extension import Extension
from Cython.Distutils import build_ext

setup(
    cmdclass = {'build_ext': build_ext},
    ext_modules = [
        Extension("phrt_dse", ["phrt_dse.pyx"]),
        Extension("PlatformGenerator", ["PlatformGenerator.pyx"]),
        Extension("PlatformParameters", ["PlatformParameters.pyx"]),
        Extension("ACSDFModel", ["ACSDFModel.pyx"]),
        Extension("ActorModel", ["ActorModel.pyx"]),
        Extension("ChannelModel", ["ChannelModel.pyx"]),
        Extension("Utilities", ["Utilities.pyx"]),
        Extension("Mapping", ["Mapping.pyx"]),
        Extension("CSDFParser", ["CSDFParser.pyx"]),
        Extension("SolveILS", ["SolveILS.pyx"]),
        Extension("ILSRoutines", ["ILSRoutines.pyx"]),
        Extension("Partition", ["Partition.pyx"]),
        Extension("DotHandler", ["DotHandler.pyx"]),
        Extension("TextHandler", ["TextHandler.pyx"]),
        Extension("EspamHandler", ["EspamHandler.pyx"])
    ]
)
