package com.example.myapplication.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class myListener {


    public StringBuffer  whatListen;
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private Toast mToast;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String language = "zh_cn";


    private String resultType = "json";

    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;
    public myListener(Context context, Handler handler) {
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=b3b2700f");
        mRecognizerDialogListener = new RecognizerDialogListener() {
            // 返回结果
            public void onResult(RecognizerResult results, boolean isLast) {
                whatListen.append(printResult(results));
                if(isLast){
                    Message myMessage= Message.obtain();
                    myMessage.what=1;
                    myMessage.obj=whatListen.toString();
                    handler.sendMessage(myMessage);
                }
            }

            // 识别回调错误
            public void onError(SpeechError error) {
                showTip(error.getPlainDescription(true),context);
            }
        };
        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    System.out.println("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                }
            }
        };
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
        mIatDialog = new RecognizerDialog(context, mInitListener);
        setParam();
    }
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1800");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

    }
    public void listenAudio(){
            whatListen=new StringBuffer();
            mIatResults.clear();
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
    }
    private String printResult(RecognizerResult results) {
        return JsonParser.parseIatResult(results.getResultString());
    }
    private void showTip(final String str,Context context) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
