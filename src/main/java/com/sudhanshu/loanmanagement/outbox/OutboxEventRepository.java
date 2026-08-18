package com.sudhanshu.loanmanagement.outbox;

import com.sudhanshu.loanmanagement.outbox.OutboxEvent.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            SELECT e FROM OutboxEvent e
            WHERE e.status = :status
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status);
}
