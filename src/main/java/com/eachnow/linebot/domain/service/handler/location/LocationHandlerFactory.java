package com.eachnow.linebot.domain.service.handler.location;

import com.eachnow.linebot.common.constant.LocationConstants;
import com.eachnow.linebot.domain.service.handler.location.impl.RestaurantLocationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationHandlerFactory {
    public static String type;
    @Autowired
    private RestaurantLocationHandler restaurantLocationHandler;

    public LocationHandler getLocationHandler() {
        LocationHandler locationHandler = null;
        if (LocationConstants.RESTAURANT.equals(type)) {
            locationHandler = restaurantLocationHandler;
        }
        return locationHandler;
    }

    public void setType(String type) {
        this.type = type;
    }
}
