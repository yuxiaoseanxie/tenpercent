package uber.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cchilton on 11/19/14.
 */
public class UberTime {
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("display_name")
    private String displayName;
    private int estimate;

    public String getDisplayName() {
        return displayName;
    }

    public String getProductId() {
        return productId;
    }

    public int getEstimate() { return estimate; }

}
