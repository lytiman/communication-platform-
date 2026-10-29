package com.maalfa.communication_platform.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private String avatarUrl;

    // Defaults to true (private/invite-only) at the service layer if
    // omitted — mirrors the safer default used on the Group entity.
    private Boolean isPrivate;
}
