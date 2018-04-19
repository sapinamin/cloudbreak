package com.sequenceiq.cloudbreak.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.sequenceiq.cloudbreak.core.flow2.Flow2Handler;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StateStatus;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class RetryProvisionServiceTest {

    private static final Long STACK_ID = 1L;
    private static final String FLOW_ID = "flowId";

    @InjectMocks
    private RetryProvisionService underTest;

    @Mock
    private Flow2Handler flow2Handler;

    @Mock
    private FlowLogRepository flowLogRepository;

    @Mock
    private Stack stackMock;

    @Test
    public void retryPending() {
        when(stackMock.getId()).thenReturn(STACK_ID);

        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL),
                createFlowLog("START_STATE", StateStatus.PENDING)
                );
        when(flowLogRepository.findAllByStackIdOrderedByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(0)).restartFlow(any(FlowLog.class));
    }

    private FlowLog createFlowLog(String currentState, StateStatus stateStatus) {
        return new FlowLog(STACK_ID, FLOW_ID, currentState, stateStatus);
    }

    @Test
    public void retrySuccessful() {
        when(stackMock.getId()).thenReturn(STACK_ID);

        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL),
                createFlowLog("START_STATE", StateStatus.SUCCESSFUL)
        );
        when(flowLogRepository.findAllByStackIdOrderedByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(0)).restartFlow(any(FlowLog.class));
    }

    @Test
    public void retry() {
        when(stackMock.getId()).thenReturn(STACK_ID);

        FlowLog failedState = createFlowLog("START_STATE", StateStatus.FAILED);
        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL),
                failedState
        );
        when(flowLogRepository.findAllByStackIdOrderedByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(1)).restartFlow(ArgumentMatchers.eq(failedState));
    }
}