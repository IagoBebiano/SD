package com.example.servingwebcontent.hackernews;

public class HackerNewsItemResponse {
    private String title;
    private String url;
    private Integer id;

    public Integer getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}