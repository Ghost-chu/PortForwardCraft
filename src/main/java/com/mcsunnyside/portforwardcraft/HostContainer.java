package com.mcsunnyside.portforwardcraft;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NotNull
public class HostContainer {
    private String host;
    private String user;
    private String pass;
    private String port;
}

