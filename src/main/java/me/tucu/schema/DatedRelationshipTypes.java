package me.tucu.schema;

public final class DatedRelationshipTypes {

    private DatedRelationshipTypes() {
        throw new IllegalAccessError("Utility class");
    }

    public static final String MENTIONED_ON = "MENTIONED_ON_";
    public static final String POSTED_ON = "POSTED_ON_";
    public static final String REPOSTED_ON = "REPOSTED_ON_";
    public static final String TAGGED_ON = "TAGGED_ON_";
}
