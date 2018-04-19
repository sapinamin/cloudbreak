package com.sequenceiq.cloudbreak.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.StateStatus;

@EntityType(entityClass = FlowLog.class)
public interface FlowLogRepository extends CrudRepository<FlowLog, Long> {

    FlowLog findFirstByFlowIdOrderByCreatedDesc(String flowId);

    @Query("SELECT DISTINCT fl.flowId FROM FlowLog fl "
            + "WHERE (fl.stateStatus IS NULL OR fl.stateStatus = 'PENDING') AND fl.stackId = :stackId "
            + "AND fl.flowType != 'com.sequenceiq.cloudbreak.core.flow2.stack.termination.StackTerminationFlowConfig'")
    Set<String> findAllRunningNonTerminationFlowIdsByStackId(@Param("stackId") Long stackId);

    @Query("SELECT DISTINCT fl.flowId, fl.stackId, fl.cloudbreakNodeId FROM FlowLog fl WHERE fl.stateStatus IS NULL OR fl.stateStatus = 'PENDING'")
    List<Object[]> findAllNonPending();

    @Query("SELECT fl FROM FlowLog fl WHERE fl.cloudbreakNodeId = :cloudbreakNodeId AND (fl.stateStatus IS NULL OR fl.stateStatus = 'PENDING')")
    Set<FlowLog> findAllByCloudbreakNodeId(@Param("cloudbreakNodeId") String cloudbreakNodeId);

    @Query("SELECT fl FROM FlowLog fl WHERE fl.cloudbreakNodeId IS NULL AND (fl.stateStatus IS NULL OR fl.stateStatus = 'PENDING')")
    Set<FlowLog> findAllUnassigned();

    @Modifying
    @Query("DELETE FROM FlowLog fl WHERE fl.stackId IN ( SELECT st.id FROM Stack st WHERE st.stackStatus.status = 'DELETE_COMPLETED')")
    int purgeTerminatedStackLogs();

    @Modifying
    @Query("UPDATE FlowLog fl SET fl.stateStatus = :stateStatus WHERE fl.id = :id")
    void updateLastLogStatusInFlow(@Param("id") Long id, @Param("stateStatus") StateStatus stateStatus);

    List<FlowLog> findAllByStackIdOrderedByCreatedDesc(Long stackId);
}
