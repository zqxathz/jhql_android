package com.jhlotus.vine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jhlotus.vine.util.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;




public class ListDialogUtils extends Dialog {



    ArrayList<expolist> list;
    ArrayList<activitylist> activity_list;
    RecyclerView mRecyclerView;
    Activity activity;
    ListAdapter adapter;
    ActivitysAdapter activitys_adapter;
    WaitAdapter adapter1;
    Context context;

    private int btn_id;

    public void setBtn_id(int btn_id) {
        this.btn_id = btn_id;
    }

    public ListDialogUtils(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public ListDialogUtils(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_dialog_layout);
        //按空白处取消
        setCanceledOnTouchOutside(true);



    }

    @Override
    public void show() {
        super.show();
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }

    private void initView(){

        mRecyclerView = findViewById(R.id.rv_list);
        Log.d("context", context+"");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        switch (btn_id){
            case R.id.btn_visitor_enter:
                /*((TextView) findViewById(R.id.tv_title)).setText("请选择展会");
                list = new ArrayList<expolist>();
                adapter = new ListAdapter();
                mRecyclerView.setAdapter(adapter);
                break;*/
            case R.id.btn_vip_reg:
                ((TextView) findViewById(R.id.tv_title)).setText("请选择展会");
                list = new ArrayList<expolist>();
                adapter = new ListAdapter();
                mRecyclerView.setAdapter(adapter);
                break;
            case R.id.btn_activity_reg:
                ((TextView) findViewById(R.id.tv_title)).setText("请选择活动");
                activity_list = new ArrayList<activitylist>();
                activitys_adapter = new ActivitysAdapter();
                mRecyclerView.setAdapter(activitys_adapter);
                break;
            default:break;
        }


    }

    private void initData(){
        switch (btn_id){
            case R.id.btn_visitor_enter:
            case R.id.btn_vip_reg:
                new GetData("https://www.jhlotus.com/crm/locale/getexlist").execute();
                break;
            case R.id.btn_activity_reg:
                new GetData("https://www.jhlotus.com/activity/locale/getexlist").execute();
                break;
        }

    }

    private void initEvent(){
       findViewById(R.id.imgbtn_close).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              dismiss();
           }
       });

    }

    class WaitAdapter extends RecyclerView.Adapter<WaitAdapter.MyViewHolder> {
        @Override
        public WaitAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(
                    context).inflate(R.layout.wait_text_item, parent,
                    false);
            WaitAdapter.MyViewHolder holder = new WaitAdapter.MyViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(WaitAdapter.MyViewHolder holder, int position) {
            //
            holder.tv_wait.setText("正在读取,请稍后...");
        }

        @Override
        public int getItemCount()
        {
            return 0;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            private TextView tv_wait;
            public MyViewHolder(View view)
            {
                super(view);
                tv_wait=(TextView) view.findViewById(R.id.tv_wait);
            }
        }
    }

    class ActivitysAdapter extends RecyclerView.Adapter<ActivitysAdapter.MyViewHolder> implements View.OnClickListener {
        @Override
        public ActivitysAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_line_list_item,parent, false);
            ActivitysAdapter.MyViewHolder holder = new ActivitysAdapter.MyViewHolder(view);
            view.setOnClickListener(this);
            return holder;

        }

        @Override
        public void onBindViewHolder(ActivitysAdapter.MyViewHolder holder, int position) {
            holder.tv.setText(activity_list.get(position).getExpo());
            holder.tv_activity.setText(activity_list.get(position).getTitle());
            holder.tv_date.setText(activity_list.get(position).getDate()+" "+activity_list.get(position).getPlace());

        }

        @Override
        public int getItemCount()
        {
            return activity_list.size();
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            int position=mRecyclerView.getChildAdapterPosition(v);
            if(activity_list.get(position).getType()==1){
                intent = new Intent(getContext(), ActivitysActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("oid",activity_list.get(position).getId());
                bundle.putInt("id",activity_list.get(position).getId());
                bundle.putString("name",activity_list.get(position).getTitle());
                bundle.putString("expo_name",activity_list.get(position).getExpo());

                intent.putExtras(bundle);
                getContext().startActivity(intent);
                dismiss();
            }
            //Toast.makeText(getContext(),"这是"+position,Toast.LENGTH_SHORT).show();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            private TextView tv,tv_activity;
            private TextView tv_date;
            public MyViewHolder(View view)
            {
                super(view);
                tv=(TextView) view.findViewById(R.id.line_item_tv);
                tv_activity=(TextView) view.findViewById(R.id.line_item_tv_name);
                tv_date=(TextView) view.findViewById(R.id.line_item_tv_date);


            }
        }


    }

    class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> implements View.OnClickListener {
        @Override
        public ListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(
                    context).inflate(R.layout.line_list_item, parent,
                    false);
            ListAdapter.MyViewHolder holder = new ListAdapter.MyViewHolder(view);
            view.setOnClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(ListAdapter.MyViewHolder holder, int position) {
            holder.tv.setText(list.get(position).getTitle());
            holder.tv_date.setText(list.get(position).getDate()+" "+list.get(position).getPlace());

        }

        @Override
        public int getItemCount()
        {
            return list.size();
        }
        @Override
        public void onClick(View v) {
            Intent intent;
            int position=mRecyclerView.getChildAdapterPosition(v);
            View parent_v = (View)v.getParent();

            String viewname = "";//getResources().getResourceEntryName(parent_v.getId());
            Log.d("viewname",viewname);
            switch (btn_id){
                case R.id.btn_visitor_enter:
                    intent = new Intent(getContext(), VisitorEnterActivity.class);
                    intent.putExtra("oid",list.get(position).getId());
                    getContext().startActivity(intent);
                    dismiss();
                    break;
                case R.id.btn_vip_reg:
                    //Toast.makeText(getContext(),"测试一下",Toast.LENGTH_LONG).show();
                    intent = new Intent(getContext(), VipRegActivity.class);
                    intent.putExtra("oid",list.get(position).getId());
                    getContext().startActivity(intent);
                    dismiss();
                    break;
                default: break;
            }

            //SnackbarUtil.ShortSnackbar(coordinatorLayout,"点击第"+position+"个",SnackbarUtil.Info).show();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            private TextView tv;
            private TextView tv_date;

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

    private class GetData extends AsyncTask<String, Integer, String> {
        private String url;

        public GetData(String url) {
            super();
            this.url=url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.tv_wait2).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            //appdata.setMobile("18606829855");


            FormBody body = new FormBody.Builder()
                    .add("mobile",appdata.getMobile())
                    .add("appclient","1")
                    .build();

            OkHttpClient client = new OkHttpClient();

            Log.d("http",this.url);
            final Request request = new Request.Builder()
                    .addHeader("X-Requested-With","XMLHttpRequest")
                    .addHeader("cookie",appdata.getSession())
                    .post(body)
                    .url(this.url)
                    .build();
           // Log.d("body",body.value(0));
            //Log.d("text",request.);
            Call call2 = client.newCall(request);
            try{
                Response res = call2.execute();
                String res_body = res.body().string();
                //Log.d("text:",res_body);
                Common.showLogCompletion(res_body,200);
                return res_body;
            }catch (IOException e){
                return "";
            }
            catch (Exception e){
                Log.d("error:",e.toString());
            }
            return "";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //WeiboDialogUtils.closeDialog(mDialogLoading);
            if(!TextUtils.isEmpty(result)){
                findViewById(R.id.tv_wait2).setVisibility(View.GONE);
                try {
                    Object object = new JSONTokener(result).nextValue();
                    if (object instanceof JSONObject){
                        dismiss();
                        JSONObject jsonObject = new JSONObject(result);
                        String result_msg = jsonObject.getString("message");
                        Toast.makeText(getContext(),result_msg,Toast.LENGTH_SHORT).show();

                    }else if(object instanceof JSONArray){
                        JSONArray jsonArray = new JSONArray(result);
                        switch (btn_id){
                            case R.id.btn_visitor_enter:
                            case R.id.btn_vip_reg:
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    expolist expo = new expolist();
                                    expo.setTitle(jsonObject.getString("name"));
                                    expo.setId(jsonObject.getInt("id"));
                                    expo.setDate(jsonObject.getLong("start_time"),jsonObject.getLong("end_time"));
                                    expo.setPlace(jsonObject.getString("place"));
                                    list.add(expo);
                                }
                                adapter.notifyDataSetChanged();
                                if (list.size()==0){
                                    dismiss();
                                    Toast.makeText(getContext(),"当前没有可用展会",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.btn_activity_reg:
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    activitylist activity = new activitylist();
                                    activity.setTitle(jsonObject.getString("name"));
                                    activity.setType(jsonObject.getInt("type"));
                                    activity.setId(jsonObject.getInt("id"));
                                    activity.setDate(jsonObject.getLong("expo_start_date"),jsonObject.getLong("expo_end_date"));
                                    activity.setPlace(jsonObject.getString("expo_place"));
                                    activity.setExpo(jsonObject.getString("expo_name"));
                                    activity_list.add(activity);
                                }
                                activitys_adapter.notifyDataSetChanged();
                                Log.d("当前活动数量",Integer.toString(activity_list.size()));
                                if (activity_list.size()==0){
                                    dismiss();
                                    Toast.makeText(getContext(),"当前没有可用活动",Toast.LENGTH_SHORT).show();
                                }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                new AlertDialog.Builder(getContext())
                        .setTitle("提示")
                        .setMessage("网络错误,请确定网络状态")
                        .setCancelable(false)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                dismiss();
            }
        }
    }
}
