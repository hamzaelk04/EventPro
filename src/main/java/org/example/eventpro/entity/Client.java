package org.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.eventpro.entity.abstracts.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class Client extends User {
    private String phone;

    @OneToMany(mappedBy = "client")
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}
