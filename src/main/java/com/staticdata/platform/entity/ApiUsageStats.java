package com.staticdata.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * API Usage Statistics Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "api_usage_stats")
public class ApiUsageStats extends BaseEntity {
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "api_path", nullable = false)
    private String apiPath;
    
    @Column(name = "http_method", nullable = false)
    private String httpMethod;
    
    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;
    
    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;
    
    @Column(name = "client_ip")
    private String clientIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "request_size")
    private Long requestSize;
    
    @Column(name = "response_size")
    private Long responseSize;
}