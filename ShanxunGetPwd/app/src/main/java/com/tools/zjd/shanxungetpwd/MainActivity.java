
package com.tools.zjd.shanxungetpwd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView msg;   // 状态信息框
    private Button submit;  //获取按钮
    //0表示初始状态,1表示短信已发送但还没收到回信,2表示短信接收成功
    private int state=0;
    private MySMSReceiver mySMSReceiver;
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            this.msg=(TextView)findViewById(R.id.msg);//获取信息提示框
            this.submit=(Button)findViewById(R.id.submit);//获取按钮
            this.submit.setOnClickListener(new submit_Listener());//给按钮设置监听事件

            //注册监听短信的广播
            this.mySMSReceiver = new MySMSReceiver();
            IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.setPriority(999999999);
            registerReceiver(this.mySMSReceiver, intentFilter);
        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }
    //”获取按钮“的鼠标点击监听类
    class submit_Listener implements OnClickListener {
        public void onClick(View v){
            try {
                if(state == 0 ){
                    msg.setText("正在获取闪讯密码，请稍等......");
                    String phoneNumber="1065930051";
                    String message="MM";
                    // 获取短信管理器
                    SmsManager smsManager = SmsManager.getDefault();
                    // 拆分短信内容（短信长度有限制）
                    List<String> divideContents = smsManager.divideMessage(message);
                    for (String text : divideContents) {
                        smsManager.sendTextMessage(phoneNumber, null, text, null, null);
                    }
                    state=1;//表示短信发送成功
                }else if(state == 1 ){
                    Toast.makeText(MainActivity.this, "正在获取中，请稍等...", Toast.LENGTH_LONG).show();
                }else if(state == 2 ){
                    Toast.makeText(MainActivity.this, "闪讯密码已经成功获取！", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                msg.setText("获取失败，请检查是否已授予本应用相应权限！");
                state=0;//表示短信发送失败
            }
        }
    }
    //短信监听类
    class MySMSReceiver extends BroadcastReceiver {
        public void onReceive(Context context,Intent intent){
            try{
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    Object [] objArray = (Object []) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[objArray.length];

                    String body = "";
                    for (int i = 0; i< objArray.length; i++){
                        messages[i] = SmsMessage.createFromPdu((byte[]) objArray[i]);
                        body += messages[i].getDisplayMessageBody();
                    }
                    String phoneNumber = messages[0].getDisplayOriginatingAddress();
                    msg.setText(bodyFormat(body,phoneNumber));
                    abortBroadcast();//中断广播的传递
                    unregisterReceiver(mySMSReceiver);//注销广播
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                msg.setText("获取失败，请重新尝试。");
                state=0;//表示短信发送失败
            }
        }
        public String bodyFormat(String body,String phone){
            String msg = "";
            String pwd = "";
            String deadline = "";
            if(body.length()!=51){
                state = 0;//接收短信失败
                return "获取失败，请重新尝试。";
            }
            if(phone!="106593005"){
                state = 0;//接收短信失败
                return "获取失败，请重新尝试";
            }
            pwd=body.substring(18,24);
            deadline=body.substring(28,46);
            msg = "密 码："+pwd+"\n"+"截止日期："+deadline;
            return msg;
        }
    }



}

