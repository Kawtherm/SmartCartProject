package com.smartlife.smartcart.enums;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartlife.smartcart.classes.AppProviders;

import static android.content.Context.MODE_PRIVATE;

/**This is where the API URLs are built*/
public enum ApiUrl {

    RETRIEVE_COMPANY_DETAILS,
    VERIFY_MEMBERSHIP,
    VERIFY_PRODUCT,
    VERIFY_WEIGHT,
    UPDATE_WEIGHT,
    RETRIEVE_WEIGHT_TOLERANCES,
    CHECK_PAYMENT_STATUS,
    ADD_ORDER,
    PLACE_ORDER,
    UPDATE_CART_BAT,
    SCALE,
    GENERATE_USER,
    LOG_ERROR;

    public String url(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(SharedPreferencesKey.SMART_CART_PREFS.name(), MODE_PRIVATE);
        int inventoryIndex = preferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), AppProviders.SMART_LIFE);
        String storeNumber = preferences.getString(SharedPreferencesKey.STORE_NUMBER.name(), "");
        String companyId = preferences.getString(SharedPreferencesKey.COMPANY_ID.name(), "");
        String inventoryApiUrl = preferences.getString(SharedPreferencesKey.INVENTORY_API_URL.name(), "");
        String smartApiUrl = preferences.getString(SharedPreferencesKey.SMART_LIFE_API_URL.name(), "");

        switch (this) {

            case VERIFY_MEMBERSHIP:
                switch (inventoryIndex) {
                    case AppProviders.SMART_LIFE: return smartApiUrl;
                    case AppProviders.EEMC: return inventoryApiUrl.concat("/api/v1/shareholder/");
                    case AppProviders.INTEGRATION: return inventoryApiUrl.concat("/api/shareholders/");
                }

            case VERIFY_PRODUCT:
                switch (inventoryIndex) {
                    case AppProviders.SMART_LIFE: return inventoryApiUrl.concat("/barcodes/product");
                    case AppProviders.EEMC: return inventoryApiUrl.concat("/api/v1/items/"+storeNumber+"/");
                    case AppProviders.INTEGRATION: return inventoryApiUrl.concat("/api/products/");
                }

            case VERIFY_WEIGHT: return smartApiUrl + "/barcodes/weight";

            case SCALE: return smartApiUrl + "/barcodes";

            case ADD_ORDER: return smartApiUrl + "/order/add"; // this adds the order before payment to SmartLife

            case PLACE_ORDER: // this places the order after payment to coop
                switch (inventoryIndex) {
                    case AppProviders.SMART_LIFE: return inventoryApiUrl + "/order/add";
                    case AppProviders.EEMC: return inventoryApiUrl + "/api/v1/possales";
                    case AppProviders.INTEGRATION: return inventoryApiUrl + "/api/orders/add";
                }

            case UPDATE_CART_BAT: return smartApiUrl + "/cart/battery";

            case UPDATE_WEIGHT: return smartApiUrl + "/barcodes/updateweightdetails";

            case RETRIEVE_WEIGHT_TOLERANCES: return smartApiUrl + "/barcodes/weighttolerances";

            case CHECK_PAYMENT_STATUS: return smartApiUrl + "/order/paymentStatus";

            case RETRIEVE_COMPANY_DETAILS: return smartApiUrl + "/companies/"+companyId;

            case GENERATE_USER: return smartApiUrl + "/cart/GenerateUser";

            case LOG_ERROR: return smartApiUrl + "/log/addrequestlog";

            default: return "";
        }
    }

}
