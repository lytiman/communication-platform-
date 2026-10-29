package com.maalfa.communication_platform.dto.channel;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Partial update — all fields optional/nullable. Service layer should
 * only overwrite a field if it's non-null in this request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateChannelRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean isPrivate;
}
