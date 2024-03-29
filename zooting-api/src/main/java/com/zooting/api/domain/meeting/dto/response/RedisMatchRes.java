package com.zooting.api.domain.meeting.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "매칭완료 Response", description = "매칭 완료 시 응답")
public record RedisMatchRes(
        @Schema(description = "매칭된 방 ID")
        String roomId

) {
}
