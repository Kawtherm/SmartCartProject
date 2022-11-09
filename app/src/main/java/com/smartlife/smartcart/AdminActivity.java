package com.smartlife.smartcart;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**This activity is for choosing one of four restricted views.
 * You can access settings, diagnostics, scale, reset orders all from here.*/
public class AdminActivity extends BaseActivity implements View.OnClickListener {

    private AlertDialog mAdminPassDialog;
    EditText mDialogPasswordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin);

        preparePasswordInputDialog();

        Button mScaleBtn = findViewById(R.id.admin_scale_btn);
        Button mSettingsBtn = findViewById(R.id.admin_settings_btn);
        Button mDiagBtn = findViewById(R.id.admin_diagnostics_btn);
        Button mOrdersBtn = findViewById(R.id.admin_reset_orders_btn);
        Button mBackBtn = findViewById(R.id.admin_back_btn);

        mScaleBtn.setOnClickListener(this);
        mSettingsBtn.setOnClickListener(this);
        mOrdersBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
    }

    private void preparePasswordInputDialog() {

        mAdminPassDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.input_admin_password, null);

        mDialogPasswordTxt = dialogView.findViewById(R.id.admin_password_no);
        mDialogPasswordTxt.setShowSoftInputOnFocus(false);

        Button oneBtn = dialogView.findViewById(R.id.pass_one_btn);
        Button twoBtn = dialogView.findViewById(R.id.pass_two_btn);
        Button threeBtn = dialogView.findViewById(R.id.pass_three_btn);
        Button fourBtn = dialogView.findViewById(R.id.pass_four_btn);
        Button fiveBtn = dialogView.findViewById(R.id.pass_five_btn);
        Button sixBtn = dialogView.findViewById(R.id.pass_six_btn);
        Button sevenBtn = dialogView.findViewById(R.id.pass_seven_btn);
        Button eightBtn = dialogView.findViewById(R.id.pass_eight_btn);
        Button nineBtn = dialogView.findViewById(R.id.pass_nine_btn);
        Button zeroBtn = dialogView.findViewById(R.id.pass_zero_btn);
        Button deleteNumberBtn = dialogView.findViewById(R.id.pass_delete_nb_btn);
        Button submitBtn = dialogView.findViewById(R.id.pass_submit_btn);

        oneBtn.setOnClickListener(this);
        twoBtn.setOnClickListener(this);
        threeBtn.setOnClickListener(this);
        fourBtn.setOnClickListener(this);
        fiveBtn.setOnClickListener(this);
        sixBtn.setOnClickListener(this);
        sevenBtn.setOnClickListener(this);
        eightBtn.setOnClickListener(this);
        nineBtn.setOnClickListener(this);
        zeroBtn.setOnClickListener(this);
        deleteNumberBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

        mAdminPassDialog.setView(dialogView);

        mAdminPassDialog.setOnShowListener(listener -> {
            mDialogPasswordTxt.requestFocus();
        });

        mAdminPassDialog.setOnDismissListener(listener -> {
            mDialogPasswordTxt.clearFocus();
            mDialogPasswordTxt.setText("");
        });
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.admin_scale_btn) {
            Intent scaleIntent = new Intent(AdminActivity.this, ScaleActivity.class);
            startActivity(scaleIntent);
        }
        else if(v.getId() == R.id.admin_reset_orders_btn) {

            Intent scaleIntent = new Intent(AdminActivity.this, ResetActivity.class);
            startActivity(scaleIntent);
        }
        else if(v.getId() == R.id.admin_settings_btn) {

            mAdminPassDialog.show();
            mAdminPassDialog.getWindow().setLayout(convertDpToPixels(300), convertDpToPixels(620));
        }
        else if(v.getId() == R.id.admin_back_btn) {
            finish();
        }
        else if(v.getId() == R.id.pass_delete_nb_btn) {

            if (mDialogPasswordTxt.getText().length() > 0) {
                CharSequence currentText = mDialogPasswordTxt.getText();
                mDialogPasswordTxt.setText(currentText.subSequence(0, currentText.length() - 1));
                mDialogPasswordTxt.setSelection(mDialogPasswordTxt.getText().length());
            } else {
                mDialogPasswordTxt.setText("");
            }
        }
        else if(v.getId() == R.id.pass_submit_btn) {

            mAdminPassDialog.dismiss();

            if (mDialogPasswordTxt.getText().length() > 0) {
                String password = mDialogPasswordTxt.getText().toString().trim();
                if(password.equals("97121011")) {
                    Intent targetIntent = new Intent(AdminActivity.this, SettingsActivity.class);
                    startActivity(targetIntent);
                }
            }
        }
        else {
            mDialogPasswordTxt.setText(mDialogPasswordTxt.getText().append(((Button)v).getText()));
            mDialogPasswordTxt.setSelection(mDialogPasswordTxt.getText().length());
        }
    }
}