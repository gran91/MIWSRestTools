/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kles.fx.custom.FxUtil;
import com.kles.mi.Field;
import com.kles.mi.FieldMetadata;
import com.kles.mi.InputFieldList;
import com.kles.mi.MIError;
import com.kles.mi.MIRecord;
import com.kles.mi.MIResult;
import com.kles.mi.MetaData;
import com.kles.mi.OutputFieldList;
import com.kles.mi.Transaction;
import com.kles.model.mi.GenericDataMIModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author GRAN_LAPTOP
 */
public class MIUtils {

    public static String[] technicalTransaction = {
        "LOOPBACK",
        "LOOPB",
        "FpwVersion",
        "GetInLayout",
        "GetOutLayout",
        "GetMIBuild",
        "GetServerTime"};

    public static List<Transaction> purify(List<Transaction> list) {
        List techList = Arrays.asList(technicalTransaction);
        List<Transaction> listPurify = new ArrayList<>();
        list.stream().filter((t) -> (!techList.contains(t.getTransaction()))).forEachOrdered((t) -> {
            listPurify.add(t);
        });
        return listPurify;
    }

    public static void showMIError(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MIError err = mapper.readValue(json, MIError.class);
        FxUtil.showAlert(Alert.AlertType.ERROR, "API", err.getType(), err.getMessage());
    }

    public static MetaData getMetadataMIField() {
        MetaData meta = new MetaData();
        List<FieldMetadata> list = new ArrayList<>();
        list.add(new FieldMetadata("FLNM", "Field Name", "A", "6"));
        list.add(new FieldMetadata("FLDS", "Field Description", "A", "36"));
        list.add(new FieldMetadata("TYPE", "Field Type", "A", "1"));
        list.add(new FieldMetadata("LENG", "Field Length", "N", "5"));
        meta.setField(list);
        return meta;
    }

    ;
    public static Node buildOutputPanel(MIRecord oData, MIResult midata) {
        return buildOutputPanel(oData, midata.getMetadata().getField());
    }

    public static Node buildOutputPanel(MIRecord oData, MetaData midata) {
        return buildOutputPanel(oData, midata.getField());
    }

    public static Node buildOutputPanel(MIRecord oData, List<FieldMetadata> midata) {
        ScrollPane scroll = new ScrollPane();
        scroll.setPrefSize(350, 500);
        GridPane g = new GridPane();
        g.setHgap(5d);
        g.setVgap(5d);
        g.setPadding(new Insets(5d));
        int row = 0;
        if (oData != null) {
            for (FieldMetadata f : midata) {
                final Label l = new Label(f.getName());
                l.setFont(Font.font(null, FontWeight.BOLD, 15));
                l.setTooltip(new Tooltip(f.getDescription()));
                final TextField t = new TextField();
                t.setEditable(false);
                if (oData.getNameValue().get(row) != null) {
                    t.setText(oData.getNameValue().get(row).getValue());
                }
                t.setTooltip(new Tooltip(f.getDescription()));
                g.add(l, 0, row);
                g.add(t, 1, row);
                row++;
            }
        }
        scroll.setContent(g);
        return scroll;
    }

    public static String[][] listTableStringToStringTable2(List<String[]> list) {
        String[][] data = new String[list.size()][list.get(0).length];
        for (int i = 0; i < list.size(); i++) {
            data[i] = list.get(i);
        }
        return data;
    }

    public static List<String[]> tableDataMIToListString(TableView t) {
        final List<String[]> list = new ArrayList<>();
        ObservableList<MIRecord> temp = t.getItems();
        temp.forEach((MIRecord t1) -> {
            String[] s = new String[t1.getNameValue().size()];
            for (int i = 0; i < t1.getNameValue().size(); i++) {
                s[i] = t1.getNameValue().get(i).getValue();
            }
            list.add(s);
        });
        return list;
    }

    public static void removeMIRecordFromField(ObservableList<MIRecord> list, List<Field> listField) {
        final List<MIRecord> listToRemove = new ArrayList<>();
        for (MIRecord r : list) {
            final String key = (r.getNameValue().get(0).getValue().trim().length() == 6) ? r.getNameValue().get(0).getValue().trim().substring(2) : r.getNameValue().get(0).getValue().trim();
            boolean test = false;
            for (Field f : listField) {
                if (f.getName().equals(key)) {
                    test = true;
                    break;
                }
            }
            if (!test) {
                listToRemove.add(r);
            }
        }
        list.removeAll(listToRemove);
    }

    public static MIRecord fieldToMIRecord(ObservableList<MIRecord> list, Field f) {
        for (MIRecord r : list) {
            final String key = (r.getNameValue().get(0).getValue().trim().length() == 6) ? r.getNameValue().get(0).getValue().trim().substring(2) : r.getNameValue().get(0).getValue().trim();
            if (key.equals(f.getName().trim())) {
                return r;
            }
        }
        return null;
    }

    public static int calculateFRPO(List<MIRecord> list, int pos) {
        int n = 16;
        for (int i = 0; i < pos; i++) {
            if (pos == i) {
                break;
            }
            try {

                n += Integer.parseInt(list.get(i).getNameValue().get(2).getValue().trim());
            } catch (NumberFormatException ex) {
            }
        }
        return n;
    }

    public static int calculateFRPO(InputFieldList list, int pos) {
        int n = 16;
        for (int i = 0; i < pos; i++) {
            if (pos == i) {
                break;
            }
            try {
                n += Integer.parseInt(list.getField().get(i).getLength().trim());
            } catch (NumberFormatException ex) {
            }
        }
        return n;
    }

    public static int calculateFRPO(OutputFieldList list, int pos) {
        int n = 16;
        for (int i = 0; i < pos; i++) {
            if (pos == i) {
                break;
            }
            try {
                n += Integer.parseInt(list.getField().get(i).getLength().trim());
            } catch (NumberFormatException ex) {
            }
        }
        return n;
    }

    public static GenericDataMIModel addFieldFromMIRecord(GenericDataMIModel m, MIRecord record, String trtp, int frpo) {
        return addFieldFromMIRecord(m, record, trtp, frpo, null);
    }

    public static GenericDataMIModel addFieldFromMIRecord(GenericDataMIModel m, MIRecord record, String trtp, int frpo, String mandatory) {
        final String key = (record.getNameValue().get(0).getValue().trim().length() == 6) ? record.getNameValue().get(0).getValue().trim().substring(2) : record.getNameValue().get(0).getValue().trim();
        m.addData("TRTP", trtp);
        m.addData("FLNM", key);
        m.addData("FLDS", record.getNameValue().get(4).getValue());
        m.addData("FRPO", "" + frpo);
        m.addData("LENG", record.getNameValue().get(2).getValue());
        m.addData("TYPE", record.getNameValue().get(1).getValue());
        if (mandatory == null) {
            m.addData("MAND", mandatory);
        }
        return m;
    }

    public static String getDescriptionTransactionFromKeys(List<MIRecord> list) {
        String s = "Keys:";
        s = list.stream().map((r) -> (r.getNameValue().get(0).getValue().trim().length() == 6) ? r.getNameValue().get(0).getValue().trim().substring(2) : r.getNameValue().get(0).getValue().trim()).map((key) -> " " + key).reduce(s, String::concat);
        return s;
    }

    public static String getTransactionTypeFromMethod(String typeMethod) {
        String type = "S";
        if (typeMethod.equals("Lst") || typeMethod.equals("Sel")) {
            type = "M";
        }
        return type;
    }
}
