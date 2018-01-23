package com.vip.my.pangu.bcg;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 描述:
 *
 * @author eric.mo
 * @create 2017-12-07 12:22
 */
public class ErrorMsgHelper {

    private static List<String> errorMsgList;

    private static List<String> errorCodeList;

    static {
        try {
            URL configUrl = ErrorMsgHelper.class.getClassLoader().getResource("conf");
            if (configUrl == null) {
                System.out.println("file path is not in the project");
            }

            parseErrorConfig(configUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseErrorConfig(URL configUrl) throws IOException {
        String content = FileUtils.readFileToString(new File(configUrl.getPath() + File.separator + "error.json"));
        System.out.println(content);
        ErrorEntity errorEntity = JSON.parseObject(content, ErrorEntity.class);


        String[] errorCodeAsStrings = errorEntity.getErrorCode().split("\\|");
        errorCodeList = Arrays.asList(errorCodeAsStrings);

        String[] errorMsgAsStrings = errorEntity.getErrorMsg().split("\\|");
        errorMsgList = Arrays.asList(errorMsgAsStrings);

        System.out.println(JSON.toJSONString(errorMsgList));
        System.out.println(JSON.toJSONString(errorCodeList));
    }

    public ErrorMsgHelper() {

    }


    public boolean containMsg(String content) {
        for (String msg : errorMsgList) {
            if (content.contains(msg)) {
                return true;
            }
        }
        return false;
    }

    public boolean containCode(String content) {
        for (String code : errorCodeList) {
            if (content.contains(code)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ErrorMsgHelper errorMsgHelper = new ErrorMsgHelper();
    }
}

@Data
@ToString
class ErrorEntity {
    String errorCode;
    String errorMsg;
}