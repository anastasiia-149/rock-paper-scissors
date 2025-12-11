package com.techub.rps.control.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class User {
    String username;
    Instant createdAt;
}
