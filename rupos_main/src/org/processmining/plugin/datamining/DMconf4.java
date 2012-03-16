package org.processmining.plugin.datamining;

import it.unipi.rupos.processmining.PetriNetEngine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.contexts.cli.ProMFactory;
import org.processmining.contexts.cli.ProMManager;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;

public class DMconf4 {

	/**
	 * @param args
	 * @throws Exception 
	 * Caso in cui tutti gli eventi possono creare degli attributi,
	 * Condizione : tutti gli attributi sono diversi
	 * Nel caso in cui ci siano due attributi uguali, viene preso in considerazione 
	 * l'ultimo attributo definito
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		List<String> NonInteressanti = new Vector<String>();
	    NonInteressanti.add("org:resource");
	    NonInteressanti.add("time:timestamp");
	    NonInteressanti.add("concept:name");
	    NonInteressanti.add("lifecycle:transition");
	    
	    
	    
		String pathLogFile="../examples/";
    	
		String logFile = pathLogFile+"prova.xes";
	    String netFile = pathLogFile+"viaggio.pnml";
		
	    	
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
		
		Map<String,Set<String>> litAttributes = new HashMap<String,Set<String>>();
		Set<String> floatAttributes = new HashSet<String>();
		Set<String> boolAttributes = new HashSet<String>();

				
		//Con una scansione ricavo gli attributi
		for( XTrace t : log ){
			for( XEvent e : t ){				
				  XAttributeMap attrs = e.getAttributes();
				  for(String key: attrs.keySet()){
					  
					  if (! NonInteressanti.contains(key))
					  {
						  XAttribute val = attrs.get(key);
						  if( val instanceof XAttributeContinuousImpl || 
								  val instanceof XAttributeDiscreteImpl )
							  floatAttributes.add(key);
						  if( val instanceof XAttributeBooleanImpl)
							  boolAttributes.add(key);
						  if( val instanceof XAttributeLiteralImpl ){
							  if(! litAttributes.containsKey(key))
								  litAttributes.put(key, new HashSet<String>());
						  	  //aggiungo il valore incontrato.
							  litAttributes.get(key).add(((XAttributeLiteral) val).getValue());
						  }
					  }
				  }
				  
				  
			}
		}
		  //List<ConformanceResult> ris = fitness.getList();

	
		 
			  
		  // Adesso preparo il file arff 
		  FileWriter fstream = new FileWriter("Prova.arff");
		  BufferedWriter file = new BufferedWriter(fstream);
		  file.write("@relation BugFix\n");
		  file.write("\n");
		  
		  List<String> litAttrNames = new Vector<String>();
		  litAttrNames.addAll(litAttributes.keySet());
		  //Gli attributi Lit
		  for( String att: litAttrNames){
			  file.write("@attribute " + att + " {");
			  Set<String> valori = litAttributes.get(att);
			  int commas = valori.size()-1;
			  for (String val : valori) {
				  file.write(val);
				  if (commas > 0)
					  file.write(",");
				  commas--;
			  }
			  file.write("}\n");
		 }
		  Vector<String> floatAttrNames = new Vector<String>();
		  floatAttrNames.addAll(floatAttributes);
		  //Gli attributi float
		  for( String att: floatAttrNames){
			  file.write("@attribute " + att + " NUMERIC" + "\n");
		  }
		  
		  Vector<String> boolAttrNames = new Vector<String>();
		  boolAttrNames.addAll(boolAttributes);
		  //Gli attributi bool
		  for( String att: boolAttrNames){
			  file.write("@attribute " + att + " {TRUE, FALSE}" + "\n");
		  }

		  //attributo di conformance
		  file.write("@attribute conf {TRUE, FALSE}\n");
		  
		  file.write("\n");
		  file.write("@data");
		  file.write("\n");
		  
		  /***************************/
		  
		  //stampo i dati
		  for(XTrace tr: log ){
			  String str = "";
			  Map<String,String> trAttributes = new HashMap<String,String>();
					  
			  for(XEvent ev : tr){
				  //Primo passo: ricavo tutti gli attributi definiti in quella traccia
				  //Hyp : gli attributi sono tutti diversi
				  if(((XAttributeLiteral) ev.getAttributes().get("lifecycle:transition")).getValue().equals("complete"))
					  continue;
				  XAttributeMap attrs = ev.getAttributes();
				  for( String key: attrs.keySet()){
					  if( NonInteressanti.contains(key))
						  continue;
					  XAttribute val = attrs.get(key);
					  if( val instanceof XAttributeContinuousImpl){
						   trAttributes.put(key,Double.toString(((XAttributeContinuousImpl) val).getValue()));
					  }
					  if(val instanceof XAttributeDiscreteImpl){
						  trAttributes.put(key, Long.toString(((XAttributeDiscreteImpl) val).getValue()));
					  }				  
					  if( val instanceof XAttributeBooleanImpl){
						  trAttributes.put(key,Boolean.toString(((XAttributeBooleanImpl) val).getValue()));
					  }
					  if( val instanceof XAttributeLiteralImpl ){
						  trAttributes.put(key, ((XAttributeLiteral) val).getValue());
					  }
						  
					  
				  }
			  }
				  //secondo passo stampo gli attributi:
				  for(String key: litAttrNames){
					  String trvalue = trAttributes.get(key);
					  if(trvalue == null )
						  str += "?, ";					 
					  else
						  str += trvalue + ", ";				  
				  }
				  for(String key: floatAttrNames){
					  String trvalue = trAttributes.get(key);
					  if(trvalue == null )
						  str += "?, ";					 
					  else
						  str += trvalue + ", ";		
				  }
				  
				  for(String key: boolAttrNames){
					  String trvalue = trAttributes.get(key);
					  if(trvalue == null )
						  str += "?, ";					 
					  else
						  str += trvalue + ", ";	
				  }
				  
				  file.write(str);
				  
				  //controllo conformità della traccia tr
				  Marking missing = fitness.getList().get(log.indexOf(tr)).getMissingMarking();
				  if(missing.isEmpty() ) {
					  file.write("TRUE" + "\n" );
				  }
				  else {
					  file.write("FALSE" + "\n" );
				  }
		  }
		  
		  /********************/
		  
		 
		  file.flush();
		  System.out.println("FINITO");

	}

}
