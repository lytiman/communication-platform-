package com.maalfa.communication_platform.dto.user;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Partial update — all fields optional/nullable. Service layer should
 * only overwrite a field if it's non-null in this request (PATCH-style,
 * not PUT-style full replacement).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    private String avatarUrl;
}
