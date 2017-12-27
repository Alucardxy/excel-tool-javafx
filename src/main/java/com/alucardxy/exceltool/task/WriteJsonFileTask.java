package com.alucardxy.exceltool.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alucardxy.exceltool.util.CellUtil;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * @author Alucardxy
 * @date 16/5/16 18:16
 */
public class WriteJsonFileTask extends Task<Void> {
    private Sheet sheet;
    private String pathClient;
    private String pathServer;
    private ObservableList<String> msgs;
//    private boolean isIncludeComments;

    private String filename;

    private int i;
    private int j;

    public WriteJsonFileTask(Sheet sheet, String pathClient, String pathServer, ObservableList<String> msgs/*,boolean isIncludeComments*/){
        this.sheet = sheet;
        this.pathClient = pathClient;
        this.pathServer = pathServer;
        this.msgs = msgs;
//        this.isIncludeComments = isIncludeComments;
    }

    @Override
    protected Void call() throws Exception {
        Row titleRow = sheet.getRow(0);
        Row nameRow = sheet.getRow(1);
        Row propertyRow = sheet.getRow(2);
        Row descRow = sheet.getRow(3);
        Row modeRow = sheet.getRow(4);
        filename = titleRow.getCell(0).getStringCellValue().substring(titleRow.getCell(0).getStringCellValue().lastIndexOf(" ") + 1);
//        String root = titleRow.getCell(1).getStringCellValue().substring(titleRow.getCell(1).getStringCellValue().lastIndexOf(" ")+1);


        List<JSONObject> listClient = new ArrayList<>();
        List<JSONObject> listServer = new ArrayList<>();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
//        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,true);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        for ( i = 5; i <sheet.getPhysicalNumberOfRows(); i++){
            Row currentRow = sheet.getRow(i);
            JSONObject clientObject = new JSONObject(true);
            JSONObject serverObject = new JSONObject(true);
            int rowNum = currentRow.getLastCellNum();
            for ( j = 0; j <rowNum; j++){
                String name = CellUtil.getValueFromCell(nameRow.getCell(j));
//                String desc = CellUtil.getValueFromCell(descRow.getCell(j));
                String property = CellUtil.getValueFromCell(propertyRow.getCell(j));
                String modeStr = CellUtil.getValueFromCell(modeRow.getCell(j));
                String value = CellUtil.getValueFromCell(currentRow.getCell(j));
                String comments = name/*+" "+desc*/;
                if(value==null||modeStr==null||property==null){
                    continue;
                }
                boolean isClient = modeStr.contains("client")||modeStr.contains("front")||modeStr.contains("C");
                boolean isServer = modeStr.contains("server")||modeStr.contains("back")||modeStr.contains("S");

                if(isClient){

                    clientObject.put(property,value);

                }
                if(isServer){
                    serverObject.put(property,value);
                }


            }
            listClient.add(clientObject);
            listServer.add(serverObject);


        }

        try {
            File file = new File(pathClient+ File.separator+ filename +".json");
            Files.write(file.toPath(), JSON.toJSONString(listClient, SerializerFeature.PrettyFormat).getBytes());

            file = new File(pathServer+ File.separator+ filename +".json");
            Files.write(file.toPath(), JSON.toJSONString(listServer, SerializerFeature.PrettyFormat).getBytes());

        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void succeeded(){
        msgs.add("写入完毕:"+pathClient+ File.separator+filename+".json"+ System.getProperty("line.separator"));
        msgs.add("写入完毕:"+pathServer+ File.separator+filename+".json"+ System.getProperty("line.separator"));
    }

//    @Override
//    protected void running(){
//        console.appendText("写入文件:"+pathClient+ File.separator+filename+".json\n");
//        console.appendText("写入文件:"+pathServer+File.separator+filename+".json\n");
//    }

    @Override
    protected void failed() {
        msgs.add("导出"+filename+".json时发生错误,在"+(i+1)+"行"+(j+1)+"列。"+ System.getProperty("line.separator"));
    }

}
