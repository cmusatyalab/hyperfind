package edu.cmu.cs.diamond.hyperfind;

import java.util.Set;

/*
 * Super-class for any class wishing to be kept abreast of the progress of a
 * HyperFind search.
 */
public abstract class HyperFindSearchMonitor {

    /*
     * Called each time a search result returns.
     */
    public abstract void notify(HyperFindResult hr) throws InterruptedException;

    /*
     * Called when the user has stopped the current search but results are still
     * available for browsing.
     */
    public void stopped() {
    }

    /*
     * Called when the current search results are no longer available for
     * browsing, such as when a new search is started.
     */
    public void terminated() {
    }

    /*
     * Returns a list of attributes that this SearchMonitor intends to access.
     */
    public abstract Set<String> getPushAttributes();

}
