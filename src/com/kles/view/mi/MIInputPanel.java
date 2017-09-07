/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import com.kles.mi.Field;
import com.kles.mi.InputFieldList;
import com.kles.mi.MIInputData;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author jchau
 */
public class MIInputPanel extends GridPane {

    private LinkedHashMap<String, Control> listControl = new LinkedHashMap<>();

    public MIInputPanel() {
        super();
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(5d));
        this.setHgap(5d);
        this.setVgap(5d);
    }

    public static MIInputPanel create() {
        return new MIInputPanel();
    }

    public MIInputPanel build(InputFieldList input) {
        return build(input, null);
    }

    public MIInputPanel build(MIInputData input) {
        return build(input.getTransaction().getInput(), input.getData());
    }

    public MIInputPanel build(InputFieldList input, LinkedHashMap<String, String> data) {
        listControl.clear();
        int row = 0;
        if (input != null) {
            for (Field f : input.getField()) {
                final Label l = new Label(f.getName());
                l.setFont(Font.font(null, FontWeight.BOLD, 15));
                l.setTooltip(new Tooltip(f.getDescription()));
                if (f.getMandatory()) {
                    l.setTextFill(Color.RED);
                }
                final TextField t = TextFields.createClearableTextField();//new TextField();
                t.setTooltip(new Tooltip(f.getDescription()));
                if (data != null) {
                    if (data.containsKey(f.getName())) {
                        t.setText(data.get(f.getName()));
                    }
                }
                this.add(l, 0, row);
                this.add(t, 1, row);
                listControl.put(f.getName(), t);
                row++;
            }
        } else {
            final Label l = new Label("Transaction sans input");
            l.setFont(Font.font(null, FontWeight.BOLD, 15));
            this.add(l, 0, 0);
        }
        return this;
    }

    public static LinkedHashMap<String, String> getDataFromPanel(MIInputPanel input) {
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        if (input != null) {
            input.getListControl().entrySet().forEach((Map.Entry<String, Control> t) -> {
                if (t.getValue() instanceof TextInputControl) {
                    data.put(t.getKey(), ((TextInputControl) t.getValue()).getText());
                }
            });
        }
        return data;
    }

    public void setData(LinkedHashMap<String, String> data) {
        listControl.entrySet().forEach((Map.Entry<String, Control> t) -> {
            if (t.getValue() instanceof TextInputControl && data.containsKey(t.getKey())) {
                ((TextInputControl) t.getValue()).setText(data.get(t.getKey()));
            }
        });
    }

    public LinkedHashMap<String, Control> getListControl() {
        return listControl;
    }
}
