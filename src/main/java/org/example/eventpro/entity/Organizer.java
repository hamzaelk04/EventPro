package org.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.eventpro.entity.abstracts.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizers")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Organizer extends User {

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @OneToMany(mappedBy = "organizer")
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "organizer")
    @Builder.Default
    private List<Promotion> promotions = new ArrayList<>();

}
