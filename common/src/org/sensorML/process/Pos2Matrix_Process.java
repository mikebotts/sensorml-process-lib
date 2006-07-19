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

import org.ogc.cdm.common.*;
import org.vast.data.*;
import org.vast.math.*;
import org.vast.process.*;


/**
 * <p><b>Title:</b><br/>
 * Generic Positioning Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * 
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class Pos2Matrix_Process extends DataProcess
{
    private DataValue txData, tyData, tzData;
    private DataValue rxData, ryData, rzData;
    private DataArray outputMatrix;
    private char[] rotationOrder = {'X','Y','Z'};
    private Matrix4d newMatrix = new Matrix4d();
    private Matrix4d xRotMatrix = new Matrix4d();
    private Matrix4d yRotMatrix = new Matrix4d();
    private Matrix4d zRotMatrix = new Matrix4d();
    
    
    public Pos2Matrix_Process()
    {    	
    }

    
    @Override
    public void init() throws ProcessException
    {
        try
        {
            txData = (DataValue)inputData.getComponent("location").getComponent("x");
            tyData = (DataValue)inputData.getComponent("location").getComponent("y");
            tzData = (DataValue)inputData.getComponent("location").getComponent("z");
            rxData = (DataValue)inputData.getComponent("orientation").getComponent("x");
            ryData = (DataValue)inputData.getComponent("orientation").getComponent("y");
            rzData = (DataValue)inputData.getComponent("orientation").getComponent("z");
            outputMatrix = (DataArray)outputData.getComponent("posMatrix");
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
    }
    

    @Override
    public void execute() throws ProcessException
    {
        double tx = 0.0;
        double ty = 0.0;
        double tz = 0.0;
        double rx = 0.0;
        double ry = 0.0;
        double rz = 0.0;
        
        if (txData != null)
            tx = txData.getData().getDoubleValue();

        if (tyData != null)
            ty = tyData.getData().getDoubleValue();

        if (tzData != null)
            tz = tzData.getData().getDoubleValue();

        if (rxData != null)
            rx = rxData.getData().getDoubleValue();

        if (ryData != null)
            ry = ryData.getData().getDoubleValue();

        if (rzData != null)
            rz = rzData.getData().getDoubleValue();

        // set up rotation matrices
        newMatrix.setIdentity();
        xRotMatrix.setIdentity();
        yRotMatrix.setIdentity();
        zRotMatrix.setIdentity();
        xRotMatrix.rotX(rx);
        yRotMatrix.rotY(ry);
        zRotMatrix.rotZ(rz);
 
        // rotate in given order
        for (int i=0; i<3; i++)
        {
            char axis = rotationOrder[i];
            
            switch (axis)
            {
                case 'X':
                    newMatrix.mul(xRotMatrix);
                    break;
                    
                case 'Y':
                    newMatrix.mul(yRotMatrix);
                    break;
                    
                case 'Z':
                    newMatrix.mul(zRotMatrix);
                    break;
            }
        }

        // translate
        newMatrix.setTranslation(tx, ty, tz);
        
        // assign values to output matrix
        DataBlock data = outputMatrix.getData();
        for (int i=0; i<16; i++)
            data.setDoubleValue(i, newMatrix.getElement(i/4, i%4));
    }
}