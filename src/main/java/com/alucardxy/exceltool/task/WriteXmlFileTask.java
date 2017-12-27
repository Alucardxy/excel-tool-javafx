package com.alucardxy.exceltool.task;

import com.alucardxy.exceltool.util.CellUtil;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Alucardxy
 * @date 16/5/16 18:15
 */
public class WriteXmlFileTask extends Task<Void> {
    private static final String ROOT = System.getProperty("line.separator");
    private static final String FOUR = System.getProperty("line.separator")+"    ";
//    private final String EIGHT = FOUR+"    ";
//    private final String SIXTEEN = EIGHT+"    ";
//    private final String THIRTY_TWO = SIXTEEN+"    ";


    private Sheet sheet;
    private String pathClient;
    private String pathServer;
    private boolean isIncludeComments;
    private ObservableList<String> msgs;

    private String filename;

    private int i;
    private int j;

    public WriteXmlFileTask(Sheet sheet, String pathClient, String pathServer, ObservableList<String> msgs ,boolean isIncludeComments){
        this.sheet = sheet;
        this.pathClient = pathClient;
        this.pathServer = pathServer;
        this.isIncludeComments = isIncludeComments;
        this.msgs = msgs;
    }
    @Override
    protected Void call() throws Exception {
        Row titleRow = sheet.getRow(0);
        Row nameRow = sheet.getRow(1);
        Row propertyRow = sheet.getRow(2);
        Row descRow = sheet.getRow(3);
        Row modeRow = sheet.getRow(4);
        String filename = titleRow.getCell(0).getStringCellValue().substring(titleRow.getCell(0).getStringCellValue().lastIndexOf(" ") + 1);
        String root = titleRow.getCell(1).getStringCellValue().substring(titleRow.getCell(1).getStringCellValue().lastIndexOf(" ")+1);



        try {
            //创建保存xml的结果流对象
            Result reultXmlClient = new StreamResult(new FileOutputStream(new File(pathClient+ File.separator+ filename +".xml")));
            Result reultXmlServer = new StreamResult(new FileOutputStream(new File(pathServer+ File.separator+ filename +".xml")));
            //获取sax生产工厂对象实例
            SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            //获取sax生产处理者对象实例
            TransformerHandler tfHandlerClient = saxTransformerFactory.newTransformerHandler();
            TransformerHandler tfHandlerServer = saxTransformerFactory.newTransformerHandler();
            tfHandlerClient.setResult(reultXmlClient);
            tfHandlerServer.setResult(reultXmlServer);
            //获取sax生产器

//            Transformer tfClient = tfHandlerClient.getTransformer();
//            tfClient.setOutputProperty(OutputKeys.INDENT,"yes");
//            tfClient.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//
//            Transformer tfServer = tfHandlerServer.getTransformer();
//            tfServer.setOutputProperty(OutputKeys.INDENT,"yes");
//            tfServer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            //开始封装document文档对象，这里和解析一样都是成双成对的构造标签
            tfHandlerClient.startDocument();
            tfHandlerServer.startDocument();

            AttributesImpl attrImple = new AttributesImpl();
//            tfHandlerClient.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
//            tfHandlerClient.startElement("","",root+"s",attrImple);
//            tfHandlerServer.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
//            tfHandlerServer.startElement("","",root+"s",attrImple);
            for (int i =5; i <sheet.getLastRowNum(); i++){
                Row currentRow = sheet.getRow(i);
                tfHandlerClient.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
                tfHandlerClient.startElement("","",root,attrImple);
                tfHandlerServer.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
                tfHandlerServer.startElement("","",root,attrImple);
                for (int j=0;j<currentRow.getLastCellNum();j++){
                    String name = CellUtil.getValueFromCell(nameRow.getCell(j));
//                    String desc = CellUtil.getValueFromCell(descRow.getCell(j));
                    String property = CellUtil.getValueFromCell(propertyRow.getCell(j));
                    String modeStr = CellUtil.getValueFromCell(modeRow.getCell(j));
                    String value = CellUtil.getValueFromCell(currentRow.getCell(j));
                    String comments = name+" "+descRow;
                    if(value==null||modeStr==null||property==null){
                        continue;
                    }

                    boolean isClient = modeStr.contains("client")||modeStr.contains("front")||modeStr.contains("C");
                    boolean isServer = modeStr.contains("server")||modeStr.contains("back")||modeStr.contains("S");

                    if(isClient){
                        addXmlNode(tfHandlerClient,property,value,comments,isIncludeComments&& i ==5);
                    }
                    if(isServer){
                        addXmlNode(tfHandlerServer,property,value,comments,isIncludeComments&& i ==5);
                    }
                }
                tfHandlerClient.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
                tfHandlerClient.endElement("","",root);
                tfHandlerServer.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
                tfHandlerServer.endElement("","",root);
            }
//            tfHandlerClient.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
//            tfHandlerClient.endElement("","",root+"s");
//            tfHandlerServer.characters(ROOT.toCharArray(),0,ROOT.toCharArray().length);
//            tfHandlerServer.endElement("","",root+"s");

            tfHandlerClient.endDocument();
            tfHandlerServer.endDocument();



        } catch (FileNotFoundException | TransformerConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void addXmlNode(TransformerHandler handler, String name, String value, String comments, boolean isIncludeComments) throws SAXException {
        handler.characters(FOUR.toCharArray(),0,FOUR.toCharArray().length);
        handler.startElement("","",name,new AttributesImpl());
        handler.characters(value.toCharArray(),0,value.toCharArray().length);
        handler.endElement("","",name);
        if(isIncludeComments&&comments!=null) {
            handler.comment(comments.toCharArray(),0,comments.toCharArray().length);
        }
    }

    @Override
    protected void succeeded() {
        msgs.add("写入完毕:"+pathClient+ File.separator+filename+".xml"+ System.getProperty("line.separator"));
        msgs.add("写入完毕:"+pathServer+ File.separator+filename+".xml"+ System.getProperty("line.separator"));
    }

//    @Override
//    protected void running() {
//        console.appendText("写入文件:"+pathClient+ File.separator+filename+".xml\n");
//        console.appendText("写入文件:"+pathServer+File.separator+filename+".xml\n");
//    }


    @Override
    protected void failed() {
        msgs.add("导出"+filename+".xml时发生错误,在"+(i+1)+"行"+(j+1)+"列。"+ System.getProperty("line.separator"));
    }
}
