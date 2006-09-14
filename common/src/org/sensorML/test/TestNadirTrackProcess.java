/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the
 University of Alabama in Huntsville (UAH).
 Portions created by the Initial Developer are Copyright (C) 2006
 the Initial Developer. All Rights Reserved.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorML.test;

import org.ogc.cdm.common.DataComponent;
import org.vast.data.DataGroup;
import org.vast.io.xml.DOMReader;
import org.vast.process.DataProcess;
import org.vast.process.ProcessChain;
import org.vast.sensorML.reader.ProcessLoader;
import org.vast.sensorML.reader.ProcessReader;
import org.vast.util.DateTime;
import org.vast.util.ExceptionSystem;
import junit.framework.TestCase;


public class TestNadirTrackProcess extends TestCase
{
    
    protected ProcessChain parseChain(String processChainURL) throws Exception
    {
        String processMapURL = TestNadirTrackProcess.class.getResource("ProcessMap.xml").toString();
        ProcessLoader.reloadMaps(processMapURL);
        
        DOMReader dom = new DOMReader(processChainURL + "#PROCESS_CHAIN", false);
        ProcessReader processReader = new ProcessReader(dom);
        processReader.setReadMetadata(false);
        processReader.setCreateExecutableProcess(true);
        
        ProcessChain process = (ProcessChain)processReader.readProcess(dom.getBaseElement());
        process.setChildrenThreadsOn(false);        
        
        return process;
    }
    
    
    public void testProcessChainParsing()
    {
        try
        {
            String processChainURL = TestNadirTrackProcess.class.getResource("NadirTrack.xml").toString();
            ProcessChain process = parseChain(processChainURL);
            System.out.println(process);
            System.out.println(process.needSync() ? "sync on" : "sync off");
        }
        catch (Exception e)
        {
            ExceptionSystem.debug = true;
            ExceptionSystem.display(e);
            fail(e.getMessage());
        }
    }
    
    
    public void testProcessChainExecution()
    {
        try
        {
            String processChainURL = TestNadirTrackProcess.class.getResource("NadirTrack.xml").toString();
            ProcessChain processChain = parseChain(processChainURL);
            
            // set tle file path
            String tlePath = TestNadirTrackProcess.class.getResource("calipso.txt").getPath();//.toString().substring(6);
            ProcessChain orbitProcessChain = (ProcessChain)processChain.getProcess("orbitPosMatrix");
            DataProcess tleProcess = orbitProcessChain.getProcess("tle");
            tleProcess.getParameterList().getComponent("tleDataUrl").getData().setStringValue(tlePath);
            System.out.println("TLE File Path: " + tlePath + "\n");
            
            // set input time range
            double julianTime = new DateTime().getJulianTime(); // get today's date
            DataComponent input = processChain.getInputList().getComponent("time");
            input.getData().setDoubleValue(julianTime);
            processChain.setInputReady(0);
            System.out.println("Input Julian Time: " + julianTime);
            
            // execute chain
            processChain.init();
            processChain.createNewInputBlocks();
            processChain.execute();
            DataGroup outputLocation = (DataGroup)processChain.getOutputList().getComponent("sampleLocation");
            String lat = outputLocation.getComponent("latitude").getData().getStringValue();
            String lon = outputLocation.getComponent("longitude").getData().getStringValue();
            String alt = outputLocation.getComponent("altitude").getData().getStringValue();            
            System.out.println("Result Location: " + lat + "," + lon + "," + alt);
        }
        catch (Exception e)
        {
            ExceptionSystem.debug = true;
            ExceptionSystem.display(e);
            fail(e.getMessage());
        }
    }
}
