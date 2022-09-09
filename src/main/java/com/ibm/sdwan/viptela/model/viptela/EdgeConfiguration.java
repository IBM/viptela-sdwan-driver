package com.ibm.sdwan.viptela.model.viptela;

import lombok.Data;

@Data
public class EdgeConfiguration {
    private String configurationType;
    private String bastionState;
    private String created;
    private String description;
    private int edgeCount;
    private String effective;
    private int id;
    private String logicalId;
    private String modified;
    private EdgeModule modules[];
    private String name;
    private String schemaVersion;
    private String version;
    private int isStaging;
}
