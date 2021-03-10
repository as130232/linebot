package com.eachnow.linebot.common.po.ig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

    @JsonProperty("__typename")
    private String typename;

    private long id;

    @JsonProperty("comments_disabled")
    private boolean commentsDisabled;

    private Dimensions dimensions;

    @JsonProperty("edge_media_preview_like")
    private EdgeMediaPreviewLike edgeMediaPreviewLike;

    @JsonProperty("gating_info")
    private String gatingInfo;

    @JsonProperty("media_preview")
    private String mediaPreview;

    private Owner owner;

    @JsonProperty("thumbnail_src")
    private String thumbnailSrc;

    @JsonProperty("thumbnail_resources")
    private List<ThumbnailResource> thumbnailResources;

    @JsonProperty("is_video")
    private boolean isVideo;

    private String code;

    private Date date;

    @JsonProperty("display_src")
    private String displaySrc;

    private String caption;

    private Comments comments;

    private Likes likes;

    @JsonProperty("edge_media_to_caption")
    private EdgeMediaToCaption edgeMediaToCaption;

    private String text;

    @JsonProperty("edge_media_to_comment")
    private EdgeMediaToComment edgeMediaToComment;

    @JsonProperty("taken_at_timestamp")
    private int takenAtTimestamp;

    @JsonProperty("edge_liked_by")
    private EdgeLikedBy edgeLikedBy;

    @JsonProperty("video_views")
    private int videoViews;

    private String shortcode;

    @JsonProperty("display_url")
    private String displayUrl;

    @JsonProperty("video_view_count")
    private int videoViewCount;

}