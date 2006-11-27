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

import org.ogc.cdm.common.DataComponent;
import org.vast.data.*;
import org.vast.process.*;

/**
 * <p><b>Title:</b><br/>
 * Counter_Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Implements a for loop style counter.
 * Variable is incremented from start value to stop value using
 * the provided step value. The pass-through data is duplicated
 * at every iteration. 
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Jan 20, 2006
 * @version 1.0
 */
public class Counter_Process extends DataProcess
{
    protected DataComponent inputPassThrough, outputPassThrough;
    protected DataValue inputStart, inputStop, inputStep;
    protected DataValue outputIndex, outputVariable;
    protected int countIndex;
    protected double start, stop, step, var;
    protected boolean useStepCount, done;
    
    
    public Counter_Process()
    {
    }


    /**
     * Initializes the process
     * Gets handles to input/output components
     */
    public void init() throws ProcessException
    {
        done = true;
        needSync = true;
        useStepCount = false;
        
        try
        {
            inputPassThrough = inputData.getComponent("pass-through");
            DataGroup group = (DataGroup) inputData.getComponent("countingRange");
            inputStart = (DataValue) group.getComponent("start");
            inputStop = (DataValue) group.getComponent("stop");
            inputStep = (DataValue) group.getComponent("stepSize");
            if (inputStep == null)
            {
                inputStep = (DataValue) group.getComponent("stepCount");
                useStepCount = true;
            }

            int outputNum = outputData.getComponentIndex("pass-through");
            outputPassThrough = outputData.getComponent(outputNum);
            DataGroup countOutput = (DataGroup) outputData.getComponent("count");
            outputIndex = (DataValue) countOutput.getComponent("index");
            outputVariable = (DataValue) countOutput.getComponent("variable");
        }
        catch (Exception e)
        {
            throw new ProcessException(ioError, e);
        }
    }


    /**
     * Executes process algorithm on inputs and set output data
     */
    public void execute() throws ProcessException
    {
        // get input variables only if previous loop is done
        if (done)
        {
            start = inputStart.getData().getDoubleValue();
            stop = inputStop.getData().getDoubleValue();
            step = inputStep.getData().getDoubleValue();
            if (useStepCount)
                step = (stop - start) / step;
            
            // forward pass-through data
            ((DataGroup)inputPassThrough).combineDataBlocks();
            outputPassThrough.setData(inputPassThrough.getData());
            
            // set inputs as not needed so that we can continue looping
            // wihtout requiring inputs
            for (int i=0; i<inputConnections.size(); i++)
                inputConnections.get(i).setNeeded(false);
            
            // output first value
            var = start;
            countIndex = 0;
            done = false;
        }
        
        // set output variable
        outputVariable.getData().setDoubleValue(var);
        outputIndex.getData().setIntValue(countIndex);
        //System.out.println("index = " + countIndex + ", var = " + var);
        
        // reset stuffs if end of loop
        countIndex++;
        var += step;
        if (var >= stop)
        {
            done = true;
            
            // set inputs as needed since loop has ended
            // and we need another set of inputs
            for (int i=0; i<inputConnections.size(); i++)
                inputConnections.get(i).setNeeded(true);            
            this.setAvailability(inputConnections, false);
        }
    }
}