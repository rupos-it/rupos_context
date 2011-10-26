package org.processmining.plugins.petrinet.replayfitness;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Set;
import java.util.Date;


import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;



import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.processmining.connections.logmodel.LogPetrinetConnection;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.util.StringVisualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.petrinet.replay.ReplayAction;
import org.processmining.plugins.petrinet.replay.Replayer;

import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class ReplayPerformancePlugin {
	@Plugin(name = "PerformanceDetailsSettings", returnLabels = { "Performance Total" }, returnTypes = { TotalPerformanceResult.class }, parameterLabels = {}, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "T. Yuliani and H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	public TotalPerformanceResult getPerformanceDetails(PluginContext context, XLog log, Petrinet net, ReplayFitnessSetting setting) {

		Marking marking;

		try {
			InitialMarkingConnection connection = context.getConnectionManager().getFirstConnection(
					InitialMarkingConnection.class, context, net);
			marking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
		} catch (ConnectionCannotBeObtained ex) {
			context.log("Petri net lacks initial marking");
			return null;
		}


		TotalPerformanceResult performance = new TotalPerformanceResult();
		
		XEventClasses classes = getEventClasses(log);
		Map<Transition, XEventClass> map = getMapping(classes, net);
		LogPetrinetConnection con =context.getConnectionManager().addConnection(new LogPetrinetConnectionImpl(log, classes, net, map));

		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);

		Replayer<ReplayFitnessCost> replayer = new Replayer<ReplayFitnessCost>(context, net, semantics, map,
				ReplayFitnessCost.addOperator);

		int replayedTraces = 0;
		for (XTrace trace : log) {
			List<XEventClass> list = getList(trace, classes);
			try {
				List<Transition> sequence = replayer.replayTrace(marking, list, setting);
				sequence = sortHiddenTransection(net, sequence, map);
				updatePerformance(net, marking, sequence, semantics, trace, performance, map);
				replayedTraces++;
			} catch (Exception ex) {
				ex.printStackTrace();
				context.log("Replay of trace " + trace + " failed: " + ex.getMessage());
			}
		}

		String text = "(based on a successful replay of " + replayedTraces + " out of " + log.size() + " traces)";

		return performance;
	}

	
	

	

	private List<Transition> sortHiddenTransection(Petrinet net, List<Transition> sequence,
			Map<Transition, XEventClass> map) {
		for (int i=1; i<sequence.size(); i++) {
			Transition current = sequence.get(i);
			// Do not move visible transitions
			if (map.containsKey(current)) {
			    continue;
			}
			Set<Place> presetCurrent = new HashSet<Place>();
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(current)) {
			    if (! (edge instanceof Arc))
				continue;
			    Arc arc = (Arc) edge;
			    Place place = (Place)arc.getSource();
			    presetCurrent.add(place);
			}

			int k = i-1;
			while (k >= 0) {
			    Transition prev = sequence.get(k);
			    Set<Place> postsetPrev = new HashSet<Place>();
			    for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(prev)) {
				if (! (edge instanceof Arc))
				    continue;
				Arc arc = (Arc) edge;
				Place place = (Place)arc.getTarget();
				postsetPrev.add(place);
			    }
			
			    // Intersection
			    Set<Place> intersection = new HashSet<Place>();
			    for (Place place : postsetPrev) {
				if (presetCurrent.contains(place))
				    intersection.add(place);
			    }
			    if (intersection.size() > 0)
				break;
			
			    // Swap Transitions
			    sequence.remove(k);
			    sequence.add(k+1, prev);

			    k-=1;
			}
		}
		return sequence;
	}






	private void updatePerformance(Petrinet net, Marking initMarking, List<Transition> sequence, PetrinetSemantics semantics, XTrace trace, TotalPerformanceResult totalResult, Map<Transition, XEventClass> map) {
		// if (trace.size() != sequence.size())
		//     System.exit(1);

		XAttributeTimestampImpl date  = (XAttributeTimestampImpl)(trace.get(0).getAttributes().get("time:timestamp"));
		long d1 = date.getValue().getTime();

		Map<Place, PerformanceResult> performance = new HashMap<Place, PerformanceResult>();

		Marking marking = new Marking(initMarking);

		for (Place place : marking) {
			PerformanceResult result = null;
			if (performance.containsKey(place))
				result = performance.get(place);
			else
				result = new PerformanceResult();

			result.addToken();

			performance.put(place, result);
		}


		int iTrace = -1;
		for (int iTrans=0; iTrans<sequence.size(); iTrans++) {
			Transition transition = sequence.get(iTrans);
			long d2=d1;
			if (map.containsKey(transition)) {
				iTrace+=1;
			}
			if(iTrace>=0){
			XEvent event = trace.get(iTrace);
			XAttributeTimestampImpl date1  = (XAttributeTimestampImpl)(event.getAttributes().get("time:timestamp"));
			d2 = date1.getValue().getTime();
			}
			float deltaTime = d2-d1;
			d1 = d2;


			boolean fittingTransition = true;
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
			.getInEdges(transition);

			Set<Place> places = new HashSet<Place>();
			places.addAll(marking);
			List<Transition> futureTrans = sequence.subList(iTrans, sequence.size());
			for (Place place : places) {
				PerformanceResult result = null;
				if (performance.containsKey(place))
					result = performance.get(place);
				else
					result = new PerformanceResult();

				int placeMarking = marking.occurrences(place);
				if (placeMarking == 0)
					continue;

				// Transitions denending on the current place
				int maxMarking = 0;
				int minTransitionDistanceInFuture = futureTrans.size();
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(place)) {
					if (! (edge instanceof Arc))
						continue;
					Arc arc = (Arc) edge;
					Transition trs = (Transition)arc.getTarget();
					int trsPos = futureTrans.indexOf(trs);
					if (trsPos < 0)
						continue;
					if (trsPos > minTransitionDistanceInFuture)
						continue;
					minTransitionDistanceInFuture = trsPos;
						
					// Transition preset
					int minMarking = placeMarking;
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge1 : net.getInEdges(trs)) {
						if (! (edge1 instanceof Arc))
							continue;
						Arc arc1 = (Arc) edge1;
						Place p1 = (Place)arc1.getSource();
						int tokens = marking.occurrences(p1);
						minMarking = Math.min(minMarking, tokens);
					}
					//maxMarking = Math.max(maxMarking, minMarking);
					maxMarking = minMarking;
				}
				// maxMarking < placeMarking
				// maxMarking is the consumable tokens
				// synchTime = (placeMarking - maxMarking) *  deltaTime;
				result.addTime(placeMarking * deltaTime, maxMarking * deltaTime);
				performance.put(place, result);
			}

			// Updates marking according with enabled transition
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				if (edge instanceof Arc) {
					Arc arc = (Arc) edge;
					Place place = (Place) arc.getSource();
					int consumed = arc.getWeight();
					int missing = 0;
					if (arc.getWeight() > marking.occurrences(place)) {
						missing = arc.getWeight() - marking.occurrences(place);
					}
					for (int i = missing; i < consumed; i++) {
						marking.remove(place);
					}
				}
			}
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
			.getOutEdges(transition);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				if (edge instanceof Arc) {
					Arc arc = (Arc) edge;
					Place place = (Place) arc.getTarget();
					int produced = arc.getWeight();
					for (int i = 0; i < produced; i++) {
						marking.add(place);

						PerformanceResult result = null;
						if (performance.containsKey(place))
							result = performance.get(place);
						else
							result = new PerformanceResult();
						result.addToken();
						performance.put(place, result);
					}
				}
			}
		}
	
		totalResult.getList().add(performance);
	}

	private List<XEventClass> getList(XTrace trace, XEventClasses classes) {
		List<XEventClass> list = new ArrayList<XEventClass>();
		for (XEvent event : trace) {
			list.add(classes.getClassOf(event));
		}
		return list;
	}

	private XEventClasses getEventClasses(XLog log) {
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClasses eventClasses = summary.getEventClasses(classifier);
		return eventClasses;
	}

	private Map<Transition, XEventClass> getMapping(XEventClasses classes, Petrinet net) {
		Map<Transition, XEventClass> map = new HashMap<Transition, XEventClass>();

		for (Transition transition : net.getTransitions()) {
			for (XEventClass eventClass : classes.getClasses()) {
				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(transition, eventClass);
				}
			}
		}
		return map;
	}

	private void suggestActions(ReplayFitnessSetting setting, XLog log, Petrinet net) {
		setting.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
		setting.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
		setting.setAction(ReplayAction.REMOVE_HEAD, true);
		setting.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, true);
		setting.setAction(ReplayAction.INSERT_DISABLED_MATCH, true);
		setting.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, true);
	}

	//List<Map<Place,PerformanceResult>> listResult;
	//PerformanceResult totalResult;


	// Rupos public methos
	@Plugin(name = "PerformanceDetails", returnLabels = { "Performance Total" }, returnTypes = { TotalPerformanceResult.class }, parameterLabels = {}, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "T. Yuliani and H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	public TotalPerformanceResult getPerformanceDetails(UIPluginContext context, XLog log, Petrinet net) {
		ReplayFitnessSetting setting = new ReplayFitnessSetting();
		suggestActions(setting, log, net);
		ReplayFitnessUI ui = new ReplayFitnessUI(setting);
		context.showWizard("Configure Fitness Settings", true, true, ui.initComponents());
		ui.setWeights();

		TotalPerformanceResult total = getPerformanceDetails(context, log, net, setting);

		return total;
	}

	@Plugin(name = "PerformanceDetails", returnLabels = { "Performance Total" }, returnTypes = { TotalPerformanceResult.class }, parameterLabels = {}, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "T. Yuliani and H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	public TotalPerformanceResult getPerformanceDetails(PluginContext context, XLog log, Petrinet net) {
		ReplayFitnessSetting setting = new ReplayFitnessSetting();
		suggestActions(setting, log, net);
		TotalPerformanceResult total = getPerformanceDetails(context, log, net, setting);

		return total;
	}



	@Visualizer
	@Plugin(name = "Performance Result Visualizer", parameterLabels = "String", returnLabels = "Label of String", returnTypes = JComponent.class)
	public static JComponent visualize(PluginContext context, TotalPerformanceResult tovisualize) {
		return StringVisualizer.visualize(context, tovisualize.toString());
	}


}
