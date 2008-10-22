/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the University of Alabama in Huntsville (UAH). <http://vast.uah.edu> Portions created by the Initial Developer are Copyright (C) 2007 the Initial Developer. All Rights Reserved. Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
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
 * @author Alexandre Robin, Gregoire Berthiau
 * @date May 2, 2007
 * @version 1.0
 */
public class TimeSynchronizer_Process extends DataProcess
{
	int masterTimeIndex, slaveTimeIndex, dataInSlaveIndex, dataOutSlaveIndex;
	int dataInMasterIndex, dataOutMasterIndex;    
	DataValue masterTimeData, slaveTimeData, computationalMethodData, slaveTimeIndexInDataInData;
	AbstractDataComponent dataInSlave, dataOutSlave, dataInMaster, dataOutMaster;
    boolean nextMasterTime, nextSlaveTime, nextOutput;
    double masterTime;
    LinkedList<SlaveData> slaveDataStack = new LinkedList<SlaveData>();
    int interpolationOrder = 1;
    String computationalMethod;
    DataBlock masterBlock;
    
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
            
            dataInSlaveIndex = inputData.getComponentIndex("dataInSlave");
            dataInSlave = inputData.getComponent(dataInSlaveIndex);
            dataOutSlaveIndex = outputData.getComponentIndex("dataOutSlave");
        	dataOutSlave = outputData.getComponent(dataOutSlaveIndex);
        	
        	dataInMasterIndex = inputData.getComponentIndex("dataInMaster");
            dataInMaster = inputData.getComponent(dataInMasterIndex);
            dataOutMasterIndex = outputData.getComponentIndex("dataOutMaster");
        	dataOutMaster = outputData.getComponent(dataOutMasterIndex);
        	
        	computationalMethodData = (DataValue)paramData.getComponent("computationalMethod");
        	computationalMethod = computationalMethodData.getData().getStringValue();
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
    	   	
    	if(computationalMethod.equalsIgnoreCase("step"))
    	{
    		// get next master time when needed
    		if (nextMasterTime)
    		{
    			masterTime = masterTimeData.getData().getDoubleValue();
    			masterBlock = dataInMaster.getData();
    			System.out.println("Master Time: " + masterTime);
               
    			if (slaveDataStack.size() > 0 &&
    					slaveDataStack.getLast().time >= masterTime)
    			{
    				lowerStep();
    		//		nextOutput();
    			}
    			else
    				nextSlaveTimeNeeded();
    		}        
            
    		// keep getting slave time until slaveTime >= masterTime
    		else if (nextSlaveTime)
    		{
    			double slaveTime = slaveTimeData.getData().getDoubleValue();
    			System.out.println("Slave Time: " + slaveTime);
    			DataBlock slaveBlock = dataInSlave.getData();
    			SlaveData newData = new SlaveData(slaveTime, slaveBlock);
    			slaveDataStack.add(newData);
    			
    			
    			// remove oldest item if stack size reached interp order
    			if (slaveDataStack.size() > 2)
    				slaveDataStack.remove(0);
                           
    			if (slaveTime >= masterTime)
    			{
    				lowerStep();
    			//	nextOutput();
    			}
    		}
            
    		// set state to get next master time value
    		else if (nextOutput)
    			nextMasterTimeNeeded();
    	}	
    	
    	if(computationalMethod.equalsIgnoreCase("linearInterpolation"))
    	{
    		// get next master time when needed
    		if (nextMasterTime)
        	{
            	masterTime = masterTimeData.getData().getDoubleValue();
            	//System.out.println("Master Time: " + masterTime);
            
            	if (slaveDataStack.size() > 0 &&
            		slaveDataStack.getLast().time >= masterTime)
            	{
                	lowerStep();
                	nextOutput();
            	}
            	else
            		nextSlaveTimeNeeded();
        	}        
        
        	// keep getting slave time until slaveTime >= masterTime
        	else if (nextSlaveTime)
        	{
            	double slaveTime = slaveTimeData.getData().getDoubleValue();
            	DataBlock slaveBlock = dataInSlave.getData();
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
    }
    
    
    protected void lowerStep()
    {
        if (slaveDataStack.size() == 2)
        {
            double currentTime = slaveDataStack.get(1).time;
            double previousTime = slaveDataStack.get(0).time;
            if(masterTime<currentTime && masterTime>previousTime)
            {
            	dataOutSlave.setData(slaveDataStack.get(0).data);
            	dataOutMaster.setData(masterBlock);
            	nextOutput();
            }
            else if(masterTime<previousTime)
            {
            	nextMasterTimeNeeded();
            }
            else if(masterTime>currentTime)
            {
            	nextSlaveTimeNeeded();
            }
        }
        else if (slaveDataStack.size() < 2)
        {
        	nextSlaveTimeNeeded();
        }
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
            int valueCount = dataInSlave.getData().getAtomCount();
            for (int i=0; i<valueCount; i++)
            {
                double currentValue = currentData.getDoubleValue(i);
                double previousValue = previousData.getDoubleValue(i);
                double outputValue = previousValue + a*(currentValue - previousValue);
                dataOutSlave.getData().setDoubleValue(i, outputValue);
            }
        }
        else
        {
            dataOutSlave.setData(slaveDataStack.get(0).data);
        }
    }
    
    
    protected void nextMasterTimeNeeded()
    {
        inputConnections.get(masterTimeIndex).setNeeded(true);
        inputConnections.get(slaveTimeIndex).setNeeded(false);
        inputConnections.get(dataInSlaveIndex).setNeeded(false);
        outputConnections.get(dataOutSlaveIndex).setNeeded(false);
        inputConnections.get(dataInMasterIndex).setNeeded(true);
        outputConnections.get(dataOutMasterIndex).setNeeded(false);
        nextMasterTime = true;
        nextSlaveTime = false;
        nextOutput = false;
    }
    
    
    protected void nextSlaveTimeNeeded()
    {
        inputConnections.get(masterTimeIndex).setNeeded(false);
        inputConnections.get(slaveTimeIndex).setNeeded(true);
        inputConnections.get(dataInSlaveIndex).setNeeded(true);
        outputConnections.get(dataOutSlaveIndex).setNeeded(false);
        inputConnections.get(dataInMasterIndex).setNeeded(false);
        outputConnections.get(dataOutMasterIndex).setNeeded(false);
        nextMasterTime = false;
        nextSlaveTime = true;
        nextOutput = false;
    }
    
    
    protected void nextOutput()
    {
        inputConnections.get(masterTimeIndex).setNeeded(false);
        inputConnections.get(slaveTimeIndex).setNeeded(false);
        inputConnections.get(dataInSlaveIndex).setNeeded(false);
        outputConnections.get(dataOutSlaveIndex).setNeeded(true);
        inputConnections.get(dataInMasterIndex).setNeeded(false);
        outputConnections.get(dataOutMasterIndex).setNeeded(true);
        nextMasterTime = false;
        nextSlaveTime = false;
        nextOutput = true;
    }
}