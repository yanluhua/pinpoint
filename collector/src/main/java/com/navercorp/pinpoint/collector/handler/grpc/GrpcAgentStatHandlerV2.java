/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GrpcAgentStatHandlerV2 implements SimpleHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private GrpcAgentStatMapper agentStatMapper;

    @Autowired
    private GrpcAgentStatBatchMapper agentStatBatchMapper;

    @Autowired(required = false)
    private List<AgentStatService> agentStatServiceList = Collections.emptyList();

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple data={}", data);
        }

        if (data instanceof PAgentStat) {
            handleAgentStat((PAgentStat) data);
        } else if (data instanceof PAgentStatBatch) {
            handleAgentStatBatch((PAgentStatBatch) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleAgentStat(PAgentStat agentStat) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStat={}", agentStat);
        }
        final AgentStatBo agentStatBo = this.agentStatMapper.map(agentStat);
        if (agentStatBo == null) {
            return;
        }

        for (AgentStatService agentStatService : agentStatServiceList) {
            agentStatService.save(agentStatBo);
        }
    }

    private void handleAgentStatBatch(PAgentStatBatch tAgentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStatBatch={}", tAgentStatBatch);
        }

        AgentHeaderFactory.Header header = ServerContext.getAgentInfo();
        final AgentStatBo agentStatBo = this.agentStatBatchMapper.map(tAgentStatBatch, header);
        if (agentStatBo == null) {
            return;
        }

        for (AgentStatService agentStatService : agentStatServiceList) {
            agentStatService.save(agentStatBo);
        }
    }
}