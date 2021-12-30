package com.stackabuse.multitenantjpaservice.entity;

public interface TimeAudit {

    String getCreatedBy();
    void setCreatedBy(String createdBy);

    Long getCreatedOn();
    void setCreatedOn(Long createdOn);

    String getLastUpdatedBy();
    void setLastUpdatedBy(String lastUpdatedBy);

    Long getLastUpdatedOn();
    void setLastUpdatedOn(Long lastUpdatedOn);
}
