package com.hmdrinks.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvgRatingProduct {
    private int proId;
    private double avgRating;
}
