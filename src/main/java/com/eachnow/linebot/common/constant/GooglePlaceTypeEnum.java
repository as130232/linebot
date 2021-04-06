package com.eachnow.linebot.common.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum GooglePlaceTypeEnum {
    AIRPORT("機場"),
    AMUSEMENT_PARK("遊樂園"),
    AQUARIUM("水族館"),
    ART_GALLERY("美術館"),
    ATM("ATM"),
    BAKERY("麵包店"),
    BANK("銀行"),
    BAR("酒吧"),
    BEAUTY_SALON("美容院"),
    BOOK_STORE("書店"),
    BUS_STATION("公車站"),
    CAFE("咖啡店"),
    CAR_RENTAL("汽車出租"),
    CAR_REPAIR("汽車修理"),
    CAR_WASH("洗車"),
    CASINO("賭場"),
    CHURCH("教會"),
    CITY_HALL("市政府"),
    CLOTHING_STORE("服飾店"),
    CONVENIENCE_STORE("便利商店"),
    DENTIST("牙醫"),
    DEPARTMENT_STORE("百貨公司"),
    DOCTOR("醫生"),
    DRUGSTORE("藥房"),
    ELECTRONICS_STORE("電子商店"),
    EMBASSY("大使館"),
    FIRE_STATION("消防局"),
    FLORIST("花店"),
    FURNITURE_STORE("家具店"),
    GAS_STATION("加油站"),
    GYM("健身房"),
    HAIR_CARE("理髮廳"),
    HARDWARE_STORE("五金行"),
    HOSPITAL("醫院"),
    JEWELRY_STORE("珠寶店"),
    LAUNDRY("洗衣店"),
    LAWYER("律師所"),
    LIBRARY("圖書館"),
    LIGHT_RAIL_STATION("輕軌"),
    LIQUOR_STORE("酒商"),
    LOCAL_GOVERNMENT_OFFICE("政府"),
    LOCKSMITH("開鎖"),
    LODGING("住宿"),
    MOSQUE("清真寺"),
    MOVIE_RENTAL("影片出租"),
    MOVIE_THEATER("電影院"),
    MUSEUM("博物館"),
    NIGHT_CLUB("夜店"),
    PAINTER("畫廊"),
    PARK("公園"),
    PARKING("停車場"),
    PET_STORE("寵物店"),
    PHARMACY("藥局"),
    PHYSIOTHERAPIST("物理治療"),
    POLICE("警察局"),
    POST_OFFICE("郵局"),
    PRIMARY_SCHOOL("小學"),
    RESTAURANT("餐廳"),
    SCHOOL("學校"),
    SECONDARY_SCHOOL("國中"),
    SHOE_STORE("鞋店"),
    SHOPPING_MALL("百貨中心"),
    SPA("SPA"),
    STADIUM("體育場"),
    STORE("商店"),
    SUBWAY_STATION("地鐵"),
    SUPERMARKET("超級市場"),
    TAXI_STAND("計程車"),
    TOURIST_ATTRACTION("旅遊景點"),
    TRAIN_STATION("火車站"),
    TRAVEL_AGENCY("旅行社"),
    UNIVERSITY("大學"),
    VETERINARY_CARE("獸醫"),
    ZOO("動物園"),
    ;

    private String name;

    GooglePlaceTypeEnum(String name) {
        this.name = name;
    }

    public static GooglePlaceTypeEnum parse(String name) {
        Optional<GooglePlaceTypeEnum> optional = Arrays.stream(GooglePlaceTypeEnum.values())
                .filter(typeEnum -> typeEnum.getName().contains(name)).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}