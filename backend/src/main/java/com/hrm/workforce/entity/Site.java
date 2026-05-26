package com.hrm.workforce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @Column(nullable = false, length = 255)
    private String location;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
