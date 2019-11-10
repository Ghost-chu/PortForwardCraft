package com.mcsunnyside.portforwardcraft;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionContainer {
    private String Ahost;
    private String Auser;
    private String Apass;
    private String Aport;
    private String Aforward;
    private String Bforward;
    private String mode;
}
