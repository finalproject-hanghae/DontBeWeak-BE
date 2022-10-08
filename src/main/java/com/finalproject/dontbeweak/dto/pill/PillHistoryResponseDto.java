package com.finalproject.dontbeweak.dto.pill;

import com.finalproject.dontbeweak.model.pill.Pill;
import com.finalproject.dontbeweak.model.pill.PillHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
//@AllArgsConstructor
public class PillHistoryResponseDto {

    @Getter
    @Builder
    private static class Body {
        private LocalDateTime usedAt;
        private String productName;
        private String customColor;
        private boolean done;
    }

    public ResponseEntity<?> done(PillHistory pillHistory) {
        Body body = Body.builder()
                .usedAt(pillHistory.getUsedAt())
                .productName(pillHistory.getProductName())
                .customColor(pillHistory.getPill().getCustomColor())
                .done(pillHistory.getPill().getDone())
                .build();
        return ResponseEntity.ok(body);
    }

    public ResponseEntity<?> undone(PillHistoryRequestDto pillHistoryRequestDto, Pill pill) {
        Body body = Body.builder()
                .usedAt(pillHistoryRequestDto.getUsedAt())
                .productName(pillHistoryRequestDto.getProductName())
                .customColor(pill.getCustomColor())
                .done(pill.getDone())
                .build();
        return ResponseEntity.ok(body);
    }
}
