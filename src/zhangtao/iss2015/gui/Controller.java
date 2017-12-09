package zhangtao.iss2015.gui;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import zhangtao.iss2015.lexer.Lexer;
import zhangtao.iss2015.lexer.Token;
import zhangtao.iss2015.lexer.TokenTree;
import zhangtao.iss2015.praser.CMMParser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    public Button OpenButton;
    public Button LexButton;
    public Button PraserButton;
    public Button RunButton;
    public TextArea CodeText;
    public TextArea ErrorOutput;
    public TextArea PrecessOutput;

    private Stage stage = null;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init");
        init();
    }

    private void init(){
        OpenButton.setOnAction(e -> {
            File file = showFileSelector("CMM 文件");
            if (file == null) {
                return;
            }
            if (!file.exists() || file.isDirectory()) {
                showErrorDialog("Error", "文件错误", "文件不存在或者是文件夹");
                return;
            }
            String text = "";
            try {
                text = readFromFile(file);
            } catch (FileNotFoundException e1) {
                showErrorDialog("ERROR","文件读取错误","不是文本文件或文件不存在！");
            }
            CodeText.textProperty().setValue(text);
        });
        LexButton.setOnAction(e->{
            ArrayList<Token> result = null;
            StringBuilder lexText= new StringBuilder();
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                lexText.append("请确认输入CMM程序不为空！");
            } else {
                TokenTree root = lexer.parse((CodeText.textProperty().get()));
                lexText.append("**********词法分析结果**********\n");
                lexText.append(lexer.getErrorInfo());
                lexText.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                result = lexer.getDisplayTokens();
                ErrorOutput.textProperty().setValue(lexText.toString());
            }
        });
        PraserButton.setOnAction(e->{
            StringBuilder out = new StringBuilder();
            TokenTree result = null;
            CMMParser parser = null;
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                out.append("请确认输入CMM程序不为空！");
            } else {
                TokenTree lexRoot = lexer.parse(CodeText.textProperty().get());
                if (lexer.getErrorNum() != 0) {
                    out.append("**********词法分析失败**********\n");
                    out.append(lexer.getErrorInfo());
                    out.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                } else {
                    out.append("**********词法分析成功，开始语法分析**********\n");
                    parser = new CMMParser(lexer.getTokens());
                    result = parser.execute();
                    out.append("**********语法分析结果**********\n");
                    out.append(parser.getErrorInfo());
                    out.append("该程序中共有" + parser.getErrorNum() + "个语法错误！\n");
                    ErrorOutput.textProperty().setValue(out.toString());
                }
            }
        });
    }
    /**
     * 错误提示框
     *
     * @param title
     * @param head
     * @param body
     */
    void showErrorDialog(String title, String head, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(head);
        alert.setContentText(body);
        alert.showAndWait();
    }

    /**
     * 文件选择框
     *
     * @param title
     * @return
     */
    File showFileSelector(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        return file;
    }
    /**
     * 输入框
     *
     * @param title
     * @param head
     * @param body
     * @return
     */
    String inputTextDialog(String title, String head, String body) {
        TextInputDialog dialog = new TextInputDialog("-1");
        dialog.setTitle(title);
        dialog.setHeaderText(head);
        dialog.setContentText(body);
        Optional<String> result = dialog.showAndWait();
        final String[] res = {"-1"};
        result.ifPresent(n -> res[0] = n);
        return res[0];
    }

    String readFromFile(File file) throws FileNotFoundException {
        String result = null;
        try (Scanner scanner = new Scanner(
                new BufferedInputStream(
                        new FileInputStream(
                                file
                        )
                )
        )) {
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()){
                stringBuilder.append(scanner.nextLine()+"\n");
            }
            result = stringBuilder.toString();
        }
        return result;
    }
}
