package com.zooting.api.global.redis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Openvidu Token Response", description = "Openvidu 토큰 응답")
public record OpenviduTokenRes(
        @Schema(description = "Websocket Message Type")
        String type,
        @Schema(description = "Openvidu 토큰")
        String token

) {
}