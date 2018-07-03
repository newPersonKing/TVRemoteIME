package com.android.tvremoteime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tvremoteime.server.RemoteServer;
import com.android.tvremoteime.adb.AdbHelper;
import com.zxt.dlna.dmr.ZxtMediaRenderer;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView qrCodeImage;
    private TextView addressView;
    private EditText dlnaNameText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeImage = this.findViewById(R.id.ivQRCode);
        addressView = this.findViewById(R.id.tvAddress);
        dlnaNameText = this.findViewById(R.id.etDLNAName);

        this.setTitle(this.getResources().getString( R.string.app_name) + "  V" + AppPackagesHelper.getCurrentPackageVersion(this));
        dlnaNameText.setText(DLNAUtils.getDLNANameSuffix(this.getApplicationContext()));

        refreshQRCode();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUseIME:
                openInputMethodSettings();
                if(Environment.isEnableIME(this)){
                    Environment.toast(getApplicationContext(), "太棒了，您已经激活启用了" + getString(R.string.keyboard_name) +"输入法！");
                }
                break;
            case R.id.btnSetIME:
                if(!Environment.isEnableIME(this)) {
                    Environment.toast(getApplicationContext(), "抱歉，请您先激活启用" + getString(R.string.keyboard_name) +"输入法！");
                    openInputMethodSettings();
                    if(!Environment.isEnableIME(this)) return;
                }
                try {
                    ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                }catch (Exception ignored) {
                    Environment.toast(getApplicationContext(), "抱歉，无法设置为系统默认输入法，请手动启动服务！");
                }
                if(Environment.isDefaultIME(this)){
                    Environment.toast(getApplicationContext(), "太棒了，" + getString(R.string.keyboard_name) +"已是系统默认输入法！");
                }
                break;
            case R.id.btnStartService:
                startService(new Intent(IMEService.ACTION));
                if(!Environment.isDefaultIME(this)) {
                    if (AdbHelper.getInstance() == null) AdbHelper.createInstance();
                }
                Environment.toast(getApplicationContext(), "服务已手动启动，稍后可尝试访问控制端页面");
                break;
            case R.id.btnSetDLNA:
                DLNAUtils.setDLNANameSuffix(this.getApplicationContext(), dlnaNameText.getText().toString());
                break;
        }
        refreshQRCode();
    }
    private void openInputMethodSettings(){
        try {
            this.startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
        }catch (Exception ignored){
            Environment.toast(getApplicationContext(), "抱歉，无法激活启用输入法，请手动启动服务！");
        }
    }
    private void refreshQRCode(){
        String address = RemoteServer.getServerAddress(this);
        addressView.setText(address);
        qrCodeImage.setImageBitmap(QRCodeGen.generateBitmap(address, 150, 150));
    }




}
