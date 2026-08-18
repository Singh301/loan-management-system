package com.sudhanshu.loanmanagement.outbox;

import com.sudhanshu.loanmanagement.outbox.OutboxEvent.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls pending outbox events and marks them processed.
 * In production, this would publish to Kafka/RabbitMQ or call external webhooks.
 * Current implementation logs and marks PROCESSED (stub for future integrations).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:10000}")
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pending =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return;
        }

        log.debug("Outbox processor found {} pending events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // Placeholder: publish to message broker / webhook
                log.info("Publishing outbox event id={}, type={}, aggregate={}:{}",
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateType(),
                        event.getAggregateId());

                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                event.setLastError(null);
            } catch (Exception ex) {
                int retries = event.getRetryCount() == null ? 0 : event.getRetryCount();
                event.setRetryCount(retries + 1);
                event.setLastError(ex.getMessage());

                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxStatus.FAILED);
                    log.error("Outbox event permanently failed. id={}", event.getId(), ex);
                } else {
                    log.warn("Outbox event failed, will retry. id={}, attempt={}",
                            event.getId(), event.getRetryCount());
                }
            }
            outboxEventRepository.save(event);
        }
    }
}
