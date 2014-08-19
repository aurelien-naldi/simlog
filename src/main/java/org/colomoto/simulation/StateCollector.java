package org.colomoto.simulation;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.operators.MDDBaseOperators;

/**
 * Created by aurelien on 15/08/14.
 */
public class StateCollector {

    private static MDDOperator AND = MDDBaseOperators.AND;
    private static MDDOperator OR = MDDBaseOperators.OR;

    private final MDDManager ddmanager;
    private final int[] counts;

    private int size = 0;
    private int content = 0;
    private int reachedCount = 1;

    public StateCollector(MDDManager manager) {
        this.ddmanager = manager;
        this.counts = new int[ddmanager.getAllVariables().length];
    }

    public void addState(byte[] state) {

        int v = ddmanager.reach(content, state);
        if (v > 0) {
            return;
        }

        int n = ddmanager.nodeFromState(state, 1);
        int newContent = OR.combine(ddmanager, content, n);
        ddmanager.free(content);
        ddmanager.free(n);
        content = newContent;

        for (int idx=0 ; idx<state.length ; idx++) {
            counts[idx] += state[idx];
        }
        size++;
    }

    public boolean mergeShared(StateCollector other) {

        int node = AND.combine(ddmanager, this.content, other.content);
        if (node == 0) {
            return false;
        }

        // avoid computing the OR if one is a subset of the other
        if (node == this.content) {
            node = other.content;
        } else if (node == other.content) {
            node = this.content;
        } else {
            ddmanager.free(node);
            node = OR.combine(ddmanager, this.content, other.content);
        }

        // apply the merge
        ddmanager.free(this.content);
        this.content = node;

        this.size += other.size;
        this.reachedCount++;
        for (int idx=0 ; idx<counts.length ; idx++) {
            counts[idx] += other.counts[idx];
        }
        return true;
    }

    public String toString() {

        return reachedCount+": "+content;
    }
}
