/*
 * Copyright (C) 2020 The Pixel Experience Project
 *               2020 The exTHmUI Open Source Project
 *               2022 Project Kaleidoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.internal.util.custom;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MeizuPropsUtils {

    private static final String TAG = MeizuPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String DISGUISE_PROPS_FOR_MUSIC_APP = "persist.sys.disguise_props_for_music_app";

    private static final Map<String, Map<String, Object>> propsToChange = new HashMap<>();
    private static final Map<String, String[]> packagesToChange = new HashMap<>();

    static {
        propsToChange.put("MeiZu", createMeiZuProps());
        packagesToChange.put("MeiZu", new String[]{
                "com.hihonor.cloudmusic",
		"com.netease.cloudmusic",
		"com.tencent.qqmusic",
		"com.kugou.android",
		"com.kugou.android.lite",
		"cmccwm.mobilemusic",
		"cn.kuwo.player",
		"com.meizu.media.music"
        });
    }

    private static Map<String, Object> createMeiZuProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("BRAND", "meizu");
        props.put("MANUFACTURER", "Meizu");
        props.put("DEVICE", "m1892");
        props.put("DISPLAY","Flyme");
        props.put("PRODUCT","meizu_16thPlus_CN");
        props.put("MODEL", "meizu 16th Plus");
        return props;
    }

    public static void setProps(Context context) {
        if (!SystemProperties.getBoolean(DISGUISE_PROPS_FOR_MUSIC_APP, false)) {
            return;
        }

        final String packageName = context.getPackageName();
        if (packageName == null || packageName.isEmpty()){
            return;
        }

        for (String device : packagesToChange.keySet()) {
            String[] packages = packagesToChange.get(device);
            if (Arrays.asList(packages).contains(packageName)) {
                dlog("Defining props for: " + packageName);
                Map<String, Object> props = propsToChange.get(device);
                for (Map.Entry<String, Object> prop : props.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
                break;
            }
        }
    }

    private static void setPropValue(String key, Object value){
        try {
            dlog("Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
