package com.twtw.backend.domain.group.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MakeGroupDto {
    private String name;
    private String groupImage;
    private String leaderId;
}
