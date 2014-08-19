package org.colomoto.simulation;

/**
 * Created by aurelien on 18/08/14.
 */
public class TestTaskProvider implements TaskProvider {

    @Override
    public void finished() {

    }

    public Runnable getTask() {
        return new TestTask();
    }
}


class TestTask implements Runnable {

    private static int NEXTID = 1;

    private final int id = NEXTID++;

    public void run() {
        System.out.println("   R:"+id);
    }
}
