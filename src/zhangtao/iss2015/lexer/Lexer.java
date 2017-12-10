package zhangtao.iss2015.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * 词法分析类
 */
public class Lexer {
    // 错误信息
    private String errorInfo = "";
    // 分析后得到的tokens集合
    private ArrayList<Token> tokens = new ArrayList<>();
    // 分析后得到的所有tokens集合
    private ArrayList<Token> displayTokens = new ArrayList<>();
    // 读取CMM文件文本
    private BufferedReader reader;
    //文本缓存
    private static BufferedReader mBufferedReader;
    // 注释的标志
    private boolean isNotation = false;
    // 错误个数
    private int errorNum = 0;
    private static int currentInt;
    private static char currentChar;
    private static int lineNo;

    public boolean isNotation() {
        return isNotation;
    }

    public void setNotation(boolean isNotation) {
        this.isNotation = isNotation;
    }

    public int getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(int errorNum) {
        this.errorNum = errorNum;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<Token> getDisplayTokens() {
        return displayTokens;
    }

    public void setDisplayTokens(ArrayList<Token> displayTokens) {
        this.displayTokens = displayTokens;
    }


    /**
     * 分析一行
     *
     * @param cmmText 当前行字符串
     * @param lineNum 当前行号
     * @return 返回分析一行得到的TreeNode根节点
     */
    private TokenTreeNode executeLine(String cmmText, int lineNum) {
        // 创建当前行根结点
        String content = "第" + lineNum + "行： " + cmmText;
        TokenTreeNode node = new TokenTreeNode(content);
        // 词法分析每行结束的标志
        cmmText += "\n";
        int length = cmmText.length();
        // switch状态值
        int state = 0;
        // 记录token开始位置
        int begin = 0;
        // 记录token结束位置
        int end = 0;
        // 逐个读取当前行字符，进行分析，如果不能判定，向前多看k位
        for (int i = 0; i < length; i++) {
            char ch = cmmText.charAt(i);
            if (!isNotation) {
                if (isKnowKey(ch)) {
                    switch (state) {
                        case 0:
                            // 分隔符
                            if (isSeparator(ch)) {
                                state = 0;
                                node.add(new TokenTreeNode("分隔符 ： " + ch));
                                tokens.add(new Token(lineNum, i + 1, "分隔符", String
                                        .valueOf(ch)));
                                displayTokens.add(new Token(lineNum, i + 1, "分隔符",
                                        String.valueOf(ch)));
                            }
                            // 加号+
                            else if (ch == '+')
                                state = 1;
                                // 减号-
                            else if (ch == '-')
                                state = 2;
                                // 乘号*
                            else if (ch == '*')
                                state = 3;
                                // 除号/
                            else if (ch == '/')
                                state = 4;
                                // 赋值符号==或者等号=
                            else if (ch == '=')
                                state = 5;
                                // 小于符号<或者不等于<>
                            else if (ch == '<')
                                state = 6;
                                // 大于>
                            else if (ch == '>')
                                state = 9;
                            else if (ch == '!') {
                                state = 11;
                            }
                            // 关键字或者标识符
                            else if (isLetter(ch)) {
                                state = 7;
                                begin = i;
                            }
                            // 整数或者浮点数
                            else if (isDigit(ch)) {
                                begin = i;
                                state = 8;
                            }
                            // 双引号"
                            else if (String.valueOf(ch).equals(ConstValues.DQ)) {
                                begin = i + 1;
                                state = 10;
                                node.add(new TokenTreeNode("分隔符 ： " + ch));
                                tokens.add(new Token(lineNum, begin, "分隔符",
                                        ConstValues.DQ));
                                displayTokens.add(new Token(lineNum, begin, "分隔符",
                                        ConstValues.DQ));
                            }
                            // 空白符
                            else if (String.valueOf(ch).equals(" ")) {
                                state = 0;
                                displayTokens.add(new Token(lineNum, i + 1, "空白符",
                                        " "));
                            }
                            // 换行符
                            else if (String.valueOf(ch).equals("\n")) {
                                state = 0;
                                displayTokens.add(new Token(lineNum, i + 1, "换行符",
                                        "\n"));
                            }
                            // 回车符
                            else if (String.valueOf(ch).equals("\r")) {
                                state = 0;
                                displayTokens.add(new Token(lineNum, i + 1, "回车符",
                                        "\r"));
                            }
                            // 制表符
                            else if (String.valueOf(ch).equals("\t")) {
                                state = 0;
                                displayTokens.add(new Token(lineNum, i + 1, "制表符",
                                        "\t"));
                            }
                            break;
                        case 1:
                            node.add(new TokenTreeNode("运算符 ： " + ConstValues.PLUS));
                            tokens.add(new Token(lineNum, i, "运算符", ConstValues.PLUS));
                            displayTokens.add(new Token(lineNum, i, "运算符",
                                    ConstValues.PLUS));
                            i--;
                            state = 0;
                            break;
                        case 2:
                            String temp = tokens.get(tokens.size() - 1).getKind();
                            String c = tokens.get(tokens.size() - 1).getContent();
                            if (temp.equals("整数") || temp.equals("标识符")
                                    || temp.equals("实数") || c.equals(")")
                                    || c.equals("]")) {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.MINUS));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.MINUS));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.MINUS));
                                i--;
                                state = 0;
                            } else if (String.valueOf(ch).equals("\n")) {
                                displayTokens.add(new Token(lineNum, i - 1, "错误",
                                        ConstValues.MINUS));
                            } else {
                                begin = i - 1;
                                state = 8;
                            }
                            break;
                        case 3:
                            if (ch == '/') {
                                errorNum++;
                                errorInfo += "\t错误:第 " + lineNum + " 行,第 " + i
                                        + " 列：" + "运算符\"" + ConstValues.TIMES
                                        + "\"使用错误  \n";
                                node.add(new TokenTreeNode(ConstValues.ERROR + "运算符\""
                                        + ConstValues.TIMES + "\"使用错误"));
                                displayTokens.add(new Token(lineNum, i, "错误",
                                        cmmText.substring(i - 1, i + 1)));
                            } else {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.TIMES));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.TIMES));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.TIMES));
                                i--;
                            }
                            state = 0;
                            break;
                        case 4:
                            if (ch == '/') {
                                node.add(new TokenTreeNode("单行注释 //"));
                                displayTokens.add(new Token(lineNum, i, "单行注释符号",
                                        "//"));
                                begin = i + 1;
                                displayTokens.add(new Token(lineNum, i, "注释",
                                        cmmText.substring(begin, length - 1)));
                                i = length - 2;
                                state = 0;
                            } else if (ch == '*') {
                                node.add(new TokenTreeNode("多行注释 /*"));
                                displayTokens.add(new Token(lineNum, i, "多行注释开始符号",
                                        "/*"));
                                begin = i + 1;
                                isNotation = true;
                            } else {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.DIVIDE));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.DIVIDE));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.DIVIDE));
                                i--;
                                state = 0;
                            }
                            break;
                        case 5:
                            if (ch == '=') {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.EQUAL));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.EQUAL));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.EQUAL));
                                state = 0;
                            } else {
                                state = 0;
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.ASSIGN));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.ASSIGN));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.ASSIGN));
                                i--;
                            }
                            break;
                        case 6:
                            if (ch == '>') {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.NOTEQUAL));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.NOTEQUAL));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.NOTEQUAL));
                                state = 0;
                            } else {
                                state = 0;
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.LT));
                                tokens
                                        .add(new Token(lineNum, i, "运算符",
                                                ConstValues.LT));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.LT));
                                i--;
                            }
                            break;
                        case 7:
                            if (isLetter(ch) || isDigit(ch)) {
                                state = 7;
                            } else {
                                end = i;
                                String id = cmmText.substring(begin, end);
                                if (isKey(id)) {
                                    node.add(new TokenTreeNode("关键字 ： " + id));
                                    tokens.add(new Token(lineNum, begin + 1, "关键字",
                                            id));
                                    displayTokens.add(new Token(lineNum, begin + 1,
                                            "关键字", id));
                                } else if (matchID(id)) {
                                    node.add(new TokenTreeNode("标识符 ： " + id));
                                    tokens.add(new Token(lineNum, begin + 1, "标识符",
                                            id));
                                    displayTokens.add(new Token(lineNum, begin + 1,
                                            "标识符", id));
                                } else {
                                    errorNum++;
                                    errorInfo += "\t错误:第 " + lineNum + " 行,第 "
                                            + (begin + 1) + " 列：" + id + "是非法标识符\n";
                                    node.add(new TokenTreeNode(ConstValues.ERROR + id
                                            + "是非法标识符"));
                                    displayTokens.add(new Token(lineNum, begin + 1,
                                            "错误", id));
                                }
                                i--;
                                state = 0;
                            }
                            break;
                        case 8:
                            if (isDigit(ch) || String.valueOf(ch).equals(".")) {
                                state = 8;
                            } else {
                                if (isLetter(ch)) {
                                    errorNum++;
                                    errorInfo += "\t错误:第 " + lineNum + " 行,第 "
                                            + i + " 列：" + "数字格式错误或者标志符错误\n";
                                    node.add(new TokenTreeNode(ConstValues.ERROR
                                            + "数字格式错误或者标志符错误"));
                                    displayTokens.add(new Token(lineNum, i, "错误",
                                            cmmText.substring(begin, find(begin,
                                                    cmmText) + 1)));
                                    i = find(begin, cmmText);
                                } else {
                                    end = i;
                                    String id = cmmText.substring(begin, end);
                                    if (!id.contains(".")) {
                                        if (matchInteger(id)) {
                                            node.add(new TokenTreeNode("整数    ： " + id));
                                            tokens.add(new Token(lineNum,
                                                    begin + 1, "整数", id));
                                            displayTokens.add(new Token(lineNum,
                                                    begin + 1, "整数", id));
                                        } else {
                                            errorNum++;
                                            errorInfo += "\t错误:第 " + lineNum
                                                    + " 行,第 " + (begin + 1) + " 列："
                                                    + id + "是非法整数\n";
                                            node.add(new TokenTreeNode(ConstValues.ERROR
                                                    + id + "是非法整数"));
                                            displayTokens.add(new Token(lineNum,
                                                    begin + 1, "错误", id));
                                        }
                                    } else {
                                        if (matchReal(id)) {
                                            node.add(new TokenTreeNode("实数    ： " + id));
                                            tokens.add(new Token(lineNum,
                                                    begin + 1, "实数", id));
                                            displayTokens.add(new Token(lineNum,
                                                    begin + 1, "实数", id));
                                        } else {
                                            errorNum++;
                                            errorInfo += "\t错误:第 " + lineNum
                                                    + " 行,第 " + (begin + 1) + " 列："
                                                    + id + "是非法实数\n";
                                            node.add(new TokenTreeNode(ConstValues.ERROR
                                                    + id + "是非法实数"));
                                            displayTokens.add(new Token(lineNum,
                                                    begin + 1, "错误", id));
                                        }
                                    }
                                    i = find(i, cmmText);
                                }
                                state = 0;
                            }
                            break;
                        case 9:
                            node.add(new TokenTreeNode("运算符 ： " + ConstValues.GT));
                            tokens.add(new Token(lineNum, i, "运算符", ConstValues.GT));
                            displayTokens.add(new Token(lineNum, i, "运算符",
                                    ConstValues.GT));
                            i--;
                            state = 0;
                            break;
                        case 10:
                            if (ch == '"') {
                                end = i;
                                String string = cmmText.substring(begin, end);
                                node.add(new TokenTreeNode("字符串 ： " + string));
                                tokens.add(new Token(lineNum, begin + 1, "字符串",
                                        string));
                                displayTokens.add(new Token(lineNum, begin + 1,
                                        "字符串", string));
                                node.add(new TokenTreeNode("分隔符 ： " + ConstValues.DQ));
                                tokens.add(new Token(lineNum, end + 1, "分隔符",
                                        ConstValues.DQ));
                                displayTokens.add(new Token(lineNum, end + 1,
                                        "分隔符", ConstValues.DQ));
                                state = 0;
                            } else if (i == length - 1) {
                                String string = cmmText.substring(begin);
                                errorNum++;
                                errorInfo += "\t错误:第 " + lineNum + " 行,第 "
                                        + (begin + 1) + " 列：" + "字符串： " + string.trim()
                                        + "  缺少引号  \n";
                                node.add(new TokenTreeNode(ConstValues.ERROR + "字符串： "
                                        + string.trim() + "  缺少引号  \n"));
                                displayTokens.add(new Token(lineNum, i + 1, "错误",
                                        string));
                            }
                            break;
                        case 11:
                            if (ch == '=') {
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.NOTEQUAL));
                                tokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.NOTEQUAL));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.NOTEQUAL));
                                state = 0;
                            } else {
                                state = 0;
                                node.add(new TokenTreeNode("运算符 ： " + ConstValues.NOT));
                                tokens
                                        .add(new Token(lineNum, i, "运算符",
                                                ConstValues.NOT));
                                displayTokens.add(new Token(lineNum, i, "运算符",
                                        ConstValues.NOT));
                                i--;
                            }
                            break;
                    }
                } else {
                    if (isUnKnowKey(ch)) {
                        errorNum++;
                        errorInfo += "\t错误:第 " + lineNum + " 行,第 "
                                + (i + 1) + " 列：" + "\"" + ch + "\"是不可识别符号  \n";
                        node.add(new TokenTreeNode(ConstValues.ERROR + "\"" + ch
                                + "\"是不可识别符号"));
                        if (state == 0)
                            displayTokens.add(new Token(lineNum, i + 1, "错误",
                                    String.valueOf(ch)));
                    }
                }
            } else {
                if (ch == '*') {
                    state = 3;
                } else if (ch == '/' && state == 3) {
                    node.add(new TokenTreeNode("多行注释 */"));
                    displayTokens.add(new Token(lineNum, begin + 1, "注释",
                            cmmText.substring(begin, i - 1)));
                    displayTokens.add(new Token(lineNum, i, "多行注释结束符号", "*/"));
                    isNotation = false;
                    state = 0;
                } else if (i == length - 2) {
                    displayTokens.add(new Token(lineNum, begin + 1, "注释",
                            cmmText.substring(begin, length - 1)));
                    displayTokens.add(new Token(lineNum, length - 1, "换行符",
                            "\n"));
                    state = 0;
                } else {
                    state = 0;
                }
            }
        }
        return node;
    }

    /**
     * 开始分析前的准备工作
     */
    private void prepare() {
        setErrorInfo("");
        setErrorNum(0);
        setTokens(new ArrayList<>());
        setDisplayTokens(new ArrayList<>());
        setNotation(false);
    }

    /**
     * 分析CMM程序
     *
     * @param cmmText CMM程序文本
     * @return 分析生成的TreeNode
     */
    public TokenTreeNode parse(String cmmText) {
        prepare();
        StringReader stringReader = new StringReader(cmmText);
        String eachLine = "";
        int lineNum = 1;
        TokenTreeNode root = new TokenTreeNode("PROGRAM");
        reader = new BufferedReader(stringReader);
        while (eachLine != null) {
            try {
                eachLine = reader.readLine();
                if (eachLine != null) {
                    if (isNotation() && !eachLine.contains("*/")) {
                        eachLine += "\n";
                        TokenTreeNode temp = new TokenTreeNode(eachLine);
                        temp.add(new TokenTreeNode("多行注释"));
                        displayTokens.add(new Token(lineNum, 1, "注释", eachLine
                                .substring(0, eachLine.length() - 1)));
                        displayTokens.add(new Token(lineNum,
                                eachLine.length() - 1, "换行符", "\n"));
                        root.add(temp);
                        lineNum++;
                        continue;
                    } else {
                        root.add((executeLine(eachLine, lineNum)));
                    }
                }
                lineNum++;
            } catch (IOException e) {
                System.err.println("读取文本失败！");
            }
        }
        return root;
    }


    /**
     * 是不是定义以内的字符
     *
     * @param ch 要识别的字符
     * @return
     */
    private boolean isKnowKey(char ch) {
        return (ch == '(' || ch == ')' || ch == ';' || ch == '{'
                || ch == '}' || ch == '[' || ch == ']' || ch == ','
                || ch == '+' || ch == '-' || ch == '*' || ch == '/'
                || ch == '=' || ch == '<' || ch == '>' || ch == '"'
                || ch == '!'
                || isLetter(ch) || isDigit(ch)
                || String.valueOf(ch).equals(" ")
                || String.valueOf(ch).equals("\n")
                || String.valueOf(ch).equals("\r")
                || String.valueOf(ch).equals("\t"));
    }

    /**
     * 是不是定义以外的字符
     *
     * @param ch 要识别的字符
     * @return
     */
    private boolean isUnKnowKey(char ch) {
        return (ch > 19967 && ch < 40870 || ch == '\\' || ch == '~'
                || ch == '；' || ch == '【' || ch == '】' || ch == '，'
                || ch == '。' || ch == '“' || ch == '”' || ch == '‘'
                || ch == '’' || ch == '？' || ch == '（' || ch == '）'
                || ch == '《' || ch == '》' || ch == '·'
                || ch == '`' || ch == '|' || ch == '、' || ch == '^'
                || ch == '?' || ch == '&' || ch == '^' || ch == '%'
                || ch == '$' || ch == '@' || ch == '!' || ch == '#');
    }

    /**
     * 是不是分隔符
     *
     * @param ch 要识别的字符
     * @return
     */
    private boolean isSeparator(char ch) {
        return (ch == '(' || ch == ')' || ch == ';' || ch == '{'
                || ch == '}' || ch == '[' || ch == ']'
                || ch == ',');
    }

    /**
     * 是不是字母
     *
     * @param c 要识别的字符
     * @return
     */
    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * 是不是数字
     *
     * @param c 要识别的字符
     * @return
     */
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 是不是正确的整数：排除多个零的情况
     *
     * @param input 要识别的字符串
     * @return
     */
    private static boolean matchInteger(String input) {
        return input.matches("^-?\\d+$") && !input.matches("^-?0{1,}\\d+$");
    }

    /**
     * 是不是正确的浮点数：排除00.000的情况
     *
     * @param input 要识别的字符串
     * @return
     */
    private static boolean matchReal(String input) {
        return input.matches("^(-?\\d+)(\\.\\d+)+$")
                && !input.matches("^(-?0{2,}+)(\\.\\d+)+$");
    }

    /**
     * 是不是正确的标识符：有字母、数字、下划线组成，必须以字母开头，不能以下划线结尾
     *
     * @param input 要识别的字符串
     * @return
     */
    private static boolean matchID(String input) {
        return input.matches("^\\w+$") && !input.endsWith("_")
                && input.substring(0, 1).matches("[A-Za-z]");
    }

    /**
     * 是不是保留字
     *
     * @param str 要分析的字符串
     * @return
     */
    private static boolean isKey(String str) {
        return str.equals(ConstValues.IF) || str.equals(ConstValues.ELSE)
                || str.equals(ConstValues.WHILE) || str.equals(ConstValues.READ)
                || str.equals(ConstValues.WRITE) || str.equals(ConstValues.INT)
                || str.equals(ConstValues.DOUBLE) || str.equals(ConstValues.BOOL)
                || str.equals(ConstValues.STRING) || str.equals(ConstValues.TRUE)
                || str.equals(ConstValues.FALSE);
    }

    /**
     * 查找
     *
     * @param begin
     * @param str
     * @return
     */
    private static int find(int begin, String str) {
        if (begin >= str.length())
            return str.length();
        for (int i = begin; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\n' || c == ',' || c == ' ' || c == '\t' || c == '{'
                    || c == '}' || c == '(' || c == ')' || c == ';' || c == '='
                    || c == '+' || c == '-' || c == '*' || c == '/' || c == '['
                    || c == ']' || c == '<' || c == '>' || c == '!')

                return i - 1;
        }
        return str.length();
    }

    /**
     * 获取TokenList
     */
    public static LinkedList<Token> getTokenList(BufferedReader br) throws IOException {
        //todo : getTokenList
        lineNo = 1;
        mBufferedReader = br;
        LinkedList<Token> tokenList = new LinkedList<Token>();
        StringBuilder sb = new StringBuilder();
        readChar();
        while (currentInt != -1) {
            //消耗空白字符
            if (currentChar == '\n'
                    || currentChar == '\r'
                    || currentChar == '\t'
                    || currentChar == '\f'
                    || currentChar == ' ') {
                readChar();
                continue;
            }
            //简单特殊符号
            switch (currentChar) {
                case ';':
                    tokenList.add(new Token(Token.SEMI, lineNo));
                    readChar();
                    continue;
                case '+':
                    tokenList.add(new Token(Token.PLUS, lineNo));
                    readChar();
                    continue;
                case '-':
                    tokenList.add(new Token(Token.MINUS, lineNo));
                    readChar();
                    continue;
                case '*':
                    tokenList.add(new Token(Token.MUL, lineNo));
                    readChar();
                    continue;
                case '(':
                    tokenList.add(new Token(Token.LPARENT, lineNo));
                    readChar();
                    continue;
                case ')':
                    tokenList.add(new Token(Token.RPARENT, lineNo));
                    readChar();
                    continue;
                case '[':
                    tokenList.add(new Token(Token.LBRACKET, lineNo));
                    readChar();
                    continue;
                case ']':
                    tokenList.add(new Token(Token.RBRACKET, lineNo));
                    readChar();
                    continue;
                case '{':
                    tokenList.add(new Token(Token.LBRACE, lineNo));
                    readChar();
                    continue;
                case '}':
                    tokenList.add(new Token(Token.RBRACE, lineNo));
                    readChar();
                    continue;
                    // no default:
            }
            //复合特殊符号
            if (currentChar == '/') {
                readChar();
                if (currentChar == '*') {//多行注释
//                    tokenList.add(new Token(Token.LCOM, lineNo));
                    readChar();
                    while (true) {//使用死循环消耗多行注释内字符
                        if (currentChar == '*') {//如果是*,那么有可能是多行注释结束的地方
                            readChar();
                            if (currentChar == '/') {//多行注释结束符号
//                                tokenList.add(new Token(Token.RCOM, lineNo));
                                readChar();
                                break;
                            }
                        } else {//如果不是*就继续读下一个,相当于忽略了这个字符
                            readChar();
                        }
                    }
                    continue;
                } else if (currentChar == '/') {//单行注释
//                    tokenList.add(new Token(Token.SCOM, lineNo));
                    while (currentChar != '\n') {//消耗这一行之后的内容
                        readChar();
                    }
                    continue;
                } else {//是除号
                    tokenList.add(new Token(Token.DIV, lineNo));
                    continue;
                }
            } else if (currentChar == '=') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.EQ, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.ASSIGN, lineNo));
                }
                continue;
            } else if (currentChar == '>') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.GET, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.GT, lineNo));
                }
                continue;
            } else if (currentChar == '<') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.LET, lineNo));
                    readChar();
                } else if (currentChar == '>') {
                    tokenList.add(new Token(Token.NEQ, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.LT, lineNo));
                }
                continue;
            } else if (currentChar == '!') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.NEQ, lineNo));
                    readChar();
                }
                continue;
            }
            //数字
            if (currentChar >= '0' && currentChar <= '9') {
                boolean isReal = false;//是否小数
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    if (currentChar == '.') {
                        if (isReal) {
                            break;
                        } else {
                            isReal = true;
                        }
                    }
                    sb.append(currentChar);
                    readChar();
                }
                if (isReal) {
                    tokenList.add(new Token(Token.LITERAL_REAL, sb.toString(), lineNo));
                } else {
                    tokenList.add(new Token(Token.LITERAL_INT, sb.toString(), lineNo));
                }
                sb.delete(0, sb.length());
                continue;
            }
            //字符组成的标识符,包括保留字和ID
            if ((currentChar >= 'a' && currentChar <= 'z') || currentChar == '_') {
                //取剩下的可能是的字符
                while ((currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar == '_'
                        || (currentChar >= '0' && currentChar <= '9')) {
                    sb.append(currentChar);
                    readChar();
                }
                Token token = new Token(lineNo);
                String sbString = sb.toString();
                if (sbString.equals("if")) {
                    token.setType(Token.IF);
                } else if (sbString.equals("else")) {
                    token.setType(Token.ELSE);
                } else if (sbString.equals("while")) {
                    token.setType(Token.WHILE);
                } else if (sbString.equals("read")) {
                    token.setType(Token.READ);
                } else if (sbString.equals("write")) {
                    token.setType(Token.WRITE);
                } else if (sbString.equals("int")) {
                    token.setType(Token.INT);
                } else if (sbString.equals("double")) {
                    token.setType(Token.DOUBLE);
                }
                else if (sbString.equals("bool")) {
                    token.setType(Token.BOOL);
                } else {
                    token.setType(Token.ID);
                    token.setValue(sbString);
                }
                sb.delete(0, sb.length());
                tokenList.add(token);
                continue;
            }
            readChar();
        }
        return tokenList;
    }

    /**
     * 这个方法也会统计换行,但是方法本身不会改变字符流的读取
     */
    private static void readChar() throws IOException {
        currentChar = (char) (currentInt = mBufferedReader.read());
        if (currentChar == '\n') {
            lineNo++;
        }
    }
}