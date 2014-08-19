package org.colomoto.simulation;

/**
 * Created by aurelien on 18/08/14.
 */
public interface TaskProvider {

    Runnable getTask(  );

    void finished();

}
