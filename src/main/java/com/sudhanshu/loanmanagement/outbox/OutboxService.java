package com.sudhanshu.loanmanagement.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudhanshu.loanmanagement.outbox.OutboxEvent.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional Outbox – store events in the same DB transaction as business data.
 * A scheduler later publishes them reliably (at-least-once).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueue(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(event);
            log.debug("Outbox event enqueued. type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
