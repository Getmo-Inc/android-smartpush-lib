package br.com.getmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObjNotif {
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("prod")
    @Expose
    private Integer prod;
    @SerializedName("when")
    @Expose
    private String when;
    @SerializedName("devid")
    @Expose
    private String devid;
    @SerializedName("notifications")
    @Expose
    private List<Notif> notifications = null;
    @SerializedName("filter")
    @Expose
    private Filter filter;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getProd() {
        return prod;
    }

    public void setProd(Integer prod) {
        this.prod = prod;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getDevid() {
        return devid;
    }

    public void setDevid(String devid) {
        this.devid = devid;
    }

    public List<Notif> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notif> notifications) {
        this.notifications = notifications;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
