package org.colomoto.simulation.random;

import org.colomoto.logicalmodel.tool.simulation.RandomUpdater;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.simulation.StateCollector;
import org.colomoto.simulation.TaskProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulation engine for stochastic updaters
 *
 */
public class RandomSimulationProvider implements TaskProvider {

    protected final RandomUpdater updater;
    protected final byte[] initialState;
    protected final int earlySteps;
    protected final int attractorSteps;

    // result data
    private final MDDManager ddCollected;
    private final List<StableReachedInfo> reachedStable = new ArrayList<StableReachedInfo>();
    private final List<StateCollector> reachedCollected = new ArrayList<StateCollector>();

    public RandomSimulationProvider(RandomUpdater updater, byte[] initialState, int earlySteps, int attractorSteps) {
        this.updater = updater;
        this.initialState = initialState;
        this.earlySteps = earlySteps;
        this.attractorSteps = attractorSteps;

        List<String> components = new ArrayList<String>();
        for (int i = 0; i < initialState.length; i++) {
            components.add("var" + i);
        }
        this.ddCollected = MDDManagerFactory.getManager(components, 2);
    }


    protected void pushResult(byte[] stableState) {
        System.out.println("--> stable");
        addReachedStable(stableState);
    }

    protected void pushResult( StateCollector collector) {
        System.out.println("--> complex");
        addReachedCollected(collector);
    }


    @Override
    public Runnable getTask() {
        return new RandomSimulationTask(this);
    }

    public void finished() {
        System.out.println("Found "+reachedStable.size() + " stable states and "+reachedCollected.size() + " other sets.");
        for (StableReachedInfo info: reachedStable) {
            System.out.println(info);
        }
        for (StateCollector info: reachedCollected) {
            System.out.println(info);
        }
    }

    private synchronized void addReachedStable(byte[] stable) {
        for (StableReachedInfo info: reachedStable) {
            if (info.isReached(stable)) {
                return;
            }
        }

        reachedStable.add( new StableReachedInfo(stable));
    }

    private synchronized void addReachedCollected(StateCollector collector) {
        for (StateCollector c: reachedCollected) {
            if (c.mergeShared(collector)) {
                return;
            }
        }

        reachedCollected.add(collector);
    }

    protected synchronized StateCollector getCollector() {
        return new StateCollector(ddCollected);
    }
}


class RandomSimulationTask implements Runnable {

    private static int NEXTID = 0;
    private final RandomSimulationProvider simulation;
    public final int agentID = NEXTID++;

    public RandomSimulationTask(RandomSimulationProvider simulation) {
        this.simulation = simulation;
    }

    public void run() {
        RandomUpdater updater = simulation.updater;

        byte[] curState = simulation.initialState;
        for (int curStep=0 ; curStep < simulation.earlySteps ; curStep++) {

            byte[] nextState = updater.pickSuccessor(curState);
            if (nextState == null) {
                simulation.pushResult( curState);
                return;
            }
            curState = nextState;
        }

        StateCollector collector = simulation.getCollector();
        for (int curStep=0 ; curStep < simulation.attractorSteps ; curStep++) {
            byte[] nextState = updater.pickSuccessor(curState);
            if (nextState == null) {
                simulation.pushResult( curState);
                return;
            }
            // add the state to the collector
            collector.addState(nextState);
            curState = nextState;
        }


        simulation.pushResult( collector);
    }


}



class StableReachedInfo {

    public final byte[] state;
    public int reachedCount = 1;

    public StableReachedInfo(byte[] state) {
        this.state = state;
    }

    public boolean isReached(byte[] state) {
        for (int i=0 ; i<state.length ; i++) {
            if (this.state[i] != state[i]) {
                return false;
            }
        }

        reachedCount++;
        return true;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer(reachedCount+": ");
        for (int v: state) {
            sb.append(v);
        }
        return sb.toString();
    }
}

