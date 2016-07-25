package test.apker.android.ss.com.apkertest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by chenlifeng on 16/5/18.
 */
public class TestActivity1 extends Activity {

    public static void launch(Context context) {
        Intent it = new Intent(context, TestActivity1.class);
        context.startActivity(it);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test1);
    }
}
