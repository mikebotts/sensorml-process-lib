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

package org.sensorML.process;

import java.util.*;
import org.vast.cdm.common.DataBlock;
import org.vast.data.*;
import org.vast.process.*;


/**
 * <p><b>Title:</b><br/>
 * Time Synchronization Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * This process synchronizes two asynchronous streams by interpolating
 * the slave data stream at times given by the master time stream
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date May 2, 2007
 * @version 1.0
 */
public class TimeSynchronizer_Process extends DataProcess
{
	int masterTimeIndex, slaveTimeIndex, dataInIndex, dataOutIndex;
    DataValue masterTimeData, slaveTimeData;
	AbstractDataComponent dataIn, dataOut;
    boolean nextMasterTime, nextSlaveTime, nextOutput;
    double masterTime;
    LinkedList<SlaveData> slaveDataStack = new LinkedList<SlaveData>();
    int interpolationOrder = 1;
    
	
	class SlaveData
	{
		public double time;
		public DataBlock data;
        
        public SlaveData(double time, DataBlock data)
        {
            this.time = time;
            this.data = data;
        }
	}

    
    @Override
    public void init() throws ProcessException
    {
        try
        {
            masterTimeIndex = inputData.getComponentIndex("masterTime");
            masterTimeData = (DataValue)inputData.getComponent(masterTimeIndex);
            slaveTimeIndex = inputData.getComponentIndex("slaveTime");
            slaveTimeData = (DataValue)inputData.getComponent(slaveTimeIndex);
            
            dataInIndex = inputData.getComponentIndex("dataIn");
            dataIn = inputData.getComponent(dataInIndex);
            dataOutIndex = outputData.getComponentIndex("dataOut");
        	dataOut = outputData.getComponent(dataOutIndex);
        	
        	// TODO check if dataIn and dataOut have the same structure
        }
        catch (Exception e)
        {
            throw new ProcessException(ioError, e);
        }
    }
    
    
    @Override
    public void reset()
    {
        nextMasterTimeNeeded();
        slaveDataStack.clear();
    }
    

    @Override
    public void execute() throws ProcessException
    {
        // get next master time when needed
        if (nextMasterTime)
        {
            masterTime = masterTimeData.getData().getDoubleValue();
            //System.out.println("Master Time: " + masterTime);
            
            if (slaveDataStack.size() > 0 &&
                slaveDataStack.getLast().time >= masterTime)
            {
                interpolateOrder1();
                nextOutput();
            }
            else
                nextSlaveTimeNeeded();
        }        
        
        // keep getting slave time until slaveTime >= masterTime
        else if (nextSlaveTime)
        {
            double slaveTime = slaveTimeData.getData().getDoubleValue();
            DataBlock slaveBlock = dataIn.getData();
            SlaveData newData = new SlaveData(slaveTime, slaveBlock);
            slaveDataStack.add(newData);
            //System.out.println("Slave Time: " + slaveTime);
            
            // remove oldest item if stack size reached interp order
            if (slaveDataStack.size() > 2)
                slaveDataStack.remove(0);
                        
            if (slaveTime >= masterTime)
            {
                interpolateOrder1();
                nextOutput();
            }
        }
        
        // set state to get next master time value
        else if (nextOutput)
            nextMasterTimeNeeded();
    }
    
    
    protected void interpolateOrder1()
    {
        if (slaveDataStack.size() > 1)
        {
            double currentTime = slaveDataStack.get(1).time;
            DataBlock currentData = slaveDataStack.get(1).data;
            double previousTime = slaveDataStack.get(0).time;
            DataBlock previousData = slaveDataStack.get(0).data;
            
            // first order interpolation factor
            double a = (masterTime - previousTime) / (currentTime - previousTime);
            
            // interpolate all values
            int valueCount = dataIn.getData().getAtomCount();
            for (int i=0; i<valueCount; i++)
            {
                double currentValue = currentData.getDoubleValue(i);
                double previousValue = previousData.getDoubleValue(i);
                double outputValue = previousValue + a*(currentValue - previousValue);
                dataOut.getData().setDoubleValue(i, outputValue);
            }
        }
        else
        {
            dataOut.setData(slaveDataStack.get(0).data);
        }
    }
    
    
    protected void nextMasterTimeNeeded()
    {
        inputConnections.get(masterTimeIndex).setNeeded(true);
        inputConnections.get(slaveTimeIndex).setNeeded(false);
        inputConnections.get(dataInIndex).setNeeded(false);
        outputConnections.get(dataOutIndex).setNeeded(false);
        nextMasterTime = true;
        nextSlaveTime = false;
        nextOutput = false;
    }
    
    
    protected void nextSlaveTimeNeeded()
    {
        inputConnections.get(masterTimeIndex).setNeeded(false);
        inputConnections.get(slaveTimeIndex).setNeeded(true);
        inputConnections.get(dataInIndex).setNeeded(true);
        outputConnections.get(dataOutIndex).setNeeded(false);
        nextMasterTime = false;
        nextSlaveTime = true;
        nextOutput = false;
    }
    
    
    protected void nextOutput()
    {
        inputConnections.get(masterTimeIndex).setNeeded(false);
        inputConnections.get(slaveTimeIndex).setNeeded(false);
        inputConnections.get(dataInIndex).setNeeded(false);
        outputConnections.get(dataOutIndex).setNeeded(true);
        nextMasterTime = false;
        nextSlaveTime = false;
        nextOutput = true;
    }
}