package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mood")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mood {

    @Id
    private String name;
}