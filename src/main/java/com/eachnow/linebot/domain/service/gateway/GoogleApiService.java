package com.eachnow.linebot.domain.service.gateway;

import com.eachnow.linebot.common.constant.GooglePlaceTypeEnum;
import com.eachnow.linebot.common.po.google.map.ResultLocationPO;

public interface GoogleApiService {

    /**
     * 翻譯
     * @param text 翻譯文字
     * @param lang 語系(LanguageEnum)
     * @return
     */
    public String translate(String text, String lang);
    /**
     * 取得地區資訊
     *
     * @param latitude  緯度
     * @param longitude 經度
     * @param type      GooglePlaceTypeEnum
     * @param language  語言
     */
    public ResultLocationPO getLocation(String latitude, String longitude, GooglePlaceTypeEnum type, String language);

    public String parseMapUrl(String lat, String lng, String placeId);

    public String parseMapPictureUrl(String photoreference);
}
