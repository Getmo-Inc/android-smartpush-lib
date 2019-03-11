package br.com.getmo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Filter {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("range")
    @Expose
    private String range;
    @SerializedName("operator")
    @Expose
    private String operator;
    @SerializedName("rules")
    @Expose
    private List<List<String>> rules = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<List<String>> getRules() {
        return rules;
    }

    public void setRules(List<List<String>> rules) {
        this.rules = rules;
    }

}
