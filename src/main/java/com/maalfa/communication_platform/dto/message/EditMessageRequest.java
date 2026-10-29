package com.maalfa.communication_platform.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditMessageRequest {

    @NotBlank
    private String content;
}
