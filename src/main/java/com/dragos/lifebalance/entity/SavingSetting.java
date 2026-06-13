package com.dragos.lifebalance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_saving_settings")
@Getter
@Setter
public class SavingSetting {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false)
    private Double percentage = 10.0;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}