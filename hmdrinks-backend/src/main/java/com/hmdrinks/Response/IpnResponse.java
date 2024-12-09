package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IpnResponse {
    @JsonProperty("RspCode")
    private String responseCode;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("Note") // Thêm trường Note, có thể là null
    private String note;
}
