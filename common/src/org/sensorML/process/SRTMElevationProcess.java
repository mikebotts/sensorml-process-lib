
//change to your package location
package org.sensorML.process;

import org.vast.data.*;
import org.vast.process.*;


/**
* <p><b>Title:  </b><br/></p>
*
* <p><b>Description:</b><br/>
* </p>
* @author 
* @date  
* @version 
*/



public class SRTMElevationProcess extends DataProcess {


   // declare input components
   DataValue CoverageIn_lon;
   DataValue CoverageIn_lat;

   // declare output components
   DataValue CoverageOut_lon;
   DataValue CoverageOut_lat;
   DataValue CoverageOut_alt;

   // declare parameters components

   // declare any other class variables needed


   public SRTMElevationProcess() {
   }


   /**
   * Initializes the process
   * Get handles to input/output components
   */
   public void init() throws ProcessException {

      try {

         // initialize input components
         DataGroup CoverageIn = (DataGroup) inputData.getComponent("CoverageIn");
         CoverageIn_lon = (DataValue) CoverageIn.getComponent("lon");
         CoverageIn_lat = (DataValue) CoverageIn.getComponent("lat");

         // initialize output components
         DataGroup CoverageOut = (DataGroup) outputData.getComponent("CoverageOut");
         CoverageOut_lon = (DataValue) CoverageOut.getComponent("lon");
         CoverageOut_lat = (DataValue) CoverageOut.getComponent("lat");
         CoverageOut_alt = (DataValue) CoverageOut.getComponent("alt");

         // initialize parameter components

      }
      catch (ClassCastException e) {
         throw new ProcessException("Invalid I/O data", e);
      }

      // initialize any class variables needed

   }

   /**
   * Executes the process
   * Get current values for all components and then executes
   */
   public void execute() throws ProcessException {


         // get values for input components
         // note: you can rename these variable names to match your code
         double CoverageIn_lon_value = CoverageIn_lon.getData().getDoubleValue();
         double CoverageIn_lat_value = CoverageIn_lat.getData().getDoubleValue();

         // get values for parameter components
         // note: you can rename these variable names to match your code

         // re-initialize values for output components to zero or default
         // note: you can rename these variable names to match your code
         double CoverageOut_lon_value = CoverageOut_lon.getData().getDoubleValue();
         CoverageOut_lon_value = 0;
         double CoverageOut_lat_value = CoverageOut_lat.getData().getDoubleValue();
         CoverageOut_lat_value = 0;
         double CoverageOut_alt_value = CoverageOut_alt.getData().getDoubleValue();
         CoverageOut_alt_value = 0;


         /****************************************
          *    PUT YOUR EXECUTION CODE HERE      *
          ****************************************/



         // set values for output components
         // note: you can rename these variable names to match your code
         CoverageOut_lon.getData().setDoubleValue(CoverageOut_lon_value);
         CoverageOut_lat.getData().setDoubleValue(CoverageOut_lat_value);
         CoverageOut_alt.getData().setDoubleValue(CoverageOut_alt_value);
   }
}
