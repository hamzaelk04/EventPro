package org.example.eventpro.repository;

import org.example.eventpro.entity.AgentControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentControlRepository extends JpaRepository<AgentControl, UUID> {
}
