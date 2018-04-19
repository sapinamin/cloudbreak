package com.sequenceiq.cloudbreak.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.core.flow2.Flow2Handler;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StateStatus;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;

@Service
public class RetryProvisionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryProvisionService.class);

    @Autowired
    private Flow2Handler flow2Handler;

    @Autowired
    private FlowLogRepository flowLogRepository;

    public void retry(Stack stack) {
        List<FlowLog> flowLogs = flowLogRepository.findAllByStackIdOrderedByCreatedDesc(stack.getId());
        if (isFlowPending(flowLogs)) {
            LOGGER.debug("Retry cannot be performed, because there is already an active flow. stackId: {}", stack.getId());
            return;
        }

        Optional<FlowLog> failedFlowLog = getMostRecentFailedLog(flowLogs);

        if (!failedFlowLog.isPresent()) {
            LOGGER.info("Retry cannot be performed, provision finished successfully. stackId: {}", stack.getId());
            return;
        }

        flow2Handler.restartFlow(failedFlowLog.get());
    }

    private Optional<FlowLog> getMostRecentFailedLog(List<FlowLog> flowLogs) {
        return flowLogs.stream()
                    .filter(log -> StateStatus.FAILED.equals(log.getStateStatus()))
                    .findFirst();
    }

    private boolean isFlowPending(List<FlowLog> flowLogs) {
        return flowLogs.stream()
                .map(FlowLog::getStateStatus)
                .anyMatch(StateStatus.PENDING::equals);
    }
}
