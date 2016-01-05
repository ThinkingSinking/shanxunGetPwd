
package com.tools.zjd.shanxungetpwd;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
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
    private int i=0;
    //0表示初始状态,1表示短信已发送但还没收到回信,2表示短信接收成功
    private int state=0;
    private static final String PREFERENCE_NAME="SaveShanxunPwd";//sharedPreference名称
    private static final int MODE=MODE_PRIVATE;//私有模式
    private String pwd=null;
    private String deadline=null;

    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            this.msg=(TextView)findViewById(R.id.msg);//获取信息提示框
            this.submit=(Button)findViewById(R.id.submit);//获取按钮
            this.submit.setOnClickListener(new submit_Listener());//给按钮设置监听事件

            SmsContent content = new SmsContent(new Handler());
            //注册短信变化监听
            this.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, content);


        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }
    protected void onStart() {
        super.onStart();
        if (state==0) loadSharedPreferences();
    }
    /**
     * 读取上一次获取的闪讯密码
     */
    private void loadSharedPreferences(){
        SharedPreferences sharedPreferences=getSharedPreferences(PREFERENCE_NAME,MODE);
        boolean isExist=sharedPreferences.getBoolean("isExist",false);
        if(isExist){
            pwd=sharedPreferences.getString("lastPwd","000000");
            deadline=sharedPreferences.getString("deadLine","1997-00-00 00:00:00");
            msg.setText("上次获取记录：\n\n闪讯密码："+pwd+"\n\n"+"截止日期："+deadline+"\n");
            submit.setText("重新获取");
        }
    }
    /**
     * 保存本次获取的闪讯密码
     */
    private void saveSharedPreferences(){
        SharedPreferences sharedPreferences=getSharedPreferences(PREFERENCE_NAME,MODE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if (state==2){
            editor.putString("lastPwd", pwd);
            editor.putString("deadLine", deadline);
            editor.putBoolean("isExist", true);
        }
        editor.commit();

    }
    /**
     *”获取按钮“的鼠标点击监听类
     */
    class submit_Listener implements OnClickListener {
        public void onClick(View v){
            try {
                if(state == 0 ){
                    msg.setText("正在获取闪讯密码，请稍等......");
                    submit.setText("获取中...");
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
                    Toast.makeText(MainActivity.this, "正在获取中，请稍等...", Toast.LENGTH_SHORT).show();
                }else if(state == 2 ){
                    Toast.makeText(MainActivity.this, "闪讯密码已经成功获取！", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                msg.setText("获取失败，请检查是否已授予本应用相应权限！");
                state=0;//表示短信发送失败
            }
        }
    }
    /**
     * 监听短信数据库变化
     */
    class SmsContent extends ContentObserver {
        private Cursor cursor = null;
        public SmsContent(Handler handler) {
            super(handler);
        }
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ContentResolver resolver=getContentResolver();
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = resolver.query(uri, null, null, null,"date desc");
            if(cursor.moveToNext()){
                String address = cursor.getString(cursor.getColumnIndex("address"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                if("106593005".equals(address)){
                    msg.setText(bodyFormat(body,address));
                    submit.setText("获取成功 !");
                    state=2;
                    saveSharedPreferences();
                    resolver.delete(Uri.parse("content://sms"), "date=" + date, null);
                    resolver.unregisterContentObserver(this);
                }
            }
            cursor.close();
        }
        public String bodyFormat(String body,String phone){
            String msg = "";
            if(body.length()!=51){
                state = 0;//接收短信失败
                return "获取失败，请重新尝试!";
            }
            if("106593005".equals(phone)==false){
                state = 0;//接收短信失败
                return "获取失败，请重新尝试";
            }
            pwd=body.substring(18,24);
            deadline=body.substring(28,47);
            msg = "闪讯密码："+pwd+"\n\n"+"截止日期："+deadline+"\n";
            return msg;
        }
    }

}

