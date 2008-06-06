/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the
 University of Alabama in Huntsville (UAH). <http://vast.uah.edu>
 Portions created by the Initial Developer are Copyright (C) 2007
 the Initial Developer. All Rights Reserved.

 Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorML.process;

import java.util.*;
import org.vast.cdm.common.DataBlock;
import org.vast.data.*;
import org.vast.process.*;


/**
 * <p><b>Title:</b><br/>
 * AIRS Preprocessing Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * This process preprocesses the airs data and format them into a ADAS input file.
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Gregoire Berthiau
 * @date May 20, 2008
 * @version 1.0
 */
public class AirsPreprocessing_Process extends DataProcess
{
	int ArrayDataIndex, ValueDataIndex, ArrayElementCount, i=0;
	AbstractDataComponent statusData, AirsData;    
	DataBlock AirsDataBlock;
	
    @Override
    public void init() throws ProcessException
    {
        try
        {
        	AirsData = inputData.getComponent(0);
        	statusData = outputData.getComponent("airsPreprocessingStatus");        	

        }
        catch (Exception e)
        {
            throw new ProcessException(ioError, e);
        }
    }
    
    @Override
    public void execute() throws ProcessException
    {
    	DataBlock AirsDataBlock = AirsData.getData();
    	
    	for(int i=0; i<AirsData.getData().getAtomCount(); i++){  		
    			System.out.println(i + "     " + AirsData.getData().getDoubleValue(i));
    	}
    	//System.out.println("tree");
    	
     } 	 


	public static void processAndWriteData(double[][] latitude, double[][] longitude, float[][] landfrac,
			float[][] psurf, float[][] elev, int[][] plevmax, float[][][] tprofK, float[][][] wcd,
			int hour, int minute) {
		
		  
/////////////////  inputs  originally read from files  //////////////////////

//   double[][] latitude = new double[30][45];
//   double[][] longitude = new double[30][45];
//   float[][] landfrac = new float[30][45];
//   float[][] psurf = new float[30][45];
//   float[][] elev = new float[30][45];
//   int[][] plevmax = new int[30][45];
//   float[][] tprofK = new float[100][30][45];
//   float[][][] wcd = new float[100][30][45];
//   int hour = 0;
//   int minute = 0;

/////////////////////////////////////////////////////////////////////////////

int numberOfScanLines = latitude[1].length;		
		
int[] pobs = new int[100];
int[][] n = new int[30][numberOfScanLines];
int[][] counter = new int[30][numberOfScanLines];
double[][][] tprof = new double[100][30][numberOfScanLines];
double[][][] LCDd = new double[100][30][numberOfScanLines];
double[][][] LCDt = new double[100][30][numberOfScanLines];
double[][][] mixr = new double[100][30][numberOfScanLines];
double[][][] mixr_kgkg = new double[100][30][numberOfScanLines];
double[][][] vap_pres = new double[100][30][numberOfScanLines];
double[][][] tdprof = new double[100][30][numberOfScanLines];
double[][][] hght = new double[100][30][numberOfScanLines];

//int[] pobs;
int x = 0;
while (x==12){
//readf,12,a,b
int a=0, b=0, dummy=a;
pobs[x]=b;
x=x+1;
}

double nobs_w=0;
double nobs_l=0;


    // printf,90,hour,minute

double avogandro = 6.02214199E+23;
double go = 980.665;
double mmd = 28.9644;
double mmw = 18.0151;
int l = 0;
double mean_tv = 0, thkns = 0, sum_hght = 0;
 // Now we want to write out the HDF files into one-dimensional arrays

//	         printf,20,hour,minute

for(int i=0; i<30; i++){
   for(int j=0; j<numberOfScanLines; j++){

//             printf,70,latitude(i,j),longitude(i,j),plevmax(i,j)

       for(int k=0; k<100; k++){ 
    	   
    	   l=k-1;
    	   if(tprofK[k][i][j]!=-9999){
    		   tprof[k][i][j] = tprofK[k][i][j] - 273.15;
    	   }
    	   if(tprofK[k][i][j]==-9999){
    		   tprof[k][i][j] = -999;
    	   }   

    	   if(wcd[k][i][j]!=-9999){

//		First calculate the total air layer column density (LCD)
    		   LCDt[k][i][j] = 1000.0 * (((pobs[k] - pobs[l]) * avogandro) / (float)(mmd * go));

//		Subtract the LCD of water (from the L2 profiles) to get dry air LCD
    		   LCDd[k][i][j] = LCDt[k][i][j] - wcd[k][i][j];

//		Indirectly use Avogandro's number and molar mass (mm) of the water and dry air consituents to find mixing ratio (g/kg)
    		   mixr[k][i][j] = (mmw * wcd[k][i][j]) / (0.001 * mmd * LCDd[k][i][j]);
    		   mixr_kgkg[k][i][j] = mixr[k][i][j] * 0.001;
    		   vap_pres[k][i][j] = (mixr_kgkg[k][i][j] * (pobs[k] + pobs[l]) * 0.5) / (mixr_kgkg[k][i][j] + (mmw / mmd)); 
    		   tdprof[k][i][j]=(243.5 * (Math.log(vap_pres[k][i][j])) - 440.8) / (19.48 - (Math.log(vap_pres[k][i][j])));

    	   }
    	   
    	   if(wcd[k][i][j]==-9999){
    		   tdprof[k][i][j]=-999;
    	   }
       }
   }
}

for(int i=0; i<30; i++){
 for(int j=0; j<numberOfScanLines; j++){
	 
   sum_hght = elev[i][j];
   for(int k=98; k>41; k--){
	   
      l=k-1;
      
      if((psurf[i][j] < pobs[k]) && (psurf[i][j] < pobs[l])){           	  
//		indicates that observation level is below ground
         hght[k][i][j]=-999.0;                
      }
      
      if((psurf[i][j] < pobs[k]) && (psurf[i][j] > pobs[l])){ 
    	  hght[k][i][j] = -999.0; 
          mean_tv = calculateVirtualTemperature(tprof[k][i][j],tdprof[k][i][j],pobs[l]);
          thkns = calculateThickness(pobs[l],psurf[i][j],mean_tv);
          
//              print,'surf',i,j,k,l,pobs(l),psurf(i,j),thkns

         hght[k][i][j]=sum_hght + thkns;
         sum_hght = hght[k][i][j];
      }
      
      
      if ((psurf[i][j] > pobs[k]) && (psurf[i][j] > pobs[l])){
    	  
         mean_tv = (calculateVirtualTemperature(tprof[k][i][j],tdprof[k][i][j],pobs[k])+
        		 calculateVirtualTemperature(tprof[k][i][j],tdprof[k][i][j],pobs[l]))*0.5;
         
         thkns = calculateThickness(pobs[l],pobs[k],mean_tv);
         
//              print,'non-surf',i,j,k,l,pobs(l),psurf(i,j),thkns
         
         hght[l][i][j] = sum_hght + thkns;
         sum_hght = hght[k][i][j];
      }

       if (hght[k][i][j] != -999.){
          counter[i][j] = counter[i][j] + 1;
       }

   }
 }
}

for(int i=0; i<30; i++){
 for(int j=0; j<numberOfScanLines; j++){
       if(plevmax[i][j] >= 100.0){
    	   if(landfrac[i][j] == 0.0){
    		   
    		   nobs_w = nobs_w + 1;

//         printf,20,'54',latitude(i,j),longitude(i,j),elev(i,j)
       
       		   for(int k=43; k<96; k++){
       		   	   if (pobs[k] >= plevmax[i][j]){
       		   	   		hght[k][i][j]=-999;
       		   	   		tprof[k][i][j]=-999;
       		   	   		tdprof[k][i][j]=-999;
       		       }
   //    		   	   		printf,20,pobs(k),hght(k,i,j),tprof(k,i,j),tdprof(k,i,j)
       		   }

    	   }
    	   if(landfrac[i][j] != 0.0){
    		   nobs_l = nobs_l + 1;

//              printf,25,'54',latitude(i,j),longitude(i,j),elev(i,j)

//              for k=43,loop_end do begin
    		   for(int k=43; k<96; k++){
    			   if (pobs[k] >= plevmax[i][j]){
    				   hght[k][i][j]=-999;
    				   tprof[k][i][j]=-999;
    				   tdprof[k][i][j]=-999;
    			   }
//          printf,25,pobs(k),hght(k,i,j),tprof(k,i,j),tdprof(k,i,j)
    		   }
    	   }
       }
 }


}


		}
	
    protected static double calculateThickness(double pt, double pressure, double virtualTemperature) {  	
    	
  /** this function to calculate thickness for converting layer-average liquid water content to 
   *  dew point */ 	
    	
    	double thickness; 	
    	if(pressure<pt){
    		thickness = -1;
    	} else{
            thickness=29.3*(virtualTemperature+273.15)*Math.log(pressure/pt);
    	}
    	return thickness;
    }
    
    
    protected static double calculateSaturationVaporPressure(double temperature) {  
    	
  /** this function returns the saturation vapor pressure saturationVaporPressure 
   *  (millibars) over liquid water given the temperature t (celsius). the polynomial
   *  approximation below is due to herman wobus, a mathematician who
   *  worked at the navy weather research facility, norfolk, virginia,
   *  but who is now retired. the coefficients of the polynomial were
   *  chosen to fit the values in table 94 on pp. 351-353 of the smith-
   *  sonian meteorological tables by roland list (6th edition). the
   *  approximation is valid for -50 < t < 100c. */

   //   es0 = saturation vapor pressure over liquid water at 0c
    	double es0 = 6.1078;
    	double  pol = 0.99999683 + temperature*(-0.90826951e-02 +
    			temperature*(0.78736169e-04   + temperature*(-0.61117958e-06 +
    					temperature*(0.43884187e-08   + temperature*(-0.29883885e-10 +
    							temperature*(0.21874425e-12   + temperature*(-0.17892321e-14 +
    									temperature*(0.11112018e-16   + temperature*(-0.30994571e-19)))))))));
    	double saturationVaporPressure = es0/(Math.pow(pol,8));
    	return saturationVaporPressure;
    }
    
    
    protected static double calculateVirtualTemperature(double temperature, double dewPoint, double pressure) {  	
    
  /** this function returns the virtual temperature (celsius) of
   *  a parcel of air at temperature(celsius), dew point td
   *  (celsius), and pressure (millibars). the equation appears
   *  in most standard meteorological texts */
    	
   //   cta = difference between kelvin and celsius temperatures.
   //   eps = ratio of the mean molecular weight of water (18.016 g/mole) to that of dry air (28.966 g/mole) 	
    	double cta = 273.15, eps = 0.62197;
        double tk = temperature + cta;

   //	calculate the dimensionless mixing ratio.

        double w = .001*calculateMixingRatio(pressure,dewPoint);
        double virtualTemperature = tk*(1.+w/eps)/(1.+w)-cta;
        return virtualTemperature;
    }
    
    
    protected static double calculateMixingRatio(double pressure, double temperature) {  	
    	
   /** this function approximates the mixing ratio (grams of water
    *  vapor per kilogram of dry air) given the pressure (mb) and the
    *  temperature (celsius). the formula used is given on p. 302 of the
    *  smithsonian meteorological tables by roland list (6th edition) */
    			
   //	eps = ratio of the mean molecular weight of water (18.016 g/mole) to that of dry air (28.966 g/mole)
    	double eps = 0.62197;

   //   the next two lines contain a formula by herman wobus for the
   //   correction factor wfw for the departure of the mixture of air
   //   and water vapor from the ideal gas law. the formula fits values
   //   in table 89, p. 340 of the smithsonian meteorological tables,
   //   but only for temperatures and pressures normally encountered in
   //   in the atmosphere.
    	double x = 0.02 * (temperature - 12.5 + 7500 / pressure);
    	double wfw = 1 + 4.5e-06 * pressure + 1.4e-03 * Math.pow(x,2);
    	double fwesw = wfw * calculateSaturationVaporPressure(temperature);
    	double r = eps * fwesw /(pressure - fwesw);

   //   convert r from a dimensionless ratio to grams/kilogram.
    	double mixingRatio = 1000 * r;
    	return mixingRatio;
    }
    
    protected static double calculateDewPoint(double temperature, double relativeHumidity) {  	
    	
  /** this function returns the dew point (celsius) given the temperature
   *  (celsius) and relative humidity (%). the formula is used in the
   *  processing of u.s. rawinsonde data and is referenced in parry, h.
   *  dean, 1969: "the semiautomatic computation of rawinsondes,"
   *  technical memorandum wbtm edl 10, u.s. department of commerce,
   *  environmental science services administration, weather bureau,
   *  office of systems development, equipment development laboratory,
   *  silver spring, md (october), page 9 and page ii-4, line 460.  */
    	
    	double x = 1 - 0.01 * relativeHumidity;
    	
   //	compute dew point depression
        double dewPoint = temperature - ((14.55+(0.114*temperature))*x) + ((2.5+(0.007*temperature))
        		          *(Math.pow(x,3))) + ((15.9+(0.117*temperature))*(Math.pow(x,14)));
    	return dewPoint;
    }
    
}
