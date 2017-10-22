package com.sequenceiq.cloudbreak.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "Stack", uniqueConstraints = @UniqueConstraint(columnNames = {"account", "name"}))
public class StackMinimal implements ProvisionEntity {

    @Id
    private Long id;

    private String name;

    private String owner;

    @OneToOne(mappedBy = "stack")
    private ClusterMinimal cluster;

    @Column(columnDefinition = "TEXT")
    private String cloudPlatform;

    @Column(columnDefinition = "TEXT")
    private String platformVariant;

    @OneToOne
    private OrchestratorMinimal orchestrator;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ClusterMinimal getCluster() {
        return cluster;
    }

    public void setCluster(ClusterMinimal cluster) {
        this.cluster = cluster;
    }

    public OrchestratorMinimal getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(OrchestratorMinimal orchestrator) {
        this.orchestrator = orchestrator;
    }

    public String getPlatformVariant() {
        return platformVariant;
    }

    public void setPlatformVariant(String platformVariant) {
        this.platformVariant = platformVariant;
    }

    public String getCloudPlatform() {
        return cloudPlatform;
    }

    public void setCloudPlatform(String cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }
}