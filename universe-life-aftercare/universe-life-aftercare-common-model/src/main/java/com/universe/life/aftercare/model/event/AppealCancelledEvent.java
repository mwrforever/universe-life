package com.universe.life.aftercare.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long appealId;

    private Long orderId;

    private Long taskId;

    private Long appellantId;

    private Integer appealType;

    private LocalDateTime cancelledAt;
}
