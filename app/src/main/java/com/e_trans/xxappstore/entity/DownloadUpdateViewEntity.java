package com.e_trans.xxappstore.entity;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by wk on 2016/5/16.
 */
public class DownloadUpdateViewEntity {
    private ProgressBar progressBar;
    private TextView textView;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}
