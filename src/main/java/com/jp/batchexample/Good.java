package com.jp.batchexample;

public class Good {
    private Long id;
    private String goodMaster;
    private String location;

    public Good(Long id, String goodMaster, String location) {
        this.id = id;
        this.goodMaster = goodMaster;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodMaster() {
        return goodMaster;
    }

    public void setGoodMaster(String goodMaster) {
        this.goodMaster = goodMaster;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
