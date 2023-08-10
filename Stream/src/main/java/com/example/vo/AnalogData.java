package com.example.vo;

import lombok.*;

/**
 * 模拟量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AnalogData {
    private String sensorId;
    private Double value;
    private Long timestamp;
}
