package com.maalfa.communication_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "privileges", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "CREATE_GROUP", "DELETE_ANY_MESSAGE", "BAN_USER", "MANAGE_USERS"
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Privilege)) return false;
        Privilege that = (Privilege) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
