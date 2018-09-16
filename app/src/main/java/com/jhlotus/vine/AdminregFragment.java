package com.jhlotus.vine;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminregFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminregFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminregFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AdminregFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminregFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminregFragment newInstance(String param1, String param2) {
        AdminregFragment fragment = new AdminregFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vw = inflater.inflate(R.layout.fragment_adminreg, container, false);
        vw.findViewById(R.id.fragment_btn_skip).setOnClickListener(this);
        vw.findViewById(R.id.fragment_btn_ok).setOnClickListener(this);
        return vw;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void onBack(Uri uri){

        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }else{
            Log.d("test","this is onDetach");
        }
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
      // Context context = activity.getBaseContext();
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        }
    }


    @Override
    public void onStop() {

        onBack(Uri.parse("login://back"));
        super.onStop();
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    private boolean CheckPassword(String pass){
        boolean result = true;
        if (pass.length()<6 || pass.length()>12){
            result = false;
            Toast.makeText(getActivity().getBaseContext(),"密码长度不正确",Toast.LENGTH_SHORT).show();
        }
        return result;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_btn_skip:
                new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setCancelable(true)
                        .setTitle("是否要跳过设置密码?")
                        .setMessage("设置密码后,在下次登录时可以使用密码,以免去有时短信验证码发送延迟")
                        .setPositiveButton("确定跳过", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onButtonPressed(Uri.parse("login://skip"));
                            }
                        })
                        .setNegativeButton("再想想",null)
                        .show();
                break;
            case R.id.fragment_btn_ok:
                String  password = ((EditText) getView().findViewById(R.id.edit_password)).getText().toString();
                if (CheckPassword(password)){
                    new task().execute();
                }
                break;

        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class task extends AsyncTask<String,Integer,String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("success")){
                Toast.makeText(getActivity(),"密码创建成功",Toast.LENGTH_SHORT).show();
                onButtonPressed(Uri.parse("login://gomain"));
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            ApplicationData appdata = (ApplicationData) ApplicationData.getMyApplication();
            OkHttpClient client = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                    .add("mobile",appdata.getMobile())
                    .add("password",((EditText) getView().findViewById(R.id.edit_password)).getText().toString())
                    .add("appclient","1")
                    .build();
            Request request = new Request.Builder()
                    .addHeader("cookie",appdata.getSession())
                    .url("https://www.jhlotus.com/activity/reg/setpass")
                    .post(body)
                    .build();
            try{
                Call call2 = client.newCall(request);
                Response res = call2.execute();
                String res_body = res.body().string();
                //Log.d("text:",res_body);
                System.out.print(res_body);
                return res_body;

            }catch (Exception e){
                Log.d("error:",e.toString());
            }
            return null;
        }
    }
}
