package com.hjnerp.business.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hjnerp.common.Constant;
import com.hjnerp.dao.BusinessBaseDao;
import com.hjnerp.dao.QiXinBaseDao;
import com.hjnerp.model.Ctlm1345;
import com.hjnerp.model.Dsaordtype;
import com.hjnerp.net.HttpClientBuilder;
import com.hjnerp.net.HttpClientManager;
import com.hjnerp.util.ToastUtil;
import com.hjnerp.util.myscom.StringUtils;
import com.hjnerp.widget.WaitDialogRectangle;
import com.hjnerpandroid.R;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.jiebao.utils.Tools.getToday;
import static com.hjnerp.common.Constant.dsaordbaseJsons_new;
import static com.hjnerp.common.Constant.user_myid;

public class OrderFinding extends AppCompatActivity implements View.OnClickListener {

    private ImageView back_img;
    private TextView title_checked;
    private TextView object_name;
    private Spinner object_person;
    private LinearLayout linear_list;
    private List<Ctlm1345> myauthuser = new ArrayList<>();
    private List<String> users_id = new ArrayList<String>();
    private List<String> users_name = new ArrayList<String>();
    private List<DsaordbaseJson> dsaordbaseJsons = new ArrayList<DsaordbaseJson>();
    private ArrayAdapter<String> adapter;
    private Button finding;
    private WaitDialogRectangle waitDialog;
    private HttpClientManager.HttpResponseHandler responseHandler = new NsyncDataHandler();
    public static final String JSON_VALUE = "values";
    public static final Pattern p = Pattern.compile("\\s*|\t|\r|\n");
    private ArrayList<ArrayList<String>> mTableDatas;
    private LockTableView mLockTableView;
    private String tab_name;
    private TextView table_begin_time;
    private LinearLayout table_date_from;
    private TextView table_end_time;
    private LinearLayout table_date_to;
    private Calendar c = Calendar.getInstance();
    private String pattern = "MM-dd-yyyy";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
    private TextView all_price_textview;
    private LinearLayout all_price_layout;
    private double allPrice = 0.0;//总价

    //网络查询表格的方法，将来可以考虑写成公用的方法
    private class NsyncDataHandler extends HttpClientManager.HttpResponseHandler {

        @Override
        public void onException(Exception e) {

        }

        @Override
        public void onResponse(HttpResponse resp) {
            // TODO Auto-generated method stub
            try {
                String contentType = resp.getHeaders("Content-Type")[0]
                        .getValue();
                // if ("application/octet-stream".equals(contentType) ) {
                if (contentType.indexOf("application/octet-stream") != -1) {
                    String contentDiscreption = resp
                            .getHeaders("Content-Disposition")[0].getValue();
                    String fileName = contentDiscreption
                            .substring(contentDiscreption.indexOf("=") + 1);
                    FileOutputStream fos = new FileOutputStream(new File(
                            getExternalCacheDir(), fileName));
                    resp.getEntity().writeTo(fos);
                    fos.close();
                    String json = processBusinessCompress(fileName);
                    JSONObject jsonObject = new JSONObject(json);
                    String value = jsonObject.getString(JSON_VALUE);

//                    Log.d("value", value);
                    processJsonValue(value);
                } else {

                }

            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //解压缩下载的zip包
    private String processBusinessCompress(String fileName) {
        // TODO Auto-generated method stub
        ZipInputStream zis = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            File f = new File(getExternalCacheDir(), fileName);
            FileInputStream fis = new FileInputStream(f);
            zis = new ZipInputStream(fis);
            ZipEntry zip = zis.getNextEntry();
            int len = 0;
            while ((len = zis.read(data)) != -1) {
                baos.write(data, 0, len);
            }
            String json = new String(baos.toByteArray(), HTTP.UTF_8);
            return json;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (zis != null) {
                    zis.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    //数据的整理，解析（每个单子都不一样）
    public void processJsonValue(String value) throws JSONException, ParseException {
        // TODO Auto-generated method stub
        value = value.trim();
        if (value.equalsIgnoreCase("[]") || value.equalsIgnoreCase(null)) {
            waitDialog.dismiss();
            mHandler.sendEmptyMessage(2);

            return;
        }
        JSONArray jsonArray = new JSONArray(value);
        mTableDatas = new ArrayList<>();
        ArrayList<String> mfristData = new ArrayList<>();
        //表头，不同表格不同表头
        switch (Constant.tab_type) {

            case 0:
                mfristData.add("客户名称");
                mfristData.add("终端名称");
                mfristData.add("业务员");
                mfristData.add("订单日期");
                mfristData.add("产品编码");
                mfristData.add("产品");
                mfristData.add("规格");
                mfristData.add("型号");
                mfristData.add("性能参数");
                mfristData.add("单价");
                mfristData.add("数量");
                mfristData.add("金额");
                mfristData.add("快递单号（货）");
                mfristData.add("快递单号（票）");
                mfristData.add("回款情况");
                mfristData.add("订单号");
                mfristData.add("行号");
                break;
            case 1:
                mfristData.add("客户名称");
                mfristData.add("业务员");
                mfristData.add("区域");
                mfristData.add("省区");
                mfristData.add("01至30天");
                mfristData.add("30至60天");
                mfristData.add("60至90天");
                mfristData.add("90至180天");
                mfristData.add("180至270天");
                mfristData.add("270至365天");
                mfristData.add("365至730天");
                mfristData.add("730至1095天");
                mfristData.add("1095天以上");
                mfristData.add("总计");
                break;
            case 2:
                mfristData.add("部门");
                mfristData.add("人员");
                mfristData.add("日期");
                mfristData.add("工作类型");
                mfristData.add("客户");
                mfristData.add("标题");
                mfristData.add("内容");
                break;
            case 3:
                mfristData.add("结算客户");
                mfristData.add("终端");
                mfristData.add("业务员");
                mfristData.add("订单日期");
                mfristData.add("物料名称");
                mfristData.add("销售类型");
                mfristData.add("规格");
                mfristData.add("型号");
                mfristData.add("数量");
                mfristData.add("单价");
                mfristData.add("金额");
                break;
            case 4:
                mfristData.add("客户名称");
                mfristData.add("客户代码");
                mfristData.add("业务员");
                mfristData.add("180至365天");
                mfristData.add("365天以上");
                mfristData.add("总欠款额");
                break;
        }

        mTableDatas.add(mfristData);
        dsaordbaseJsons.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            String temp = jsonArray.getString(i);
            Matcher m = p.matcher(temp);
            String subValue = temp.substring(temp.indexOf("{"),
                    temp.indexOf("}") + 1);
            Gson gson = new Gson();
//            com.hjnerp.util.Log.d(subValue);
            DsaordbaseJson dsaordbaseJson = gson.fromJson(subValue, DsaordbaseJson.class);

            dsaordbaseJsons.add(dsaordbaseJson);
        }
        Collections.sort(dsaordbaseJsons);
        allPrice = 0.0;
        for (int i = 0; i < dsaordbaseJsons.size(); i++) {
            DsaordbaseJson dsaordbaseJson = dsaordbaseJsons.get(i);
            ArrayList<String> mRowDatas = new ArrayList<String>();
            String time_begin = table_begin_time.getText().toString().isEmpty() ? "1800-00-00" : table_begin_time.getText().toString();
            String time_end = table_end_time.getText().toString().isEmpty() ? "2999-12-31" : table_end_time.getText().toString();
            DecimalFormat formatNum = new DecimalFormat(",##0.00");
            DecimalFormat formatNo = new DecimalFormat("#0.00");
            switch (Constant.tab_type) {
                case 0:
                    long date_opr = dateFormat.parse(dsaordbaseJson.getDate_opr()).getTime();
                    if (format.parse(time_begin).getTime() <= date_opr && date_opr <= format.parse(time_end).getTime()) {
                        mRowDatas.add(dsaordbaseJson.getName_corr());
                        mRowDatas.add(dsaordbaseJson.getName_terminal());
                        mRowDatas.add(dsaordbaseJson.getName_seller());
                        mRowDatas.add(format.format(new Date(date_opr)));
                        mRowDatas.add(dsaordbaseJson.getId_item());
                        mRowDatas.add(dsaordbaseJson.getName_item());
                        mRowDatas.add(dsaordbaseJson.getVar_ispec().trim());
                        mRowDatas.add(dsaordbaseJson.getVar_pattern().trim());
                        mRowDatas.add(dsaordbaseJson.getVar_chkparm().trim());
                        mRowDatas.add(formatNum.format(Double.valueOf(dsaordbaseJson.getDec_price())));
                        mRowDatas.add(formatNo.format(Double.valueOf(dsaordbaseJson.getDec_ordqty())));
                        double dec_amt = Double.valueOf(dsaordbaseJson.getDec_ordamt());
                        allPrice += dec_amt;
                        mRowDatas.add(formatNum.format(dec_amt));
                        mRowDatas.add(dsaordbaseJson.getVar_epsno().trim());
                        mRowDatas.add(dsaordbaseJson.getInv_epsno().trim());
                        mRowDatas.add(formatNum.format(Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.getDec_setamt().trim()) ? "0.00" : dsaordbaseJson.getDec_setamt())));
                        mRowDatas.add(dsaordbaseJson.getDsaord_no());
                        mRowDatas.add(dsaordbaseJson.getLine_no());
                    } else {
                        continue;
                    }
                    break;
                case 1:
                    /**
                     * 去掉了开始日期结束日期的查询条件，如果需要再释放
                     */
//                    long begin = dateFormat.parse(dsaordbaseJson.getDate_begin()).getTime();
//                    long end = dateFormat.parse(dsaordbaseJson.getDate_end()).getTime();
//                    if (format.parse(time_begin).getTime() <= begin && end <= format.parse(time_end).getTime()) {
                    mRowDatas.add(dsaordbaseJson.getName_corr());
                    mRowDatas.add(dsaordbaseJson.getName_seller());
                    mRowDatas.add(dsaordbaseJson.getName_area());
                    mRowDatas.add(dsaordbaseJson.getName_zone());
                    double _$130 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$130天().trim()) ? "0.00" : dsaordbaseJson.get_$130天());
                    double _$3060 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$3060天().trim()) ? "0.00" : dsaordbaseJson.get_$3060天());
                    double _$6090 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$6090天().trim()) ? "0.00" : dsaordbaseJson.get_$6090天());
                    double _$90180 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$90180天().trim()) ? "0.00" : dsaordbaseJson.get_$90180天());
                    double _$180270 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$180270天().trim()) ? "0.00" : dsaordbaseJson.get_$180270天());
                    double _$270365 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$270365天().trim()) ? "0.00" : dsaordbaseJson.get_$270365天());
                    double _$365730 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$365730天().trim()) ? "0.00" : dsaordbaseJson.get_$365730天());
                    double _$7301095 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$7301095天().trim()) ? "0.00" : dsaordbaseJson.get_$7301095天());
                    double _$1095up = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$1095天以上().trim()) ? "0.00" : dsaordbaseJson.get_$1095天以上());
                    double all_acc = _$130 + _$3060 + _$6090 + _$90180 + _$180270 + _$270365 + _$365730 + _$7301095 + _$1095up;
                    mRowDatas.add(formatNum.format(_$130));
                    mRowDatas.add(formatNum.format(_$3060));
                    mRowDatas.add(formatNum.format(_$6090));
                    mRowDatas.add(formatNum.format(_$90180));
                    mRowDatas.add(formatNum.format(_$180270));
                    mRowDatas.add(formatNum.format(_$270365));
                    mRowDatas.add(formatNum.format(_$365730));
                    mRowDatas.add(formatNum.format(_$7301095));
                    mRowDatas.add(formatNum.format(_$1095up));
                    mRowDatas.add(formatNum.format(all_acc));

//                    mRowDatas.add(format.format(new Date(begin)));
//                    mRowDatas.add(format.format(new Date(end)));
//                    mRowDatas.add(dsaordbaseJson.getDec_samt());
//                    } else {
//                        continue;
//                    }
                    break;
                case 2:
                    long date_task = dateFormat2.parse(dsaordbaseJson.getDate_task()).getTime();
                    if (format.parse(time_begin).getTime() <= date_task && date_task <= format.parse(time_end).getTime()) {
                        mRowDatas.add(dsaordbaseJson.getName_dept());
                        mRowDatas.add(dsaordbaseJson.getName_user());
                        mRowDatas.add(format.format(new Date(date_task)));
                        mRowDatas.add(dsaordbaseJson.getName_type());
                        mRowDatas.add(dsaordbaseJson.getName_wproj());
                        mRowDatas.add(dsaordbaseJson.getVar_wtitle());
                        mRowDatas.add(dsaordbaseJson.getVar_remark());
                    } else {
                        continue;
                    }

                    break;
                case 3:
                    long date_opr2 = dateFormat.parse(dsaordbaseJson.getDate_opr()).getTime();
                    if (format.parse(time_begin).getTime() <= date_opr2 && date_opr2 <= format.parse(time_end).getTime()) {
                        mRowDatas.add(dsaordbaseJson.getName_corr());
                        mRowDatas.add(dsaordbaseJson.getName_terminal());
                        mRowDatas.add(dsaordbaseJson.getName_seller());
                        mRowDatas.add(format.format(new Date(date_opr2)));
                        mRowDatas.add(dsaordbaseJson.getName_item());
                        mRowDatas.add(dsaordbaseJson.getName_samth());
                        mRowDatas.add(dsaordbaseJson.getVar_ispec().trim());
                        mRowDatas.add(dsaordbaseJson.getVar_pattern().trim());
                        mRowDatas.add(formatNo.format(Double.valueOf(dsaordbaseJson.getDec_ordqty())));
                        mRowDatas.add(formatNum.format(Double.valueOf(dsaordbaseJson.getDec_price())));
                        double dec_amt = Double.valueOf(dsaordbaseJson.getDec_ordamt());
                        allPrice += dec_amt;
                        mRowDatas.add(formatNum.format(dec_amt));
                    } else {
                        continue;
                    }
                    break;
                case 4:
                    mRowDatas.add(dsaordbaseJson.getName_corr());
                    mRowDatas.add(dsaordbaseJson.getId_corr());
                    mRowDatas.add(dsaordbaseJson.getName_seller());
                    double _$180365 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$180365天().trim()) ? "0.00" : dsaordbaseJson.get_$180365天());
                    double _$365up = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$365天以上().trim()) ? "0.00" : dsaordbaseJson.get_$365天以上());
                    double all_acc2 = Double.valueOf(TextUtils.isEmpty(dsaordbaseJson.get_$总欠款额().trim()) ? "0.00" : dsaordbaseJson.get_$总欠款额());
                    mRowDatas.add(formatNum.format(_$180365));
                    mRowDatas.add(formatNum.format(_$365up));
                    mRowDatas.add(formatNum.format(all_acc2));
                    break;
            }

            mTableDatas.add(mRowDatas);

        }
//大数据测试
//        for (int i = 0; i < 1000; i++) {
//            ArrayList<String> mRowDatas = new ArrayList<String>();
//            mRowDatas.add("测试" + i);
//            mTableDatas.add(mRowDatas);
//        }
        if (mTableDatas.size() == 1) {//如果只有表头
            mHandler.sendEmptyMessage(2);

        } else {
            mHandler.sendEmptyMessage(0);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_finding);
        initView();
        initData();

    }

    //加载数据，必要的页面数据
    private void initData() {
        //不同表格的不同数据
        switch (Constant.tab_type) {
            case 0:
                title_checked.setText("订单查询");
                tab_name = "dsaordquery";
                table_date_from.setVisibility(View.VISIBLE);
                table_date_to.setVisibility(View.VISIBLE);
                all_price_layout.setVisibility(View.VISIBLE);
                table_begin_time.setText(getToday());
                break;
            case 1:
                title_checked.setText("应收账龄");
                tab_name = "accountage";
                table_date_from.setVisibility(View.GONE);
                table_date_to.setVisibility(View.GONE);
                break;
            case 2:
                title_checked.setText("日志查询");
                tab_name = "dgtdrec";
                table_date_from.setVisibility(View.VISIBLE);
                table_date_to.setVisibility(View.VISIBLE);
                break;
            case 3:
                title_checked.setText("订单明细");
                tab_name = "dsaordquery";
                table_date_from.setVisibility(View.VISIBLE);
                table_date_to.setVisibility(View.VISIBLE);
                all_price_layout.setVisibility(View.VISIBLE);
                table_begin_time.setText(getToday());
                break;
            case 4:
                title_checked.setText("客户应收");
                tab_name = "accountage2";
                table_date_from.setVisibility(View.GONE);
                table_date_to.setVisibility(View.GONE);
                break;

        }
        users_id.add("");
        users_name.add("请选择业务员");
        myauthuser = BusinessBaseDao.getCTLM1345ByIdTable("sauser");
        if (myauthuser.size() == 0) {
//            ToastUtil.ShowShort(this, "请先下载基础数据");
//            finish();
//            return;
            users_name.add(QiXinBaseDao.queryCurrentUserInfo().username);
            users_id.add(QiXinBaseDao.queryCurrentUserInfo().userID);

        } else {
            users_name.add(QiXinBaseDao.queryCurrentUserInfo().username);
            users_id.add(QiXinBaseDao.queryCurrentUserInfo().userID);
            Gson gson1 = new Gson();

            for (int i = 0; i < myauthuser.size(); i++) {
                Dsaordtype dsaordtype = gson1.fromJson(myauthuser.get(i).getVar_value(), Dsaordtype.class);
                if (!users_id.contains(dsaordtype.getId_user())) {
                    users_name.add(dsaordtype.getName_user());
                    users_id.add(dsaordtype.getId_user());
                }

            }

        }

        adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, users_name);
        adapter.setDropDownViewResource(R.layout.spinner_item_hint);
        object_person.setAdapter(adapter);


    }

    //加载页面
    private void initView() {
        back_img = (ImageView) findViewById(R.id.back_img);
        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title_checked = (TextView) findViewById(R.id.title_checked);
        object_name = (TextView) findViewById(R.id.object_name);
        object_name.setOnClickListener(this);
        object_person = (Spinner) findViewById(R.id.object_person);
        linear_list = (LinearLayout) findViewById(R.id.linear_list);
        finding = (Button) findViewById(R.id.finding);
        finding.setOnClickListener(this);
        table_begin_time = (TextView) findViewById(R.id.table_begin_time);
        //此段代码为开始时间提前一个月
        try {
            Calendar cal = Calendar.getInstance();
            Date date = format.parse(getToday());
            cal.setTime(date);
            cal.add(Calendar.DATE, -30);//为了和ios一致，改为减30天
//            cal.add(Calendar.MONTH, -1);
            Date fDate = cal.getTime();
            table_begin_time.setText(format.format(fDate));
        } catch (ParseException e) {
            e.printStackTrace();
            table_begin_time.setText(getToday());
        }

        table_begin_time.setOnClickListener(this);
        table_date_from = (LinearLayout) findViewById(R.id.table_date_from);
        table_end_time = (TextView) findViewById(R.id.table_end_time);
        table_end_time.setText(getToday());
        table_end_time.setOnClickListener(this);
        table_date_to = (LinearLayout) findViewById(R.id.table_date_to);
        all_price_textview = (TextView) findViewById(R.id.all_price_textview);
        all_price_layout = (LinearLayout) findViewById(R.id.all_price_layout);
    }

    //提交查询数据
    private void submit() {
        // validate
        String id_terminal;
        if (dsaordbaseJsons_new == null || dsaordbaseJsons_new.size() == 0) {
            id_terminal = "";
        } else {
            id_terminal = dsaordbaseJsons_new.get(0).getName_corr();

        }
        String id_user = users_id.get(object_person.getSelectedItemPosition());
        // TODO validate success, do something
        waitDialog = new WaitDialogRectangle(this);
        waitDialog.show();
        try {
            HttpClientBuilder.HttpClientParam param = HttpClientBuilder
                    .createParam(Constant.NBUSINESS_SERVICE_ADDRESS);
            String condition = "";
            if (StringUtils.isEmpty(id_user)) {
                String cons = "1=1 and var_value like '%" + id_terminal + "%' and (";
                for (int i = 1; i < users_id.size(); i++) {
                    if (i == users_id.size() - 1) {
                        cons = cons + "id_column='" + users_id.get(i) + "')";
                    } else {
                        cons = cons + "id_column='" + users_id.get(i) + "' or ";

                    }
                }
                condition = cons;
            } else {
                condition = "1=1 and id_column='" + id_user + "' and var_value like '%" + id_terminal + "%'";
            }
            param.addKeyValue(Constant.BM_ACTION_TYPE, "MobileSyncDataDownload")
                    .addKeyValue("id_table", StringUtils.join(tab_name))
                    .addKeyValue("condition", condition);


            HttpClientManager.addTask(responseHandler, param.getHttpPost());

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //页面关闭时，数据清零
    @Override
    protected void onDestroy() {
        super.onDestroy();
        user_myid = "";
        Constant.project_type = 0;
        Constant.travel = false;
        if (dsaordbaseJsons_new != null && dsaordbaseJsons_new.size() > 0) {
            dsaordbaseJsons_new.clear();
        }
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.object_name:

                Intent intent = new Intent(getApplicationContext(), BusinessSearch.class);
                if (users_id.get(object_person.getSelectedItemPosition()).equalsIgnoreCase("")) {
                    String a = "";
                    for (int i = 1; i < users_id.size(); i++) {
                        a = a + users_id.get(i) + ",";
                    }
                    user_myid = a;
                } else {
                    user_myid = users_id.get(object_person.getSelectedItemPosition()) + ",";
                }
                Constant.project_type = 2;
                Constant.travel = true;
                startActivityForResult(intent, 44);
                break;
            case R.id.finding:
                submit();
                break;
            case R.id.table_begin_time:
                showCalendar(table_begin_time);
                break;
            case R.id.table_end_time:
                showCalendar(table_end_time);
                break;

        }
    }

    //选择日期
    private void showCalendar(final TextView editText) {
//        c = Calendar.getInstance();
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        int month = monthOfYear + 1;
                        if (month < 10 && dayOfMonth < 10) {
                            editText.setText(year + "-0" + month
                                    + "-0" + dayOfMonth);
                        } else if (month < 10 && dayOfMonth >= 10) {
                            editText.setText(year + "-0" + month
                                    + "-" + dayOfMonth);
                        } else if (month >= 10 && dayOfMonth < 10) {
                            editText.setText(year + "-" + month
                                    + "-0" + dayOfMonth);
                        } else {
                            editText.setText(year + "-" + month
                                    + "-" + dayOfMonth);
                        }

                    }
                }
                , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                .get(Calendar.DAY_OF_MONTH)).show();
    }

    //选择后的返回事件
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 44 && resultCode == 22) {
            mHandler.sendEmptyMessage(1);
        }
    }

    //异步任务
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    getTab();
                    break;
                case 1:
                    setTxt();
                    break;
                case 2:
                    waitDialog.dismiss();
                    ToastUtil.ShowShort(getApplicationContext(), "结果为空");
                    linear_list.removeAllViews();
                    all_price_textview.setText("0.00");
                    break;

            }
        }
    };

    //设置数据
    private void setTxt() {
        object_name.setText(dsaordbaseJsons_new.get(0).getName_corr());

    }

    //获取表格
    private void getTab() {
        linear_list.removeAllViews();
        DecimalFormat formatNum = new DecimalFormat(",##0.00");
        all_price_textview.setText(formatNum.format(allPrice));
        mLockTableView = new LockTableView(this, linear_list, mTableDatas);
        mLockTableView.setLockFristColumn(true) //是否锁定第一列
                .setLockFristRow(true) //是否锁定第一行
                .setMaxColumnWidth(100) //列最大宽度
                .setMinColumnWidth(10) //列最小宽度
                .setMinRowHeight(10)//行最小高度
                .setMaxRowHeight(60)//行最大高度
                .setTextViewSize(12) //单元格字体大小
                .setFristRowBackGroudColor(R.color.beijin)//表头背景色
                .setTableHeadTextColor(R.color.white)//表头字体颜色
                .setTableContentTextColor(R.color.text_color)//单元格字体颜色
                .setNullableString("--") //空值替换值
                .show(); //显示表格,此方法必须调用
        waitDialog.dismiss();
    }
}
