package com.twtw.backend.domain.group.entity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;
    private String groupImage;

    @OneToMany(mappedBy = "group")
    private List<GroupMember> groupMembers = new ArrayList<>();
}