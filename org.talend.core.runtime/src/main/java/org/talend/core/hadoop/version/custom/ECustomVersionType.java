package org.talend.core.hadoop.version.custom;

/**
 * created by ycbai on 2013-3-13 Detailled comment
 * 
 */
public enum ECustomVersionType {

    HDFS("HDFS", ECustomVersionGroup.COMMON), //$NON-NLS-1$

    HCATALOG("HCatalog", ECustomVersionGroup.COMMON), //$NON-NLS-1$

    OOZIE("Oozie", ECustomVersionGroup.COMMON), //$NON-NLS-1$

    HIVE("Hive", ECustomVersionGroup.HIVE), //$NON-NLS-1$

    HBASE("HBase", ECustomVersionGroup.HBASE), //$NON-NLS-1$

    PIG("Pig", ECustomVersionGroup.PIG), //$NON-NLS-1$

    ALL("All", ECustomVersionGroup.ALL); //$NON-NLS-1$

    private String displayName;

    private ECustomVersionGroup group;

    ECustomVersionType(String displayName, ECustomVersionGroup group) {
        this.displayName = displayName;
        this.group = group;
    }

    public String getName() {
        return name();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public ECustomVersionGroup getGroup() {
        return this.group;
    }

    public boolean isSameGroup(ECustomVersionType type) {
        return this.getGroup().equals(type.getGroup());
    }

}