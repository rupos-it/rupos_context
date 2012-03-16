package org.processmining.plugin.datamining;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import it.unipi.rupos.processmining.PetriNetEngine;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
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


public class DMconf3 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	// caso 3: attributi di vario tipo di cui non conosco il nome e nemmeno i valori
	
	public static void main(String[] args) throws Exception {
		// TODBO Auto-generated method stub
	    List<String> NonInteressanti = new Vector<String>();
	    NonInteressanti.add("org:resource");
	    NonInteressanti.add("time:timestamp");
	    NonInteressanti.add("concept:name");
	    NonInteressanti.add("lifecycle:transition");
	    


	    
	    
		String pathLogFile="../examples/";
    	
		String logFile = pathLogFile+"logViaggio2.xes";
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

				
		// posso fare che dal primo evento della prima trace ricavo quali sono gli attributi
		// e li metto in una struttura vector !
		
		  XAttributeMap attrs = log.get(0).get(0).getAttributes();
		  for(String key: attrs.keySet()){
			  
			  if (!NonInteressanti.contains(key))
			  {
				  // a seconda del tipo scrivo nel primo elemento del vettore dei valori,il tipo:
				  XAttribute val = attrs.get(key);
				  if( val instanceof XAttributeContinuousImpl || 
						  val instanceof XAttributeDiscreteImpl )
					  floatAttributes.add(key);
				  if( val instanceof XAttributeBooleanImpl)
					  boolAttributes.add(key);
				  if( val instanceof XAttributeLiteralImpl )
					  litAttributes.put(key, new HashSet<String>());
			  }
		  }
		  
		  List<ConformanceResult> ris = fitness.getList();
		  
		  for(int i=0; i<ris.size(); i++){
				for( String key: litAttributes.keySet() ){
					XAttribute attr = log.get(i).get(0).getAttributes().get(key);
					if( attr instanceof XAttributeLiteralImpl){
						litAttributes.get(key).add(((XAttributeLiteral) attr).getValue());
					}
				}
		  }
			  
		  // Adesso preparo il file arff 
		  FileWriter fstream = new FileWriter("viaggioProva.arff");
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

		  // stampo le istanze
		  for(int i=0; i<ris.size() ; i++) {
			  String str = "";
			  for( String key: litAttrNames) {
				  XAttribute attr = log.get(i).get(0).getAttributes().get(key);
				  str += ((XAttributeLiteral) attr).getValue() + ", ";
			  }
			  for( String key: floatAttrNames) {
				  XAttribute attr = log.get(i).get(0).getAttributes().get(key);
				  String value = "";
				  if (attr instanceof  XAttributeContinuousImpl){
					  value = Double.toString(((XAttributeContinuousImpl) attr).getValue());
				  }
				  else if(attr instanceof XAttributeDiscreteImpl){
						value = Long.toString(((XAttributeDiscreteImpl) attr).getValue());
				  }
				  str += value + ", ";
			  }
			  for( String key: boolAttrNames) {
				  XAttribute attr = log.get(i).get(0).getAttributes().get(key);
				  Boolean value = ((XAttributeBooleanImpl) attr).getValue();
				  str += value.toString() + ", ";
			  }

			  //file.write(str.substring(0, str.length()) );
			  file.write(str);
			  
			   // controllo la conformità della traccia i-esima
			  Marking missing = fitness.getList().get(i).getMissingMarking();
			  if(missing.isEmpty() ) {
				  file.write("TRUE" + "\n" );
			  }
			  else {
				  file.write("FALSE" + "\n" );
			  }
		  }
		  
		  file.flush();
		  System.out.println("FINITO");
	}

}
