
//change to your package location
package org.sensorML.process;

import org.vast.data.*;
import org.vast.process.*;


/**
* <p><b>Title:  </b><br/></p>
*
* <p><b>Description:</b><br/>
* Determines whether input test value is greater than the threshold value. Return true or false.
* </p>
* @author 
* @date  
* @version 
*/



public class GreaterThan_Process extends DataProcess {


   // declare input components
   DataValue inputValue;

   // declare output components
   DataValue trueOrFalse;

   DataValue threshold;


   public GreaterThan_Process() {
   }


   /**
   * Initializes the process
   * Get handles to input/output components
   */
   public void init() throws ProcessException {

      try {

         // initialize input components
         inputValue= (DataValue) inputData.getComponent("inputValue");

         // initialize output components
         trueOrFalse = (DataValue) outputData.getComponent("trueOrFalse");

	   threshold = (DataValue) paramData.getComponent("threshold");


      }
      catch (ClassCastException e) {
         throw new ProcessException("Invalid I/O data", e);
      }

   }

   /**
   * Executes the process
   * Get current values for all components and then executes
   */
   public void execute() throws ProcessException {


         // get values for input components
         // note: you can rename these variable names to match your code
         double inputValueData = inputValue.getData().getDoubleValue();
         double thresholdData = threshold.getData().getDoubleValue();

         // set values for output components
         trueOrFalse.getData().setBooleanValue(inputValueData > thresholdData);
   }
}
