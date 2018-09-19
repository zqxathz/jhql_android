package com.jhlotus.vine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>,View.OnClickListener,AdminregFragment.OnFragmentInteractionListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private static int CurrSwitchMode =0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPhoneView;
    private EditText mCodeView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mGetcodeView;

    public String session;
    public CountDownTimer cdt;

    String lastmobile="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        //mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();


        final Toast toast=Toast.makeText(getApplicationContext(), "默认的Toast", Toast.LENGTH_SHORT);


        session="";

        final Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPhoneView = (EditText) findViewById(R.id.phone);
        mPhoneView.addTextChangedListener(mTextWatcher);
        mPhoneView.setFilters(new InputFilter[]{new  InputFilter.LengthFilter(11)});

        mCodeView = (EditText) findViewById(R.id.code);
        mCodeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length()==4){
                    mSignInButton.setEnabled(true);
                }else{
                    mSignInButton.setEnabled(false);
                }
            }
        });
        mGetcodeView = (Button) findViewById(R.id.getcode);
        mGetcodeView.setOnClickListener(this);
        findViewById(R.id.btn_login_switch).setOnClickListener(this);
        findViewById(R.id.sign_in_button2).setOnClickListener(this);

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        String result = sharedPreferences.getString("mobile", null);
        if (result!=null){
            mPhoneView.setText(result);
        }
        result = sharedPreferences.getString("login_model","0");

        switchLoginMode(Integer.parseInt(result));

        findViewById(R.id.imgbtn_cleare_mobile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneView.setText("");

                if (cdt!=null) cdt.cancel();
                mGetcodeView.setText("获取验证码");
                mPhoneView.setFocusable(true);
                mPhoneView.setFocusableInTouchMode(true);
                mPhoneView.requestFocus();
            }
        });




    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           //
        }

        @Override
        public void afterTextChanged(Editable s) {
            mCodeView.setEnabled(false);
            ((Button) findViewById(R.id.sign_in_button)).setEnabled(false);
            if (validatePhone(s.toString())){
                if (!lastmobile.equals(s.toString())){
                    if (cdt!=null) cdt.cancel();
                    mGetcodeView.setText("获取验证码");
                    mGetcodeView.setEnabled(true);
                }else{
                    //mGetcodeView.setEnabled(false);
                    cdt.start();
                }
            }else{
                mGetcodeView.setEnabled(false);
            }
        }
    };

    private boolean validatePhone(String mobiles){
        /*
		移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		联通：130、131、132、152、155、156、185、186
		电信：133、153、180、189、（1349卫通）
		总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		*/
        String telRegex = "[1][3456789]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles)) return false;
        else return mobiles.matches(telRegex);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.getcode:
                 lastmobile = mPhoneView.getText().toString();
                 getSession();
                 //Toast.makeText(getApplicationContext(),mPhoneView.getText().toString(),Toast.LENGTH_SHORT).show();

                break;
            case R.id.sign_in_button:
                showProgress(true);
                validatesmscode();
                break;
            case R.id.btn_login_switch:
                if (CurrSwitchMode==0){
                    switchLoginMode(1);
                }else{
                    switchLoginMode(0);
                }
                break;
            case R.id.sign_in_button2:
                showProgress(true);
                getSession();
                //loginwithpass();
                break;
            default:
                break;
        }
    }

    private void loginwithpass(){

        //应用全局数据
        ApplicationData appdata = (ApplicationData)getApplication();
        appdata.setMobile(mPhoneView.getText().toString());

        EditText mPassView = findViewById(R.id.edit_login_pass);
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("mobile",appdata.getMobile())
                .add("password",mPassView.getText().toString())
                .add("__token__",appdata.getToken())
                .add("appclient","1")
                .build();
        Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",appdata.getSession())
                .url("https://www.jhlotus.com/activity/reg/login")
                .post(body)
                .build();

        Call call2 = client.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("info_call2fail", e.toString());
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                System.out.print(str);
                if (response.isSuccessful()) {
                    //  Log.i("info_call2success",response.body().string());

                    try {
                        JSONObject jsonObject1 = new JSONObject(str);

                        String token = "";
                        if (jsonObject1.has("newtoken")) {
                            token = jsonObject1.getString("newtoken");
                        }
                        String res = jsonObject1.getString("response");
                        final String msg = jsonObject1.getString("message");

                        if (res.equals("success")/* && (token != null || token.length() > 0)*/) {
                            //Log.i("token is:",token);
                            ApplicationData appdata = (ApplicationData) getApplication();
                            //appdata.setToken(token);
                            appdata.setMobile(mPhoneView.getText().toString());
                            Log.d("mobile:", appdata.getMobile());
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    if (msg.equals("ok")) {
                                        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("login_model", Integer.toString(CurrSwitchMode));
                                        editor.putString("mobile", mPhoneView.getText().toString());
                                        editor.commit();
                                        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("mobile", mPhoneView.getText().toString());
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }));
                        } else {
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void switchLoginMode(int mode){
        CurrSwitchMode = mode;
        Button btn_switch_mode = (Button) findViewById(R.id.btn_login_switch);
        ((TextView)findViewById(R.id.code)).setText(null);
        ((TextView)findViewById(R.id.edit_login_pass)).setText(null);
        if (mode==0){
            btn_switch_mode.setText("使用密码登录");
            findViewById(R.id.til_login_pass).setVisibility(View.GONE);
            findViewById(R.id.til_login_smscode).setVisibility(View.VISIBLE);
            findViewById(R.id.getcode).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button2).setVisibility(View.GONE);
        }else if(mode==1){
            btn_switch_mode.setText("使用短信验证码登录");
            findViewById(R.id.til_login_pass).setVisibility(View.VISIBLE);
            findViewById(R.id.til_login_smscode).setVisibility(View.GONE);
            findViewById(R.id.getcode).setVisibility(View.VISIBLE);
            findViewById(R.id.getcode).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button2).setVisibility(View.VISIBLE);

        }
    }

    private void getSession(){
        ApplicationData appdata = (ApplicationData)getApplication();
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",appdata.getSession())
                .get()
                .url("https://www.jhlotus.com/crm/index/index")
                .build();
        //通过client发起请求

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("info_call2fail",e.toString());
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String str = response.body().string();
                    Headers headers = response.headers();
                    Log.d("info_headers", "header " + headers);

                    List<String> cookies = headers.values("Set-Cookie");
                    if (cookies.size()>0){
                        String session1 = cookies.get(0);
                        //Log.d("info_cookies", "onResponse-size: " + cookies);
                        session = session1.substring(0, session1.indexOf(";"));
                        Log.d("info_s", "session is  :" + session1);
                        ApplicationData appdata = (ApplicationData)getApplication();
                        appdata.setSession(session);
                    }
                    //Log.i("token_header:",headers.values("__token__").toString());


                    //Log.i("body:",str);
                    try {
                        JSONObject jsonObject = new JSONObject(str);
                        String token = jsonObject.getString("token");
                        String res = jsonObject.getString("response");

                        if (res.equals("success") && token.length()>0){
                            //Log.i("token is:",token);
                            final ApplicationData appdata = (ApplicationData)getApplication();
                            appdata.setToken(token);
                            if(CurrSwitchMode==0){
                                LoginActivity.this.getSmsCode(LoginActivity.this.mPhoneView.getText().toString());
                            }else{
                                LoginActivity.this.loginwithpass();
                            }

                        }else{
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "出错了,无法得到TOKEN", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                            }
                        });
                    }

                    /*System.out.println(response);
                    System.out.println(str);*/
                    //TextMessage.set
                    //Toast.makeText(null,str,Toast.LENGTH_LONG).show();
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // String a = str;
//                            mTextMessage.setText(str);
//                        }
//                    });

                }

            }
        });
    }

    public void getSmsCode(String phone){
        ApplicationData appdata = (ApplicationData)getApplication();
        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("mobilephone",mPhoneView.getText().toString())
                .add("__token__",appdata.getToken())
                .add("appclient","1")
                .build();
        Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",appdata.getSession())
                .url("https://www.jhlotus.com/crm/reg/getcode")
                .post(body)
                .build();

        Call call2 = client.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("info_call2fail",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    //Log.i("info_call2success",response.body().string());
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String token = jsonObject.getString("newtoken");
                        String res = jsonObject.getString("response");
                        final String msg = jsonObject.getString("message");

                        if (res.equals("success") && token.length()>0){
                            //Log.i("token is:",token);
                            ApplicationData appdata = (ApplicationData)getApplication();
                            appdata.setToken(token);

                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    mGetcodeView.setEnabled(false);
                                    cdt = new CountDownTimer(60000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            mGetcodeView.setText(millisUntilFinished/1000 + "秒");
                                        }
                                        @Override
                                        public void onFinish() {
                                            mGetcodeView.setEnabled(true);
                                            mGetcodeView.setText("重新获取验证码");
                                        }

                                    };
                                    cdt.start();
                                    mCodeView.setText("");
                                    mCodeView.setEnabled(true);
                                    mCodeView.setFocusable(true);
                                    mCodeView.setFocusableInTouchMode(true);
                                    mCodeView.requestFocus();
                                }
                            }));



                        }else{
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Headers headers = response.headers();
                /*ApplicationData appdata = (ApplicationData)getApplication();
                appdata.setToken(headers.values("__token__").toString());*/
                //Log.i("info_respons.headers",headers+"");
            }
        });
    }

    public void validatesmscode(){
        //应用全局数据
        ApplicationData appdata = (ApplicationData)getApplication();
        appdata.setMobile(mPhoneView.getText().toString());

        OkHttpClient client = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("smscode",mCodeView.getText().toString())
                .add("__token__",appdata.getToken())
                .add("appclient","1")
                .build();
        Request request = new Request.Builder()
                .addHeader("X-Requested-With","XMLHttpRequest")
                .addHeader("cookie",appdata.getSession())
                .url("https://www.jhlotus.com/crm/reg/validatesmscode")
                .post(body)
                .build();

        Call call2 = client.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("info_call2fail",e.toString());
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                  //  Log.i("info_call2success",response.body().string());
                    String str = response.body().string();
                    try {
                        JSONObject jsonObject1 = new JSONObject(str);

                        String token = "";
                        if (jsonObject1.has("newtoken")){
                            token = jsonObject1.getString("newtoken");
                        }
                        String res = jsonObject1.getString("response");
                        final String msg = jsonObject1.getString("message");
                        final String status= jsonObject1.optString("status","0");

                        if (res.equals("success") && (token != null || token.length()>0)){
                            //Log.i("token is:",token);
                            ApplicationData appdata = (ApplicationData)getApplication();
                            appdata.setToken(token);
                            appdata.setMobile(mPhoneView.getText().toString());
                            Log.d("mobile:",appdata.getMobile());
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {

                                    if (status.equals("isbaned")){
                                        showProgress(false);
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (status.equals("notfindadmin")){

                                        findViewById(R.id.login_form).setVisibility(View.GONE);

                                        mProgressView.setVisibility(View.GONE);
                                        mProgressView.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                mProgressView.setVisibility(View.GONE);
                                            }
                                        });

                                        FragmentManager fm = getFragmentManager();
                                        fm.beginTransaction()
                                                .add(R.id.fladminreg,AdminregFragment.newInstance("",""))
                                                //.addToBackStack(null)
                                                .commit();



                                        return;
                                    }
                                    if (msg.equals("ok")){
                                        //showProgress(false);
                                        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("mobile",mPhoneView.getText().toString());
                                        editor.putString("login_model",Integer.toString(CurrSwitchMode));
                                        editor.commit();
                                        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("mobile",mPhoneView.getText().toString());
                                        startActivity(intent);
                                        finish();
                                    }


                                }
                            }));
                        }else{
                            LoginActivity.this.runOnUiThread((new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                            }
                        });
                    }
                }
                //Headers headers = response.headers();
                /*ApplicationData appdata = (ApplicationData)getApplication();
                appdata.setToken(headers.values("__token__").toString());*/
                //Log.d("info_respons.headers",headers+"");
               // Log.d("info_res:",response.body().toString());
            }
        });

    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
           // mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("调试",uri.getHost());
        if (uri.getHost().equals("back")){
            findViewById(R.id.login_form).setVisibility(View.VISIBLE);
        }
        if (uri.getHost().equals("skip") || uri.getHost().equals("gomain")){
            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("mobile",mPhoneView.getText().toString());
            startActivity(intent);
            finish();
        }

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

