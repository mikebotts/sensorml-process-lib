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
   Kevin Carter <kcarter@nsstc.uah.edu>
   Dr. Mike Botts <mike.botts@uah.edu>

******************************* END LICENSE BLOCK ***************************/

package org.sensorML.process;

import org.vast.data.*;
import org.vast.process.*;

/**
* <p><b>Title:RPC_Process</b><br/>
* 
* </p>
* <p><b>Description:</b><br/>
* 	Process for supporting Rational Polynomial Coefficients (RPC) 
* 	  assuming object-to-image mapping
* 	  (e.g. typically converting Latitude,Longitude,Altitude to image coordinates)
*
* </p>
* <p>Copyright (c) 2007</p>
* @author Kevin Carter
* @date March 10, 2006
* @version 1.0
* 
* ---------
* @author Mike Botts
* @date May 11, 2006
* corrected, cleaned up, and validated
*
* @date October 5, 2007 
* updated for revised schema
* 
* @date January 3, 2008
* updated for revised schema
*/
public class RPC_Process extends DataProcess
{
	DataValue inputX, inputY, inputZ;
	DataValue outputX, outputY;
	//DataValue minX, minY, maxX, maxY;      NOT CURRENTLY USED
	DataValue img_xo, img_xs, img_yo, img_ys;
	DataValue tar_xo, tar_xs, tar_yo, tar_ys, tar_zo, tar_zs;
	DataValue[] x_num, x_den, y_num, y_den;
	//DataValue errorBias, errorRandom;    NOT CURRENTLY USED

	public RPC_Process(){
		
	}
	
	/**
	 * Initializes the process
	 * Gets handles to input/output components
	 */
	public void init() throws ProcessException
	{
		try
        {
            //I/O mappings
    		inputX = (DataValue) inputData.getComponent("target_location").getComponent("x");
    		inputY = (DataValue) inputData.getComponent("target_location").getComponent("y");
    		inputZ = (DataValue) inputData.getComponent("target_location").getComponent("z");
    		outputX = (DataValue) outputData.getComponent("image_location").getComponent("x");
    		outputY = (DataValue) outputData.getComponent("image_location").getComponent("y");
    		
    		//Parameter mappings
    		DataGroup rpcParams = (DataGroup) paramData.getComponent("rpc_parameter_series"); 
    		DataGroup rpcParamSet = (DataGroup) rpcParams.getComponent("rpc_parameter_set"); 

    		// image region - NOT CURRENTLY USED
//    		DataGroup rpcImageRegion = (DataGroup) rpcParamSet.getComponent("image_region"); 
//    		minX = (DataValue) rpcImageRegion.getComponent("zone_minX");
//    		minY = (DataValue) rpcImageRegion.getComponent("zone_minY");
//    		maxX = (DataValue) rpcImageRegion.getComponent("zone_maxX");
//    		maxY = (DataValue) rpcImageRegion.getComponent("zone_maxY");
    		
    		// image adjustment parameters
    		DataGroup rpcImageAdj = (DataGroup) rpcParamSet.getComponent("image_adjustment"); 
    		img_xo = (DataValue) rpcImageAdj.getComponent("image_x_offset");
    		img_xs = (DataValue) rpcImageAdj.getComponent("image_x_scale");
    		img_yo = (DataValue) rpcImageAdj.getComponent("image_y_offset");
    		img_ys = (DataValue) rpcImageAdj.getComponent("image_y_scale");
    		
    		// target adjustment parameters
    		DataGroup rpcTargetAdj = (DataGroup) rpcParamSet.getComponent("target_adjustment"); 
    		tar_xo = (DataValue) rpcTargetAdj.getComponent("target_x_offset");
    		tar_xs = (DataValue) rpcTargetAdj.getComponent("target_x_scale");
    		tar_yo = (DataValue) rpcTargetAdj.getComponent("target_y_offset");
    		tar_ys = (DataValue) rpcTargetAdj.getComponent("target_y_scale");
    		tar_zo = (DataValue) rpcTargetAdj.getComponent("target_z_offset");
    		tar_zs = (DataValue) rpcTargetAdj.getComponent("target_z_scale");
    		
    		// x numerator coefficients
    		DataGroup x_num_coefs = (DataGroup) rpcParamSet.getComponent("x_numerator_coefficients");
    		x_num = new DataValue[20];
    		x_num[0] = (DataValue) x_num_coefs.getComponent("constant");
    		x_num[1] = (DataValue) x_num_coefs.getComponent("x");
    		x_num[2] = (DataValue) x_num_coefs.getComponent("y");
    		x_num[3] = (DataValue) x_num_coefs.getComponent("z");
    		x_num[4] = (DataValue) x_num_coefs.getComponent("xy");
    		x_num[5] = (DataValue) x_num_coefs.getComponent("xz");
    		x_num[6] = (DataValue) x_num_coefs.getComponent("yz");
    		x_num[7] = (DataValue) x_num_coefs.getComponent("xx");
    		x_num[8] = (DataValue) x_num_coefs.getComponent("yy");
    		x_num[9] = (DataValue) x_num_coefs.getComponent("zz");
    		x_num[10] = (DataValue) x_num_coefs.getComponent("xyz");
    		x_num[11] = (DataValue) x_num_coefs.getComponent("xxx");
    		x_num[12] = (DataValue) x_num_coefs.getComponent("xyy");
    		x_num[13] = (DataValue) x_num_coefs.getComponent("xzz");
    		x_num[14] = (DataValue) x_num_coefs.getComponent("xxy");
    		x_num[15] = (DataValue) x_num_coefs.getComponent("yyy");
    		x_num[16] = (DataValue) x_num_coefs.getComponent("yzz");
    		x_num[17] = (DataValue) x_num_coefs.getComponent("xxz");
    		x_num[18] = (DataValue) x_num_coefs.getComponent("yyz");
    		x_num[19] = (DataValue) x_num_coefs.getComponent("zzz");
    		
    		// x denominator coefficients
    		DataGroup x_denom_coefs = (DataGroup) rpcParamSet.getComponent("x_denominator_coefficients");
    		x_den = new DataValue[20];
    		x_den[0] = (DataValue) x_denom_coefs.getComponent("constant");
    		x_den[1] = (DataValue) x_denom_coefs.getComponent("x");
    		x_den[2] = (DataValue) x_denom_coefs.getComponent("y");
    		x_den[3] = (DataValue) x_denom_coefs.getComponent("z");
    		x_den[4] = (DataValue) x_denom_coefs.getComponent("xy");
    		x_den[5] = (DataValue) x_denom_coefs.getComponent("xz");
    		x_den[6] = (DataValue) x_denom_coefs.getComponent("yz");
    		x_den[7] = (DataValue) x_denom_coefs.getComponent("xx");
    		x_den[8] = (DataValue) x_denom_coefs.getComponent("yy");
    		x_den[9] = (DataValue) x_denom_coefs.getComponent("zz");
    		x_den[10] = (DataValue) x_denom_coefs.getComponent("xyz");
    		x_den[11] = (DataValue) x_denom_coefs.getComponent("xxx");
    		x_den[12] = (DataValue) x_denom_coefs.getComponent("xyy");
    		x_den[13] = (DataValue) x_denom_coefs.getComponent("xzz");
    		x_den[14] = (DataValue) x_denom_coefs.getComponent("xxy");
    		x_den[15] = (DataValue) x_denom_coefs.getComponent("yyy");
    		x_den[16] = (DataValue) x_denom_coefs.getComponent("yzz");
    		x_den[17] = (DataValue) x_denom_coefs.getComponent("xxz");
    		x_den[18] = (DataValue) x_denom_coefs.getComponent("yyz");
    		x_den[19] = (DataValue) x_denom_coefs.getComponent("zzz");
    		
    		// y numerator coefficients
    		DataGroup y_num_coefs = (DataGroup) rpcParamSet.getComponent("y_numerator_coefficients");
    		y_num = new DataValue[20];
    		y_num[0] = (DataValue) y_num_coefs.getComponent("constant");
    		y_num[1] = (DataValue) y_num_coefs.getComponent("x");
    		y_num[2] = (DataValue) y_num_coefs.getComponent("y");
    		y_num[3] = (DataValue) y_num_coefs.getComponent("z");
    		y_num[4] = (DataValue) y_num_coefs.getComponent("xy");
    		y_num[5] = (DataValue) y_num_coefs.getComponent("xz");
    		y_num[6] = (DataValue) y_num_coefs.getComponent("yz");
    		y_num[7] = (DataValue) y_num_coefs.getComponent("xx");
    		y_num[8] = (DataValue) y_num_coefs.getComponent("yy");
    		y_num[9] = (DataValue) y_num_coefs.getComponent("zz");
    		y_num[10] = (DataValue) y_num_coefs.getComponent("xyz");
    		y_num[11] = (DataValue) y_num_coefs.getComponent("xxx");
    		y_num[12] = (DataValue) y_num_coefs.getComponent("xyy");
    		y_num[13] = (DataValue) y_num_coefs.getComponent("xzz");
    		y_num[14] = (DataValue) y_num_coefs.getComponent("xxy");
    		y_num[15] = (DataValue) y_num_coefs.getComponent("yyy");
    		y_num[16] = (DataValue) y_num_coefs.getComponent("yzz");
    		y_num[17] = (DataValue) y_num_coefs.getComponent("xxz");
    		y_num[18] = (DataValue) y_num_coefs.getComponent("yyz");
    		y_num[19] = (DataValue) y_num_coefs.getComponent("zzz");
    		
    		// y denominator coefficients
    		DataGroup y_denom_coefs = (DataGroup) rpcParamSet.getComponent("y_denominator_coefficients");
    		y_den = new DataValue[20];
    		y_den[0] = (DataValue) y_denom_coefs.getComponent("constant");
    		y_den[1] = (DataValue) y_denom_coefs.getComponent("x");
    		y_den[2] = (DataValue) y_denom_coefs.getComponent("y");
    		y_den[3] = (DataValue) y_denom_coefs.getComponent("z");
    		y_den[4] = (DataValue) y_denom_coefs.getComponent("xy");
    		y_den[5] = (DataValue) y_denom_coefs.getComponent("xz");
    		y_den[6] = (DataValue) y_denom_coefs.getComponent("yz");
    		y_den[7] = (DataValue) y_denom_coefs.getComponent("xx");
    		y_den[8] = (DataValue) y_denom_coefs.getComponent("yy");
    		y_den[9] = (DataValue) y_denom_coefs.getComponent("zz");
    		y_den[10] = (DataValue) y_denom_coefs.getComponent("xyz");
    		y_den[11] = (DataValue) y_denom_coefs.getComponent("xxx");
    		y_den[12] = (DataValue) y_denom_coefs.getComponent("xyy");
    		y_den[13] = (DataValue) y_denom_coefs.getComponent("xzz");
    		y_den[14] = (DataValue) y_denom_coefs.getComponent("xxy");
    		y_den[15] = (DataValue) y_denom_coefs.getComponent("yyy");
    		y_den[16] = (DataValue) y_denom_coefs.getComponent("yzz");
    		y_den[17] = (DataValue) y_denom_coefs.getComponent("xxz");
    		y_den[18] = (DataValue) y_denom_coefs.getComponent("yyz");
    		y_den[19] = (DataValue) y_denom_coefs.getComponent("zzz");
    		
    		// error parameters   - NOT CURRENTLY USED
//    		DataGroup rpcError = (DataGroup) rpcParamSet.getComponent("error_parameters"); 
//    		errorBias = (DataValue) rpcError.getComponent("error_bias");
//    		errorRandom = (DataValue) rpcError.getComponent("error_random");
    		
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
		double posX = inputX.getData().getDoubleValue();
		double posY = inputY.getData().getDoubleValue();
		double posZ = inputZ.getData().getDoubleValue();
		
		double xo = tar_xo.getData().getDoubleValue();
		double xs = tar_xs.getData().getDoubleValue();
		double yo = tar_yo.getData().getDoubleValue();
		double ys = tar_ys.getData().getDoubleValue();
		double zo = tar_zo.getData().getDoubleValue();
		double zs = tar_zs.getData().getDoubleValue();
		
		double so = img_xo.getData().getDoubleValue();
		double ss = img_xs.getData().getDoubleValue();
		double lo = img_yo.getData().getDoubleValue();
		double ls = img_ys.getData().getDoubleValue();
		
		// get all coefficient values
		double x_num_coef[] = new double[20];
		double x_den_coef[] = new double[20];
		double y_num_coef[] = new double[20];
		double y_den_coef[] = new double[20];
		
		for (int i=0; i<20; i++){
			x_num_coef[i] = x_num[i].getData().getDoubleValue();
			x_den_coef[i] = x_den[i].getData().getDoubleValue();
			y_num_coef[i] = y_num[i].getData().getDoubleValue();
			y_den_coef[i] = y_den[i].getData().getDoubleValue();
		}

		//Scaling the object space coordinates to a range of ± 1	
		double scaleX = (posX-xo)/xs;
		double scaleY = (posY-yo)/ys;
		double scaleZ = (posZ-zo)/zs;
		
		//Ratio of two cubic functions calculated for sample and line
		double u_num=0;
			u_num+=x_num_coef[0];
			u_num+=x_num_coef[1]*scaleX;
			u_num+=x_num_coef[2]*scaleY;
			u_num+=x_num_coef[3]*scaleZ;
			u_num+=x_num_coef[4]*scaleX*scaleY;
			u_num+=x_num_coef[5]*scaleX*scaleZ;
			u_num+=x_num_coef[6]*scaleY*scaleZ;
			u_num+=x_num_coef[7]*scaleX*scaleX;
			u_num+=x_num_coef[8]*scaleY*scaleY;
			u_num+=x_num_coef[9]*scaleZ*scaleZ;
			u_num+=x_num_coef[10]*scaleX*scaleY*scaleZ;
			u_num+=x_num_coef[11]*scaleX*scaleX*scaleX;
			u_num+=x_num_coef[12]*scaleX*scaleY*scaleY;
			u_num+=x_num_coef[13]*scaleX*scaleZ*scaleZ;
			u_num+=x_num_coef[14]*scaleX*scaleX*scaleY;
			u_num+=x_num_coef[15]*scaleY*scaleY*scaleY;
			u_num+=x_num_coef[16]*scaleY*scaleZ*scaleZ;
			u_num+=x_num_coef[17]*scaleX*scaleX*scaleZ;
			u_num+=x_num_coef[18]*scaleY*scaleY*scaleZ;
			u_num+=x_num_coef[19]*scaleZ*scaleZ*scaleZ;	
		double u_den=0;
			u_den+=x_den_coef[0];
			u_den+=x_den_coef[1]*scaleX;
			u_den+=x_den_coef[2]*scaleY;
			u_den+=x_den_coef[3]*scaleZ;
			u_den+=x_den_coef[4]*scaleX*scaleY;
			u_den+=x_den_coef[5]*scaleX*scaleZ;
			u_den+=x_den_coef[6]*scaleY*scaleZ;
			u_den+=x_den_coef[7]*scaleX*scaleX;
			u_den+=x_den_coef[8]*scaleY*scaleY;
			u_den+=x_den_coef[9]*scaleZ*scaleZ;
			u_den+=x_den_coef[10]*scaleX*scaleY*scaleZ;
			u_den+=x_den_coef[11]*scaleX*scaleX*scaleX;
			u_den+=x_den_coef[12]*scaleX*scaleY*scaleY;
		 	u_den+=x_den_coef[13]*scaleX*scaleZ*scaleZ;
			u_den+=x_den_coef[14]*scaleX*scaleX*scaleY;
			u_den+=x_den_coef[15]*scaleY*scaleY*scaleY;
			u_den+=x_den_coef[16]*scaleY*scaleZ*scaleZ;
			u_den+=x_den_coef[17]*scaleX*scaleX*scaleZ;
			u_den+=x_den_coef[18]*scaleY*scaleY*scaleZ;
			u_den+=x_den_coef[19]*scaleZ*scaleZ*scaleZ;
		
        double v_num=0;
			v_num+=y_num_coef[0];
			v_num+=y_num_coef[1]*scaleX;
			v_num+=y_num_coef[2]*scaleY;
			v_num+=y_num_coef[3]*scaleZ;
			v_num+=y_num_coef[4]*scaleX*scaleY;
			v_num+=y_num_coef[5]*scaleX*scaleZ;
			v_num+=y_num_coef[6]*scaleY*scaleZ;
			v_num+=y_num_coef[7]*scaleX*scaleX;
			v_num+=y_num_coef[8]*scaleY*scaleY;
			v_num+=y_num_coef[9]*scaleZ*scaleZ;
			v_num+=y_num_coef[10]*scaleX*scaleY*scaleZ;
			v_num+=y_num_coef[11]*scaleX*scaleX*scaleX;
			v_num+=y_num_coef[12]*scaleX*scaleY*scaleY;
			v_num+=y_num_coef[13]*scaleX*scaleZ*scaleZ;
			v_num+=y_num_coef[14]*scaleX*scaleX*scaleY;
			v_num+=y_num_coef[15]*scaleY*scaleY*scaleY;
			v_num+=y_num_coef[16]*scaleY*scaleZ*scaleZ;
			v_num+=y_num_coef[17]*scaleX*scaleX*scaleZ;
			v_num+=y_num_coef[18]*scaleY*scaleY*scaleZ;
			v_num+=y_num_coef[19]*scaleZ*scaleZ*scaleZ;	
		double v_den=0;
			v_den+=y_den_coef[0];
			v_den+=y_den_coef[1]*scaleX;
			v_den+=y_den_coef[2]*scaleY;
			v_den+=y_den_coef[3]*scaleZ;
			v_den+=y_den_coef[4]*scaleX*scaleY;
			v_den+=y_den_coef[5]*scaleX*scaleZ;
			v_den+=y_den_coef[6]*scaleY*scaleZ;
			v_den+=y_den_coef[7]*scaleX*scaleX;
			v_den+=y_den_coef[8]*scaleY*scaleY;
			v_den+=y_den_coef[9]*scaleZ*scaleZ;
			v_den+=y_den_coef[10]*scaleX*scaleY*scaleZ;
			v_den+=y_den_coef[11]*scaleX*scaleX*scaleX;
			v_den+=y_den_coef[12]*scaleX*scaleY*scaleY;
			v_den+=y_den_coef[13]*scaleX*scaleZ*scaleZ;
			v_den+=y_den_coef[14]*scaleX*scaleX*scaleY;
			v_den+=y_den_coef[15]*scaleY*scaleY*scaleY;
			v_den+=y_den_coef[16]*scaleY*scaleZ*scaleZ;
			v_den+=y_den_coef[17]*scaleX*scaleX*scaleZ;
			v_den+=y_den_coef[18]*scaleY*scaleY*scaleZ;
			v_den+=y_den_coef[19]*scaleZ*scaleZ*scaleZ;
		
		double u = u_num / u_den;
		double v = v_num / v_den;
			
		//Denormalize the result to sample and line
		double S = u*ss+so;
		double L = v*ls+lo;

			
		//Set Output Data
		outputX.getData().setDoubleValue(S);
		outputY.getData().setDoubleValue(L);
	}
}