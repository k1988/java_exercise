package com.zhaoxiuyuan.aidltest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ITestAidl pIterface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 绑定本地Service
        Intent service = new Intent(this, MyIntentService.class);
//        //绑定远程Service
        bindService(service, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("MainActivity","onServiceConnected:"+name);
                pIterface = ITestAidl.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                pIterface = null;
            }
        }, BIND_AUTO_CREATE);
    }

    public void OnClick(View v){
        try {

            Person a = new Person(1,"k1988");
            Person b = pIterface.modifyPerson(a);
            Log.d("MainActivity", b.toString());
            Toast.makeText(this, a.toString() + " -> " + b.toString(), Toast.LENGTH_SHORT).show();

            pIterface.basicTypes(1,2,false, 0.1f, 0.2f,"test");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
