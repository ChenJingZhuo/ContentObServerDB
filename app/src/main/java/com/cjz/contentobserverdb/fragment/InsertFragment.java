package com.cjz.contentobserverdb.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cjz.contentobserverdb.MainActivity;
import com.cjz.contentobserverdb.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InsertFragment extends Fragment {

    MainActivity main;
    EditText editText;

    public InsertFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        main = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_insert, container, false);
        editText = view.findViewById(R.id.et_insert);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    switch (main.flag) {
                        case 0:
                            main.values.put("name", editText.getText().toString());
                            if (flag){
                                main.update();
                                flag=false;
                            }
                            main.updateStr = "";
                            main.refresh();
                            main.refresh2();
                            MainActivity.hideKeyBoard(editText);
                            getFragmentManager().beginTransaction().hide(main.getInsertFragment()).commit();
                            break;
                        case 1:
                            main.values.put("name", editText.getText().toString());
                            if (flag){
                                main.insert();
                                flag=false;
                            }
                            main.updateStr = "";
                            main.refresh();
                            main.refresh2();
                            MainActivity.hideKeyBoard(editText);
                            getFragmentManager().beginTransaction().hide(main.getInsertFragment()).commit();
                            Toast.makeText(main, "添加成功", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
                return false;
            }
        });
        return view;
    }
    public boolean flag=true;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.showKeyBoard(editText);
        editText.setText(main.updateStr);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (main.getInsertFragment().isVisible()) {
            MainActivity.showKeyBoard(editText);
            editText.setText(main.updateStr);
        }
    }
}
