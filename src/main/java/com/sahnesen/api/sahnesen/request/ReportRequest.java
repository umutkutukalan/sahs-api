package com.sahnesen.api.sahnesen.request;

import com.sahnesen.api.sahnesen.enums.ReportType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private Long targetId;
    private ReportType reportType;
    private String reason;
}
