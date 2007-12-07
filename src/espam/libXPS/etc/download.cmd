/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

setMode -bscan
setCable -p lpt1
addDevice -p 1 -file implementation/download.bit
addDevice -p 2 -file etc/xc2v1000_fg456.bsd
assignfile -p 2 -file  etc/xc2v1000_fg456.bsd
addDevice -p 3 -file etc/xc18v04_vq44.bsd
assignfile -p 3 -file  etc/xc18v04_vq44.bsd
program -p 1
quit
