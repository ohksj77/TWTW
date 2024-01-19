package com.twtw.backend.domain.group.entity;

import com.twtw.backend.domain.member.entity.Member;
import com.twtw.backend.domain.place.entity.Coordinate;
import com.twtw.backend.global.audit.AuditListener;
import com.twtw.backend.global.audit.Auditable;
import com.twtw.backend.global.audit.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Where(clause = "deleted_at is null and group_invite_code != 'EXPIRED'")
@EntityListeners(AuditListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember implements Auditable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded private Coordinate coordinate;

    private Boolean isShare;

    @Enumerated(EnumType.STRING)
    private GroupInviteCode groupInviteCode;

    @Setter
    @Embedded
    @Column(nullable = false)
    private BaseTime baseTime;

    public GroupMember(Group group, Member member) {
        this.group = group;
        this.member = member;
        this.isShare = true;
        member.addGroupMember(this);
        this.groupInviteCode = GroupInviteCode.REQUESTED;
    }

    public void share() {
        this.isShare = true;
    }

    public void unShare() {
        this.isShare = false;
    }

    public void acceptInvite() {
        if (isRequestNotExpired()) {
            this.groupInviteCode = GroupInviteCode.ACCEPTED;
            return;
        }
        remove();
    }

    private void remove() {
        this.groupInviteCode = GroupInviteCode.EXPIRED;
        this.member.removeGroupMember(this);
        this.group.removeMember(this);
    }

    public UUID getGroupId() {
        return this.group.getId();
    }

    public Coordinate getCoordinate() {
        if (isShare) {
            return coordinate;
        }
        return Coordinate.empty();
    }

    public void updateCoordinate(final Double longitude, final Double latitude) {
        if (isShare) {
            this.coordinate = new Coordinate(longitude, latitude);
        }
    }

    public boolean isSameMember(final Member member) {
        return this.member.getId().equals(member.getId());
    }

    public boolean isLeader() {
        return this.group.getLeaderId().equals(this.member.getId());
    }

    public boolean isAccepted() {
        return this.groupInviteCode == GroupInviteCode.ACCEPTED;
    }

    private boolean isRequestNotExpired() {
        return this.baseTime == null
                || (this.groupInviteCode == GroupInviteCode.REQUESTED
                        && this.baseTime
                                .getCreatedAt()
                                .isAfter(LocalDateTime.now().minusMinutes(30L)));
    }
}
