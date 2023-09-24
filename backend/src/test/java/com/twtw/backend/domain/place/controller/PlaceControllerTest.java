package com.twtw.backend.domain.place.controller;

import com.twtw.backend.domain.place.dto.response.PlaceResponse;
import com.twtw.backend.domain.place.entity.CategoryGroupCode;
import com.twtw.backend.domain.place.service.PlaceService;
import com.twtw.backend.domain.plan.dto.client.PlaceDetails;
import com.twtw.backend.domain.plan.dto.response.PlanDestinationResponse;
import com.twtw.backend.support.docs.RestDocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.twtw.backend.support.docs.ApiDocsUtils.getDocumentRequest;
import static com.twtw.backend.support.docs.ApiDocsUtils.getDocumentResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PlaceController의")
@WebMvcTest(PlaceController.class)
class PlaceControllerTest extends RestDocsTest {
    @MockBean private PlaceService placeService;

    @Test
    @DisplayName("주변 장소 검색 API가 수행되는가")
    void searchSurroundPlace() throws Exception {
        final PlaceResponse expected =
                new PlaceResponse(
                        List.of(
                                new PlaceDetails(
                                        "이디야커피 안성죽산점",
                                        "435",
                                        "http://place.map.kakao.com/1562566188",
                                        "음식점 > 카페 > 커피전문점 > 이디야커피",
                                        "경기 안성시 죽산면 죽산리 118-3",
                                        "경기 안성시 죽산면 죽주로 287-1",
                                        CategoryGroupCode.CE7,
                                        "127.426865189637",
                                        "37.0764635355795"),
                                new PlaceDetails(
                                        "카페 온마이마인드",
                                        "345",
                                        "https://place.map.kakao.com/1625295668",
                                        "음식점 > 카페",
                                        "경기 안성시 죽산면 죽산리 414",
                                        "경기 안성시 죽산면 죽산초교길 36-4",
                                        CategoryGroupCode.CE7,
                                        "127.420430538256",
                                        "37.0766874564297")),
                        false);
        given(placeService.searchSurroundPlace(any())).willReturn(expected);

        // when
        final ResultActions perform =
                mockMvc.perform(
                        get("/places/surround")
                                .queryParam("query", "이디야 안성")
                                .queryParam("x", "127.426")
                                .queryParam("y", "37.0764")
                                .queryParam("page", "1")
                                .queryParam("categoryGroupCode", "CE7")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(
                                        "Authorization",
                                        "Bearer wefa3fsdczf32.gaoiuergf92.gb5hsa2jgh"));

        // then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.isLast").isBoolean());

        // docs
        perform.andDo(print())
                .andDo(
                        document(
                                "get search surround place",
                                getDocumentRequest(),
                                getDocumentResponse()));
    }
}
