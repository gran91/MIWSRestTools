/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.output.xls;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Sheet;
import com.kles.utils.MIUtils;
import com.kles.view.util.TableViewUtils;
import javafx.scene.control.TableView;

/**
 *
 * @author Jeremy.CHAUT
 */
public class ExcelTableView extends ExcelOutput {

    protected final int beginROW = 4;
    protected final int beginCOL = 2;

    private TableView table;

    public ExcelTableView(TableView m) {
        super();
        table = m;
    }

    /**
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public void write() throws FileNotFoundException, IOException {
        Sheet s = workbook.createSheet("DiffTable");
        writeXlsTable(s, "", TableViewUtils.listColumns(table), MIUtils.listTableStringToStringTable2(MIUtils.tableDataMIToListString(table)), beginROW, beginCOL);

    }

    @Override
    public void read() throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void process() throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public TableView getTable() {
        return table;
    }

    public void setTable(TableView table) {
        this.table = table;
    }

    @Override
    protected Void call() throws Exception {
        return super.call();
    }
}
