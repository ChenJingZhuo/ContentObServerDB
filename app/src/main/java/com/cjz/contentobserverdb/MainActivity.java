package com.cjz.contentobserverdb;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.cjz.contentobserverdb.adapter.AppAdapter;
import com.cjz.contentobserverdb.bean.Information;
import com.cjz.contentobserverdb.fragment.InsertFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ContentResolver resolver;
    private Uri uri;
    public ContentValues values;

    private ImageView mIvBack;
    /**
     * 标题
     */
    private TextView mTvTitle;
    private ImageView mIvInsert;
    private SwipeMenuListView mListView;
    private AppAdapter adapter;
    private AlertDialog dialog;
    private SwipeRefreshLayout mSwipeRefresh;

    public static void showKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }

    private InsertFragment insertFragment;

    public InsertFragment getInsertFragment() {
        return insertFragment;
    }

    public static void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);
        initView();
        //createDB();
        resolver = getContentResolver();
        uri = Uri.parse("content://com.cjz.contentobserverdb/info");
        refresh();
        adapter = new AppAdapter(this);
        mListView.setAdapter(adapter);
    }
    
    private void createDB(){
        PersonDBOpenHelper helper = new PersonDBOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        for (int i = 0; i < 3; i++) {
            ContentValues values = new ContentValues();
            values.put("name","cjz"+i);
            db.insert("info",null,values);
        }
        db.close();
    }

    public int flag = -1;
    public int position = 0;
    public Information information;
    public String updateStr="";
    public void initView() {
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvTitle.setText("内容观察者观察的数据库");
        mIvInsert = (ImageView) findViewById(R.id.iv_insert);
        mIvInsert.setOnClickListener(this);
        mListView = (SwipeMenuListView) findViewById(R.id.list_view);
        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, final int index) {
                information = (Information) adapter.getItem(position);
                switch (index) {
                    case 0:
                        // open
                        values = new ContentValues();
                        flag = 0;
                        updateStr=information.getInfo();
                        if (insertFragment != null) {
                            insertFragment.flag=true;
                            getSupportFragmentManager().beginTransaction().show(insertFragment).commit();
                        } else {
                            insertFragment = new InsertFragment();
                            replace(insertFragment);
                        }
                        break;
                    case 1:
                        // delete
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                .setMessage("您确定要删除该信息吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        informations.remove(adapter.getItem(position));
                                        int deleteCount = resolver.delete(uri, "_id = ?", new String[]{information.getId()});
                                        Toast.makeText(MainActivity.this, "成功删除了" + deleteCount + "行", Toast.LENGTH_SHORT).show();
                                        Log.d("数据库应用", "删除");
                                        refresh2();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create();
                        dialog.show();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
                mListView.smoothOpenMenu(position);
            }
        });
        mListView.setOpenInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return 200;
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeColors(Color.BLUE);
        mSwipeRefresh.setProgressViewEndTarget(true,100);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                refresh2();
                Log.d("数据库应用", "查询结果：" + informations.toString());
            }
        });
    }

    public void insert() {
        do {
            resolver.insert(uri, values);
            Log.d("数据库应用", "添加");
        }while (false);
    }

    private List<Information> informations;

    public List<Information> getInformations() {
        return informations;
    }

    public void refresh() {
        if (informations!=null){
            informations.clear();
        }else {
            informations = new ArrayList<>();
        }
        Cursor cursor = resolver.query(uri, new String[]{"_id", "name"}, null, null, null);
        while (cursor.moveToNext()) {
            informations.add(new Information(cursor.getString(0),cursor.getString(1)));
        }
        cursor.close();
        if (mSwipeRefresh.isRefreshing()){
            mSwipeRefresh.setRefreshing(false);
        }
    }

    public void update() {
        int updateCount = resolver.update(uri, values, "_id = ?", new String[]{information.getId()});
        Toast.makeText(MainActivity.this, "成功更新了" + updateCount + "行", Toast.LENGTH_SHORT).show();
        Log.d("数据库应用", "更新");
    }

    public void refresh2() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        values = new ContentValues();
        switch (v.getId()) {
            case R.id.iv_insert:
                flag = 1;
                if (insertFragment != null) {
                    insertFragment.flag=true;
                    getSupportFragmentManager().beginTransaction().show(insertFragment).commit();
                } else {
                    insertFragment = new InsertFragment();
                    replace(insertFragment);
                }
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    public void replace(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.frag, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (insertFragment != null && insertFragment.isVisible()) {
            updateStr="";
            getSupportFragmentManager().beginTransaction().hide(insertFragment).commit();
        } else {
            finish();
        }
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "open" item
            SwipeMenuItem openItem = new SwipeMenuItem(MainActivity.this);
            // set item background
            openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                    0xCE)));
            // set item width
            openItem.setWidth(dp2px(90));
            // set item title
            openItem.setTitle("更新");
            // set item title fontsize
            openItem.setTitleSize(18);
            // set item title font color
            openItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(MainActivity.this);
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            deleteItem.setWidth(dp2px(90));
            // set a icon
            deleteItem.setIcon(R.drawable.delete);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };

    public int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
