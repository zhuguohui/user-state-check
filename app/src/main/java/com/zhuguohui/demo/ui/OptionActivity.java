package com.zhuguohui.demo.ui;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhuguohui.demo.R;
import com.zhuguohui.demo.impl.DemoUserState;
import com.zhuguohui.demo.impl.DemoUserStateManager;
import com.zhuguohui.demo.userstate.IUserState;
import com.zhuguohui.demo.userstate.IUserStatePage;

public  class OptionActivity extends BaseActivity implements IUserStatePage {


    private IUserState userState;

    public static void openForState(Context context, IUserState userState){
        Intent intent=new Intent(context,OptionActivity.class);
        intent.putExtra(DemoUserState.class.getName(),userState);
        context.startActivity(intent);
    }

    IUserState[] states;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        TextView tvOption=findViewById(R.id.tv_option);
        Button btnOption=findViewById(R.id.btn_option);
        userState = (IUserState) getIntent().getSerializableExtra(DemoUserState.class.getName());
        states=new IUserState[]{userState};
        tvOption.setText(userState.getDesc()+"页面");
        btnOption.setText(userState.getDesc());
        btnOption.setOnClickListener(v->doOption(v));
    }



    protected void doOption(View v) {
         DemoUserStateManager.getInstance().matchUserStateSuccess(userState);
         finish();
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IUserState[] getUserSatePageTypes() {
        return states;
    }
}