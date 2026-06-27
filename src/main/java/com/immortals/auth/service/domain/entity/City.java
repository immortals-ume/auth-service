package com.immortals.auth.service.domain.entity;


import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;

import org.hibernate.envers.Audited;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "cities_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "cities",
        schema = "user_auth",
        indexes = {
                @Index(name = "idx_cities_name", columnList = "city_name"),
                @Index(name = "idx_cities_state_id", columnList = "state_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cities_name_state", columnNames = {"city_name", "state_id"})
        }
)
@Audited
public class City extends Auditable {

    private static final long serialVersionUID = 1L;

    @Column(name = "city_name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "active_ind", nullable = false)
    private Boolean activeInd;
}
