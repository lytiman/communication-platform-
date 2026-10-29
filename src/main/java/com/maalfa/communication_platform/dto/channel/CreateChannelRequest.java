package com.maalfa.communication_platform.dto.channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChannelRequest {
    @NotNull
    private Long groupId;
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 500)
    private String description;
    @Builder.Default
    private boolean isPrivate = false;

}
