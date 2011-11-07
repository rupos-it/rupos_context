package org.processmining.contexts.cli;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.lang.InterruptedException;

import java.lang.Thread;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;


import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.plugin.PluginExecutionResult;


import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExt;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XLogImpl;

import org.processmining.plugins.bpmn.exporting.metrics.BPMNConfMetrics;
import org.processmining.plugins.bpmn.exporting.metrics.BPMNPerfMetrics;
import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;


import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.conformance.TotalConformanceResult;
import org.processmining.plugins.petrinet.replay.performance.PerformanceResult;
import org.processmining.plugins.petrinet.replay.performance.TotalPerformanceResult;

import org.processmining.contexts.cli.CLIContext;

import it.unipi.rupos.processmining.PetriNetEngine;

/**
 * @author Dipartimento di Informatica - Rupos
 * 
 */
public class ProMManager {
	PluginDescriptor openLogPlugin = null;
	PluginDescriptor alphaPlugin = null;
	PluginDescriptor conformancePlugin = null;
	PluginDescriptor conformancePluginMarki = null;
	PluginDescriptor importNetPlugin = null;
	PluginDescriptor performancePlugin = null;
	PluginDescriptor suggestPlugin = null;
	PluginDescriptor OpenBpmnPlugin = null;
	PluginDescriptor BpmnPlugin = null;
	PluginDescriptor performancewithMarkingPlugin= null;
	PluginDescriptor BPMNMeasureswithAnalisysDetails= null;
	PluginDescriptor BPMNImport= null;
	PluginDescriptor BPMNMetricsConf= null;
	PluginDescriptor BPMNMetricsPerf= null;
	PluginDescriptor PNArtificialEnd= null;
	PluginDescriptor LogArtificialEnd= null;

	CLIContext globalContext = null;
	PluginContext context = null;
	private PluginDescriptor BPMNexport = null;

	@Plugin(name = "ProMManager", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) {
		globalContext = new CLIContext();
		context = globalContext.getMainPluginContext();

		System.out.println("------------------------------");
		for (PluginDescriptor plugin : context.getPluginManager()
				.getAllPlugins()) {
			if ("Open XES Log File".equals(plugin.getName()))
				openLogPlugin = plugin;
			else if ("Alpha Miner".equals(plugin.getName()))
				alphaPlugin = plugin;
			else if ("ConformanceDetailsSettings".equals(plugin.getName()))
				conformancePlugin = plugin;
			else if ("PerformanceDetailsSettings".equals(plugin.getName()))
				performancePlugin = plugin;
			else if ("Import Petri net from PNML file".equals(plugin.getName()))
				importNetPlugin = plugin;
			else if ("FitnessSuggestSettings".equals(plugin.getName()))
				suggestPlugin = plugin;//Import BPMN model from XPDL 2.1 file
			else if ("BPMN to PetriNet".equals(plugin.getName()))
				BpmnPlugin = plugin;
			else if ("Import BPMN model from XPDL 2.1 file".equals(plugin.getName()))
				OpenBpmnPlugin = plugin;
			else if ("ConformanceDetailsSettingsWithMarking".equals(plugin.getName()))
				conformancePluginMarki = plugin;
			else if ("PerformanceDetailsSettingsWithMarking".equals(plugin.getName()))
				performancewithMarkingPlugin = plugin;
			else if ("BPMNMeasureswithAnalisysDetails".equals(plugin.getName()))
				BPMNMeasureswithAnalisysDetails = plugin;
			else if ("XPDL export (Bussines Notation with Artifact)".equals(plugin.getName()))
				BPMNexport  = plugin;
			else if ("Import BPMN model from BPMN 2.0 file".equals(plugin.getName()))
				BPMNImport  = plugin;//
			else if ("BPMNMAnalisysDetailsintoMetricsConformance".equals(plugin.getName()))
				BPMNMetricsConf  = plugin;//
			else if ("BPMNMAnalisysDetailsintoMetricsPerformance".equals(plugin.getName()))
				BPMNMetricsPerf  = plugin;//PNArtificialEnd
			else if ("Add Artificial End Transition Variant".equals(plugin.getName()))
				PNArtificialEnd = plugin;//
			else if ("Add Artificial End Event Filter".equals(plugin.getName()))
				LogArtificialEnd = plugin;//
			else 
				continue;
			if (false) {
				System.out.println(plugin.getName());
				for (int j = 0; j < plugin.getParameterTypes().size(); j++) {
					System.out.println("  " + j + " ) "
							+ plugin.getMethodLabel(j) + " "
							+ plugin.getParameterTypes(j));
				}
			}
		}
		try {
			Thread.sleep(1 * 1000);
		} catch (java.lang.InterruptedException e) {
		}

		if (openLogPlugin == null) {
			System.out.println("Plugin OpenLog not found");
		}
		if (alphaPlugin == null) {
			System.out.println("Plugin Alpha not found");
		}
		if (conformancePlugin == null) {
			System.out.println("Plugin Conformance not found");
		}
		if (importNetPlugin == null) {
			System.out.println("Plugin ImportNet not found");
		}
		if (performancePlugin == null) {
			System.out.println("Plugin Performance not found");
		}
		if (suggestPlugin == null) {
			System.out.println("Plugin SuggestSettings not found");
		}
		if (BpmnPlugin == null) {
			System.out.println("Plugin BpmntopnPlugin not found");
		}
		if (conformancePluginMarki == null) {
			System.out.println("Plugin fitness with marking not found");
		}
		if (performancewithMarkingPlugin == null) {
			System.out.println("Plugin performance with marking not found");
		}
		if (BPMNMeasureswithAnalisysDetails == null) {
			System.out.println("Plugin BPMNMeasureswithAnalisysDetails not found");
		}
		if (OpenBpmnPlugin == null) {
			System.out.println("Plugin import bpmn not found");
		}
		if (BPMNexport == null) {
			System.out.println("XPDL export (Bussines Notation with Artifact) not found");
		}//
		if (BPMNImport == null) {
			System.out.println("Import BPMN model from BPMN 2.0 file not found");
		}//
		if (BPMNMetricsConf == null) {
			System.out.println("BPMN Analisys Details into Metrics Conformance not found");
		}
		if (PNArtificialEnd == null) {
			System.out.println("Add Artificial End Transition Variant not found");
		}
		if (LogArtificialEnd == null) {
			System.out.println("Add Artificial End Event Filter not found");
		}
		if (BPMNMetricsPerf== null) {
			System.out.println("BPMN Analisys Details into Metrics Performance not found");
		}
		
		context = context.createChildContext("MainContext");

		System.out.println("End Initializazion 1");
		return this;
	}

	/**
	 * @param petriNetFile
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public PetriNetEngine createPetriNetEngine(String petriNetFile)
			throws ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Import Net");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Import Net");
		importNetPlugin.invoke(0, context1, petriNetFile);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res = context1.getResult();
		System.out.println("Obtained " + res.getSize() + " results");
		System.out.println("------------------------------");
		Petrinet net = res.getResult(0);
		Marking startMarking = res.getResult(1);
		System.out.println("------------------------------");
		//GraphLayoutConnection layout = res.getResult(2);
		PetriNetEngine res1 = new PetriNetEngine(this, net, startMarking);
		context1.getParentContext().deleteChild(context1);
		return res1;
	}

	/**
	 * @param logFile
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public XLog openLog(String logFile) throws ExecutionException,
	InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Open Log");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("Result of Import Log Error");
		openLogPlugin.invoke(0, context1, logFile);
		context1.getResult().synchronize();
		XLog res = (XLog) context1.getResult().getResult(0);
		context1.getParentContext().deleteChild(context1);
		return res;
	}

	/**
	 * @param net
	 * @param log
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public ReplayFitnessSetting suggestSettings(Petrinet net, XLog log)
			throws ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Suggest settings");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Result of suggest settings");
		suggestPlugin.invoke(0, context1, log, net);
		context1.getResult().synchronize();
		ReplayFitnessSetting res = (ReplayFitnessSetting) context1.getResult()
				.getResult(0);
		context1.getParentContext().deleteChild(context1);
		return res;
	}

	/**
	 * @param net
	 * @param log
	 * @param settings
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public TotalConformanceResult getConformance(Petrinet net, XLog log,
			ReplayFitnessSetting settings) throws ExecutionException,
			InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Conformance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Conformance Checking");

		conformancePlugin.invoke(0, context1, log, net, settings);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalConformanceResult fitness = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return fitness;
	}

	public TotalConformanceResult getConformance(Petrinet net, XLog log,
			ReplayFitnessSetting settings, Marking marking) throws ExecutionException,
			InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Conformance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Conformance Checking");

		conformancePluginMarki.invoke(0, context1, log, net, settings,marking);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalConformanceResult fitness = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return fitness;
	}
	/**
	 * @param net
	 * @param log
	 * @param settings
	 * @return
	 * @throws CancellationException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public TotalPerformanceResult getPerformance(Petrinet net, XLog log,
			ReplayFitnessSetting settings) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Performance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("Performance Checking");

		performancePlugin.invoke(0, context1, log, net, settings);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalPerformanceResult performance = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return performance;
	}

	public XLog getLogwithArtificialend( XLog log) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Log with Artificial End");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("LogwithArtificialend");
		
		XEvent end_event = makeEvent(log);


		LogArtificialEnd.invoke(0, context1, log, end_event);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		XLog newlog = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return newlog;
	}
	
	private XEvent makeEvent(XLog log) {
		Date time = new Date();
		XTrace oldTrace = log.get(log.size()-1);
		try {
			time = ((XAttributeTimestampImpl) oldTrace.get(oldTrace.size() - 1).getAttributes()
					.get("time:timestamp")).getValue();
			time.setTime(time.getTime() + 1);
		} catch (Exception ex) {}
		XAttributeMap attMap = new XAttributeMapImpl();
		putLiteral(attMap, "concept:name", "ArtificialEnd");
		putLiteral(attMap, "lifecycle:transition", "complete");
		putLiteral(attMap, "org:resource", "artificial");
		putTimestamp(attMap, "time:timestamp", time);
		XEvent newEvent = new XEventImpl(attMap);
		return newEvent;
	}
	
	private void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}

	private void putTimestamp(XAttributeMap attMap, String key, Date value) {
		attMap.put(key, new XAttributeTimestampImpl(key, value));
	}
	
	public PetriNetEngine getPNwithArtificialEnd(Petrinet net) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("ArtificialEnd");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("ArtificialEnd");
		
		PNArtificialEnd.invoke(0, context1, net);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		Petrinet res = (Petrinet) context1.getResult().getResult(0);
		Marking marking= (Marking) context1.getResult().getResult(1);
		PetriNetEngine res1 = new PetriNetEngine(this, res, marking);
		System.out.println("Obtained " + res1 + " results");
		System.out.println("------------------------------");
		
		//context1.getParentContext().deleteChild(context1);
		return res1;
	}
	public TotalPerformanceResult getPerformance(Petrinet net, XTrace trace,
			ReplayFitnessSetting settings, Marking marking) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Performance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("Performance Checking");
		XLog log = new XLogImpl(new XAttributeMapImpl());
		log.add(trace);

		performancewithMarkingPlugin.invoke(0, context1, log, net, settings, marking);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalPerformanceResult performance = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return performance;
	}
	public TotalPerformanceResult getPerformance(Petrinet net, XLog log,
			ReplayFitnessSetting settings, Marking marking) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Performance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("Performance Checking");


		performancewithMarkingPlugin.invoke(0, context1, log, net, settings, marking);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalPerformanceResult performance = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return performance;
	}
	public TotalPerformanceResult getPerformance(Petrinet net, XTrace trace,
			ReplayFitnessSetting settings) throws CancellationException,
			ExecutionException, InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Performance Details");
		System.out.println("------------------------------");
		PluginContext context1 = context
				.createChildContext("Performance Checking");
		XLog log = new XLogImpl(new XAttributeMapImpl());
		log.add(trace);

		performancePlugin.invoke(0, context1, log, net, settings);
		context1.getResult().synchronize();
		System.out.println("------------------------------");
		PluginExecutionResult res2 = context1.getResult();
		System.out.println("Obtained " + res2.getSize() + " results");
		System.out.println("------------------------------");
		TotalPerformanceResult performance = res2.getResult(0);
		System.out.println("------------------------------");

		//context1.getParentContext().deleteChild(context1);
		return performance;
	}
	public PetriNetEngine getBpmntoPn(BPMNDiagram bpmn) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("Convert BPMN to Petri Nets");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Result of Trasform");
		BpmnPlugin.invoke(0, context1, bpmn);
		context1.getResult().synchronize();
		Petrinet res = (Petrinet) context1.getResult().getResult(0);
		Marking marking= (Marking) context1.getResult().getResult(1);
		PetriNetEngine res1 = new PetriNetEngine(this, res, marking);
		//context1.getParentContext().deleteChild(context1);
		return res1;
	}
	//BPMNMeasureswithAnalisysDetails
	public BPMNDiagramExt getBPMNwithAnalysis(TotalConformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN with conformance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Conformance");
		BPMNMeasureswithAnalisysDetails.invoke(0, context1, tcr);
		context1.getResult().synchronize();
		BPMNDiagramExt res = (BPMNDiagramExt) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}

	public List<BPMNConfMetrics> getBPMNMetrics(TotalConformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN Metrics Conformance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Conformance");
		BPMNMetricsConf.invoke(0, context1, tcr);
		context1.getResult().synchronize();
		List<BPMNConfMetrics>  res = (List<BPMNConfMetrics> ) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}


	public BPMNDiagramExt getBPMNwithAnalysis(Petrinet pn , ConformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN with conformance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Conformance");
		BPMNMeasureswithAnalisysDetails.invoke(0, context1, pn,tcr);
		context1.getResult().synchronize();
		BPMNDiagramExt res = (BPMNDiagramExt) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}
	public BPMNDiagramExt getBPMNwithAnalysis(TotalPerformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN with performance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Performance");
		BPMNMeasureswithAnalisysDetails.invoke(2, context1, tcr);
		context1.getResult().synchronize();
		BPMNDiagramExt res = (BPMNDiagramExt) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}
	
	public List<BPMNPerfMetrics> getBPMNMetrics(TotalPerformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN with performance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Performance");
		BPMNMetricsPerf.invoke(0, context1, tcr);
		context1.getResult().synchronize();
		List<BPMNPerfMetrics> res = (List<BPMNPerfMetrics>) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}

	public BPMNDiagramExt getBPMNwithAnalysis(Petrinet pn , PerformanceResult tcr) throws CancellationException, ExecutionException, InterruptedException{
		System.out.println("------------------------------");
		System.out.println("BPMN with performance analysis ");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Calc Performance");
		BPMNMeasureswithAnalisysDetails.invoke(3, context1, pn,tcr);
		context1.getResult().synchronize();
		BPMNDiagramExt res = (BPMNDiagramExt) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}

	public BPMNDiagram openBpmn(String BpmnFile) throws ExecutionException,
	InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Open BPMN XPDL");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Result of Import");
		OpenBpmnPlugin.invoke(0, context1, BpmnFile);
		context1.getResult().synchronize();
		BPMNDiagram res = (BPMNDiagram) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}

	public BPMNDiagram openBpmnfromOMGS(String BpmnFile) throws ExecutionException,
	InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Open BPMN from OMG serial");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Result of Import");
		BPMNImport.invoke(0, context1, BpmnFile);
		context1.getResult().synchronize();
		BPMNDiagram res = (BPMNDiagram) context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);
		return res;
	}
	public void writefilebpmn(File BpmnFilename, BPMNDiagramExt bpmn) throws ExecutionException,
	InterruptedException {
		System.out.println("------------------------------");
		System.out.println("Write BPMN esteso to  XPDL");
		System.out.println("------------------------------");
		PluginContext context1 = context.createChildContext("Result of Export");
		BPMNexport.invoke(0, context1, bpmn,BpmnFilename);
		context1.getResult().synchronize();
		context1.getResult().getResult(0);
		//context1.getParentContext().deleteChild(context1);

	}
	public void closeContext() {
		// for ( PluginContext c:
		// globalContext.getMainPluginContext().getChildContexts()) {
		// globalContext.getMainPluginContext().deleteChild(c);
		// }
	}
	public PluginContext getPluginContext() {
		return this.context;
		// for ( PluginContext c:
		// globalContext.getMainPluginContext().getChildContexts()) {
		// globalContext.getMainPluginContext().deleteChild(c);
		// }
	}

}
