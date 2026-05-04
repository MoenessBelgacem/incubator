package com.enicarthage.incubator.dto.request;

import lombok.Data;

@Data
public class WebRtcSignal {
    private Long applicationId;
    private String type; // offer, answer, candidate
    private Object payload; // The SDP or ICE candidate object
}
