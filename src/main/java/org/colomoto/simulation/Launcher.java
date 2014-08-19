package org.colomoto.simulation;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.io.LogicalModelFormat;
import org.colomoto.logicalmodel.services.ServiceManager;
import org.colomoto.logicalmodel.tool.simulation.RandomUpdater;
import org.colomoto.logicalmodel.tool.simulation.updater.RandomUpdaterWithRates;
import org.colomoto.simulation.random.RandomSimulationProvider;

import java.io.File;

public class Launcher {

    private static final ServiceManager manager = ServiceManager.getManager();

    public static void main(String[] args) {
        System.out.println("run it ;)");

        if (args.length < 1) {
            System.out.println("required arg");
            return;
        }

        if ("test".equals(args[0])) {

            TestTaskProvider provider = new TestTaskProvider();
            new RunManager(provider, 50, 5).go();
            return;
        }

        int nruns = 100;
        int nthreads = 1;

        if (args.length > 1) {
            nruns = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            nthreads = Integer.parseInt(args[2]);
        }


        LogicalModel model = load(args[0]);
        if (model == null) {
            System.out.println("failed to load model");
            return;
        }

        RandomUpdater updater = new RandomUpdaterWithRates(model);
        byte[] init = new byte[ model.getNodeOrder().size() ];
        RandomSimulationProvider sim = new RandomSimulationProvider(updater, init, 500, 100);
        new RunManager(sim, nruns, nthreads).go();
    }

    private static LogicalModel load(String path) {
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        String name = f.getName();
        String extension = name.substring( name.lastIndexOf('.')+1 );
        LogicalModelFormat format = manager.getFormat(extension);
        if (format == null) {
            return null;
        }

        try {
            return format.importFile(f);
        } catch (Exception e) {
        }

        return null;
    }
}
