package com.zfy.social.wb;

import android.app.Activity;
import android.text.TextUtils;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.zfy.social.core.SocialSdk;
import com.zfy.social.core.exception.SocialError;
import com.zfy.social.core.listener.OnShareListener;
import com.zfy.social.core.model.ShareObj;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bolts.Task;

/**
 * CreateAt : 2018/6/24
 * Describe : 使用 openApi 分享动图
 *
 * @author chendong
 */
public class OpenApiShareHelper {

    private WbLoginHelper mWbLoginHelper;
    private OnShareListener mOnShareListener;

    OpenApiShareHelper(WbLoginHelper wbLoginHelper, OnShareListener onShareListener) {
        mWbLoginHelper = wbLoginHelper;
        mOnShareListener = onShareListener;
    }

    public void post(Activity activity, final ShareObj obj) {
        mWbLoginHelper.justAuth(activity, new WbAuthListenerImpl() {
            @Override
            public void onSuccess(final Oauth2AccessToken token) {
                Task.callInBackground(() -> {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", token.getToken());
                    params.put("status", obj.getSummary());
                    return SocialSdk.getRequestAdapter().postData("https://api.weibo.com/2/statuses/share.json", params, "pic", obj.getThumbImagePath());
                }).continueWith(task -> {
                    if (task.isFaulted() || TextUtils.isEmpty(task.getResult())) {
                        throw SocialError.make(SocialError.CODE_PARSE_ERROR, "open api 分享失败 " + task.getResult(), task.getError());
                    } else {
                        JSONObject jsonObject = new JSONObject(task.getResult());
                        if (jsonObject.has("id") && jsonObject.get("id") != null) {
                            mOnShareListener.onSuccess();
                            return true;
                        } else {
                            throw SocialError.make(SocialError.CODE_PARSE_ERROR, "open api 分享失败 " + task.getResult());
                        }
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(task -> {
                    if (task != null && task.isFaulted()) {
                        Exception error = task.getError();
                        if (error instanceof SocialError) {
                            mOnShareListener.onFailure((SocialError) error);
                        } else {
                            mOnShareListener.onFailure(SocialError.make(SocialError.CODE_REQUEST_ERROR, "open api 分享失败", error));
                        }
                    }
                    return true;
                }, Task.UI_THREAD_EXECUTOR);
            }
        });
    }

    public static final String TAG = OpenApiShareHelper.class.getSimpleName();


    class WbAuthListenerImpl implements WbAuthListener {
        @Override
        public void onSuccess(Oauth2AccessToken token) {
        }

        @Override
        public void cancel() {
            mOnShareListener.onCancel();
        }

        @Override
        public void onFailure(WbConnectErrorMessage msg) {
            mOnShareListener.onFailure(SocialError.make(SocialError.CODE_SDK_ERROR, TAG + "#WbAuthListenerImpl#wb auth fail," + msg.getErrorCode() + " " + msg.getErrorMessage()));
        }
    }
}
