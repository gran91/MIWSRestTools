package com.kles.view.mi;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kles.MainApp;
import com.kles.mi.MIError;
import com.kles.mi.MIProgramMetadata;
import com.kles.mi.Transaction;
import com.kles.model.IRestConnection;
import com.kles.model.mi.DefaultRestClientMetadataConnection;
import com.kles.model.mi.GenericDataMIModel;
import com.kles.task.rest.RestGetTask;
import com.kles.utils.MIUtils;
import static com.kles.utils.MIUtils.getTransactionTypeFromMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jchau
 */
public class MIExportTask extends Task<Boolean> {

    private int id = 0;
    private Transaction transaction;
    private IRestConnection restConnection;
    private final List<Transaction> listTransaction = new ArrayList<>();
    private RestGetTask restTaskListTransaction, restTaskCreate, restTaskCreateFields, restTaskDelTrans;
    private Service<String> restServiceListTransaction, restServiceCreate, restServiceCreateFields, restServiceDeleteTransaction;
    private final IntegerProperty cptFields = new SimpleIntegerProperty(0);
    private final DefaultRestClientMetadataConnection restClient = new DefaultRestClientMetadataConnection();
    private boolean hasError = false;
    private boolean isFinished = false;
    private int limit = 3;
    private int updateType = 0;
    private MainApp mainApp;

    public MIExportTask(int id) {
        this(id, null, null);
    }

    public MIExportTask(int id, Transaction t, IRestConnection con) {
        this.id = id;
        transaction = t;
        setRestConnection(con);
        if (con != null && transaction != null) {
            updateTitle(con.toString() + ": " + t.getTransaction());
        }
        updateProgress(0, limit);
        createRestServiceDeleteTransaction();
        createRestServiceListTransaction();
        createRestServiceCreate();
        createRestServiceCreateFields();

    }

    private void createRestServiceDeleteTransaction() {
        restServiceDeleteTransaction = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskDelTrans;
            }
        };
        restServiceDeleteTransaction.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    break;
                case FAILED:
                    hasError = true;
                    updateProgress(limit, limit);
                    break;
                case CANCELLED:
                    break;
                case RUNNING:
                    break;
                case SUCCEEDED:
                    runCreateTransaction();
                    break;
            }
        }
        );
    }

    private void createRestServiceListTransaction() {
        restServiceListTransaction = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskListTransaction;
            }
        };
        restServiceListTransaction.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case FAILED:
                    hasError = true;
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.search.error"));
                    updateProgress(limit, limit);
                    break;
                case CANCELLED:
                    isFinished = true;
                    break;
                case RUNNING:
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.search"));
                    break;
                case SUCCEEDED:
                    if (!restTaskListTransaction.getValue().isEmpty()) {
                        ObjectMapper mapper = new XmlMapper();
                        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                        try {
                            MIProgramMetadata miprogram = mapper.readValue(restTaskListTransaction.getValue(), MIProgramMetadata.class);
                            listTransaction.addAll(MIUtils.purify(miprogram.getTransactions()));
                            updateMessage(String.format(mainApp.getResourceBundle().getString("mi.transaction.find"), "" + listTransaction.size()));
                            updateProgress(getProgress() + 1, limit);
                        } catch (Exception e) {
                            listTransaction.clear();
                            hasError = true;
                            updateMessage(mainApp.getResourceBundle().getString("mi.transaction.search.error"));
                            updateProgress(limit, limit);
                        }
                        boolean test = isInTransactionList(listTransaction, transaction);
                        if (!test) {
                            runCreateTransaction();
                        } else if (test && updateType == 0) {
                            runDeleteTransaction();
                        }
                    }
                    break;
            }
        }
        );
    }

    private void createRestServiceCreate() {
        restServiceCreate = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskCreate;
            }
        };
        restServiceCreate.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case FAILED:
                    hasError = true;
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create.error"));
                    updateProgress(limit, limit);
                    break;
                case CANCELLED:
                    isFinished = true;
                    break;
                case RUNNING:
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create"));
                    break;
                case SUCCEEDED:
                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                     {
                        try {
                            MIError err = mapper.readValue(restServiceCreate.getValue(), MIError.class);
                            updateProgress(limit, limit);
                            updateMessage(err.getMessage());
                            hasError = true;
                        } catch (IOException ex) {
                            updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create.header"));
                            updateProgress(getProgress() + 1, limit);
                            cptFields.set(0);
                            runCreateFields(transaction.getTransaction());
                        }
                    }
                    break;
            }
        }
        );
    }

    private void createRestServiceCreateFields() {
        restServiceCreateFields = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return restTaskCreateFields;
            }
        };
        restServiceCreateFields.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
            switch (newValue) {
                case FAILED:
                    hasError = true;
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create.error"));
                    updateProgress(limit, limit);
                    break;
                case CANCELLED:
                    isFinished = true;
                    break;
                case RUNNING:
                    updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create.field"));
                    break;
                case SUCCEEDED:
                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                     {
                        try {
                            MIError err = mapper.readValue(restServiceCreate.getValue(), MIError.class);
                            updateProgress(limit, limit);
                            updateMessage(err.getMessage());
                            hasError = true;
                        } catch (IOException ex) {
                            runCreateFields(transaction.getTransaction());
                            cptFields.set(cptFields.get() + 1);
                        }
                    }

                    break;
            }
        }
        );
    }

    private void runDeleteTransaction() {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("DelTransaction");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", transaction.getTransaction());
        restClient.setDataModel(m);
        restTaskDelTrans = new RestGetTask(restClient);
        restTaskDelTrans.setMediaType(MediaType.APPLICATION_JSON);
        restServiceDeleteTransaction.restart();
    }

    public void runListTransaction() {
        restClient.setAction(DefaultRestClientMetadataConnection.LIST_TRANS);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MDBREADMI");
        restClient.setDataModel(m);
        restTaskListTransaction = new RestGetTask(restClient);
        restTaskListTransaction.setMediaType(MediaType.APPLICATION_XML);
        restServiceListTransaction.restart();
    }

    public void runCreateTransaction() {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("AddTransaction");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", transaction.getTransaction());
        m.addData("TRDS", transaction.getDescription());
        m.addData("STAT", "20");
        m.addData("SIMU", getTransactionTypeFromMethod(transaction.getTransaction().substring(0, 3)));
        restClient.setDataModel(m);
        restTaskCreate = new RestGetTask(restClient);
        restTaskCreate.setMediaType(MediaType.APPLICATION_JSON);
        restServiceCreate.restart();
    }

    public void runCreateFields(String transactionName) {
        restClient.setAction(DefaultRestClientMetadataConnection.EXCUTE_API);
        GenericDataMIModel m = new GenericDataMIModel();
        m.setMIProgram("MRS001MI");
        m.setMITransaction("AddField");
        m.addData("MINM", "MDBREADMI");
        m.addData("TRNM", transactionName);
        if (cptFields.get() < transaction.getInput().getField().size()) {
            m = transaction.getInput().getField().get(cptFields.get()).buildFieldModel(m, "I", MIUtils.calculateFRPO(transaction.getInput(), cptFields.get()));
        } else if (cptFields.get() < transaction.getInput().getField().size() + transaction.getOutput().getField().size()) {
            m = transaction.getOutput().getField().get(cptFields.get() - transaction.getInput().getField().size()).buildFieldModel(m, "O", MIUtils.calculateFRPO(transaction.getOutput(), cptFields.get() - transaction.getInput().getField().size()));
        } else {
            isFinished = true;
            updateMessage(mainApp.getResourceBundle().getString("mi.transaction.create.success"));
            updateProgress(limit, limit);
            return;
        }
        restClient.setDataModel(m);
        restTaskCreateFields = new RestGetTask(restClient);
        restTaskCreateFields.setMediaType(MediaType.APPLICATION_JSON);
        restServiceCreateFields.restart();
    }

    private boolean isInTransactionList(List<Transaction> list, Transaction t) {
        for (Transaction t1 : list) {
            if (t1.getTransaction().equals(t.getTransaction())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Boolean call() throws Exception {
        runListTransaction();
        while (!isFinished && !hasError) {
            System.out.println("Waiting....Finish=" + isFinished + " Error=" + hasError);
        }
        if (hasError) {
            throw new RuntimeException(this.getException());
        }
        return !hasError;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public IRestConnection getRestConnection() {
        return restConnection;
    }

    public void setRestConnection(IRestConnection restConnection) {
        this.restConnection = restConnection;
        restClient.setEnvironment(restConnection);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUpdateType() {
        return updateType;
    }

    public void setUpdateType(int updateType) {
        this.updateType = updateType;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

}
