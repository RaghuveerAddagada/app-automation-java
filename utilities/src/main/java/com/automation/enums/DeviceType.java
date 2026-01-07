package com.automation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceType {
    VIRTUAL("virtual"),
    REAL("real");

    private final String value;
}
