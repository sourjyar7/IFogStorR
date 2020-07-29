package org.fog.test.perfeval;

import java.util.*;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import org.fog.test.perfeval.HungarianAlgorithm;
public class MIPSolver {
	static {
	    System.loadLibrary("jniortools");
	  }
	int storageDelay[][],transmitDelay[][],data_consumption[][],cost[][];
	long capacity[];int size[];
	int data[];int no_datahost,no_dataitems;
	public MIPSolver(int storageDelay[][],int transmitDelay[][],int data_consumption[][],long capacity[],int size[]) {
		this.storageDelay=storageDelay;
		this.data_consumption=data_consumption;
		this.transmitDelay=transmitDelay;
		this.cost=new int[this.storageDelay.length][this.storageDelay[0].length];
		this.data=new int[this.storageDelay.length];
		this.capacity=capacity;
		this.size=size;
		this.no_datahost=transmitDelay[0].length;
		this.no_dataitems=size.length;
	}
	
	public int[][] gapSolver(MIPSolver m){
		int temp[][]=m.multiply(data_consumption,transmitDelay);
		cost=m.add(temp, storageDelay);
		int min=5,max=10;
		
		
		
			for(int j=0;j<cost.length;j++) {
				for(int k=0;k<cost[0].length;k++) {
					cost[j][k]=cost[j][k]*size[j];
				}
			}
		cost=m.transpose(cost);	
		//printing cost matrix
		/*for(int i=0;i<cost.length;i++) {
			for(int j=0;j<cost[0].length;j++) {
				System.out.print(cost[i][j]+" ");
				
			}
			System.out.println();
		}*/
		long initialTime=System.currentTimeMillis();
		MPSolver solver = new MPSolver("AssignmentMip", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		MPVariable[][] x = new MPVariable[no_datahost][no_dataitems];
	    for (int i = 0; i < no_datahost; ++i) {
	      for (int j = 0; j < no_dataitems; ++j) {
	        x[i][j] = solver.makeIntVar(0, 1, "");
	      }
	    }
	    
	   //Constraints 
	    //Each data item is assigned to only one data host
	   for (int j = 0; j < no_dataitems; ++j) {
	    	  MPConstraint constraint = solver.makeConstraint(1, 1, "");
	    	  for (int i = 0; i < no_datahost ; ++i) {
	    	    constraint.setCoefficient(x[i][j], 1);
	    	  }
	    	}
	   //Each data host should not be assigned data items more than its capacity
	   for(int i=0;i < no_datahost;i++) {
		   MPConstraint constraint = solver.makeConstraint(0,capacity[i] , "");
		   for(int j=0;j < no_dataitems;j++) {
			   constraint.setCoefficient(x[i][j],size[j]);
		   }
	   }
	   
	   //Creating Objective function
	   MPObjective objective = solver.objective();
	   for (int i = 0; i < no_datahost; ++i) {
	     for (int j = 0; j < no_dataitems; ++j) {
	       objective.setCoefficient(x[i][j], cost[i][j]);
	     }
	   }
	   objective.setMinimization();
	   //Invoking solver
	   
	   MPSolver.ResultStatus resultStatus = solver.solve();
	  
	   
	   
	   int solution[][]=new int[no_datahost][no_dataitems];
	   //Checking whether the problem has a feasible solution
	   if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
			  System.out.println("Total cost: " + objective.value() + "\n");
			  for(int i=0;i<no_datahost;i++) {
				  for(int j=0;j<no_dataitems;j++) {
					  solution[i][j]=(int)x[i][j].solutionValue();
				  }
			  }
			  long finalTime=System.currentTimeMillis();
			   System.out.println("Time taken to solve: "+(finalTime-initialTime));
			  return solution;  
			  		  
			} else {
			  System.err.println("No solution found.");
			  return solution;
			}
	
	}
	
	
	public int[][] assignmentSolver(MIPSolver m){
		int temp[][]=m.multiply(data_consumption,transmitDelay);
		cost=m.add(temp, storageDelay);
		
		
		
		
			for(int j=0;j<cost.length;j++) {
				for(int k=0;k<cost[0].length;k++) {
					cost[j][k]=cost[j][k]*size[j];
				}
			}
		cost=m.transpose(cost);	
		
		 HungarianAlgorithm ha = new HungarianAlgorithm(cost);
	      
		 int[][] assignment = ha.findOptimalAssignment();
		 
	      return assignment;
		//printing cost matrix
		/*for(int i=0;i<cost.length;i++) {
			for(int j=0;j<cost[0].length;j++) {
				System.out.print(cost[i][j]+" ");
				
			}
			System.out.println();
		}*/
	
/*		MPSolver solver = new MPSolver("AssignmentMip", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		MPVariable[][] x = new MPVariable[no_datahost][no_dataitems];
	    for (int i = 0; i < no_datahost; ++i) {
	      for (int j = 0; j < no_dataitems; ++j) {
	        x[i][j] = solver.makeIntVar(0, 1, "");
	      }
	    }
	    
	   //Constraints 
	    //Each data item is assigned to only one data host
	   for (int j = 0; j < no_dataitems; ++j) {
	    	  MPConstraint constraint = solver.makeConstraint(1, 1, "");
	    	  for (int i = 0; i < no_datahost ; ++i) {
	    	    constraint.setCoefficient(x[i][j], 1);
	    	  }
	    	}
	   //Each data host is assigned at most 1 data item
	   for(int i=0;i < no_datahost;i++) {
		   MPConstraint constraint = solver.makeConstraint(0, 1 , "");
		   for(int j=0;j < no_dataitems;j++) {
			   constraint.setCoefficient(x[i][j],1);
		   }
	   }
	   
	   //Creating Objective function
	   MPObjective objective = solver.objective();
	   for (int i = 0; i < no_datahost; ++i) {
	     for (int j = 0; j < no_dataitems; ++j) {
	       objective.setCoefficient(x[i][j], cost[i][j]);
	     }
	   }
	   objective.setMinimization();
	   //Invoking solver
	   long initialTime=System.currentTimeMillis();
	   MPSolver.ResultStatus resultStatus = solver.solve();
	   long finalTime=System.currentTimeMillis();
	   System.out.println("Time taken to solve: "+(finalTime-initialTime));
	   System.out.println("Iterations taken to solve: "+solver.iterations());
	
	   int solution[][]=new int[no_datahost][no_dataitems];
	   //Checking whether the problem has a feasible solution
	   if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
			  System.out.println("Total cost: " + objective.value() + "\n");
			  for(int i=0;i<no_datahost;i++) {
				  for(int j=0;j<no_dataitems;j++) {
					  solution[i][j]=(int)x[i][j].solutionValue();
				  }
			  }
			  return solution;  
			  		  
			} else {
			  System.err.println("No solution found.");
			  return solution;
			}*/
	
	}
	
	
	public int[][] multiply(int a[][],int b[][]) {
		
		int c[][]=new int[a.length][b[0].length];
		
		for(int i=0;i<a.length;i++) {
			for(int j=0;j<b[0].length;j++) {
				c[i][j]=0;
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
