package com.maalfa.communication_platform.dto.group;

import com.maalfa.communication_platform.enums.GroupRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddGroupMemberRequest {

    @NotNull
    private Long userId;

    // Defaults to MEMBER (least-privilege) if omitted.
    @Builder.Default
    private GroupRole role = GroupRole.MEMBER;
}
