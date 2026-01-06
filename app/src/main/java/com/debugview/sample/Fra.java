package com.debugview.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @author newChar
 * date 2025/7/3
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
public class Fra extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new FrameLayout(container.getContext());
    }

}
