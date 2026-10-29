package com.maalfa.communication_platform.dto.channel;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinChannelRequest {

    @NotNull
    private Long channelId;

    // Optional — only relevant if the channel is private and requires an
    // invite code rather than group-admin approval. Leave null otherwise.
    private String inviteCode;
}
