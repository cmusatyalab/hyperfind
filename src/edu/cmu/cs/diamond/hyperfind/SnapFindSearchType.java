package edu.cmu.cs.diamond.hyperfind;

public enum SnapFindSearchType {
    CODEC, FILTER;

    public static SnapFindSearchType fromString(String s) {
        return valueOf(s.toUpperCase());
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
