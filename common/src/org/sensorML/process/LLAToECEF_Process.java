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

import org.vast.process.*;
import org.ogc.cdm.common.*;
import org.vast.data.*;
import org.vast.math.*;
import org.vast.physics.*;
import org.vast.mapping.*;


/**
 * <p><b>Title:</b><br/>
 * LLA To ECEF coordinate Transform Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO LLAToECEFProcess type description
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 2, 2005
 * @version 1.0
 */
public class LLAToECEF_Process extends DataProcess
{
    AbstractDataComponent latData, lonData, altData;
    AbstractDataComponent lrxData, lryData, lrzData;
    AbstractDataComponent outputPos;
    
    protected boolean nadirOriented = true;
    protected int upAxis = 3;
    protected int northAxis = 2;
    char[] rotationOrder = {'Z','X','Y'};


    public LLAToECEF_Process()
    {    	
    }
    
    
    // helper method to build outputs if not reading from xml
    protected void buildIO()
    {
    	// build inputs
    	DataGroup geoLocData = new DataGroup(3);
    	geoLocData.addComponent("latitude", new DataValue(DataType.DOUBLE));
    	geoLocData.addComponent("longitude", new DataValue(DataType.DOUBLE));
    	geoLocData.addComponent("altitude", new DataValue(DataType.DOUBLE));
    	this.addInput("geoLocation", geoLocData);
    	
    	DataGroup locAttData = new DataGroup(3);
    	locAttData.addComponent("x", new DataValue(DataType.DOUBLE));
    	locAttData.addComponent("y", new DataValue(DataType.DOUBLE));
    	locAttData.addComponent("z", new DataValue(DataType.DOUBLE));
    	this.addInput("localOrientation", locAttData);
    	
    	// build outputs
    	DataArray ecefMatrix = new DataArray(16);
    	ecefMatrix.addComponent(new DataValue(DataType.DOUBLE));
    	this.addOutput("ecefPosition", ecefMatrix);
    }

    
    public void init() throws ProcessException
    {
    	if (inputData.getComponentCount() == 0 && outputData.getComponentCount() == 0)
    		buildIO();
    	
    	try
        {
            // get input data containers + create appropriate Unit Converters
    		AbstractDataComponent locationData = inputData.getComponent("geoLocation");
            if (locationData != null)
            {
	            latData = locationData.getComponent("latitude");          
	            lonData = locationData.getComponent("longitude");       
	            altData = locationData.getComponent("altitude");
            }
            
            // get orientation data containers + create appropriate Unit Converters
            AbstractDataComponent orientationData = inputData.getComponent("localOrientation");
            if (orientationData != null)
            {
	            lrxData = orientationData.getComponent("x");          
	            lryData = orientationData.getComponent("y");    
	            lrzData = orientationData.getComponent("z");
            }
            
            // read rotation order
            String rotOrder = orientationData.getComponent("order").getData().getStringValue();
            if (rotOrder != null && rotOrder.length() == 3)
            	rotationOrder = rotOrder.toCharArray();
            
            // set up upAxis and northAxis depending on the frame (ENU, NED, etc...)
            String refFrame = (String)orientationData.getProperty("referenceFrame");
            if (refFrame != null)
            {
            	if (refFrame.contains("NED"))
            	{
            		northAxis = 1;
            		upAxis = -3;
            	}
            	else if (refFrame.contains("WND"))
            	{
            		northAxis = 2;
            		upAxis = -3;
            	}
            }
            
            
            // output data containers
        	outputPos = outputData.getComponent("ecefPosition");
        	outputPos.assignNewDataBlock();
        }
        catch (Exception e)
        {
            throw new ProcessException("Invalid input structure");
        }
    }
   
    
    public void execute() throws ProcessException
    {
    	Matrix4x4 toEcefMatrix;
    	Matrix3x3 nadirMatrix;
    	Matrix3x3 attitudeMatrix;
    	
    	// get lat,lon,alt coordinates from input and convert to SI
    	double lat = latData.getData().getDoubleValue();
    	double lon = lonData.getData().getDoubleValue();
    	double alt = altData.getData().getDoubleValue();
        
        // convert to ECEF
        double[] ecefPos = MapProjection.LLAtoECF(lat, lon, alt, new Datum());
        
        // compute nadir orientation if needed
        // default = north/east/up orientation
        if (nadirOriented == true)
        {
        	Vector3D ecfPosition;
        	
        	ecfPosition = new Vector3D(ecefPos[0], ecefPos[1], ecefPos[2]);        	
            Vector3D toEcfNorth = NadirPointing.getEcfVectorToNorth(ecfPosition);
            nadirMatrix = NadirPointing.getRotationMatrix(ecfPosition, toEcfNorth, northAxis, upAxis);
            toEcefMatrix = new Matrix4x4(nadirMatrix);
        }
        else
        {
        	toEcefMatrix = new Matrix4x4();
        }
        
        // add translation coordinates
        for (int i=0; i<3; i++)
        	toEcefMatrix.setElement(i, 3, ecefPos[i]);
        
        // apply pitch/roll/yaw rotations
        // local order z y x / yaw pitch roll
        attitudeMatrix = computeMatrix();
        toEcefMatrix.multiply(attitudeMatrix);
        
        // set output matrix values
        for (int i=0; i<15; i++)
        	outputPos.getData().setDoubleValue(i, toEcefMatrix.getElement(i/4, i%4));
        
        //System.out.println("lla = " + lat + "," + lon + "," + alt);
    }
    
    
    /**
     * Computes attitude matrix with respect to local nadir orientation
     * @return 3x3 orientation matrix
     */
    protected Matrix3x3 computeMatrix()
	{
    	// retrieve rotation values (converted to SI)
    	double rx = lrxData.getData().getDoubleValue();
    	double ry = lryData.getData().getDoubleValue();
    	double rz = lrzData.getData().getDoubleValue();

    	// set up rotation matrices
    	Matrix3x3 newMatrix = new Matrix3x3();

		// rotate in given order
		for (int i=0; i<3; i++)
		{
			char axis = rotationOrder[i];
			
			switch (axis)
			{
				case 'X':
					newMatrix.rotateX(rx);
					break;
					
				case 'Y':
					newMatrix.rotateY(ry);
					break;
					
				case 'Z':
					newMatrix.rotateZ(rz);
					break;
			}
		}
		
		//System.out.println("xyz = " + rx + "," + ry + "," + rz);
		
		return newMatrix;
	}
}
