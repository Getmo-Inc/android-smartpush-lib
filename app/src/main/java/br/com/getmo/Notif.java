package br.com.getmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notif {

    @SerializedName("appid")
    @Expose
    private String appid;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("params")
    @Expose
    private Params params;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }


    @Override
    public String toString() {
        return "{ appid:'" + appid + '\'' +
                ", platform:'" + platform + '\'' +
                ", params:" + params +  '}';
    }
}