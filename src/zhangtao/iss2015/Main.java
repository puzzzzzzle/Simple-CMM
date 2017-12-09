package zhangtao.iss2015;

import zhangtao.iss2015.lexer.Lexer;
import zhangtao.iss2015.lexer.Token;
import zhangtao.iss2015.lexer.TokenTree;
import zhangtao.iss2015.praser.CMMParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //参数判断
        if (args.length != 1) {
            System.out.println("参数错误！");
            return;
        }
//        //System.in获取文件信息
//        Scanner scanner1 = new Scanner(System.in);
//        String name = scanner1.nextLine();
        //预制文件信息
//        args = new String[1];
//        args[0]= "test8_阶乘.cmm";
//        args[0]= "error2_array.cmm";
//        args[0] = "test1_变量声明.cmm";
//        args[0]="error3_comment.cmm";

        if (!new File(args[0]).exists()) {
            System.out.println(args[0] + "：文件不存在！");
            return;
        }
        //开始分析
        String path = args[0];
        StringBuilder stringBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(new InputStreamReader(
                new BufferedInputStream(
                        new FileInputStream(path)
                ), "UTF-8"
        )
        )) {
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
//        ArrayList<Token> result = lex(stringBuilder.toString(), System.out);
//        System.out.println("**********打印已获取的tokens**********");
//        result.forEach(n -> System.out.print(n.getKind()+"\t:\t"+n.getContent() + "\n"));
        TokenTree root = parser(stringBuilder.toString(), System.out);
    }

    /**
     * 测试lex方法
     *
     * @param text 要解析的CMM程序
     * @param out  解析信息输出到PrintStream中，需要的地方获取
     * @return 解析结果ArrayList<Token>
     */
    public static ArrayList<Token> lex(String text, PrintStream out) {
        ArrayList<Token> result = null;
        PrintStream printStream = new PrintStream(out);
        Lexer lexer = new Lexer();
        if (text.equals("")) {
            printStream.println("请确认输入CMM程序不为空！");
        } else {
            TokenTree root = lexer.parse(text);
            printStream.println("**********词法分析结果**********\n");
            printStream.println(lexer.getErrorInfo());
            printStream.println("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
            result = lexer.getDisplayTokens();
        }
        return result;
    }

    /**
     * 测试语法分析
     *
     * @param text
     * @param out
     * @return
     */
    public static TokenTree parser(String text, PrintStream out) {
        TokenTree result = null;
        CMMParser parser = null;
        PrintStream printStream = new PrintStream(out);
        Lexer lexer = new Lexer();
        if (text.equals("")) {
            printStream.println("请确认输入CMM程序不为空！");
        } else {
            TokenTree lexRoot = lexer.parse(text);
            if (lexer.getErrorNum() != 0) {
                printStream.println("**********词法分析失败**********\n");
                printStream.println(lexer.getErrorInfo());
                printStream.println("该程序中共有" + lexer.getErrorNum() + "个词法错误！\n");
            } else {
                printStream.println("**********词法分析成功，开始语法分析**********\n");
                parser = new CMMParser(lexer.getTokens());
                result = parser.execute();
                printStream.println("**********语法分析结果**********\n");
                printStream.println(parser.getErrorInfo());
                printStream.println("该程序中共有" + parser.getErrorNum() + "个语法错误！\n");
            }
        }
        return result;
    }
}
