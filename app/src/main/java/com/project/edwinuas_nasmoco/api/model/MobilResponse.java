package com.project.edwinuas_nasmoco.api.model;

import java.util.List;

public class MobilResponse {
    private boolean success;
    private String message;
    private List<Mobil> data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Mobil> getData() { return data; }
}

