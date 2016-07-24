package com.monlong.gridlayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.monlong.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @Descirption:
 * @Author: monlong
 * @Email: 826309156@qq.com
 * @Date: 2016-07-18 08:50
 * @Version: 1.0.0
 */
public class ExampleActivity extends Activity {

    private Button mAddButton;
    private GridLayout mGridLayout;
    private ExampleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        mGridLayout = (GridLayout) findViewById(R.id.gridlayout);
        mAddButton = (Button) findViewById(R.id.add);

        List<String> strings = generateGridLayoutData();
        mAdapter = new ExampleAdapter(this, strings);
        mGridLayout.setAdapter(mAdapter);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.addItem("Item");
            }
        });

        mGridLayout.setOnItemClickListener(new GridLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int index) {
                Toast.makeText(ExampleActivity.this, String.valueOf(index), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> generateGridLayoutData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 0; i++) {
            strings.add(new String().valueOf(i));
        }
        return strings;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
