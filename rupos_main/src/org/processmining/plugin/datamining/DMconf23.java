package org.processmining.plugin.datamining;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import it.unipi.rupos.processmining.PetriNetEngine;

import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.ProMFactory;
import org.processmining.contexts.cli.ProMManager;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;


public class DMconf23 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	// caso 23: attributi di tipo litteral di cui non conosco il nome e nemmeno i valori
	//public enum NonInteressanti {org:resource, time:timestamp, concept:name, lifecycle:transition};
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
	    List<String> NonInteressanti = new Vector<String>();
	    NonInteressanti.add("org:resource");
	    NonInteressanti.add("time:timestamp");
	    NonInteressanti.add("concept:name");
	    NonInteressanti.add("lifecycle:transition");
	    


	    
	    
		String pathLogFile="../examples/";
    	
		String logFile = pathLogFile+"logBugFix21.mxml";
	    String netFile = pathLogFile+"BugFix.pnml";
		
	    	
		ProMManager manager = new ProMFactory().createManager(); 	// istanzia il fw PROM
		PetriNetEngine engine = manager.createPetriNetEngine(netFile); // engine ha metodi per processare la PN


		XLog log = manager.openLog(logFile); // il manager torna XLOG
		System.out.println("Log size: " + log.size());

		// settings per il log-replay
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
		
		Map<String,List<String>> Attributi = new HashMap<String,List<String>>();
		Map<String,List<String>> Istanze = new HashMap<String,List<String>>();
		List<String> istance_conf = new Vector<String>();		/*assumo che la conformità c'è sempre*/


		//Marking missing = fitness.getList().get(0).getMissingMarking();
		//missing.isEmpty();
		
		//XAttributeLiteral attr = (XAttributeLiteral) log.get(0).get(0).getAttributes().get("urg");
		//attr.getValue();
		
		// posso fare che dal primo evento della prima trace ricavo quali sono gli attributi
		// e li metto in una struttura vector !
		
		  XAttributeMap attrs = log.get(0).get(0).getAttributes();
		  for(String key: attrs.keySet()){
			  
			  if (!NonInteressanti.contains(key))
			  {
				  if(! Attributi.containsKey(key) )
					  Attributi.put(key,new Vector<String>());
			  }
		  }
		  
		  //creo le istanze
		  for( String key: Attributi.keySet())
			  Istanze.put(key, new Vector<String>());
		  
		  
		  List<ConformanceResult> ris = fitness.getList();
		  
		  for(int i=0; i<ris.size(); i++){
			  
			   // controllo la conformità della traccia i-esima
				Marking missing = fitness.getList().get(i).getMissingMarking();
				if(missing.isEmpty() ) {istance_conf.add("TRUE");}
				else {istance_conf.add("FALSE");}
			  
				
				// per ogni attributo di Attributi ricavo il valore dell'istanza i-esima( sempre all'evento 0 )
				// aggiungo il valore eventualmente, ed aggiungo l'istanza
				
				for( String chiave: Attributi.keySet() ){
					XAttributeLiteral attr = (XAttributeLiteral) log.get(i).get(0).getAttributes().get(chiave);
					if( ! Attributi.get(chiave).contains(attr.getValue()) )
						Attributi.get(chiave).add(attr.getValue());
					Istanze.get(chiave).add(attr.getValue());
					
					
				}
		  }
		  
		  // Adesso preparo il file arff 
		  FileWriter fstream = new FileWriter("BugFix23.arff");
		  BufferedWriter file = new BufferedWriter(fstream);
		  file.write("@relation BugFix\n");
		  file.write("\n");
		  
		  List<String> attrNames = new Vector<String>();
		  attrNames.addAll(Attributi.keySet());
		  
		  //Gli attributi 
		  for( String att: attrNames){
			  file.write("@attribute " + att + " {");
			  List<String> valori = Attributi.get(att);
			  for(int i=0; i<valori.size() - 1; i++){
				  file.write(valori.get(i) + ", ");
			  }
			  file.write(valori.get(valori.size() - 1 ) + "}\n");
		  }
		  //attributo di conformance
		  file.write("@attribute conf {TRUE, FALSE}\n");
		  
		  file.write("\n");
		  file.write("@data");
		  file.write("\n");

		  // stampo le istanze
		  for(int i=0; i<ris.size() ; i++){
			  String str = "";
			  for( String key: attrNames){
				  str += Istanze.get(key).get(i) + ", ";
			  }
			  file.write(str.substring(0, str.length()) );
			  file.write(istance_conf.get(i) + "\n" );
		  }
		  
		  file.flush();
		  System.out.println("FINITO");
	}
	

}
