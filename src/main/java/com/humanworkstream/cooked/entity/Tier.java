package com.humanworkstream.cooked.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A subscription tier (plan). `rank` orders tiers; higher = more access. */
@Entity
@Table(name = "tier")
@Data
@NoArgsConstructor
public class Tier {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "label")
    private String label;

    @Column(name = "rank", nullable = false)
    private int rank;
}
