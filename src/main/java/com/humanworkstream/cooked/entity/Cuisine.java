package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cuisine")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cuisine {

    @Id
    private String name;
}