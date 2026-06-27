package com.immortals.auth.service.domain.entity;


import com.immortals.auth.service.domain.Auditable;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.Set;

@Entity
@SequenceGenerator(name = "id_seq", sequenceName = "countries_seq", schema = "user_auth", allocationSize = 50)
@Table(
        name = "countries",
        schema = "user_auth",
        indexes = {
                @Index(name = "idx_countries_name", columnList = "country_name"),
                @Index(name = "idx_countries_code", columnList = "country_code")
        }
)
@Audited
public class Country extends Auditable {

    private static final long serialVersionUID = 1L;

    @Column(name = "country_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "country_code", nullable = false, unique = true, length = 5)
    private String code;

    @OneToMany(mappedBy = "country")
    private transient Set<State> states;

    @Column(name = "active_ind", nullable = false)
    private Boolean activeInd;
}
