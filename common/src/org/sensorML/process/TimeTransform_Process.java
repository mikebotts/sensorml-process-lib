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

import org.vast.data.*;
import org.vast.process.*;
import org.ogc.cdm.common.*;


/**
 * <p><b>Title:</b><br/>
 * Time Transform Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO TimeTransformProcess type description
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class TimeTransform_Process extends DataProcess
{
	AbstractDataComponent refTime, localTime, resultTime;
    
    
    public TimeTransform_Process()
    {    	
    }
    
    
    protected void buildIO()
    {
    	this.addInput("referenceTime", new DataValue(DataType.DOUBLE));
    	this.addInput("localTime", new DataValue(DataType.DOUBLE));
    	inputData.assignNewDataBlock();
    	
    	this.addOutput("time", new DataValue(DataType.DOUBLE));
    }

    
    public void init() throws ProcessException
    {
    	if (inputData.getComponentCount() == 0 && outputData.getComponentCount() == 0)
    		buildIO();
    	
    	try
        {
            refTime = inputData.getComponent("referenceTime");
            localTime = inputData.getComponent("localTime");
            resultTime = outputData.getComponent("time");
        }
        catch (RuntimeException e)
        {
            throw new ProcessException("Invalid i/o structure");
        }
    }
    

    public void execute() throws ProcessException
    {
        // compute transformed time
        double time = refTime.getData().getDoubleValue() + localTime.getData().getDoubleValue();
        
        resultTime.getData().setDoubleValue(time);
        
        //System.out.println("refTime = " + refTime.getData().getDoubleValue() + ", localTime = " + localTime.getData().getDoubleValue());
    }    
}