package com.twtw.backend.domain.plan.dto.response;

import com.twtw.backend.domain.plan.dto.client.PlaceDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlanDestinationResponse {
    private List<PlaceDetails> results;
    private Boolean isLast;
}