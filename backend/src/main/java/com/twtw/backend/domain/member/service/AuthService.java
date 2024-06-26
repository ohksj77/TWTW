package com.twtw.backend.domain.member.service;

import com.twtw.backend.config.security.jwt.TokenProvider;
import com.twtw.backend.domain.member.dto.request.DeviceTokenRequest;
import com.twtw.backend.domain.member.dto.request.MemberSaveRequest;
import com.twtw.backend.domain.member.dto.request.OAuthRequest;
import com.twtw.backend.domain.member.dto.request.TokenRequest;
import com.twtw.backend.domain.member.dto.response.AfterLoginResponse;
import com.twtw.backend.domain.member.dto.response.TokenDto;
import com.twtw.backend.domain.member.entity.AuthStatus;
import com.twtw.backend.domain.member.entity.DeviceToken;
import com.twtw.backend.domain.member.entity.Member;
import com.twtw.backend.domain.member.entity.RefreshToken;
import com.twtw.backend.domain.member.exception.NicknameExistsException;
import com.twtw.backend.domain.member.exception.RefreshTokenInfoMismatchException;
import com.twtw.backend.domain.member.exception.RefreshTokenValidationException;
import com.twtw.backend.domain.member.mapper.MemberMapper;
import com.twtw.backend.domain.member.repository.MemberRepository;
import com.twtw.backend.domain.member.repository.RefreshTokenRepository;
import com.twtw.backend.global.exception.AuthorityException;
import com.twtw.backend.global.exception.EntityNotFoundException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenProvider tokenProvider;
    private final MemberMapper memberMapper;

    public AuthService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenProvider tokenProvider,
            MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.memberMapper = memberMapper;
    }

    /*
     * 1. after social SignUp
     * 2. 기본 정보 기입
     * 3. OAuth Info 저장 -> kakao , apple enum 구분
     * 4. 저장
     * 5. 토큰(jwt) 발급
     * */

    @Transactional
    public AfterLoginResponse saveMember(MemberSaveRequest request) {
        validateNickname(request);

        Member member = memberMapper.toMemberEntity(request);

        memberRepository.save(member);

        UsernamePasswordAuthenticationToken credit = tokenProvider.makeCredit(member);
        TokenDto tokenDto = saveRefreshToken(credit, member.getId().toString());

        return new AfterLoginResponse(AuthStatus.SIGNIN, tokenDto);
    }

    private void validateNickname(final MemberSaveRequest request) {
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new NicknameExistsException();
        }
    }

    /*
     * 1.로그인(Social) 후의 토큰 발급
     * 2.JWT 토큰 발급 -> OAuth 정보 (clientId , AuthType)으로 진행
     *
     * */
    public AfterLoginResponse getTokenByOAuth(OAuthRequest request) {
        String clientId = request.getToken();

        Optional<Member> member =
                memberRepository.findByOAuthIdAndAuthType(clientId, request.getAuthType());

        return member.map(this::getAfterLoginResponse).orElseGet(AfterLoginResponse::ofSignup);
    }

    private AfterLoginResponse getAfterLoginResponse(final Member member) {
        UsernamePasswordAuthenticationToken credit = tokenProvider.makeCredit(member);
        TokenDto tokenDto = saveRefreshToken(credit, member.getId().toString());
        return new AfterLoginResponse(AuthStatus.SIGNIN, tokenDto);
    }

    /*
     * Token 재발급
     * 1. refreshToken validate 확인
     * 2. refreshToken DB 정보 확인
     * 3. 토큰 만들어서 반환
     * */

    public TokenDto refreshToken(TokenRequest tokenRequest) {
        final String refreshToken = tokenRequest.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RefreshTokenValidationException();
        }

        Optional<Authentication> authentication =
                tokenProvider.getAuthentication(tokenRequest.getAccessToken());

        return authentication
                .map(auth -> getTokenDto(auth, refreshToken))
                .orElseThrow(AuthorityException::new);
    }

    private TokenDto getTokenDto(final Authentication auth, final String refreshToken) {
        String userName = auth.getName();
        if (!getRefreshTokenValue(userName).equals(refreshToken)) {
            throw new RefreshTokenInfoMismatchException();
        }
        return saveRefreshToken(auth, auth.getName());
    }

    public String getRefreshTokenValue(String tokenKey) {
        return refreshTokenRepository
                .findByTokenKey(tokenKey)
                .orElseThrow(EntityNotFoundException::new)
                .getTokenValue();
    }

    public TokenDto saveRefreshToken(Authentication authentication, String userName) {
        TokenDto token = tokenProvider.createToken(authentication);
        refreshTokenRepository.save(new RefreshToken(userName, token.getRefreshToken()));

        return token;
    }

    public Member getMemberByJwt() {
        String memberIdValue = getMemberIdValue();

        UUID id = UUID.fromString(memberIdValue);

        return memberRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public String getMemberIdValue() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    public void updateDeviceToken(DeviceTokenRequest request) {
        Member member = getMemberByJwt();
        DeviceToken deviceToken = new DeviceToken(request.getDeviceToken());
        member.updateDeviceToken(deviceToken);
    }
}
