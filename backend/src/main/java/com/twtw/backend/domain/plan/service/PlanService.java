package com.twtw.backend.domain.plan.service;

import com.twtw.backend.domain.group.entity.Group;
import com.twtw.backend.domain.group.service.GroupService;
import com.twtw.backend.domain.member.entity.Member;
import com.twtw.backend.domain.member.service.AuthService;
import com.twtw.backend.domain.place.entity.Place;
import com.twtw.backend.domain.place.mapper.PlaceMapper;
import com.twtw.backend.domain.plan.dto.client.SearchDestinationRequest;
import com.twtw.backend.domain.plan.dto.client.SearchDestinationResponse;
import com.twtw.backend.domain.plan.dto.request.SavePlanRequest;
import com.twtw.backend.domain.plan.dto.response.PlanDestinationResponse;
import com.twtw.backend.domain.plan.dto.response.SavePlanResponse;
import com.twtw.backend.domain.plan.entity.Plan;
import com.twtw.backend.domain.plan.mapper.PlanMapper;
import com.twtw.backend.domain.plan.repository.PlanRepository;
import com.twtw.backend.global.client.MapClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final GroupService groupService;
    private final AuthService authService;
    private final PlaceMapper placeMapper;

    private final PlanMapper planMapper;
    private final MapClient<SearchDestinationRequest, SearchDestinationResponse> destinationClient;

    public PlanDestinationResponse searchPlanDestination(final SearchDestinationRequest request) {
        final SearchDestinationResponse response = requestMapClient(request);
        return new PlanDestinationResponse(response.getDocuments(), response.getMeta().getIsEnd());
    }

    private SearchDestinationResponse requestMapClient(final SearchDestinationRequest request) {
        return destinationClient.request(request);
    }

    @Transactional
    public SavePlanResponse savePlan(final SavePlanRequest request) {
        Member member = authService.getMemberByJwt();
        Group group = groupService.getGroupEntity(request.getGroupId());
        Place place = placeMapper.toEntity(request.getPlaceDetails());
        Plan plan = new Plan(member, place, group);

        return planMapper.toPlanResponse(planRepository.save(plan));
    }
}
