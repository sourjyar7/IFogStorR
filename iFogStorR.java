package org.fog.test.perfeval;

import java.util.*;
import org.fog.test.perfeval.MIPSolver;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;


public class iFogStorR {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<FogDevice> DCs=new ArrayList<FogDevice>();
	static List<FogDevice> RPOPs=new ArrayList<FogDevice>();
	static List<FogDevice> LPOPs=new ArrayList<FogDevice>();
	static List<FogDevice> GWs=new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static List<FogDevice> datahosts=new ArrayList<FogDevice>();
	static List<FogDevice> producers=new ArrayList<FogDevice>();
	static List<FogDevice> consumers=new ArrayList<FogDevice>();
	static int numOfDCs=2;
	static int numOfRPOPs=4;
	static int numOfLPOPs=8;
	static int numOfGWs=56*3;
	static int numOfSensorsPerGW=5;
	static int cp=5;
	static int SENSOR_TRANSMISSION_TIME=5;
	static Application application;
	
	
	public static void main(String[] args) {

		Log.printLine("Starting Simulation...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "iFogStorR"; // identifier of the application
			
			FogBroker broker = new FogBroker("broker");
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			System.out.println("App created successfully !");
			
			createFogDevices(broker.getId(), appId);
			
			System.out.println("Devices created successfully !");
			
			for(int i=0;i<fogDevices.size();i++) {
				System.out.print(fogDevices.get(i).getName()+"  ");
			}
			
	        System.out.println();
			
			/*
			 * Adding modules (vertices) to the application model (directed graph)
			 */
			
		    for(int i=0;i<fogDevices.size();i++) {
		    	application.addAppModule("datahost"+fogDevices.get(i).getId(), 10); // adding module Client to the application model	
		    	
		    }
		    
		    for(int i=0;i<GWs.size();i++) {
			    application.addAppModule("producer"+GWs.get(i).getId(), 10); // adding module Concentration Calculator to the application model
			   
		    }
		    
		    for(int i=0;i<fogDevices.size();i=i+5) {
		        application.addAppModule("consumer"+fogDevices.get(i).getId(), 10); // adding module Connector to the application model
		        
		    }
			
		
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
			//Adding data hosts to all DCs,*2 RPOPs,*3 LPOPs
			for(int i=0;i<DCs.size();i++) {
				moduleMapping.addModuleToDevice("datahost"+DCs.get(i).getId(), DCs.get(i).getName());
				datahosts.add(DCs.get(i));
			}
			
			for(int i=0;i<RPOPs.size();i++) {
				moduleMapping.addModuleToDevice("datahost"+RPOPs.get(i).getId(), RPOPs.get(i).getName());
				datahosts.add(RPOPs.get(i));
			}
			
			for(int i=0;i<LPOPs.size();i++) {
				moduleMapping.addModuleToDevice("datahost"+LPOPs.get(i).getId(), LPOPs.get(i).getName());
				datahosts.add(LPOPs.get(i));
			}
			
			
			System.out.println("Datahosts Added");
			//Adding producers to all gateways
			for(int i=0;i<GWs.size();i++) {
				moduleMapping.addModuleToDevice("producer"+GWs.get(i).getId(), GWs.get(i).getName());
				producers.add(GWs.get(i));
				
			}
			//Adding consumers to every 5th fog device
			for(int i=0;i<fogDevices.size();i=i+5) {
				moduleMapping.addModuleToDevice("consumer"+fogDevices.get(i).getId(), fogDevices.get(i).getName());
				consumers.add(fogDevices.get(i));
			}
			System.out.println("Producers and Consumers added ");
			
			//Implementing the changes from iFogStor
			double rf=producers.size()/datahosts.size();
			rf=Math.ceil(rf);
			int replication_Factor=(int)rf;
					
			//Building the storage cost matrix(producers * data hosts)
			int storageTime[][]=new int[producers.size()][datahosts.size()*replication_Factor];
			for(int i=0;i<producers.size();i++) {
				int k=0;
				for(int j=0;j<datahosts.size();j++) {
				    int count=replication_Factor;
					while(count-- != 0)
					   storageTime[i][k++]=getLatency(producers.get(i),datahosts.get(j));
				}
			}
			System.out.println(" Storage cost built ");
			
			//Building the retrieval cost matrix(consumers * data hosts)
			int retrievalTime[][]=new int[consumers.size()][datahosts.size()*replication_Factor];
			for(int i=0;i<consumers.size();i++) {
				int k=0;
				for(int j=0;j<datahosts.size();j++) {
					int count=replication_Factor;
					while(count-- != 0)
					   retrievalTime[i][k++]=getLatency(consumers.get(i),datahosts.get(j));
				}
			}
			System.out.println(" Retrieval cost built ");
			
			//Building the consumer to data item consumed matrix(producers * consumers)
			int consumerToData[][]=new int[producers.size()][consumers.size()];
			Random rand=new Random();
			int cp1;
			for(int i=0;i<producers.size();i++) {
				cp1=cp;
				while(cp1 !=0) {
				  int random=rand.nextInt(consumers.size());
                  if(consumerToData[i][random] == 1) 
                	  continue;
                  else {
                	  consumerToData[i][random]=1;
                	  cp1--;
                  }
                	  
                  
				}
			  }
			System.out.println(" Consumer to data built ");
			
			//Making Apploops
			
			List<AppLoop> list = new ArrayList<>();
            List<ArrayList> temp_loops=new ArrayList<>();
            List<ArrayList> temp_loops1=new ArrayList<>();
			
			//Storage of each data host
			long max_capacity[]=new long[datahosts.size()];
			for(int i=0;i<datahosts.size();i++) {
				max_capacity[i]=datahosts.get(i).getHost().getStorage();
			}
			//Size of each data item
			int size[]=new int[producers.size()];
			int max=20,min=10;
			for(int i=0;i<producers.size();i++) {
				size[i]=min * 960;
				min+=5;
			}
			System.out.println("About to solve ");
			//Creating instance of MIPSolver
			MIPSolver solver=new MIPSolver(storageTime,retrievalTime,consumerToData,max_capacity,size);
			int h_solution[][]=solver.assignmentSolver(solver);
			System.out.println("Solved ");
			int solution[][]=new int[datahosts.size()*replication_Factor][producers.size()];
			for(int i=0;i<h_solution.length;i++) {
				solution[h_solution[i][1]][h_solution[i][0]]=1;
			}
			
			
			//Adding edges from sensors to producers
			for(int i=0;i<producers.size();i++) {
				
				application.addTupleMapping("producer"+producers.get(i).getId(), "data"+(i+1), "data"+(i+1), new FractionalSelectivity(0.1));
				for(int j=1;j<=numOfSensorsPerGW;j++) {
				    	application.addAppEdge("data"+(i+1), "producer"+producers.get(i).getId(), 20, 96, "data"+(i+1), Tuple.UP, AppEdge.SENSOR);
				}
				  
				
				        ArrayList<String> temp=new ArrayList<>();
				        temp.add("data"+(i+1));
				        temp.add("producer"+producers.get(i).getId());
				        temp_loops.add(temp); 
		      
			}
			
			//adding edges from producers to chosen data hosts
			for(int j=0;j<producers.size();j++){
				int k=0;
				for(int i=0;i<datahosts.size();i++){
					int count=replication_Factor;
					while(count-- != 0) {
				 	
					if(solution[k++][j]==1) {
					   
						 application.addTupleMapping("datahost"+datahosts.get(i).getId(), "data"+(j+1), "data"+(j+1), new FractionalSelectivity(0.1));	
					   if(producers.get(j).getLevel() < datahosts.get(i).getLevel())	
					       application.addAppEdge("producer"+producers.get(j).getId(), "datahost"+datahosts.get(i).getId(), 20, 960, "data"+(j+1), Tuple.DOWN, AppEdge.MODULE);
					   else
						   application.addAppEdge("producer"+producers.get(j).getId(), "datahost"+datahosts.get(i).getId(), 20, 960, "data"+(j+1), Tuple.UP, AppEdge.MODULE);
				       
					  
					   
					   for(int l=0;l<temp_loops.size();l++) {
				    	   if(temp_loops.get(l).contains("producer"+producers.get(j).getId())) {
				    		   temp_loops.get(l).add("datahost"+datahosts.get(i).getId());   		   
				    	   }
				       }
					}
				}
			  }	
			}
			System.out.println("Added edges from producers to data hosts");
			
			//adding edges from data hosts to required consumers
		
				for(int j=0;j<producers.size();j++) {
				   for(int k=0;k<consumers.size();k++) {
					   if(consumerToData[j][k]==1) {
						   int l=0;
						   for(int i=0;i<datahosts.size();i++) {
							   int count=replication_Factor;
							   while(count-- != 0) {
							   if(solution[l++][j]==1) {
							      
						    	   application.addTupleMapping("consumer"+consumers.get(k).getId(), "data"+(j+1), "data"+(j+1), new FractionalSelectivity(0.0)); 
						    	   
						    	  if(datahosts.get(i).getLevel() < consumers.get(k).getLevel())
						    	      application.addAppEdge("datahost"+datahosts.get(i).getId(), "consumer"+consumers.get(k).getId(), 20, 960, "data"+(j+1), Tuple.DOWN, AppEdge.MODULE);
							      else
							    	  application.addAppEdge("datahost"+datahosts.get(i).getId(), "consumer"+consumers.get(k).getId(), 20, 960, "data"+(j+1), Tuple.UP, AppEdge.MODULE);
							      
							      for(int m=0;m<temp_loops.size();m++) {
					                 if(temp_loops.get(m).contains("datahost"+datahosts.get(i).getId())) {
					                	 ArrayList<String> copy=new ArrayList<>();
					                	 ArrayList<String> temp=temp_loops.get(m);
					                	 for(int n=0;n<temp.size();n++) {
					                	     copy.add(temp.get(n));	 
					                	 }
					                	 copy.add("consumer"+consumers.get(k).getId());
					                	 temp_loops1.add(copy);
					                	 
					                	 
					                 }
					             }
						        }
							   }
						   
						     }
					      }
				      }
					
				}
				System.out.println("Added edges from datahosts to consumers");	
			   for(int i=0;i<temp_loops1.size();i++) {
			       AppLoop loop=new AppLoop(temp_loops1.get(i));
			       list.add(loop);
			    }
				System.out.println(list.size()); 
				application.setLoops(list);
	        
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
			
			controller.submitApplication(application, 0, (new ModulePlacementMapping(fogDevices, application, moduleMapping)));
		    
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			
			
			System.out.println("Invoking cloudsim");
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("Simulation finished!");
     
		
		}catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	//Method returns latency between two given fog devices
	private static int getLatency(FogDevice f1,FogDevice f2) {
		int max_level,min_level;int latency=0;
		FogDevice temp;
		
		if(f1.getLevel()>f2.getLevel()) {
			max_level=f1.getLevel();
			min_level=f2.getLevel();
			temp=getFogDeviceById(f1.getId());
			
		}
		else {
			max_level=f2.getLevel();
			min_level=f1.getLevel();
			temp=getFogDeviceById(f2.getId());
			
		}
		
		if(min_level==max_level) {
			latency=latency+(int)temp.getUplinkLatency();
		}
		
		for(int i=max_level;i>min_level;i--) {
			latency= latency + (int)temp.getUplinkLatency();
			temp=getFogDeviceById(temp.getParentId());
		}
		
		return latency;
	}
	//Method returns a fog device of the given id
	private static FogDevice getFogDeviceById(int id){
		for(FogDevice fogDevice : fogDevices){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}
	
	
private static Application createApplication(String appId, int userId){
		
	    application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)
		
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		/*
	    for(int i=0;i<fogDevices.size();i++) {
	    	application.addAppModule("datahost"+fogDevices.get(i).getId(), 10); // adding module Client to the application model	
	    	application.addTupleMapping("datahost"+fogDevices.get(i).getId(), "data", "data", new FractionalSelectivity(0.1));
	    }
	    
	    for(int i=0;i<GWs.size();i++) {
		    application.addAppModule("producer"+GWs.get(i).getId(), 10); // adding module Concentration Calculator to the application model
		    application.addTupleMapping("producer"+GWs.get(i).getId(), "data", "data", new FractionalSelectivity(0.1));
	    }
	    
	    for(int i=0;i<fogDevices.size();i=i+5) {
	        application.addAppModule("consumer"+fogDevices.get(i).getId(), 10); // adding module Connector to the application model
	        application.addTupleMapping("consumer"+fogDevices.get(i).getId(), "data", "data", new FractionalSelectivity(0.0));
	    }
		
		*/
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
	   /* for(int i=0;i<producers.size();i++) {
	    	application.addAppEdge("data", "producer"+producers.get(i).getId(), 20, 96, "data", Tuple.UP, AppEdge.SENSOR);
	    		
	    }*/
	/*	
		application.addAppEdge("producer", "datahost", 20, 96, "data", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("datahost", "consumer", 20, 96, "data", Tuple.UP, AppEdge.MODULE); 
				
		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		/*
	     
		 
		 		 
	
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("sensor");add("producer");add("datahost");add("consumer");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		application.setLoops(loops);
		*/
		
		return application;
	}

private static void createFogDevices(int userId, String appId) {
	//Creating Data center
	for(int i=1;i<=numOfDCs;i++) {
	    FogDevice dc = createFogDevice("DC-"+i, 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25,(long)Math.pow(10, 16)); 
	    dc.setParentId(-1);
	    dc.setUplinkLatency(0);
	    DCs.add(dc);
	    fogDevices.add(dc);
	}
	//Creating RPOPs
	int temp=numOfRPOPs/numOfDCs;
	int temp1 = temp * DCs.size();
	int i=0;
	for(int j=1;j<=temp1;j++) {
		 FogDevice rpop = createFogDevice("rpop-"+j, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333,(long)Math.pow(10, 14)); 
	     rpop.setParentId(DCs.get(i).getId());
	     rpop.setUplinkLatency(100);
	     RPOPs.add(rpop);
	     fogDevices.add(rpop);	        
	     if(j%temp==0)
	       i++;
	}	
	
	//Creating LPOPs
    temp=numOfLPOPs/numOfRPOPs;
    temp1 = temp * RPOPs.size();
    i=0;
	for(int j=1;j<=temp1;j++) {
		    FogDevice lpop = createFogDevice("lpop-"+j, 2800, 4000, 10000, 10000, 2, 0.0, 107.339, 83.4333,(long)Math.pow(10, 13)); 
	     	lpop.setParentId(RPOPs.get(i).getId()); 
	    	lpop.setUplinkLatency(5); 
	        LPOPs.add(lpop);
	        fogDevices.add(lpop);	    
	        if(j%temp==0)
	          i++;
		}	
	
	
   //Creating Gateways
    temp=numOfGWs/numOfLPOPs;
    temp1=temp * LPOPs.size();
    i=0;
	for(int j=1;j<=temp1;j++) {
		    FogDevice gw = createFogDevice("gw"+j, 2800, 4000, 10000, 10000, 3, 0.0, 107.339, 83.4333,(long)Math.pow(10, 11)); 
	     	gw.setParentId(LPOPs.get(i).getId()); 
	    	gw.setUplinkLatency(50); 
	        GWs.add(gw);
	        fogDevices.add(gw);	        
	        if(j%temp==0)
	          i++;
		}	
	
    
    //Creating Sensors
        int nos=numOfSensorsPerGW*GWs.size();
        i=0;
    	for(int j=1;j<=nos;j++) {
    		Sensor s = new Sensor("s-"+j, "data"+(i+1), userId, appId, new DeterministicDistribution(SENSOR_TRANSMISSION_TIME));
    		sensors.add(s);
    		s.setGatewayDeviceId(GWs.get(i).getId());
    		s.setLatency(10.0);  
    		if(j%numOfSensorsPerGW==0)
    		  i++;
    	}
    
	
	
}


private static FogDevice createFogDevice(String nodeName, long mips,
		int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower,long storage) {
	
	List<Pe> peList = new ArrayList<Pe>();

	// 3. Create PEs and add these into a list.
	peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

	int hostId = FogUtils.generateEntityId();
	//long storage = 1000000; // host storage
	int bw = 10000;

	PowerHost host = new PowerHost(
			hostId,
			new RamProvisionerSimple(ram),
			new BwProvisionerOverbooking(bw),
			storage,
			peList,
			new StreamOperatorScheduler(peList),
			new FogLinearPowerModel(busyPower, idlePower)
		);

	List<Host> hostList = new ArrayList<Host>();
	hostList.add(host);

	String arch = "x86"; // system architecture
	String os = "Linux"; // operating system
	String vmm = "Xen";
	double time_zone = 10.0; // time zone this resource located
	double cost = 3.0; // the cost of using processing in this resource
	double costPerMem = 0.05; // the cost of using memory in this resource
	double costPerStorage = 0.001; // the cost of using storage in this
									// resource
	double costPerBw = 0.0; // the cost of using bw in this resource
	LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
												// devices by now

	FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
			arch, os, vmm, host, time_zone, cost, costPerMem,
			costPerStorage, costPerBw);

	FogDevice fogdevice = null;
	try {
		fogdevice = new FogDevice(nodeName, characteristics, 
				new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	fogdevice.setLevel(level);
	return fogdevice;
}

	
	
	
}
