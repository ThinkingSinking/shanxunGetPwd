package com.tools.zjd.shanxungetpwd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView msg;   // 状态信息框
    private Button submit;  //获取按钮
    private boolean state=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.msg=(TextView)findViewById(R.id.msg);
        this.submit=(Button)findViewById(R.id.submit);
        this.submit.setOnClickListener(new submit_Listener());
    }
    class submit_Listener implements OnClickListener {
        public void onClick(View v){
            try {
                if(state){
                    msg.setText("正在获取闪讯密码，请稍等......");
                    state=false;
                    String phoneNumber="1065930051";
                    String message="MM";
                    // 获取短信管理器
                    SmsManager smsManager = SmsManager.getDefault();
                    // 拆分短信内容（手机短信长度限制）
                    List<String> divideContents = smsManager.divideMessage(message);
                    for (String text : divideContents) {
                        smsManager.sendTextMessage(phoneNumber, null, text, null, null);
                    }
                    //Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, "正在获取中，请稍等...", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


}
