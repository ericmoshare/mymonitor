package com.vip.my.pangu.bcg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 描述:
 *
 * @author eric.mo
 * @create 2017-12-07 12:22
 */
public class FlowHelper {
    final static String TEMPLATE = "心跳:%s, 渠道:%s, 发卡行:%s, 交易:%s, 调用:%s, 耗时:%s ms, 结论:%s";

    static final String CONNECTION_ERROR = "网络异常";
    static final String SUCCESS_MSG = "正常";
    private static final String CONNECTION_TIME_LONG = "耗时较长";

    private String content;

    private static JSONObject timeoutConfig = null;
    private static ErrorMsgHelper errorMsgHelper = new ErrorMsgHelper();

    static {
        try {
            URL configUrl = FlowHelper.class.getClassLoader().getResource("conf");
            if (configUrl == null) {
                System.out.println("file path is not in the project");
            }

            parseTimeoutConfig(configUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseTimeoutConfig(URL configUrl) throws IOException {
        String content = FileUtils.readFileToString(new File(configUrl.getPath() + File.separator + "timeout.json"));
        //System.out.println(content);
        timeoutConfig = JSON.parseObject(content);
    }


    public FlowHelper(String content) {
        this.content = content;
    }

    public final String format() throws Exception {
        if (!content.contains(",")) {
            return "";
        }
        String[] clips = content.split("\\,");
        String callRespCode = clips[4];
        String respMsg = clips[clips.length - 2];
        String routeOrg = getRouteOrg(clips[0]);
        long costTime = Long.valueOf(getCostTime(clips));
        String result = getResultByCondition(routeOrg, callRespCode, costTime, respMsg);
        return String.format(TEMPLATE, getHeartBeat(clips[0]), routeOrg, clips[1], clips[2], clips[4], costTime, result);
    }

    private String getRouteOrg(String clip) {
        int index = clip.indexOf("F2P");
        return clip.substring(index, clip.length());
    }

    private String getHeartBeat(String clip) {
        String[] subClips = clip.split("\\ ");
        return subClips[1].substring(0, subClips[1].length() - 1);
    }

    private String getCostTime(String[] clips) {
        return clips[clips.length - 1];
    }

    private String getResultByCondition(String routeOrg, String respCode, long costTime, String respMsg) {

        if ("SUCCESS".contains(respCode)) {
            Object timeout = timeoutConfig.get(routeOrg) != null ? timeoutConfig.get(routeOrg) : timeoutConfig.get("common");

            if (errorMsgHelper.containMsg(respMsg)) {
                return org.apache.commons.lang3.StringUtils.left(respMsg, 8);
            }

            if (costTime > Integer.parseInt(String.valueOf(timeout))) {
                return CONNECTION_TIME_LONG;
            } else {
                return SUCCESS_MSG;
            }
        }


//        if(errorMsgs.contains())
        return CONNECTION_ERROR;
    }


}
