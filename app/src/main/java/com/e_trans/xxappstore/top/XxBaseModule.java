package com.e_trans.xxappstore.top;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class XxBaseModule {
    public interface IUpdateUI {
        public void onUpdateUI(Message msg);
    }

    protected Handler mHandler;
    protected Context mContext;

    private boolean mUpdateUI = false;

    protected List<IUpdateUI> mRemoveList;

    //private Map<String, IUpdateUI> mIUpdateUI;
    private List<IUpdateUI> mIUpdateUI;

    protected XxBaseModule() {
    }

    ;

    public XxBaseModule init(Context context) {
        this.mContext = context;
        mHandler = new XxBaseModuleHandler(mContext.getMainLooper());
        mIUpdateUI = new ArrayList<IUpdateUI>();
        mRemoveList = new ArrayList<IUpdateUI>();
        return this;
    }

    public void addUpdateUIListener(IUpdateUI listener) {
        mIUpdateUI.add(listener);
        if (!mRemoveList.isEmpty())
            mRemoveList.remove(listener);
    }

    public void removeUpdateUIListener(IUpdateUI listener) {
        if (mUpdateUI) {
            mRemoveList.add(listener);
        } else {
            if (!mIUpdateUI.isEmpty()) {
                mIUpdateUI.remove(listener);
            }
        }
    }

    private void removeAllListener() {
        for (int i = 0; i < mRemoveList.size(); i++) {
            IUpdateUI listener = mRemoveList.remove(i);
            mIUpdateUI.remove(listener);
        }
    }

    protected void onUpdateUI(Message msg) {
        if (!mIUpdateUI.isEmpty()) {
            for (int i = 0; i < mIUpdateUI.size(); i++) {
                IUpdateUI iUpdateUI = mIUpdateUI.get(i);
                if (iUpdateUI != null)
                    iUpdateUI.onUpdateUI(msg);
            }
        }
    }

    class XxBaseModuleHandler extends Handler {
        XxBaseModuleHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO �Զ����ɵķ������
            super.handleMessage(msg);
            mUpdateUI = true;
            onUpdateUI(msg);
            removeAllListener();
            mUpdateUI = false;
        }

    }
}
