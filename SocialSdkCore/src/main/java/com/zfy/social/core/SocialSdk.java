package com.zfy.social.core;

import android.content.Context;
import android.util.SparseArray;

import com.zfy.social.core.adapter.IJsonAdapter;
import com.zfy.social.core.adapter.IRequestAdapter;
import com.zfy.social.core.adapter.impl.DefaultRequestAdapter;
import com.zfy.social.core.exception.SocialError;
import com.zfy.social.core.platform.IPlatform;
import com.zfy.social.core.platform.PlatformFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CreateAt : 2017/5/19
 * Describe : SocialSdk
 *
 * @author chendong
 */
public class SocialSdk {

    private static SocialOptions sSocialOptions;
    private static IJsonAdapter sJsonAdapter;
    private static IRequestAdapter sRequestAdapter;
    private static ExecutorService sExecutorService;

    public static SocialOptions getConfig() {
        if (sSocialOptions == null) {
            throw SocialError.make(SocialError.CODE_SDK_INIT_ERROR);
        }
        return sSocialOptions;
    }

    public static void init(SocialOptions config) {
        sSocialOptions = config;
    }

    public static IPlatform getPlatform(Context context, int target) {
        SparseArray<PlatformFactory> array = sSocialOptions.getPlatformFactoryArray();
        PlatformFactory platformFactory = array.get(target);
        if (platformFactory != null) {
            return platformFactory.create(context, target);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // JsonAdapter
    ///////////////////////////////////////////////////////////////////////////

    public static IJsonAdapter getJsonAdapter() {
        if (sJsonAdapter == null) {
            throw new IllegalStateException("为了不引入其他的json解析依赖，特地将这部分放出去，必须添加一个对应的 json 解析工具，参考代码 sample/GsonJsonAdapter.java");
        }
        return sJsonAdapter;
    }

    public static void setJsonAdapter(IJsonAdapter jsonAdapter) {
        sJsonAdapter = jsonAdapter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // RequestAdapter
    ///////////////////////////////////////////////////////////////////////////

    public static IRequestAdapter getRequestAdapter() {
        if (sRequestAdapter == null) {
            sRequestAdapter = new DefaultRequestAdapter();
        }
        return sRequestAdapter;
    }

    public static void setRequestAdapter(IRequestAdapter requestAdapter) {
        sRequestAdapter = requestAdapter;
    }

    public static ExecutorService getExecutorService() {
        if (sExecutorService == null) {
            sExecutorService = Executors.newCachedThreadPool();
        }
        return sExecutorService;
    }
}