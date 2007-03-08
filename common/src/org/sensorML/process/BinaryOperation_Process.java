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
    Gregoire Berthiau <berthiau@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorML.process;

import org.vast.cdm.common.DataBlock;
import org.vast.data.*;
import org.vast.process.*;
import org.vast.math.*;


/**
 * <p><b>Title:</b><br/>
 * Binary Operation Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Implementation of a binary operation with respect to a parameter (addition, 
 * soustraction, multiplication, division)
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin & Gregoire Berthiau
 * @date Mar 7, 2007
 * @version 1.0
 */
public class BinaryOperation_Process extends DataProcess
{
	AbstractDataComponent operatorData;
    private DataValue operand1, operand2, result;
    int operator;
    
    public BinaryOperation_Process()
    {
    	
    }

    
    public void init() throws ProcessException
    {
    	try
        {
            //I/O/P mappings
            operand1 = (DataValue) inputData.getComponent("operand1");
            operand2 = (DataValue) inputData.getComponent("operand2");

            result = (DataValue) outputData.getComponent("result");
            
            // read operator method
            DataValue operatorData = (DataValue)paramData.getComponent("operator");
 
            if (operatorData.getData().getStringValue().equalsIgnoreCase("addition"))
              	operator = 0;
            else if (operatorData.getData().getStringValue().equalsIgnoreCase("soustraction"))
               	operator = 1;
            else if (operatorData.getData().getStringValue().equalsIgnoreCase("multiplication"))
               	operator = 2;
            else if (operatorData.getData().getStringValue().equalsIgnoreCase("division"))
              	operator = 3;
            else if (operatorData.getData().getStringValue().equalsIgnoreCase("power"))
               	operator = 4;
            
        }
        catch (RuntimeException e)
        {
            throw new ProcessException(ioError);
        }
    }
    

    public void execute() throws ProcessException
    {
        double N1 = 0.0;
        double N2 = 0.0;
        double Nr = 0.0;
        
        if (operand1 != null)
            N1 = operand1.getData().getDoubleValue();

        if (operand2 != null)
            N2 = operand2.getData().getDoubleValue();
        
        switch (operator)
        {
            case '0':
                Nr=N1+N2;
                break;
                
            case '1':
                Nr=N1-N2;
                break;
                
            case '2':
                Nr=N1*N2;
                break;
                
            case '3':
                Nr=N1/N2;
                break;
                
            case '4':
                Nr=Math.pow(N1,N2);
                break;
        }

        result.getData().setDoubleValue(Nr);
 
    } 
}