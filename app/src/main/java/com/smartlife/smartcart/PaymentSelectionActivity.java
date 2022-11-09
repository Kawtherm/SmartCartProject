package com.smartlife.smartcart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.model.Order;
import com.smartlife.smartcart.enums.ActivityExtraKey;

/**This activity takes the user to checkout screen where he can choose to pay by going to KNet or go back and modify the order*/
public class PaymentSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_payment_selection);

        Button backBtn = findViewById(R.id.pagesix_back_btn);
        backBtn.setText(isArabic() ? R.string.back_ar : R.string.back);
        backBtn.setOnClickListener(view -> finish());

        ImageButton knetBtn = findViewById(R.id.pagesix_knet_btn);
        ImageButton visaBtn = findViewById(R.id.pagesix_visa_btn);
        Button doneBtn = findViewById(R.id.btn_done);
        loadImageURL(findViewById(R.id.pagesix_comp_logo));

        Order order = (Order)getIntent().getSerializableExtra(ActivityExtraKey.ORDER.name());

        knetBtn.setOnClickListener(view -> {
            proceedToPayment(order, true);
        });

        visaBtn.setOnClickListener(view -> {
            proceedToPayment(order, false);
        });

        SharedPreferences preferences = getSharedPreferences(SharedPreferencesKey.SMART_CART_PREFS.name(), MODE_PRIVATE);
        boolean hasCreditCardPayment = preferences.getBoolean(SharedPreferencesKey.HAS_CREDIT_CARD_PAYMENT.name(), false);
        boolean hasKnetPayment = preferences.getBoolean(SharedPreferencesKey.HAS_KNET_PAYMENT.name(), false);
        boolean isTwoLights = preferences.getBoolean(SharedPreferencesKey.IS_TWO_LIGHTS.name(), false);

        visaBtn.setVisibility(hasCreditCardPayment ? View.VISIBLE : View.GONE);
        knetBtn.setVisibility(hasKnetPayment ? View.VISIBLE : View.GONE);

        if(isTwoLights) switchBothLights(true);

        TextView tvPaymentLabel = findViewById(R.id.payment_lbl);
        if(visaBtn.getVisibility() != View.GONE || knetBtn.getVisibility() != View.GONE) {
            tvPaymentLabel.setText(isArabic() ? R.string.txt_payment_methods_ar : R.string.txt_payment_methods);
            doneBtn.setVisibility(View.GONE);
        }
        else {
            tvPaymentLabel.setText(isArabic() ? R.string.txt_payment_cashier_ar : R.string.txt_payment_cashier);
            doneBtn.setText(isArabic() ? R.string.btn_done_ar : R.string.btn_done);
            doneBtn.setVisibility(View.VISIBLE);
        }

        doneBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PaymentSelectionActivity.this, AdsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void proceedToPayment(Order order, boolean isKNet) {

        Intent intent = new Intent(PaymentSelectionActivity.this, PaymentActivity.class);
        intent.putExtra(ActivityExtraKey.ORDER.name(), order);
        intent.putExtra(ActivityExtraKey.IS_KNET.name(), isKNet);
        startActivity(intent);
        finish();
    }
}