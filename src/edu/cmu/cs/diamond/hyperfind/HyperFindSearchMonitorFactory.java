package edu.cmu.cs.diamond.hyperfind;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import edu.cmu.cs.diamond.opendiamond.CookieMap;
import edu.cmu.cs.diamond.opendiamond.Filter;

/*
 * Factory for HyperFindSearchMonitors.  This class implements a SearchMonitor
 * plugin and is instantiated by HyperFind during start-up.
 */
public abstract class HyperFindSearchMonitorFactory {
    private static final List<HyperFindSearchMonitorFactory> factories = generateFactories();

    /*
     * If the SearchMonitor is interested in this particular search, this method
     * should return an initialized copy of the monitor.
     */
    protected abstract HyperFindSearchMonitor createSearchMonitor(
            List<Filter> filters);

    public static List<HyperFindSearchMonitor> getInterestedSearchMonitors(
            CookieMap cookies, List<Filter> filters) {
        List<HyperFindSearchMonitor> interestedFactories = new ArrayList<HyperFindSearchMonitor>();
        for (HyperFindSearchMonitorFactory f : factories) {
            HyperFindSearchMonitor sm = f.createSearchMonitor(filters);
            if (sm != null)
                interestedFactories.add(sm);
        }
        return interestedFactories;
    }

    private static List<HyperFindSearchMonitorFactory> generateFactories() {
        List<HyperFindSearchMonitorFactory> factories = new ArrayList<HyperFindSearchMonitorFactory>();
        ServiceLoader<HyperFindSearchMonitorFactory> factoryLoader = ServiceLoader
                .load(HyperFindSearchMonitorFactory.class);
        for (HyperFindSearchMonitorFactory f : factoryLoader) {
            factories.add(f);
        }
        return factories;
    }
}
