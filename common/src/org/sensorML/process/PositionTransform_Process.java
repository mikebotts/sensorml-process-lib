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
import org.vast.math.*;
import org.ogc.cdm.common.*;


/**
 * <p><b>Title:</b><br/>
 * Position Transform Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Implementation of the Position Transformation Process
 * using homogeneous matrices
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class PositionTransform_Process extends DataProcess
{
	AbstractDataComponent refPos, localPos, resultPos;
    
    
    public PositionTransform_Process()
    {
    	
    }
    
    
    protected void buildIO()
    {
    	// create inputs
    	DataArray refMatrix = new DataArray(16);
    	refMatrix.addComponent(new DataValue(DataType.DOUBLE));
    	this.addInput("localPosition", refMatrix);
    	DataArray localMatrix = new DataArray(16);
    	localMatrix.addComponent(new DataValue(DataType.DOUBLE));    	
    	this.addInput("referencePosition", localMatrix);    	
     	
    	// create outputs
    	DataArray outMatrix = new DataArray(16);
    	outMatrix.addComponent(new DataValue(DataType.DOUBLE));    	
    	this.addOutput("position", outMatrix);
    }

    
    public void init() throws ProcessException
    {
    	if (inputData.getComponentCount() == 0 && outputData.getComponentCount() == 0)
    		buildIO();
    	
    	try
        {
            refPos = inputData.getComponent("referencePosition");
            localPos = inputData.getComponent("localPosition");
            
            resultPos = outputData.getComponent("position");
            resultPos.assignNewDataBlock();
        }
        catch (RuntimeException e)
        {
            throw new ProcessException("Invalid i/o structure");
        }
    }
    

    public void execute() throws ProcessException
    {
        DataBlock refMatrixData = refPos.getData();
        DataBlock locMatrixData = localPos.getData();
    	
    	// compute transformed position
        Matrix4x4 refMatrix = new Matrix4x4();
        for (int i=0; i<16; i++)
        	refMatrix.setElement(i/4, i%4, refMatrixData.getDoubleValue(i));
        
        Matrix4x4 locMatrix = new Matrix4x4();
        for (int i=0; i<16; i++)
        	locMatrix.setElement(i/4, i%4, locMatrixData.getDoubleValue(i));
             
        refMatrix.multiply(locMatrix);
        
        // assign output values
        DataBlock resultMatrixData = resultPos.getData();
        for (int i=0; i<16; i++)
        	resultMatrixData.setDoubleValue(i, refMatrix.getElement(i/4, i%4));
        
        //System.out.println(name + ":\n" + locMatrix);
    } 
}