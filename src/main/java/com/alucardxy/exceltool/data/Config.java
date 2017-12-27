package com.alucardxy.exceltool.data;

/**
 * @author Alucardxy
 * @date 16/5/17 09:23
 */
public class Config {
    private String pathExcel;
    private String pathCient;
    private String pathServer;
    private ExportFormat exportFormat;

    public String getPathExcel() {
        return pathExcel;
    }

    public void setPathExcel(String pathExcel) {
        this.pathExcel = pathExcel;
    }

    public String getPathCient() {
        return pathCient;
    }

    public void setPathCient(String pathCient) {
        this.pathCient = pathCient;
    }

    public String getPathServer() {
        return pathServer;
    }

    public void setPathServer(String pathServer) {
        this.pathServer = pathServer;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }
}
