package com.wj.commonlib.ui.weight.share;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wj.commonlib.R;


/**
 * 自定义底部弹出分享带动画效果对话框
 * Created by mustang on 2019/3/25.
 */

public class ShareDialog extends Dialog implements View.OnClickListener {

    private final Context context;
    private Handler mHandler = new Handler();
    private View view;
    private LinearLayout linearLayout;
    private View.OnClickListener onItemClickListener;
    public TextView tv_weixin,tv_pyq,tv_qq,tv_qqhy;


    public ShareDialog(Context context) {
        super(context, R.style.shareDialog);
        this.context = context;

    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.commlib_share_dialog, null);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(view);

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        findViewById(R.id.cancel).setOnClickListener(this);

        linearLayout=findViewById(R.id.line1);
        tv_weixin=findViewById(R.id.tv_weixin);
        tv_pyq=findViewById(R.id.tv_pyq);
        tv_qq=findViewById(R.id.tv_qq);
        tv_qqhy=findViewById(R.id.tv_qqhy);
    }

    @Override
    public void show() {
        super.show();
        showAnimation();
    }

    private void showAnimation() {
        try {
            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(view, "translationY", 1200, 0);
            fadeAnim.setDuration(400);
            fadeAnim.start();
            //菜单项弹出动画
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                final View child = linearLayout.getChildAt(i);
                child.setOnClickListener(ShareDialog.this);
                child.setVisibility(View.INVISIBLE);
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        child.setVisibility(View.VISIBLE);
                        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 800, 0);
                        fadeAnim.setDuration(200);
                        BottomShareAnimator kickAnimator = new BottomShareAnimator();
                        kickAnimator.setDuration(150);
                        fadeAnim.setEvaluator(kickAnimator);
                        fadeAnim.start();
                    }
                }, i * 50 + 400);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void dismiss() {
        view.clearAnimation();
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(view, "translationY", 0, 1000);
        fadeAnim.setDuration(400);
        fadeAnim.start();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ShareDialog.super.dismiss();
            }
        }, 300);

    }

    /**
     * 点击事件处理
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        if (isShowing()) {
            dismiss();
        }
        if (onItemClickListener != null) {
            onItemClickListener.onClick(v);
        }

    }

    private int dip2px(float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
