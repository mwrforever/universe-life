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
public class AppealHandledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long appealId;

    private Long orderId;

    private Long taskId;

    private Long appellantId;

    private Integer appealType;

    private Integer result;

    private Long handlerId;

     /**
      * 裁决后的应付金额（分，可选）
      */
     private Long payableAmountCents;

     /**
      * 已支付金额（分，可选）
      */
     private Long paidAmountCents;

    private LocalDateTime handledAt;
}
