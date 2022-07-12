package com.eachnow.linebot.common.po.ig;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ProfilePage {

    private User user;

    @JsonProperty("logging_page_id")
    private String loggingPageId;

    @JsonProperty("show_suggested_profiles")
    private boolean showSuggestedProfiles;

    private Graphql graphql;

}