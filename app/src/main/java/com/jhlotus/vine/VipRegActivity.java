package com.jhlotus.vine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VipRegActivity extends AppCompatActivity implements View.OnClickListener {

    private Integer oid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_reg);

        findViewById(R.id.btn_vip_nowreg).setOnClickListener(this);

        oid = getIntent().getIntExtra("oid",0);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_vip_nowreg:
                Intent intent = new Intent(this,VipRegNowActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
                break;

            default:break;
        }
    }
}
