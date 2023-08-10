package com.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 开关量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwitchData {
    private String deviceId;
    private Boolean status;
    private Long timestamp;
}
