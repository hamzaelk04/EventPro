package org.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.eventpro.entity.abstracts.User;

@Entity
@Table(name = "agents_control")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class AgentControl extends User {
}
