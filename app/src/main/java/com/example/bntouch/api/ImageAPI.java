package com.example.bntouch.api;

public class ImageAPI {

    private String imagename;
    private String imagedata;

    public ImageAPI(String imagename, String imagedata) {
        this.imagename = imagename;
        this.imagedata = imagedata;
    }

    public String getImagename() {
        return imagename;
    }

    public  String getImagedata() {
        return imagedata;
    }
}
