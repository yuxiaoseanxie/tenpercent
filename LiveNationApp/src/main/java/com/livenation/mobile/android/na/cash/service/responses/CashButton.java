package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashButton extends CashResponse {
    @JsonProperty("label")
    private String label;

    @JsonProperty("link")
    private String link;


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    @Override
    public String toString() {
        return "CashButton{" +
                "label='" + label + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
