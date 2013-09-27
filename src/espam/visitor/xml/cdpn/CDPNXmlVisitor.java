
package espam.visitor.xml.cdpn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.Vector;
import java.util.Iterator;

import espam.visitor.CDPNVisitor;
import espam.visitor.xml.adg.ADGraphXmlVisitor;

import espam.datamodel.pn.Process;
import espam.datamodel.pn.Gate;

import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.pn.cdpn.CDGate;
import espam.datamodel.pn.cdpn.CDChannel;

import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.graph.adg.ADGInPort;
import espam.datamodel.graph.adg.ADGOutPort;
import espam.datamodel.graph.adg.ADGEdge;
import espam.datamodel.graph.adg.ADGParameter;

import espam.main.UserInterface;
import espam.datamodel.EspamException;
import espam.datamodel.LinearizationType;

//////////////////////////////////////////////////////////////////////////
//// CDPNXmlVisitor

/**
 *  This class is a visitor that is used to generate
 *  Compaan Dynamic Process Network description in Xml format.
 *
 * @author  Todor Stefanov
 * @version  $Id: CDPNXmlVisitor.java,v 1.1 2007/12/07 22:07:35 stefanov Exp $
 */

public class CDPNXmlVisitor extends CDPNVisitor {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///
    
    /**
     * Constructor for the CDPNXmlVisitor object
     *
     * @param printStream
     *            the output Xml print stream
     */
    public CDPNXmlVisitor(PrintStream printStream) {
        _printStream = printStream;
        _adgVisitor = new ADGraphXmlVisitor(_printStream);
        _adgVisitor.setPrefix(_prefix);
    }
    
    /**
     *  Visit a CDProcessNetwork component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDProcessNetwork x) {
        
        Process levelUpProcess = x.getLevelUpProcess();
        String name;
        if( levelUpProcess != null ) {
            name = levelUpProcess.getName();
        } else {
            name = "null";
        }
        
        _printStream.println("<cdpn name=\""           + x.getName() + "\" " +
                             "levelUpProcess=\"" + name        + "\""  +
                             ">");
        _printStream.println("");
        
        //visit the list of parameters of this CDProcessNetwork
        _adgVisitor.prefixInc();
        Vector parameterList = (Vector) x.getAdg().getParameterList();
        if( parameterList != null ) {
            Iterator i = parameterList.iterator();
            while( i.hasNext() ) {
                ADGParameter parameter = (ADGParameter) i.next();
                parameter.accept(_adgVisitor);
            }
        }
        _adgVisitor.prefixDec();
        _printStream.println("");
        
        
        //visit the list of gates of this CDProcessNetwork
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector gateList = (Vector) x.getGateList();
        if( gateList != null ) {
            Iterator i = gateList.iterator();
            while( i.hasNext() ) {
                Gate gate = (Gate) i.next();
                gate.accept(this);
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        _printStream.println("");
        
        //visit the list of processes of this CDProcessNetwork
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector processList = (Vector) x.getProcessList();
        if( processList != null ) {
            Iterator i = processList.iterator();
            while( i.hasNext() ) {
                CDProcess process = (CDProcess) i.next();
                process.accept(this);
                _printStream.println("");
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        
        //visit the list of channels of this CDProcessNetwork
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector channelList = (Vector) x.getChannelList();
        if( channelList != null ) {
            Iterator i = channelList.iterator();
            while( i.hasNext() ) {
                CDChannel channel = (CDChannel) i.next();
                channel.accept(this);
                _printStream.println("");
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        
        _printStream.println("</cdpn>");
    }
    
    /**
     *  Visit a CDProcess component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDProcess x) {
        
        CDProcessNetwork levelUpProcess = (CDProcessNetwork) x.getLevelUpProcess();
        String name;
        if( levelUpProcess != null ) {
            name = levelUpProcess.getName();
        } else {
            name = "null";
        }
        
        _printStream.println(_prefix + "<process name=\"" + x.getName() + "\" " +
                             "levelUpProcess=\""+ name + "\""  + ">");
        
        //visit the list of ports of this CDProcess
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector gateList = (Vector) x.getGateList();
        if( gateList != null ) {
            Iterator i = gateList.iterator();
            while( i.hasNext() ) {
                CDGate gate = (CDGate) i.next();
                if( gate instanceof CDInGate ) {
                    ((CDInGate) gate).accept(this);
                } else if( gate instanceof CDOutGate ) {
                    ((CDOutGate) gate).accept(this);
                }
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        _printStream.println("");
        
        //visit the list of nodes of this CDProcess
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector nodeList = (Vector) x.getAdgNodeList();
        if( nodeList != null ) {
            Iterator i = nodeList.iterator();
            while( i.hasNext() ) {
                ADGNode node = (ADGNode) i.next();
                node.accept(_adgVisitor);
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        _printStream.println("");
        
        //visit the shcedule of this CDProcess
        _prefixInc();
        _printStream.println(_prefix + "<schedule type=\"parsetree\" >");
        _printStream.println(_prefix + "</schedule>");
        _prefixDec();
        _printStream.println("");
        
        _printStream.println(_prefix + "</process>");
    }
    
    /**
     *  Visit a CDInGate component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDInGate x) {
        
        CDProcess process = (CDProcess) x.getProcess();
        String nameProcess;
        if( process != null ) {
            nameProcess = process.getName();
        } else {
            nameProcess = "null";
        }
        
        CDChannel channel = (CDChannel) x.getChannel();
        String nameChannel;
        if( channel != null ) {
            nameChannel = channel.getName();
        } else {
            nameChannel = "null";
        }
        
        _printStream.println(_prefix + "<ingate name=\""    + x.getName() + "\" " +
                             "process=\"" + nameProcess    + "\" " +
                             "channel=\"" + nameChannel    + "\" " +
                             ">");
        
        //visit the list of ports of this CDInGate
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector portList = (Vector) x.getAdgPortList();
        if( portList != null ) {
            Iterator i = portList.iterator();
            while( i.hasNext() ) {
                ADGInPort port = (ADGInPort) i.next();
                port.accept(_adgVisitor);
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        _printStream.println("");
        
        //visit the list of key functions of this CDInGate
        _prefixInc();
        _printStream.println(_prefix + "<keyfunctions type=\"polynomial\" >");
        Vector keyFuncList = (Vector) x.getKeyFuncList();
        if( keyFuncList != null ) {
            Iterator i = keyFuncList.iterator();
            while( i.hasNext() ) {
                //TODO
            }
        }
        _printStream.println(_prefix + "</keyfunctions>");
        _prefixDec();
        _printStream.println("");
        
        _printStream.println(_prefix + "</ingate>");
    }
    
    
    /**
     *  Visit a CDOutGate component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDOutGate x) {
        
        CDProcess process = (CDProcess) x.getProcess();
        String nameProcess;
        if( process != null ) {
            nameProcess = process.getName();
        } else {
            nameProcess = "null";
        }
        
        CDChannel channel = (CDChannel) x.getChannel();
        String nameChannel;
        if( channel != null ) {
            nameChannel = channel.getName();
        } else {
            nameChannel = "null";
        }
        
        _printStream.println(_prefix + "<outgate name=\""    + x.getName() + "\" " +
                             "process=\"" + nameProcess    + "\" " +
                             "channel=\"" + nameChannel    + "\" " +
                             ">");
        
        //visit the list of ports of this CDOutGate
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector portList = (Vector) x.getAdgPortList();
        if( portList != null ) {
            Iterator i = portList.iterator();
            while( i.hasNext() ) {
                ADGOutPort port = (ADGOutPort) i.next();
                port.accept(_adgVisitor);
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        _printStream.println("");
        
        //visit the list of key functions of this CDInGate
        _prefixInc();
        _printStream.println(_prefix + "<keyfunctions type=\"polynomial\" >");
        Vector keyFuncList = (Vector) x.getKeyFuncList();
        if( keyFuncList != null ) {
            Iterator i = keyFuncList.iterator();
            while( i.hasNext() ) {
                //TODO
            }
        }
        _printStream.println(_prefix + "</keyfunctions>");
        _prefixDec();
        _printStream.println("");
        
        _printStream.println(_prefix + "</outgate>");
    }
    
    
    /**
     *  Visit an CDChannel component.
     *
     * @param  x A Visitor Object.
     */
    public void visitComponent(CDChannel x) {
        
        CDOutGate fromGate = x.getFromGate();
        String nameFromGate;
        String nameFromProcess;
        if( fromGate != null ) {
            nameFromGate = fromGate.getName();
            nameFromProcess = fromGate.getProcess().getName();
            
        } else {
            nameFromGate = "null";
            nameFromProcess = "null";
        }
        
        CDInGate toGate = x.getToGate();
        String nameToGate;
        String nameToProcess;
        if( toGate != null ) {
            nameToGate = toGate.getName();
            nameToProcess = toGate.getProcess().getName();
            
        } else {
            nameToGate = "null";
            nameToProcess = "null";
        }
        
        _printStream.println(_prefix + "<channel name=\""     + x.getName()     + "\" " +
                             "fromGate=\""    + nameFromGate    + "\" " +
                             "fromProcess=\"" + nameFromProcess + "\" " +
                             "toGate=\""      + nameToGate      + "\" " +
                             "toProcess=\""   + nameToProcess   + "\" " +
                             ">");
        
        //visit the list of edges of this CDChannel
        _prefixInc();
        _adgVisitor.prefixInc();
        Vector adgEdgeList = (Vector) x.getAdgEdgeList();
        if( adgEdgeList != null ) {
            Iterator i = adgEdgeList.iterator();
            while( i.hasNext() ) {
                ADGEdge edge = (ADGEdge) i.next();
                edge.accept(_adgVisitor);
            }
        }
        _adgVisitor.prefixDec();
        _prefixDec();
        //_printStream.println("");
        
        
        //visit the communication model of this CDChannel
        _prefixInc();
        LinearizationType cm = x.getCommunicationModel();
        _printStream.println(_prefix + "<communcationmodel type=\"" + cm + "\" />");
        _prefixDec();
        
        _printStream.println(_prefix + "</channel>");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ///
    
    /**
     *  An ADG Xml visitor.
     *
     */
    
    private ADGraphXmlVisitor _adgVisitor = null;
}

