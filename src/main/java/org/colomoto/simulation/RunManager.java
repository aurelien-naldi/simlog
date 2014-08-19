package org.colomoto.simulation;

/**
 *
 */
public class RunManager {

    private final TaskProvider provider;
    private final Agent[] agents;

    private int nbRuns;
    private int runningAgents = 0;


    public RunManager(TaskProvider provider, int nbRuns, int nbThreads) {
        this.provider = provider;
        this.nbRuns = nbRuns;
        this.agents = new Agent[nbThreads];
        for (int i=0 ; i<nbThreads ; i++) {
            Runnable task = provider.getTask();
            agents[i] = new Agent(this, task);
        }
    }

    protected synchronized boolean continueAgent(Agent agent) {
        if (nbRuns > 0) {
            nbRuns--;
            return true;
        }

        runningAgents--;
        if (runningAgents <= 0) {
            provider.finished();
        } else {
            System.out.print("S:  ");
            for (Agent a: agents) {
                if (a == agent) {
                    System.out.print("* ");
                } else if (a.isAlive()) {
                    System.out.print("A ");
                } else {
                    System.out.print("S ");
                }
            }
            System.out.println();

        }

        return false;
    }

    public synchronized void go() {

        for (Agent agent: agents) {
            runningAgents++;
            agent.start();
        }
    }
}


class Agent extends Thread {

    private final RunManager manager;
    private final Runnable task;

    public Agent(RunManager manager, Runnable task) {
        this.manager = manager;
        this.task = task;
    }

    public void run() {
        while (manager.continueAgent(this)) {
            try {
                task.run();
            } catch (Exception e) {

            }
        }
    }

}
