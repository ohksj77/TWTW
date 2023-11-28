package com.twtw.backend.domain.place.dto.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SurroundPlaceRequest {
    private Double x;
    private Double y;
    private Integer page;
}
