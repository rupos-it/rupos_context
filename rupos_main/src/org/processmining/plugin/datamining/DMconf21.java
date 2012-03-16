package org.processmining.plugin.datamining;

import it.unipi.rupos.processmining.PetriNetEngine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.ProMFactory;
import org.processmining.contexts.cli.ProMManager;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;

public class DMconf21 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	// Caso 21: 2 attributi --> URG LIV un solo evento
	// condizione logica : if( URG=true OR LIV=CORE || GUI ) fai il check senno' NO.
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		

		String pathLogFile="../examples/";
    	
		String logFile = pathLogFile+"logBugFix21.mxml";
	    String netFile = pathLogFile+"BugFix.pnml";
		
	    	
		ProMManager manager = new ProMFactory().createManager(); 	// istanzia il fw PROM
		PetriNetEngine engine = manager.createPetriNetEngine(netFile); // engine ha metodi per processare la PN


		XLog log = manager.openLog(logFile); // il manager torna XLOG
		System.out.println("Log size: " + log.size());

		// settings per il log replay
		ReplayFitnessSetting settings = engine.suggestSettings(log);
		System.out.println("Settings: " + settings);
		settings.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
		settings.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
		settings.setAction(ReplayAction.REMOVE_HEAD, false);
		settings.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
		settings.setAction(ReplayAction.INSERT_DISABLED_MATCH, true);
		settings.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
		
		
		TotalConformanceResult fitness = engine.getFitness(log, settings);  //--> torna il risultato di conf della PN rispetto a log
		System.out.println("Fitness: " + fitness);
		
		//Marking missing = fitness.getList().get(0).getMissingMarking();
		//missing.isEmpty();
		
		//XAttributeLiteral attr = (XAttributeLiteral) log.get(0).get(0).getAttributes().get("urg");
		//attr.getValue();
		
		List<String> istance_urg = new Vector<String>();
		List<String> istance_conf = new Vector<String>();
		List<String> istance_liv = new Vector<String>();
		
		List<ConformanceResult> ris = fitness.getList();
		
		for( int i = 0; i<ris.size(); i++ ){
			
			// controllo la conformità della traccia i-esima
			Marking missing = fitness.getList().get(i).getMissingMarking();
			if(missing.isEmpty() ) {istance_conf.add("TRUE");}
			else {istance_conf.add("FALSE");}
							
			//controllo l'urgenza della traccia i-esima
			XAttributeLiteral attr_urg = (XAttributeLiteral) log.get(i).get(0).getAttributes().get("urg");
			XAttributeLiteral attr_liv = (XAttributeLiteral) log.get(i).get(0).getAttributes().get("liv");
			istance_urg.add(attr_urg.getValue().toUpperCase());	
			istance_liv.add(attr_liv.getValue());		

		}
		
		
		// faccio il file arff
		  FileWriter fstream = new FileWriter("BugFix21.arff");
		  BufferedWriter file = new BufferedWriter(fstream);
		  file.write("@relation BugFix\n");
		  file.write("\n");
		  
		  file.write("@attribute urg {TRUE, FALSE} \n");
		  file.write("@attribute conf {TRUE, FALSE}\n");
		  file.write("@attribute livello {GUI, CORE, DB, IO}\n");
		  
		  file.write("\n");		  
		  file.write("@data");
		  file.write("\n");
		  
		  for( int i=0; i<istance_urg.size(); i++ ){
			  file.write(istance_urg.get(i) + ",");
			  file.write(istance_conf.get(i) + ",");
			  file.write(istance_liv.get(i) + "\n");
		  }
		
		  file.flush();
		  
		  System.out.println("FINITO!");
	}
	
}
	




