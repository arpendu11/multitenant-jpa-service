package com.stackabuse.multitenantjpaservice.entity;

public interface TimeAudit {

    Long getCreatedBy();
    void setCreatedBy(String createdBy);

    Long getCreatedOn();
    void setCreatedOn(Long createdOn);

    Long getLastUpdatedBy();
    void setLastUpdatedBy(String lastUpdatedBy);

    Long getLastUpdatedOn();
    void setLastUpdatedOn(Long lastUpdatedOn);
}
