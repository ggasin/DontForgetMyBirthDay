package com.dfmbd.dontforgetbirthdayproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dfmbd.dontforgetbirthdayproject.R;

public class PrivatePolicyActivity extends AppCompatActivity {


    private Button closeBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_policy);
        closeBtn = findViewById(R.id.private_policy_close_btn);
        WebView webView = findViewById(R.id.private_policy_webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://sites.google.com/view/dontforgetmybirthday/%ED%99%88");

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrivatePolicyActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // 이전 activity를 스택에서 찾아서 활성화
                // 이전 activity로 이동
                startActivity(intent);

            }
        });
    }
}
