package com.eachnow.linebot.domain.service.handler.location;

import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.domain.service.handler.DefaultHandler;
import com.eachnow.linebot.domain.service.handler.location.impl.GoogleLocationHandler;
import com.eachnow.linebot.domain.service.handler.location.impl.RestaurantLocationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationHandlerFactory {
    public static GooglePlaceTypeEnum type;

    @Autowired
    private RestaurantLocationHandler restaurantLocationHandler;
    @Autowired
    private GoogleLocationHandler googleLocationHandler;
    @Autowired
    private DefaultHandler defaultHandler;

    public LocationHandler getLocationHandler() {
        LocationHandler locationHandler;
        if (type == null)
            return defaultHandler;
        if (GooglePlaceTypeEnum.RESTAURANT.equals(type)) {
            locationHandler = restaurantLocationHandler;
        } else {
            locationHandler = googleLocationHandler;
        }
        return locationHandler;
    }

}
