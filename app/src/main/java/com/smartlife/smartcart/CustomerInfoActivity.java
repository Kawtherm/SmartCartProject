package com.smartlife.smartcart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.smartlife.smartcart.classes.AppProviders;
import com.smartlife.smartcart.enums.ActivityExtraKey;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import org.json.JSONObject;

/**This activity is where the user logs in. If the user enters membership number,
 * it will check its validity with the server then goes to next activity if valid.
 * It also provides a place for the user to enter his email to receive payment confirmation.*/
public class CustomerInfoActivity extends BaseActivity implements OnClickListener {

    private final String TAG = "PageFour";
    private EditText mWhatsNumber;
    private EditText mEmail;
    private EditText mMembershipNumber;

    private String mMembershipID = null;
    private String mMembershipNo = null;
    private Double mBalance = 0.0;
    private String mCartUser = null;

    private TextView mMobileOptionalLbl;
    private TextView mMembershipOptionalLbl;
    private TextView mEmailOptionalLbl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_customer_info);

        Button backBtn = findViewById(R.id.pagefour_back_btn);
        Button nextBtn = findViewById(R.id.pagefour_next_btn);
        Button buttonZero = findViewById(R.id.zero_btn);
        Button buttonOne = findViewById(R.id.one_btn);
        Button buttonTwo = findViewById(R.id.two_btn);
        Button buttonThree = findViewById(R.id.three_btn);
        Button buttonFour = findViewById(R.id.four_btn);
        Button buttonFive = findViewById(R.id.five_btn);
        Button buttonSix = findViewById(R.id.six_btn);
        Button buttonSeven = findViewById(R.id.seven_btn);
        Button buttonEight = findViewById(R.id.eight_btn);
        Button buttonNine = findViewById(R.id.nine_btn);
        Button buttonClear = findViewById(R.id.delete_nb_btn);

        backBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        buttonZero.setOnClickListener(this);
        buttonOne.setOnClickListener(this);
        buttonTwo.setOnClickListener(this);
        buttonThree.setOnClickListener(this);
        buttonFour.setOnClickListener(this);
        buttonFive.setOnClickListener(this);
        buttonSix.setOnClickListener(this);
        buttonSeven.setOnClickListener(this);
        buttonEight.setOnClickListener(this);
        buttonNine.setOnClickListener(this);
        buttonClear.setOnClickListener(this);

        RelativeLayout userInput = findViewById(R.id.page_four_user_input);
        RelativeLayout emailContainer = findViewById(R.id.email_container);
        TextView emailLbl = findViewById(R.id.email_lbl);
        TextView whatsappLbl = findViewById(R.id.whatsapp_nb_lbl);
        TextView membershipNoLbl = findViewById(R.id.membership_nb_lbl);
        mMobileOptionalLbl = findViewById(R.id.mobile_optional_lbl);
        mMembershipOptionalLbl = findViewById(R.id.membership_optional_lbl);
        mEmailOptionalLbl = findViewById(R.id.email_optional_lbl);

        loadImageURL(findViewById(R.id.iv_logo));
        checkAndHandleOptionalLabelsVisibilities();

        mWhatsNumber = findViewById(R.id.whatsapp_nb_txt);
        mEmail = findViewById(R.id.email_txt);
        mMembershipNumber = findViewById(R.id.membership_nb_txt);

        mWhatsNumber.setShowSoftInputOnFocus(false);
        mMembershipNumber.setShowSoftInputOnFocus(false);

        boolean hasEmailConfig = mPreferences.getBoolean(SharedPreferencesKey.HAS_EMAIL_CONFIG.name(), false);
        emailContainer.setVisibility(hasEmailConfig ? View.VISIBLE : View.GONE);

        if(isArabic()) {

            backBtn.setText(R.string.back_ar);
            nextBtn.setText(R.string.next_ar);
            emailLbl.setText(R.string.lbl_email_ar);
            whatsappLbl.setText(R.string.whatsapp_ar);
            membershipNoLbl.setText(R.string.membership_no_ar);
            mMobileOptionalLbl.setText(R.string.optional_ar);
            mMembershipOptionalLbl.setText(R.string.optional_ar);
            mEmailOptionalLbl.setText(R.string.optional_ar);

            final RelativeLayout.LayoutParams userInputLayoutParams = new RelativeLayout.LayoutParams(convertDpToPixels(260), ViewGroup.LayoutParams.WRAP_CONTENT);
            userInputLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            userInputLayoutParams.rightMargin = convertDpToPixels(100);
            userInputLayoutParams.topMargin = convertDpToPixels(200);
            userInput.setLayoutParams(userInputLayoutParams);

            final RelativeLayout.LayoutParams whatsappLayoutParams = (RelativeLayout.LayoutParams)whatsappLbl.getLayoutParams();
            whatsappLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            whatsappLbl.setLayoutParams(whatsappLayoutParams);

            final RelativeLayout.LayoutParams emailLayoutParams = (RelativeLayout.LayoutParams)emailLbl.getLayoutParams();
            emailLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            emailLbl.setLayoutParams(emailLayoutParams);

            final RelativeLayout.LayoutParams membershipLayoutParams = (RelativeLayout.LayoutParams)membershipNoLbl.getLayoutParams();
            membershipLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            membershipNoLbl.setLayoutParams(membershipLayoutParams);

            final RelativeLayout.LayoutParams optional1LayoutParams = (RelativeLayout.LayoutParams) mMobileOptionalLbl.getLayoutParams();
            optional1LayoutParams.addRule(RelativeLayout.ALIGN_END, R.id.whatsapp_nb_txt);
            optional1LayoutParams.addRule(RelativeLayout.BELOW, R.id.whatsapp_nb_txt);
            mMobileOptionalLbl.setLayoutParams(optional1LayoutParams);

            final RelativeLayout.LayoutParams optional2LayoutParams = (RelativeLayout.LayoutParams) mMembershipOptionalLbl.getLayoutParams();
            optional2LayoutParams.addRule(RelativeLayout.ALIGN_END, R.id.membership_nb_txt);
            optional2LayoutParams.addRule(RelativeLayout.BELOW, R.id.membership_nb_txt);
            mMembershipOptionalLbl.setLayoutParams(optional2LayoutParams);

            final RelativeLayout.LayoutParams optional3LayoutParams = (RelativeLayout.LayoutParams) mEmailOptionalLbl.getLayoutParams();
            optional3LayoutParams.addRule(RelativeLayout.ALIGN_END, R.id.email_txt);
            optional3LayoutParams.addRule(RelativeLayout.BELOW, R.id.email_txt);
            mEmailOptionalLbl.setLayoutParams(optional3LayoutParams);

        } else {
            final RelativeLayout.LayoutParams userInputLayoutParams = new RelativeLayout.LayoutParams(convertDpToPixels(260), ViewGroup.LayoutParams.WRAP_CONTENT);
            userInputLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            userInputLayoutParams.rightMargin = convertDpToPixels(100);
            userInputLayoutParams.topMargin = convertDpToPixels(200);
            userInput.setLayoutParams(userInputLayoutParams);

            final RelativeLayout.LayoutParams whatsappLayoutParams = (RelativeLayout.LayoutParams)whatsappLbl.getLayoutParams();
            whatsappLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            whatsappLbl.setLayoutParams(whatsappLayoutParams);

            final RelativeLayout.LayoutParams emailLayoutParams = (RelativeLayout.LayoutParams)emailLbl.getLayoutParams();
            emailLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            emailLbl.setLayoutParams(emailLayoutParams);

            final RelativeLayout.LayoutParams membershipLayoutParams = (RelativeLayout.LayoutParams)membershipNoLbl.getLayoutParams();
            membershipLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            membershipNoLbl.setLayoutParams(membershipLayoutParams);

            final RelativeLayout.LayoutParams optional1LayoutParams = (RelativeLayout.LayoutParams) mMobileOptionalLbl.getLayoutParams();
            optional1LayoutParams.addRule(RelativeLayout.ALIGN_START, R.id.whatsapp_nb_txt);
            optional1LayoutParams.addRule(RelativeLayout.BELOW, R.id.whatsapp_nb_txt);
            mMobileOptionalLbl.setLayoutParams(optional1LayoutParams);

            final RelativeLayout.LayoutParams optional2LayoutParams = (RelativeLayout.LayoutParams) mMembershipOptionalLbl.getLayoutParams();
            optional2LayoutParams.addRule(RelativeLayout.ALIGN_START, R.id.membership_nb_txt);
            optional2LayoutParams.addRule(RelativeLayout.BELOW, R.id.membership_nb_txt);
            mMembershipOptionalLbl.setLayoutParams(optional2LayoutParams);

            final RelativeLayout.LayoutParams optional3LayoutParams = (RelativeLayout.LayoutParams) mEmailOptionalLbl.getLayoutParams();
            optional3LayoutParams.addRule(RelativeLayout.ALIGN_START, R.id.email_txt);
            optional3LayoutParams.addRule(RelativeLayout.BELOW, R.id.email_txt);
            mEmailOptionalLbl.setLayoutParams(optional3LayoutParams);
        }
    }

    private void checkAndHandleOptionalLabelsVisibilities() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                boolean isEmailOptionalLblVisible = firebaseRemoteConfig.getBoolean("isOptionalEmailVisible");
                boolean isMobileOptionalLblVisible = firebaseRemoteConfig.getBoolean("isOptionalMobileVisible");
                boolean isMembershipOptionalLblVisible = firebaseRemoteConfig.getBoolean("isOptionalMembershipVisible");

                mEmailOptionalLbl.setVisibility(isEmailOptionalLblVisible ? View.VISIBLE : View.GONE);
                mMobileOptionalLbl.setVisibility(isMobileOptionalLblVisible ? View.VISIBLE : View.GONE);
                mMembershipOptionalLbl.setVisibility(isMembershipOptionalLblVisible ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void createUser() {

        JSONObject parameters = new JSONObject();
        try {
            String companyId = mPreferences.getString(SharedPreferencesKey.COMPANY_ID.name(), "");
            String cartNo = mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), "");
            parameters.put("company_id", companyId);
            parameters.put("cart_number", cartNo);
        }
        catch (Exception e) { }

        makeHttpPostRequest(ApiUrl.GENERATE_USER, parameters, null);
    }

    private void startShopping() {

        Intent intent = new Intent(CustomerInfoActivity.this, ShoppingActivity.class);
        intent.putExtra(ActivityExtraKey.USER_ID.name(), mWhatsNumber.getText().toString().trim());
        intent.putExtra(ActivityExtraKey.USER_EMAIL.name(), mEmail.getText().toString().trim());
        intent.putExtra(ActivityExtraKey.CART_USER.name(), mCartUser);

        if(mMembershipID != null && mMembershipNo != null) {
            intent.putExtra(ActivityExtraKey.MEMBERSHIP_NUM.name(), mMembershipNo);
            intent.putExtra(ActivityExtraKey.MEMBERSHIP_ID.name(), mMembershipID);
            intent.putExtra(ActivityExtraKey.MEMBERSHIP_BALANCE.name(), mBalance);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode)  {
        super.handleResponse(response, method, barcode);

        runOnUiThread(() -> {

            hideActivityIndicator();

            if(method == ApiUrl.VERIFY_MEMBERSHIP) {

                int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);

                if(inventoryIndex == AppProviders.EEMC) {

                    if(response.toString().trim().isEmpty()) {

                        hideActivityIndicator();
                        doError(getString(R.string.error), isArabic() ? getString(R.string.error_membership_num_ar) : getString(R.string.error_membership_num));
                    }
                    else {
                        try {

                            mMembershipNo = response.getString("membershipNo").trim();
                            mMembershipID = response.getString("recId").trim();
                            mBalance = response.getDouble("credit");
                            createUser();

                        } catch (Exception e) {
                            logError(TAG+" => handleResponse => verifyMembership => "+e.getMessage());

                            hideActivityIndicator();
                            doError(getString(R.string.error), isArabic() ? getString(R.string.error_membership_num_ar) : getString(R.string.error_membership_num));
                        }
                    }
                }
                else if (inventoryIndex == AppProviders.INTEGRATION) {

                    if(response.toString().trim().isEmpty()) {

                        hideActivityIndicator();
                        doError(getString(R.string.error), isArabic() ? getString(R.string.error_membership_num_ar) : getString(R.string.error_membership_num));
                    }
                    else {
                        try {

                            mMembershipNo = response.getString("customerNumber").trim();
                            mMembershipID = response.getString("customerID").trim();
                            mBalance = response.getDouble("currentBalance");
                            createUser();

                        } catch (Exception e) {
                            logError(TAG+" => handleResponse => verifyMembership => "+e.getMessage());

                            hideActivityIndicator();
                            doError(getString(R.string.error), isArabic() ? getString(R.string.error_membership_num_ar) : getString(R.string.error_membership_num));
                        }
                    }
                }
            }
            else if(method == ApiUrl.GENERATE_USER) {

                hideActivityIndicator();
                try {
                    int code = response.getInt("code");
                    String msg = response.getString("msg");

                    if(code == 0) {

                        JSONObject data = response.getJSONObject("data");
                        mCartUser = data.getString("id").trim();

                        startShopping();
                    }
                    else {
                        doError(getString(R.string.error), msg);
                    }

                } catch(Exception e) {
                    doError(getString(R.string.error), isArabic() ? getString(R.string.error_request_failed_ar) : getString(R.string.error_request_failed));
                }
            }
        });
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        runOnUiThread(() -> {
            hideActivityIndicator();
            if(method == ApiUrl.VERIFY_MEMBERSHIP) {
                doError(getString(R.string.error), isArabic() ? getString(R.string.error_membership_num_ar) : getString(R.string.error_membership_num));
            }
            else if(method == ApiUrl.GENERATE_USER) {
                doError(getString(R.string.error), isArabic() ? getString(R.string.error_request_failed_ar) : getString(R.string.error_request_failed));

            }
        });
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.pagefour_back_btn) {
            finish();
        }
        else if(v.getId() == R.id.pagefour_next_btn) {

            String membershipNumber = mMembershipNumber.getText().toString().trim();

            if(membershipNumber.equals("")) {
                createUser();
            }
            else {
                int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);

                if (inventoryIndex == AppProviders.EEMC) {
                    showActivityIndicator(isArabic() ? getString(R.string.loading_ar) : getString(R.string.loading));
                    makeHttpGetRequest(ApiUrl.VERIFY_MEMBERSHIP, membershipNumber, null);
                }
                else if (inventoryIndex == AppProviders.INTEGRATION) {
                    showActivityIndicator(isArabic() ? getString(R.string.loading_ar) : getString(R.string.loading));
                    makeHttpGetRequest(ApiUrl.VERIFY_MEMBERSHIP, membershipNumber, null);
                }
            }
        }
        else if(v.getId() == R.id.delete_nb_btn) {

            if(getCurrentFocus() != null) {
                if(getCurrentFocus().getId() == R.id.whatsapp_nb_txt) {
                    if(mWhatsNumber.getText().length() > 0) {
                        CharSequence currentText = mWhatsNumber.getText();
                        mWhatsNumber.setText(currentText.subSequence(0, currentText.length()-1));
                        mWhatsNumber.setSelection(mWhatsNumber.getText().length());
                    } else {
                        mWhatsNumber.setText("");
                    }
                } else {
                    if(mMembershipNumber.getText().length() > 0) {
                        CharSequence currentText = mMembershipNumber.getText();
                        mMembershipNumber.setText(currentText.subSequence(0, currentText.length()-1));
                        mMembershipNumber.setSelection(mMembershipNumber.getText().length());
                    } else {
                        mMembershipNumber.setText("");
                    }
                }
            }
        }
        else {
            if(getCurrentFocus() != null) {

                if(getCurrentFocus().getId() == R.id.whatsapp_nb_txt) {

                    mWhatsNumber.setText(mWhatsNumber.getText().append(((Button) v).getText()));
                    mWhatsNumber.setSelection(mWhatsNumber.getText().length());
                } else {
                    mMembershipNumber.setText(mMembershipNumber.getText().append(((Button) v).getText()));
                    mMembershipNumber.setSelection(mMembershipNumber.getText().length());
                }
            }
        }
    }
}