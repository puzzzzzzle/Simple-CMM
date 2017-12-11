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
import zhangtao.iss2015.praser.CMMParser;
import zhangtao.iss2015.praser.TokenTreeNode;
import zhangtao.iss2015.semantic.CodeGenerater;
import zhangtao.iss2015.semantic.FourCodeItem;
import zhangtao.iss2015.semantic.Semantic;
import zhangtao.iss2015.semantic.SymbolTable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

/**
 * GUI控制器
 */
public class Controller implements Initializable, GUIOuterController {

    //xml中的控件
    public Button OpenButton;
    public Button LexButton;
    public Button PraserButton;
    public Button RunButton;

    public TextArea CodeText;
    public TextArea StateOutput;
    public TextArea PrecessOutput;
    public TextArea TreeText;
    public Button FourCodeButton;
    public Button LexTreeButton;
    public Button GrammarTreeButton;

    private Stage stage = null;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * 初始化
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init");
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //打开按钮
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
                showErrorDialog("ERROR", "文件读取错误", "不是文本文件或文件不存在！");
            }
            CodeText.textProperty().setValue(text);
        });
        //词法分析按钮
        LexButton.setOnAction(e -> {
            ArrayList<Token> result = null;
            StringBuilder lexText = new StringBuilder();
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                lexText.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode root = lexer.parse((CodeText.textProperty().get()));
                lexText.append("**********词法分析结果**********\n");
                lexText.append(lexer.getErrorInfo());
                lexText.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                result = lexer.getDisplayTokens();
                StateOutput.textProperty().setValue(lexText.toString());
            }
        });

        //语法分析按钮
        PraserButton.setOnAction(e -> {
            StringBuilder out = new StringBuilder();
            TokenTreeNode result = null;
            CMMParser parser = null;
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                out.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode lexRoot = lexer.parse(CodeText.textProperty().get());
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
                    StateOutput.textProperty().setValue(out.toString());
                }
            }
        });
        //运行按钮
        RunButton.setOnAction(e -> {
            PrecessOutput.textProperty().setValue("");
            TreeText.textProperty().setValue("");
            StateOutput.textProperty().setValue("");

            StringBuilder out = new StringBuilder();
            TokenTreeNode result = null;
            CMMParser parser = null;
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get() == null || CodeText.textProperty().get().equals("")) {
                out.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode lexRoot = lexer.parse(CodeText.textProperty().get());
                if (lexer.getErrorNum() != 0) {
                    out.append("**********词法分析失败**********\n");
                    out.append(lexer.getErrorInfo());
                    out.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                    StateOutput.textProperty().setValue(out.toString());
                    PrecessOutput.textProperty().setValue("词法分析失败");

                    return;
                }
                parser = new CMMParser(lexer.getTokens());
                TokenTreeNode node = parser.execute();
                if (parser.getErrorNum() != 0) {
                    out.append("**********语法分析失败**********\n");
                    out.append(parser.getErrorInfo());
                    out.append("该程序中共有" + parser.getErrorNum() + "个语法错误！\n");
                    StateOutput.textProperty().setValue(out.toString());
                    PrecessOutput.textProperty().setValue("语法分析失败");
                    return;
                }
                StateOutput.textProperty().setValue("**********语法分析成功，开始语义分析**********\n");
                Semantic semanticAnalysis = new Semantic(node, this);
                try {
                    semanticAnalysis.start();
                    if (semanticAnalysis.getErrorNum() != 0) {
                        out.append("**********语意分析失败**********\n");
                        out.append(semanticAnalysis.getErrorInfo());
                        out.append("该程序中共有" + semanticAnalysis.getErrorNum() + "个语意错误！\n");
                        StateOutput.textProperty().setValue(out.toString());
                        PrecessOutput.textProperty().setValue(PrecessOutput.textProperty().get()+"\n"+"语意分析失败");
                    } else {
                        StateOutput.textProperty().setValue(StateOutput.textProperty().get() + "**********程序正常结束**********\n");
                    }
                } catch (Exception e1) {
                    StateOutput.textProperty().setValue("*********语意分析失败,出现未记录的语意错误！*********");
                    PrecessOutput.textProperty().setValue(PrecessOutput.textProperty().get()+"\n"+"语意分析失败");
                }

            }
        });
        //四元式解析按钮
        FourCodeButton.setOnAction(e -> {
            PrecessOutput.textProperty().setValue("");
            TreeText.textProperty().setValue("");
            StateOutput.textProperty().setValue("");

            StringBuilder out = new StringBuilder();
            TokenTreeNode result = null;
            CMMParser parser = null;
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get() == null || CodeText.textProperty().get().equals("")) {
                out.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode lexRoot = lexer.parse(CodeText.textProperty().get());
                if (lexer.getErrorNum() != 0) {
                    out.append("**********词法分析失败**********\n");
                    out.append(lexer.getErrorInfo());
                    out.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                    StateOutput.textProperty().setValue(out.toString());
                    PrecessOutput.textProperty().setValue("词法分析失败");

                    return;
                }
                parser = new CMMParser(lexer.getTokens());
                TokenTreeNode node = parser.execute();
                if (parser.getErrorNum() != 0) {
                    out.append("**********语法分析失败**********\n");
                    out.append(parser.getErrorInfo());
                    out.append("该程序中共有" + parser.getErrorNum() + "个语法错误！\n");
                    StateOutput.textProperty().setValue(out.toString());
                    PrecessOutput.textProperty().setValue("语法分析失败");
                    return;
                }
                StateOutput.textProperty().setValue("**********语法分析成功，开始语义分析**********\n");
                Semantic semanticAnalysis = new Semantic(node, this);
                try {
                    semanticAnalysis.start();
                    if (semanticAnalysis.getErrorNum() != 0) {
                        out.append("**********语意分析失败**********\n");
                        out.append(semanticAnalysis.getErrorInfo());
                        out.append("该程序中共有" + semanticAnalysis.getErrorNum() + "个语意错误！\n");
                        StateOutput.textProperty().setValue(out.toString());
                        PrecessOutput.textProperty().setValue("语意分析失败");
                    } else {
                        StateOutput.textProperty().setValue("**********程序正常结束**********\n");
                    }
                } catch (Exception e1) {
                    StateOutput.textProperty().setValue("*********语意分析失败,出现未记录的语意错误！*********");
                    PrecessOutput.textProperty().setValue("语意分析失败");
                    return;
                }
            }
            generateCode();
        });
        LexTreeButton.setOnAction(e -> {
            ArrayList<Token> result = null;
            StringBuilder lexText = new StringBuilder();
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                lexText.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode root = lexer.parse((CodeText.textProperty().get()));
                lexText.append("**********词法分析结果**********\n");
                lexText.append(lexer.getErrorInfo());
                lexText.append("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
                result = lexer.getDisplayTokens();
                StateOutput.textProperty().setValue(lexText.toString());
                StringBuilder tree = new StringBuilder();
                tree.append("   第1行：\n");
                int j[] = {1};
                result.forEach(i -> {
                    if (i.getContent().equals("\n")) {
                        j[0]++;
                        tree.append("\n   第" + j[0] + "行：\n");
                    }
                    if (i.getContent().equals("\n")) {
                        tree.append(i.getKind() + " : " + "\\n" + "\n");
                    } else {
                        tree.append(i.getKind() + " : " + i.getContent() + "\n");
                    }
                });
                TreeText.textProperty().setValue(tree.toString());
            }
        });
        GrammarTreeButton.setOnAction(e -> {
            StringBuilder out = new StringBuilder();
            TokenTreeNode result = null;
            CMMParser parser = null;
            Lexer lexer = new Lexer();
            if (CodeText.textProperty().get().equals("")) {
                out.append("请确认输入CMM程序不为空！");
            } else {
                TokenTreeNode lexRoot = lexer.parse(CodeText.textProperty().get());
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
                    StateOutput.textProperty().setValue(out.toString());
                    TreeText.textProperty().setValue(iteratorTree(result, ""));
                }
            }
        });
    }

    //遍历多叉树
    public String iteratorTree(TokenTreeNode treeNode, String space) {
        space += " ";
        StringBuilder sb = new StringBuilder();
        if (treeNode != null) {
            if ("Parser".equals(treeNode.getContent())) {
                sb.append(treeNode.getContent() + "\n");
            }
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                sb.append(space + treeNode.getChildAt(i).getContent() + "\n");
                if (treeNode.getChildAt(i).getChildCount() != 0) {
                    sb.append(iteratorTree(treeNode.getChildAt(i), space));
                }
            }
        }

        return sb.toString();
    }

    //生成中间代码
    private void generateCode() {
        String text = CodeText.textProperty().get();
        LinkedList<FourCodeItem> codes;
        SymbolTable symbolTable = SymbolTable.getSymbolTable();
        symbolTable.newTable();
        codes = CodeGenerater.generateCode(text);
        symbolTable.deleteTable();
        StringBuilder sb = new StringBuilder();
        for (FourCodeItem code : codes) {
            sb.append(code.toString() + "\r\n");
        }
        TreeText.textProperty().setValue(sb.toString());
    }

    /**
     * 错误提示框
     *
     * @param title
     * @param head
     * @param body
     */
    @Override
    public void showErrorDialog(String title, String head, String body) {
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
    private File showFileSelector(String title) {
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
    @Override
    public String inputTextDialog(String title, String head, String body) {
        TextInputDialog dialog = new TextInputDialog("-1");
        dialog.setTitle(title);
        dialog.setHeaderText(head);
        dialog.setContentText(body);
        Optional<String> result = dialog.showAndWait();
        final String[] res = {"-1"};
        result.ifPresent(n -> res[0] = n);
        return res[0];
    }

    private String readFromFile(File file) throws FileNotFoundException {
        String result = null;
        try (Scanner scanner = new Scanner(
                new BufferedInputStream(
                        new FileInputStream(
                                file
                        )
                )
        )) {
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine() + "\n");
            }
            result = stringBuilder.toString();
        }
        return result;
    }

    @Override
    public void writeToConsole(String s) {
        if (PrecessOutput.textProperty().get().equals("")) {
            PrecessOutput.textProperty().setValue(s);
        } else {
            PrecessOutput.textProperty().setValue(PrecessOutput.textProperty().get() + "\n" + s);

        }
    }
}
