package com.eachnow.linebot.common.util;

import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.flex.unit.*;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.Arrays;
import java.util.List;


public class LineTemplateUtils {

    public static Message getCancelTemplate(String label) {
        List<FlexComponent> headerContents = Arrays.asList(Text.builder().text(label).size(FlexFontSize.LG).weight(Text.TextWeight.BOLD).align(FlexAlign.CENTER).color("#ffffff").build());
        Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).paddingAll(FlexPaddingSize.MD).backgroundColor("#FF6B6E").build();
        FlexContainer contents = Bubble.builder().header(header).hero(null).body(null).footer(null).build();
        return FlexMessage.builder().altText(label).contents(contents).build();
    }

    public static Message getLocationButtonsTemplate(String loaction) {
        String text = "Please tell me where you are?";
        String title = null;
        if (loaction != null)
            title = "Search: " + loaction;
        URI uri = URI.create("https://line.me/R/nv/location");
        List<FlexComponent> bodyContents = Arrays.asList(
                Text.builder().text(title).weight(Text.TextWeight.BOLD).size(FlexFontSize.XL).build(),  //標頭
                Text.builder().text(text).margin(FlexMarginSize.SM).offsetTop(FlexOffsetSize.XS).build()
        );
        Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();
        List<FlexComponent> footerContents = Arrays.asList(
                Button.builder().style(Button.ButtonStyle.PRIMARY).height(Button.ButtonHeight.SMALL).action(new URIAction("Send my location", uri, new URIAction.AltUri(uri))).build()
        );
        Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(footerContents).build();
        FlexContainer contents = Bubble.builder().header(null).hero(null).body(body).footer(footer)
                .action(new URIAction("Send my location", uri, new URIAction.AltUri(uri))).build();
        return new FlexMessage("Search location", contents);
    }

}
