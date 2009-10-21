package edu.cmu.cs.diamond.hyperfind;

import java.util.List;

import javax.swing.JTable;

public class SearchList extends JTable {

    public SearchList(List<SnapFindSearchFactory> factories) {
        // get codecs
        for (SnapFindSearchFactory sf : factories) {
            if (sf.getType() == SnapFindSearchType.CODEC) {
                populateCodecList(sf);
            }
        }
    }

    private void populateCodecList(SnapFindSearchFactory sf) {
        // TODO Auto-generated method stub

    }
}
