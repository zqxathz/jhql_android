package com.jhlotus.vine;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.zxing.activity.CaptureActivity;
import com.jhlotus.vine.util.Common;
import com.jhlotus.vine.util.Constant;
import com.jhlotus.vine.util.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{



    RecyclerView mRecyclerView;
    RecyclerView mRecyclerView_vip;
    MyAdapter adapter;
    ArrayList<expolist> list;
    Timer timer;
    Timer update_timer;



    ListDialogUtils dialogUtils;

    long cur_time;

    private ListDialogUtils mListDialog;

    private Dialog mDialogLoading;




    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what){
                case 1:
                    int val = data.getInt("value");
                    Log.d("test:",val+"");
                    if (val==0){
                        AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("确认" ) ;
                        builder.setMessage(data.getString("message") ) ;
                        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    };
                    break;
                case 2:
                    timer.cancel();
                    AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("确认" ) ;
                    builder.setMessage("当前会话已经失效" ) ;
                    builder.setCancelable(false);
                    builder.setNegativeButton("关闭" ,  new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.show();
                    break;
            }
        }
    };

    private List<View> getAllChildViews(Class<?> T) {
        View view = this.getWindow().getDecorView();
        return getAllChildViews(view, T);
    }

    private List<View> getAllChildViews(View parent, Class<?> T) {
        List<View> allchildren = new ArrayList<View>();
        if (parent instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) parent;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                if (viewchild.getClass().equals(T)) {
                    allchildren.add(viewchild);
                }
                allchildren.addAll(getAllChildViews(viewchild, T));
            }
        }
        return allchildren;
    }

    TimerTask task = new TimerTask(){
        public void run() {
            ApplicationData appdata = (ApplicationData)getApplication();
            String session = appdata.getSession();
            String mobile=Common.getSession(session);
            if (mobile==null || mobile!=appdata.getMobile()){
                Log.d("Tag", "run: "+mobile);
                Message msg = new Message();
                msg.what=2;
                handler.sendMessage(msg);
            }
        }
    };

    TimerTask update_task = new TimerTask() {
        @Override
        public void run() {

           // Log.d("aaa",cur_activity.getLocalClassName());
            if (isAppIsInBackground(MainActivity.this)==false){

               // Log.d("aaa","thisis");
                Activity cur_activity = getRunningActivity();
                //String temp =cur_activity.getLocalClassName();
                Log.d("test","1111");
                //if (cur_activity.getLocalClassName().equals("ActivitysActivity")){
                //if (cur_activity!=null){
                    Log.d("activiy",cur_activity.getLocalClassName());
                    Update.getUpdate(cur_activity).checkupdate(false);
                //}

                //}

            }

        }
    };

    public static Activity getRunningActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread")
                    .invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
        //throw new RuntimeException("Didn't find the running activity");
    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                //前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_visitor_enter).setOnClickListener(this);
        findViewById(R.id.btn_vip_reg).setOnClickListener(this);
        findViewById(R.id.btn_activity_reg).setOnClickListener(this);
        findViewById(R.id.fab_exit).setOnClickListener(this);
        findViewById(R.id.fab_switch_user).setOnClickListener(this);
        findViewById(R.id.fab_update_app).setOnClickListener(this);
        findViewById(R.id.fab_download).setOnClickListener(this);
        findViewById(R.id.scrollView3).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (((FloatingActionsMenu) findViewById(R.id.multiple_actions)).isExpanded()){
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                }
                return false;
            }
        });



        ((TextView)findViewById(R.id.tv_mobile)).setText("管理员:"+getIntent().getStringExtra("mobile"));

//        ((Button)findViewById(R.id.btn_visitor_enter)).
        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.btn_colored_material);

       /* ((InsetDrawable)drawable).
        //XmlPullParser xml= Xml.newPullParser();

        InsetDrawable d = (InsetDrawable) findViewById(R.id.btn_visitor_enter).getBackground();
        d.*/

        //((Button)findViewById(R.id.btn_visitor_enter)).setBackground(drawable);


        mRecyclerView = findViewById(R.id.vistor_ex_list);
        mRecyclerView_vip = findViewById(R.id.vip_ex_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView_vip.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<expolist>();

        adapter = new MyAdapter();
        mRecyclerView.setAdapter(adapter);

        cur_time = 0;
        if (!Common.isApkInDebug(this)){
            timer = new Timer(true);
            timer.schedule(task,600000, 600000);
        }

        update_timer = new Timer(false);
        update_timer.schedule(update_task,100,5*60*1000);
        //Update.getUpdate(this).checkupdate();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Update.getUpdate(MainActivity.this).checkupdate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long t = new Date().getTime();
            if (t>cur_time){
                Toast.makeText(this,"再次按返回退出",Toast.LENGTH_SHORT).show();
                cur_time= t+2000;
            }else{
                cur_time=0;
                this.moveTaskToBack(false);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*Log.d("touch","yes");
        if (((FloatingActionsMenu) findViewById(R.id.multiple_actions)).isExpanded()){
            ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
        }
*/
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        List<View> allRecyclerViews =getAllChildViews(mRecyclerView.getClass());
        for(int i=0;i<allRecyclerViews.size();i++){
            RecyclerView rview = (RecyclerView)allRecyclerViews.get(i);
            rview.setAdapter(null);
            rview =null;
        }
        //mDialogLoading = WeiboDialogUtils.createLoadingDialog(MainActivity.this,"正在加载,请稍后...");

        if (dialogUtils==null){
            dialogUtils = new ListDialogUtils(MainActivity.this);
        }
        dialogUtils.setBtn_id(view.getId());
        dialogUtils.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (view.getId()!=R.id.multiple_actions){
            ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
        }

        switch (view.getId()){
            case R.id.btn_visitor_enter:
                ApplicationData appdata = (ApplicationData)getApplication();
                //Toast.makeText(getApplicationContext(),appdata.getMobile(),Toast.LENGTH_LONG).show();
                //startQrCode();
               // mRecyclerView.setAdapter(adapter);
                    //list.clear();
                    //new GetData().execute();
               /* if (list.size()==0){

                }else{
                    list.clear();
                }*/
               /* TransData  transdata =  new TransData();
                transdata.setHandler(handler);
                int r = transdata.visitorEnter("v4a33077353d01740a04fe32237c63492ba0fcdd5804efb2885102249e70bd29c");
                if (r==1){
                    Toast.makeText(getApplicationContext(),"可以使用",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"无法使用",Toast.LENGTH_SHORT).show();
                }
*/
               dialogUtils.show();
                break;
            case R.id.btn_vip_reg:
                dialogUtils.show();
                break;
            case R.id.btn_activity_reg:
                dialogUtils.show();
                break;
            case R.id.fab_download:
                Intent download_intent = new Intent(MainActivity.this , DownloadActivity.class);
                startActivity(download_intent);
                break;
            case R.id.fab_exit:
                finish();
                break;
            case R.id.fab_switch_user:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                //intent.putExtra("mobile",mPhoneView.getText().toString());
                startActivity(intent);
                finish();
                break;
            case R.id.fab_update_app:
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                //update_timer.cancel();
                SharedPreferences sp = getSharedPreferences("ingoreList", 0);
                SharedPreferences.Editor mEdit1 = sp.edit();
                mEdit1.clear();
                mEdit1.commit();
                mEdit1.apply();
                Update.getUpdate(this).checkupdate(true);
                break;
            default:
                break;
        }
    }

    private class GetData extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            //appdata.setMobile("18606829855");

            OkHttpClient client = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                    .add("mobile",appdata.getMobile())
                    .add("appclient","1")
                    .build();
            Request request = new Request.Builder()
                    .addHeader("cookie",appdata.getSession())
                    .url("https://www.jhlotus.com/crm/locale/getexlist")
                    .post(body)
                    .build();
            try{
                Call call2 = client.newCall(request);
                Response res = call2.execute();
                String res_body = res.body().string();
                Log.d("text:",res_body);
                return res_body;

            }catch (Exception e){
                Log.d("error:",e.toString());
            }
            return "";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            WeiboDialogUtils.closeDialog(mDialogLoading);
            if(!TextUtils.isEmpty(result)){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        expolist expo = new expolist();
                        expo.setTitle(jsonObject.getString("name"));
                        expo.setId(jsonObject.getInt("id"));
                        expo.setDate(jsonObject.getLong("start_time"),jsonObject.getLong("end_time"));
                        expo.setPlace(jsonObject.getString("place"));
                        list.add(expo);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements View.OnClickListener {


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(
                    MainActivity.this).inflate(R.layout.line_list_item, parent,
                    false);
            MyViewHolder holder = new MyViewHolder(view);

            view.setOnClickListener(this);

            return holder;

        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            holder.tv.setText(list.get(position).getTitle());
            holder.tv_date.setText(list.get(position).getDate());
            holder.tv_place.setText(list.get(position).getPlace());
        }

        @Override
        public int getItemCount()
        {
            return list.size();
        }


        @Override
        public void onClick(View v) {

            int position=mRecyclerView.getChildAdapterPosition(v);

            View parent_v = (View)v.getParent();

            String viewname = getResources().getResourceEntryName(parent_v.getId());
            Log.d("viewname",viewname);
            if (viewname.equals("vistor_ex_list")){
                //Toast.makeText(getApplicationContext(),"点击了ID为"+list.get(position).getId()+"的项目",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, VisitorEnterActivity.class);
                intent.putExtra("oid",list.get(position).getId());
                startActivity(intent);
            }
            if (viewname.equals("vip_ex_list")){

            }


            //SnackbarUtil.ShortSnackbar(coordinatorLayout,"点击第"+position+"个",SnackbarUtil.Info).show();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            private TextView tv;
            private TextView tv_date;
            private TextView tv_place;
            public MyViewHolder(View view)
            {
                super(view);
                tv=(TextView) view.findViewById(R.id.line_item_tv);
                tv_date=(TextView) view.findViewById(R.id.line_item_tv_date);


            }
        }

        public void addItem(List<String> list, int position) {
            //list.add(position, list);
            notifyItemInserted(position);
            mRecyclerView.scrollToPosition(position);
        }

        public void removeItem(final int position) {
           /* final Meizi removed=meizis.get(position);
            meizis.remove(position);*/
            notifyItemRemoved(position);
            /*SnackbarUtil.ShortSnackbar(coordinatorLayout,"你删除了第"+position+"个item",SnackbarUtil.Warning).setAction("撤销", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItem(removed,position);
                    SnackbarUtil.ShortSnackbar(coordinatorLayout,"撤销了删除第"+position+"个item",SnackbarUtil.Confirm).show();
                }
            }).setActionTextColor(Color.WHITE).show();*/
        }

    }

    // 开始扫码
    private void startQrCode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            Toast.makeText(getApplicationContext(),scanResult,Toast.LENGTH_LONG).show();
            /*TransData  transdata =  new TransData();
            int r = transdata.visitorEnter(scanResult);
            if (r==1){
                Toast.makeText(getApplicationContext(),"可以使用",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"无法使用",Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的文件访问权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
