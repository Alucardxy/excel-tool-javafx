package com.alucardxy.exceltool.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alucardxy.exceltool.data.Config;
import com.alucardxy.exceltool.data.ExportFormat;
import com.alucardxy.exceltool.task.WriteJsonFileTask;
import com.alucardxy.exceltool.task.WriteXmlFileTask;
import de.felixroske.jfxsupport.FXMLController;
import de.felixroske.jfxsupport.GUIState;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * HomeController
 *
 * @author Alucardxy
 * @date 2017/12/5 11:32
 */
@FXMLController
public class HomeController implements Initializable {

    @FXML private TextField textExcel;
    @FXML private TextField textClient;
    @FXML private TextField textServer;
    @FXML private ListView<String> console;


    @FXML private RadioButton radioButtonXml;
    @FXML private RadioButton radioButtonJson;

    private Config config;
    private ObservableList<String> msgs;


    private ExecutorService pool = Executors.newWorkStealingPool();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = loadConfig();
        if(config!=null){
            ExportFormat format = config.getExportFormat();
            String pathExcel = config.getPathExcel();
            String pathClient = config.getPathCient();
            String pathServer = config.getPathServer();
            if(format!=null){
                switch (format){
                    case XML:radioButtonXml.setSelected(true);break;
                    case JSON:radioButtonJson.setSelected(true);break;
                }
            }
            if(pathExcel!=null) textExcel.setText(pathExcel);
            if(pathClient!=null) textClient.setText(pathClient);
            if(pathServer!=null) textServer.setText(pathServer);
        }else {
            config = new Config();
            formatChanged();
        }

        textExcel.textProperty().addListener((observable, oldValue, newValue) -> config.setPathExcel(newValue));
        textClient.textProperty().addListener((observable,oldValue,newValue) -> config.setPathCient(newValue));
        textServer.textProperty().addListener(((observable, oldValue, newValue) -> config.setPathServer(newValue)));

        msgs = console.getItems();
        msgs.addListener((ListChangeListener<String>) c -> {
              if(msgs.size()>0){
                  console.scrollTo(msgs.size()-1);
              }
        });

    }

    private Config loadConfig(){
        Config config = null;
        File file = new File("config.json");
        if(file.exists()&&file.canRead()){
            try {
                byte[] bytes = Files.readAllBytes(file.toPath()) ;
                String data = new String(bytes);
                config = JSON.parseObject(data, Config.class);
            } catch (IOException e){
//                console.appendText(e.getMessage());
            }
        }


        return config;
    }

    private void saveConfig(){
        File file = new File("config.json");
        String data = JSON.toJSONString(config, SerializerFeature.PrettyFormat);
//        if(Files.isWritable(file.toPath())){
        try {
            Files.write(file.toPath(),data.getBytes());
        } catch (IOException e) {
//            console.appendText(e.getMessage());
        }
//        }

    }

    @FXML protected void formatChanged(){
        if (radioButtonXml.isSelected()) config.setExportFormat( ExportFormat.XML);
        else if (radioButtonJson.isSelected()) config.setExportFormat( ExportFormat.JSON);
    }

    @FXML protected void clickOpenExcel(ActionEvent event){
        DirectoryChooser chooser = new DirectoryChooser();
        File chosenDir = chooser.showDialog(GUIState.getStage());
        if (chosenDir != null) {
            textExcel.setText(chosenDir.getAbsolutePath());
        }
    }

    @FXML protected void clickOpenClient(ActionEvent event){
        DirectoryChooser chooser = new DirectoryChooser();
        File chosenDir = chooser.showDialog(GUIState.getStage());
        if (chosenDir != null) {
            textClient.setText(chosenDir.getAbsolutePath());
        }
    }

    @FXML protected void clickOpenServer(ActionEvent event){
        DirectoryChooser chooser = new DirectoryChooser();
        File chosenDir = chooser.showDialog(GUIState.getStage());
        if (chosenDir != null) {
            textServer.setText(chosenDir.getAbsolutePath());
        }
    }

    @FXML protected void clickStart(ActionEvent event){
        if(config!=null){
            String pathExcel = config.getPathExcel();
            String pathClient = config.getPathCient();
            String pathServer = config.getPathServer();
            ExportFormat format = config.getExportFormat();
            saveConfig();
            if(!pathExcel.equals("")&&!pathClient.equals("")&&!pathServer.equals("")){
                exportData(pathExcel,pathClient,pathServer,format);
            }
        }

    }


    private void exportData(String pathExcel,String pathClient,String pathServer,ExportFormat exportFormat){
        File directory = new File(pathExcel);

//        String[] filelist = directory.list();

        try(Stream<Path> files = Files.list(directory.toPath())){
            files.forEach(path -> {
                File file = path.toFile();
                String filename = file.getName();
//               Path filePath = path.getFileName();
                if(!filename.startsWith("~$")&&(filename.endsWith("xls")||filename.endsWith("xlsx"))) {
                    try(FileInputStream filein = new FileInputStream(file)){
                        Workbook wb;
                        if(filename.endsWith("xls")){
                            wb = new HSSFWorkbook(filein);
                        }else {
                            wb = new XSSFWorkbook(filein);
                        }
                        wb.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        Task<Void> task = null;
                        switch (exportFormat){
                            case XML:{
                                task = new WriteXmlFileTask(wb.getSheetAt(0),pathClient,pathServer,msgs,true);
//                                writeXMLFile(wb.getSheetAt(0),pathClient,pathServer,true) ;


                            }break;
                            case JSON:{
                                task = new WriteJsonFileTask(wb.getSheetAt(0),pathClient,pathServer,msgs);
//                                writeJSONFile(wb.getSheetAt(0),pathClient,pathServer,false)  ;
                            }break;
                        }
                        pool.submit(task);
                    } catch (IOException e){
//                        console.appendText(e.getMessage());
                    }

                }
            });
        }catch (IOException e){
//            console.appendText(e.getMessage());
        }

//        FileInputStream filein = null;
//        try {
//            for(String file:filelist){
//                String pathFile = pathExcel+File.separator+file;
//                File readfile = new File(pathFile);
//                if(!readfile.isDirectory()){
//                    String filename = readfile.getName();
//                    String extensionName = filename.substring(filename.indexOf(".")+1);
////                String extensionName = filenameArr[1];
//                    if(filename.startsWith("~$")) continue;
//                    if(extensionName.equals("xls")||extensionName.equals("xlsx")){
//                        filein = new FileInputStream(pathFile);
//                        Workbook wb;
//                        if(extensionName.equals("xls")){
//                            wb = new HSSFWorkbook(filein);
//                        }else {
//                            wb = new XSSFWorkbook(filein);
//                        }
//                        wb.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
//                        Task<Void> task = null;
//                        switch (exportFormat){
//                            case XML:{
//                                task = new WriteXmlFileTask(wb.getSheetAt(0),pathClient,pathServer,console,true);
////                                writeXMLFile(wb.getSheetAt(0),pathClient,pathServer,true) ;
//
//
//                            }break;
//                            case JSON:{
//                                task = new WriteJsonFileTask(wb.getSheetAt(0),pathClient,pathServer,console);
////                                writeJSONFile(wb.getSheetAt(0),pathClient,pathServer,false)  ;
//                            }break;
//                        }
//                        pool.submit(task);
//
//                    }
//
//                }
//
//            }
//        }catch (IOException e){
//            console.appendText(e.getMessage());
//        }finally {
//            if(filein!=null){
//                try {
//                    filein.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }


    }
}
