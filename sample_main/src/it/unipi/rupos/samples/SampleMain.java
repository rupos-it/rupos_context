package it.unipi.rupos.samples;

import java.io.File;
import java.util.List;

import it.unipi.rupos.processmining.PetriNetEngine;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.ProMFactory;
import org.processmining.contexts.cli.ProMManager;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExt;
import org.processmining.plugins.bpmn.exporting.metrics.BPMNConfMetrics;
import org.processmining.plugins.bpmn.exporting.metrics.BPMNPerfMetrics;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replay.performance.TotalPerformanceResult;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;

public class SampleMain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	    String pathLogFile="../../examples/";
    	
		String logFile = pathLogFile+"wsfm.mxml";
	    String netFile = pathLogFile+"residency.pnml";
	    String BpmnFile = pathLogFile+"Residency.xpdl";
		
	    	
		ProMManager manager = new ProMFactory().createManager();
		BPMNDiagram bpmn = manager.openBpmn(BpmnFile);
		PetriNetEngine engine = manager.getBpmntoPn(bpmn);
		
		PetriNetEngine enginewithartificialend = manager.getPNwithArtificialEnd(engine.getNet());

		XLog log = manager.openLog(logFile);
		XLog Artificialendlog = manager.getLogwithArtificialend(log);

		ReplayFitnessSetting settings = engine.suggestSettings(log);
		System.out.println("Settings: " + settings);
		settings.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
		settings.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
		settings.setAction(ReplayAction.REMOVE_HEAD, false);
		settings.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
		settings.setAction(ReplayAction.INSERT_DISABLED_MATCH, true);
		settings.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
		
		TotalConformanceResult conformance = enginewithartificialend.getFitness(Artificialendlog, settings);
		
		List<BPMNConfMetrics> metricconf = manager.getBPMNMetrics(conformance);
		
		System.out.println(metricconf);
		/*BPMNDiagramExt bpmnext = manager.getBPMNwithAnalysis(conformance);
		 
		 File f = new File(pathLogFile+"Conformance.xpdl");
		 if(!f.exists()){
			  f.createNewFile();
		 }
		 manager.writefilebpmn(f, bpmnext);*/

		 settings.setAction(ReplayAction.INSERT_DISABLED_MATCH, false);
		 
		 TotalPerformanceResult performance = enginewithartificialend.getPerformance(Artificialendlog, settings);
		 
		 List<BPMNPerfMetrics> metricperf= manager.getBPMNMetrics(performance);
		 
		 System.out.println(metricperf);
		 
		 /*bpmnext = manager.getBPMNwithAnalysis(performance);
		 
		 File f2 = new File( pathLogFile+"Performance.xpdl");
		 if(!f2.exists()){
			  f2.createNewFile();
		 }
		 manager.writefilebpmn(f2, bpmnext);*/
		

		manager.closeContext();

	}

}
