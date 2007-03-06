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
import org.vast.cdm.common.DataType;
import org.vast.data.*;
import org.vast.process.*;


/**
 * <p><b>Title:</b><br/>
 * Time Synchronization Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * A look up table process allows to map one input variable (independent)
 * to several output variables (dependent) using discrete tie points from
 * a table and given interpolation/extrapolation methods.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class TimeSynchronizer_Process extends DataProcess
{
	DataValue masterTimeData, slaveTimeData;
	AbstractDataComponent dataIn, dataOut;
    DataQueue masterTimeQueue;
    boolean getNextTime = true;
    double masterTime;
    List<InputData> inputDataStack = new LinkedList<InputData>();
    
	
	class InputData
	{
		public double time;
		public DataBlock data;
	}
	
    
    public TimeSynchronizer_Process()
    {
    	this.addInput("masterTime", new DataValue(DataType.DOUBLE));
    	this.addInput("slaveTime", new DataValue(DataType.DOUBLE));  	
    }

    
    public void init() throws ProcessException
    {
        dataIn = inputData.getComponent(2);
    	dataOut = outputData.getComponent(0);
    	
    	// TODO check if dataIn and dataOut have the same structure
    	

    }
    

    /**
     * Executes process algorithm on inputs and set output data
     */
    public void execute() throws ProcessException
    {
    	// get value from master time if necessary
    	if (getNextTime)
    	{
    		try
			{
				masterTime = masterTimeQueue.get().getDoubleValue();
				getNextTime = false;
			}
			catch (InterruptedException e)
			{
				return;
			}		
    	}
    	
    	// read one more slave time + data
    	InputData inputData = new InputData();
    	inputData.time = slaveTimeData.getData().getDoubleValue();
    	inputData.data = dataIn.getData();
    	inputDataStack.add(0, inputData);
    	
    	// check if we reached master time
    	if (inputData.time == masterTime)
    	{
    		// pass data block through
    		dataOut.setData(inputData.data);
    		getNextTime = true;
    	}
    	else
    	{
    		if (inputData.time > masterTime)
    		{
    			double currentTime = inputData.time;
    			DataBlock currentData = inputData.data;
    			double previousTime = inputDataStack.get(1).time;
    			DataBlock previousData = inputDataStack.get(1).data;
    			
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
                
                getNextTime = true;
    		}
    	}
    }    
}