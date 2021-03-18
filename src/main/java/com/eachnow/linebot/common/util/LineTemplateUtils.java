package com.eachnow.linebot.common.util;

import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;

import java.net.URI;
import java.util.Arrays;


public class LineTemplateUtils {


    public static Message getLocationButtonsTemplate() {
        String text = "Please tell me where you are?";
        URI uri = URI.create("https://line.me/R/nv/location");
        ButtonsTemplate template = new ButtonsTemplate(null, null, text, Arrays.asList(
                new URIAction("Send my location", uri, new URIAction.AltUri(uri))));
        return new TemplateMessage(text, template);
    }
}
