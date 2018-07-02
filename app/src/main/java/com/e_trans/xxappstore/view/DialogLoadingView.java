package com.e_trans.xxappstore.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.e_trans.xxappstore.R;

/**
 * App详情页
 * Created by wk on 2016/3/31 0031.
 */
public class DialogLoadingView extends Dialog {

    private TextView dialogTextNameID;

    public DialogLoadingView(Context context, String titleName) {
        super(context, R.style.MyProgressDialog);
        this.setContentView(R.layout.loading_dialog_view);

        dialogTextNameID = (TextView) findViewById(R.id.dialogTextNameID);

        dialogTextNameID.setText(TextUtils.isEmpty(titleName) == true ? context.getString(R.string.loading) : titleName);

        setCanceledOnTouchOutside(false);

//		setCanceleable(false);
    }

    public void setTileName(String titleName) {
        dialogTextNameID.setText(TextUtils.isEmpty(titleName) == true ? "加载中..." : titleName);
    }

}
