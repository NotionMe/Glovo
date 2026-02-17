package com.glovo.delivery.dto;

import com.glovo.delivery.model.Point;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class UpdateLocationRequest {

    @NotNull(message = "Location is required")
    @Valid
    private Point location;

    public UpdateLocationRequest() {
    }

    public UpdateLocationRequest(Point location) {
        this.location = location;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
