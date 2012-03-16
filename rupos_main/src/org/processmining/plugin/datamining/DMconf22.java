package org.processmining.plugin.datamining;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Vector;

import it.unipi.rupos.processmining.PetriNetEngine;

import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.ProMFactory;
import org.processmining.contexts.cli.ProMManager;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;

public class DMconf22 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	// caso 22 : 2 attributi Litteral di cui conosco il nome ( conf urg ed liv ) ma non i valori
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
		
		
		List<String> istance_urg = new Vector<String>();
		List<String> valori_urg = new Vector<String>();
		
		List<String> istance_conf = new Vector<String>();

		List<String> istance_liv = new Vector<String>();
		List<String> valori_liv = new Vector<String>();

		
		List<ConformanceResult> ris = fitness.getList();
		
		for( int i = 0; i<ris.size(); i++ ){
			
			// controllo la conformità della traccia i-esima
			Marking missing = fitness.getList().get(i).getMissingMarking();
			if(missing.isEmpty() ) {istance_conf.add("TRUE");}
			else {istance_conf.add("FALSE");}
							
			//controllo l'urgenza della traccia i-esima
			XAttributeLiteral attr_urg = (XAttributeLiteral) log.get(i).get(0).getAttributes().get("urg");
			//controllo se il valore è uno di quelli che conosco ed eventulamente lo aggiungo
			if( ! valori_urg.contains(attr_urg.getValue()) ){
				valori_urg.add(attr_urg.getValue());
			}
			
			XAttributeLiteral attr_liv = (XAttributeLiteral) log.get(i).get(0).getAttributes().get("liv");
			if( ! valori_liv.contains(attr_liv.getValue()) ){
				valori_liv.add(attr_liv.getValue());
			}
			
			// aggiungo le istanze
			istance_urg.add(attr_urg.getValue().toUpperCase());	
			istance_liv.add(attr_liv.getValue());		

		}
		
		
		// faccio il file arff
		  int j;
		  FileWriter fstream = new FileWriter("BugFix22.arff");
		  BufferedWriter file = new BufferedWriter(fstream);
		  file.write("@relation BugFix\n");
		  file.write("\n");
		  
		  file.write("@attribute urg {"); // {TRUE, FALSE} \n");
		  for( j = 0; j< valori_urg.size() - 1 ; j++ ){
			  file.write(valori_urg.get(j) + ", ");
		  }
		  file.write(valori_urg.get(valori_urg.size() -1) + "}\n");
		  		  
		  file.write("@attribute conf {TRUE, FALSE}\n");
		  
		  file.write("@attribute livello {");
		  for( j = 0; j< valori_liv.size() - 1 ; j++ ){
			  file.write(valori_liv.get(j) + ", ");
		  }
		  file.write(valori_liv.get(valori_liv.size() -1) + "}\n");
		  
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
