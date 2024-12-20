package com.twtw.backend.domain.friend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.twtw.backend.domain.friend.dto.request.FriendRequest;
import com.twtw.backend.domain.friend.dto.request.FriendUpdateRequest;
import com.twtw.backend.domain.friend.dto.response.FriendResponse;
import com.twtw.backend.domain.friend.entity.Friend;
import com.twtw.backend.domain.friend.entity.FriendStatus;
import com.twtw.backend.domain.friend.repository.FriendCommandRepository;
import com.twtw.backend.domain.friend.repository.FriendQueryRepository;
import com.twtw.backend.domain.member.entity.AuthType;
import com.twtw.backend.domain.member.entity.Member;
import com.twtw.backend.domain.member.entity.OAuth2Info;
import com.twtw.backend.domain.member.repository.MemberRepository;
import com.twtw.backend.support.service.LoginTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.UUID;

@DisplayName("FriendService의")
class FriendServiceTest extends LoginTest {

    @Autowired private FriendService friendService;

    @Autowired
    @Qualifier("fakeFriendQueryRepository")
    private FriendQueryRepository friendQueryRepository;

    @Autowired
    @Qualifier("fakeFriendCommandRepository")
    private FriendCommandRepository friendCommandRepository;

    @Autowired private MemberRepository memberRepository;

    @Test
    @DisplayName("요청 추가가 수행되는가")
    void addRequest() {
        // given
        final UUID id =
                memberRepository
                        .save(
                                new Member(
                                        "abc123",
                                        "12",
                                        new OAuth2Info("123321", AuthType.APPLE),
                                        "deviceToken"))
                        .getId();

        // when
        friendService.addRequest(new FriendRequest(id));

        // then
        final List<Friend> result =
                friendQueryRepository.findByMemberAndFriendStatus(
                        loginUser, FriendStatus.REQUESTED);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("상태 업데이트가 수행되는가")
    void updateStatus() {
        // given
        final Member toMember =
                memberRepository.save(
                        new Member(
                                "1", "12", new OAuth2Info("123", AuthType.APPLE), "deviceToken"));
        final Friend friend = friendCommandRepository.save(new Friend(loginUser, toMember));

        // when
        final FriendStatus status = FriendStatus.ACCEPTED;
        friendService.updateStatus(new FriendUpdateRequest(toMember.getId(), status));

        // then
        final Friend result =
                friendQueryRepository
                        .findByTwoMemberId(
                                friend.getToMember().getId(), friend.getFromMember().getId())
                        .orElseThrow();
        assertThat(result.getFriendStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("친구 목록 조회가 수행되는가")
    void getFriends() {
        // given
        final Member toMember =
                memberRepository.save(
                        new Member(
                                "1", "12", new OAuth2Info("123", AuthType.APPLE), "deviceToken"));
        friendCommandRepository.save(new Friend(loginUser, toMember));
        friendService.updateStatus(
                new FriendUpdateRequest(toMember.getId(), FriendStatus.ACCEPTED));

        // when
        final List<FriendResponse> result = friendService.getFriends();

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("상태를 통한 친구 조회가 수행되는가")
    void getFriendsByStatus() {
        // given
        final Member toMember =
                memberRepository.save(
                        new Member(
                                "1", "12", new OAuth2Info("123", AuthType.APPLE), "deviceToken"));
        friendCommandRepository.save(new Friend(loginUser, toMember));

        // when
        final List<FriendResponse> result =
                friendService.getFriendsByStatus(FriendStatus.REQUESTED);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("닉네임을 통한 친구 조회가 수행되는가")
    void getFriendByNickname() {
        // given
        final String nickname = "1";
        final Member toMember =
                memberRepository.save(
                        new Member(
                                nickname,
                                "12",
                                new OAuth2Info("123", AuthType.APPLE),
                                "deviceToken"));
        final Friend expected = friendCommandRepository.save(new Friend(loginUser, toMember));
        expected.updateStatus(FriendStatus.ACCEPTED);

        // when
        final List<FriendResponse> result = friendService.getFriendByNickname(nickname);

        // then
        assertThat(result).hasSize(1);
    }
}
