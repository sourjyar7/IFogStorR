package org.fog.test.perfeval;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
public class iFogStor {
	 static {
		    System.loadLibrary("jniortools");
		  }
	public static void main(String args[]) {
		iFogStor fog=new iFogStor();
		int b=2;
		int no_data,no_datahost,no_producers,no_consumers;
		int data[]= {4,2,6,12};		//1 * no.of data items
		int size[]= {4,2,6,12};
		no_data=4;no_datahost=4;
		int storage_delay[][]= {{1,7,3,8},{12,2,9,1},{2,2,2,2},{10,2,7,6}};// no.of data items * no.of data hosts
		no_consumers=4;
		int data_consumption[][]= {{1,0,0,1},{0,0,1,0},{1,0,1,1},{0,1,0,0}};//no.of data items * no.of consumers
		int transmit_delay[][]= {{1,2,3,4},{4,5,6,7},{2,6,1,12},{1,5,7,11}};//no.of consumers * no.of data hosts
		int capacity[]= {20,15,30,25};
		int cost[][]=new int[no_data][no_datahost];
		int temp[][]=fog.multiply(data_consumption,transmit_delay);
		cost=fog.add(temp, storage_delay);
		for(int i=0;i<data.length;i++)
			data[i]=data[i]/b;
		
			for(int j=0;j<cost.length;j++) {
				for(int k=0;k<cost[0].length;k++) {
					cost[j][k]=cost[j][k]*data[j];
				}
			}
		cost=fog.transpose(cost);	
		//printing cost matrix
		for(int i=0;i<cost.length;i++) {
			for(int j=0;j<cost[0].length;j++) {
				System.out.print(cost[i][j]+" ");
				
			}
			System.out.println();
		}
		
		
		MPSolver solver = new MPSolver("AssignmentMip", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		MPVariable[][] x = new MPVariable[no_datahost][no_data];
	    for (int i = 0; i < no_datahost; ++i) {
	      for (int j = 0; j < no_data; ++j) {
	        x[i][j] = solver.makeIntVar(0, 1, "");
	      }
	    }
	    
	   //Constraints 
	    //Each data item is assigned to only one data host
	   for (int j = 0; j < no_data; ++j) {
	    	  MPConstraint constraint = solver.makeConstraint(1, 1, "");
	    	  for (int i = 0; i < no_datahost ; ++i) {
	    	    constraint.setCoefficient(x[i][j], 1);
	    	  }
	    	}
	   //Each data host should not be assigned data items more than its capacity
	   for(int i=0;i < no_datahost;i++) {
		   MPConstraint constraint = solver.makeConstraint(0,capacity[i] , "");
		   for(int j=0;j < no_data;j++) {
			   constraint.setCoefficient(x[i][j],size[j]);
		   }
	   }
	   
	   //Creating Objective function
	   MPObjective objective = solver.objective();
	   for (int i = 0; i < no_datahost; ++i) {
	     for (int j = 0; j < no_data; ++j) {
	       objective.setCoefficient(x[i][j], cost[i][j]);
	     }
	   }
	   objective.setMinimization();
	   //Invoking solver
	   MPSolver.ResultStatus resultStatus = solver.solve();
	   //Checking whether the problem has a feasible solution
	   if (resultStatus == MPSolver.ResultStatus.OPTIMAL
			    || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
			  System.out.println("Total cost: " + objective.value() + "\n");
			  for (int i = 0; i < no_datahost; ++i) {
			    for (int j = 0; j < no_data; ++j) {
			      // Test if x[i][j] is 0 or 1 (with tolerance for floating point
			      // arithmetic).
			      if (x[i][j].solutionValue() > 0.5) {
			        System.out.println(
			            "Data Host " + i + " assigned to data item " + j + ".  Cost = " + cost[i][j]);
			      }
			    }
			  }
			  
			  		  
			} else {
			  System.err.println("No solution found.");
			}
		
	}
	
	public int[][] multiply(int a[][],int b[][]) {
		int col=0;
		int c[][]=new int[a.length][b[0].length];
		for(int i=0;i<a.length;i++) {
			for(int j=0;j<b[0].length;j++) {
				for(int k=0;k<a[0].length;k++)
				   c[i][j]=c[i][j]+(a[i][k]*b[k][j]);
			}
		}
		return c;
	}
	
   public int[][] add(int a[][],int b[][]){
	   int c[][]=new int[a.length][a[0].length];
	   for(int i=0;i<a.length;i++) {
		   for(int j=0;j<a[0].length;j++) {
			   c[i][j]=a[i][j]+b[i][j];
		   }
	   }
	   return c;
   }
   
   public int[][] transpose(int a[][]){
	   int i, j; 
	   int b[][]=new int[a[0].length][a.length];
       for (i = 0; i < a[0].length; i++) 
           for (j = 0; j < a.length; j++) 
               b[i][j] = a[j][i]; 
   
   
   return b;
   }

}
