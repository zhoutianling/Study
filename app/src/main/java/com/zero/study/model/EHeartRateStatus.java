package com.zero.study.model;

import android.content.Context;

import androidx.annotation.Keep;

import com.zero.study.R;


@Keep
public enum EHeartRateStatus {
    Sitting(R.string.sitting),
    Walking(R.string.walking),
    LyingDown(R.string.lying_down),
    Running(R.string.running),
    Swimming(R.string.swimming);

    private final int key;

    EHeartRateStatus(int i) {
        this.key = i;
    }

    public final String getKey(Context context) {
        return context.getResources().getString(this.key);
    }
}
