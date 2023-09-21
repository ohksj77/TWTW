package com.twtw.backend.domain.path.client;

import com.twtw.backend.domain.path.dto.client.car.SearchCarPathRequest;
import com.twtw.backend.domain.path.dto.client.car.SearchCarPathResponse;
import com.twtw.backend.global.client.MapClient;
import com.twtw.backend.global.exception.WebClientResponseException;
import com.twtw.backend.global.properties.NaverProperties;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class SearchCarPathClient implements MapClient<SearchCarPathRequest, SearchCarPathResponse> {
    private final WebClient webClient;
    private final NaverProperties naverProperties;

    public SearchCarPathClient(final WebClient webClient, final NaverProperties naverProperties) {
        this.webClient = webClient;
        this.naverProperties = naverProperties;
    }

    /*상세 검색을 위한 변경 필요*/
    private URI getPathUri(final SearchCarPathRequest request, final UriBuilder uriBuilder) {

        final UriBuilder builder =
                uriBuilder
                        .path(naverProperties.getUrl())
                        .path("driving")
                        .queryParam("start", request.getStart())
                        .queryParam("goal", request.getEnd())
                        .queryParam("option", request.getOption().toSmallOption())
                        .queryParam("cartype", request.getCar())
                        .queryParam("fueltype", request.getFuel().toSmallFuel());

        String wayPoints = request.getWay();
        if (wayPoints.isEmpty()) {
            return builder.build();
        }

        return builder.queryParam("waypoints", wayPoints).build();
    }

    @Override
    public SearchCarPathResponse request(final SearchCarPathRequest request) {
        return webClient
                .get()
                .uri(uri -> getPathUri(request, uri))
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .header(naverProperties.getHeaderClientId(), naverProperties.getId())
                .header(naverProperties.getHeaderClientSecret(), naverProperties.getSecret())
                .retrieve()
                .bodyToMono(SearchCarPathResponse.class)
                .blockOptional()
                .orElseThrow(WebClientResponseException::new);
    }
}
