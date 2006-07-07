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
 * Nadir Pointing Matrix Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Generates an homogeneous matrix corresponding to an orientation
 * where the upAxis is aligned with nadir direction (normal to
 * ellipsoid surface), forwardAxis orthogonal to upAxis and in the
 * plane of the velocity vector and the third axis chosen so that
 * the result XYZ reference frame is direct.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class NadirPointingMatrix_Process extends DataProcess
{
    private DataValue xData, yData, zData;
    private DataValue vxData, vyData, vzData;
    private DataArray outputMatrix;
    char forwardAxis, upAxis;
    
    
    public NadirPointingMatrix_Process()
    {    	
    }

    
    @Override
    public void init() throws ProcessException
    {
        try
        {
            // input mappings
            xData = (DataValue)inputData.getComponent("location").getComponent("x");
            yData = (DataValue)inputData.getComponent("location").getComponent("y");
            zData = (DataValue)inputData.getComponent("location").getComponent("z");
            vxData = (DataValue)inputData.getComponent("velocity").getComponent("x");
            vyData = (DataValue)inputData.getComponent("velocity").getComponent("y");
            vzData = (DataValue)inputData.getComponent("velocity").getComponent("z");
            
            // output mapping
            outputMatrix = (DataArray)outputData.getComponent("posMatrix");
            
            // read parameters
            upAxis = paramData.getComponent("upAxis").getData().getStringValue().charAt(0);
            forwardAxis = paramData.getComponent("forwardAxis").getData().getStringValue().charAt(0);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
    }
    

    @Override
    public void execute() throws ProcessException
    {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double vx = 0.0;
        double vy = 0.0;
        double vz = 0.0;
        
        if (xData != null)
            x = xData.getData().getDoubleValue();

        if (yData != null)
            y = yData.getData().getDoubleValue();

        if (zData != null)
            z = zData.getData().getDoubleValue();

        if (vxData != null)
            vx = vxData.getData().getDoubleValue();

        if (vyData != null)
            vy = vyData.getData().getDoubleValue();

        if (vzData != null)
            vz = vzData.getData().getDoubleValue();

        Matrix4x4 newMatrix = null;        
        Vector3D heading, other;
        
        Vector3D up = new Vector3D(x, y, z);
        up.normalize();
        
        Vector3D velocity = new Vector3D(vx, vy, vz);
        velocity.normalize();

        if ((forwardAxis == 'X') && (upAxis == 'Z'))
        {
            other = up.cross(velocity);
            other.normalize();
            heading = other.cross(up);
            newMatrix = new Matrix4x4(heading, other, up);
        }

        else if ((forwardAxis == 'X') && (upAxis == 'Y'))
        {
            other = velocity.cross(up);
            other.normalize();
            heading = up.cross(other);
            newMatrix = new Matrix4x4(heading, up, other);
        }

        else if ((forwardAxis == 'Y') && (upAxis == 'X'))
        {
            other = up.cross(velocity);
            other.normalize();
            heading = other.cross(up);
            newMatrix = new Matrix4x4(up, heading, other);
        }

        else if ((forwardAxis == 'Y') && (upAxis == 'Z'))
        {
            other = velocity.cross(up);
            other.normalize();
            heading = up.cross(other);
            newMatrix = new Matrix4x4(other, heading, up);
        }

        else if ((forwardAxis == 'Z') && (upAxis == 'X'))
        {
            other = velocity.cross(up);
            other.normalize();
            heading = up.cross(other);
            newMatrix = new Matrix4x4(up, other, heading);
        }

        else if ((forwardAxis == 'Z') && (upAxis == 'Y'))
        {
            other = up.cross(velocity);
            other.normalize();
            heading = other.cross(up);
            newMatrix = new Matrix4x4(other, up, heading);
        }
        
        newMatrix.translate(x, y, z);
        
        // assign values to output matrix
        DataBlock data = outputMatrix.getData();
        for (int i=0; i<16; i++)
            data.setDoubleValue(i, newMatrix.getElement(i/4, i%4));
    }
}