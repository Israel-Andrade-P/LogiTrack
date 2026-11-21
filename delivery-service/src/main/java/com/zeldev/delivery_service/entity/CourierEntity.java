package com.zeldev.delivery_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;
}
