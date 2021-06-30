package com.example.busstop_esp8266;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.net.wifi.WifiManager;


import java.io.IOException;
import java.io.PrintStream;
import  java.lang.String;
import  java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String InfoPack;
    private Socket SocketPack;
    private String IP;
    private int Port;
    private PrintStream Out;
    private StringBuilder BUG_Info=new StringBuilder();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.报站按钮).setOnClickListener(this);
        findViewById(R.id.显示按钮).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v.getId()!=R.id.报站按钮&&v.getId()!=R.id.显示按钮){
            return;
        }

        if(SocketPack==null||!SocketPack.isConnected()){
            BUG_Info.setLength(0);
            BUG_Info.append("BUG信息:");
            WifiManager WifiMan=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(WifiMan.isWifiEnabled()) {
                ((Switch) findViewById(R.id.WIFI打开)).setChecked(true);
            }else{
                if(WifiMan.setWifiEnabled(true)){
                    ((Switch) findViewById(R.id.WIFI打开)).setChecked(true);
                }else {
                    ((Switch) findViewById(R.id.WIFI打开)).setChecked(false);
                    BUG_Info.append("WIFI打开失败.");
                    ((TextView)findViewById(R.id.BUG_INFO)).setText(BUG_Info.toString());
                    return;
                }
            }

            WifiInfo WifiIn=WifiMan.getConnectionInfo();
            DhcpInfo DhcpIn=WifiMan.getDhcpInfo();

            if (WifiIn==null){
                ((Switch) findViewById(R.id.WIFI连接)).setChecked(false);
                BUG_Info.append("WIFI连接失败.");
                ((TextView)findViewById(R.id.BUG_INFO)).setText(BUG_Info.toString());
                return;
            }
            ((Switch) findViewById(R.id.WIFI连接)).setChecked(true);

            IP=intToIp(DhcpIn.gateway);
            Port=8080;


            switch(v.getId()){
                case R.id.报站按钮:
                    InfoPack="7";
                break;
                case R.id.显示按钮:
                    InfoPack="8";
                break;
                default: break;
            }


            new Thread("Socket线程."){
                @Override
                public void run(){
                    try {
                        SocketPack = new Socket(IP, Port);

                        Out=new PrintStream(SocketPack.getOutputStream());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Switch) findViewById(R.id.链接建立成功)).setChecked(true);
                            }
                        });
                        Out.print(InfoPack);
                        Out.flush();
                        Out.close();
                        SocketPack.close();
                        SocketPack = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Switch) findViewById(R.id.信息发送成功)).setChecked(true);
                                ((TextView)findViewById(R.id.BUG_INFO)).setText("");
                            }
                        });
                    }catch (UnknownHostException e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BUG_Info.append("Socket连接失败.\n\n");
                                BUG_Info.append("IP::      " + IP + "\n");
                                BUG_Info.append("Port::   " + Port + "\n");
                                ((Switch) findViewById(R.id.链接建立成功)).setChecked(false);
                                ((TextView)findViewById(R.id.BUG_INFO)).setText(BUG_Info.toString());
                            }
                        });
                    }catch (IOException e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BUG_Info.append("IO连接失败.");
                                ((Switch) findViewById(R.id.信息发送成功)).setChecked(false);
                                ((TextView)findViewById(R.id.BUG_INFO)).setText(BUG_Info.toString());
                            }
                        });
                    }
                }
            }.start();

        }

    }

    private String intToIp(int i)  {
        return (i&(0xFF))+"."+(((i>>8)&(0xFF)))+"."+((i>>16)&(0xFF))+"."+((i>>24)&(0xFF));
    }


}
