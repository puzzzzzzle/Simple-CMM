package zhangtao.iss2015.praser;


import zhangtao.iss2015.lexer.ConstValues;
import zhangtao.iss2015.lexer.Token;
import zhangtao.iss2015.lexer.TokenTree;

import java.util.List;

/**
 * CMM语法分析器
 */
public class CMMParser {

    // 词法分析得到的tokens
    private List<Token> tokens;
    // 标记当前token的目录
    private int index = 0;
    // 存放当前token的值
    private Token currentToken = null;
    // 错误个数
    private int errorNum = 0;
    // 错误信息
    private String errorInfo = "";
    // 语法分析根结点
    private static TokenTree root;

    public CMMParser(List<Token> tokens) {
        this.tokens = tokens;
        if (tokens.size() != 0)
            currentToken = tokens.get(0);
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    /**
     * 开始分析
     *
     * @return
     */
    public TokenTree execute() {
        this.setIndex(0);
        this.setErrorInfo("");
        this.setErrorNum(0);
        root = new TokenTree("Parser");
        while (index < tokens.size()) {
            root.add(statement());
        }
        return root;
    }

    /**
     * 调到当前语句的；出，用于结束当前语句的分析
     */
    private void jumpToNextStatement() {
        while (!currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        }
    }

    /**
     * 取出tokens中的下一个token
     */
    private void nextToken() {
        index++;
        if (index > tokens.size() - 1) {
            currentToken = null;
            if (index > tokens.size())
                index--;
            return;
        }
        currentToken = tokens.get(index);
    }

    /**
     * 取出tokens中的上一个token
     */
    private void preToken() {
        index--;
        if (index < 0) {
            index = 0;
        }
        currentToken = tokens.get(index);
    }

    /**
     * 出错处理
     *
     * @param error 出错信息
     */
    private void error(String error) {
        String line = "    错误:第 ";
        Token previous = tokens.get(index - 1);
        if (currentToken != null
                && currentToken.getLine() == previous.getLine()) {
            line += currentToken.getLine() + " 行,第 " + currentToken.getColumn()
                    + " 列：";
        } else
            line += previous.getLine() + " 行,第 " + previous.getColumn() + " 列：";
        errorInfo += line + error;
        errorNum++;
    }


    /**
     * 处理每条语句
     *
     * @return TokenTree
     */
    private final TokenTree statement() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        // 声明语句
        if (currentToken != null
                &&
                ((currentToken.getContent().equals(ConstValues.INT)
                        || currentToken.getContent().equals(ConstValues.DOUBLE) || currentToken
                        .getContent().equals(ConstValues.BOOL))
                        || currentToken.getContent().equals(ConstValues.STRING))
                ) {
            tempNode = declareStatement();
        }
        // 赋值语句
        else if (currentToken != null && currentToken.getKind().equals("标识符")) {
            tempNode = assignStatement(false);
        }

        // For循环语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.FOR)) {
            tempNode = forStatement();
        }
        // If条件语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.IF)) {
            tempNode = ifStatement();
        }
        // While循环语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.WHILE)) {
            tempNode = whileStatement();
        }
        // read语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.READ)) {
            TokenTree readNode = new TokenTree("关键字", ConstValues.READ, currentToken
                    .getLine());
            readNode.add(readStatement());
            tempNode = readNode;
        }
        // write语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.WRITE)) {
            TokenTree writeNode = new TokenTree("关键字", ConstValues.WRITE,
                    currentToken.getLine());
            writeNode.add(writeStatement());
            tempNode = writeNode;
        }
        // 出错处理
        else {
            String error = " 语法错误：" + currentToken.getContent() + "\n";
            error(error);
            tempNode = new TokenTree(ConstValues.ERROR + "语法错误！");
            nextToken();
        }
        return tempNode;
    }

    /**
     * 声明变量
     *
     * @param root 根结点
     * @return TokenTree
     */
    private final TokenTree declareProcess(TokenTree root) {
        //数组开始
        if (currentToken.getContent().equals(ConstValues.LBRACKET)) {
//            System.out.println("[");
//            if (currentToken != null
//                    && currentToken.getContent().equals(ConstValues.LBRACKET)) {
//                idNode.add(array());
//            } else
            // 保存要返回的结点
            TokenTree tempNode = null;
            nextToken();
            if (currentToken.getKind().equals("整数")) {
                String num = currentToken.getContent();
                nextToken();
                if (currentToken.getContent().equals(ConstValues.RBRACKET)) {
//                    TokenTree idNode = new TokenTree("数组标识符", currentToken.getContent(),
//                            currentToken.getLine());
//                    root.add(idNode);
                    nextToken();
                    if (currentToken.getKind().equals("标识符")) {
                        TokenTree idNode = new TokenTree("标识符", currentToken.getContent(),
                                currentToken.getLine());
                        idNode.setArraySize(Integer.parseInt(num));
                        root.add(idNode);
                    } else {
                        String error = " 数组声明失败，不是标识符\"" + "\n";
                        error(error);
                        jumpToNextStatement();
                        return new TokenTree(ConstValues.ERROR + "数组声明失败，不是标识符\"]\"");
                    }
                    nextToken();
                    if (!currentToken.getContent().equals(ConstValues.SEMICOLON)) {
                        String error = " 数组一次只能声明一个，且不能赋值\"" + "\n";
                        error(error);
                        jumpToNextStatement();
                        return new TokenTree(ConstValues.ERROR + "数组一次只能声明一个，且不能赋值\"]\"");
                    }
                } else {
                    String error = " 缺少右中括号\"]\"" + "\n";
                    error(error);
                    jumpToNextStatement();
                    return new TokenTree(ConstValues.ERROR + "缺少右中括号\"]\"");
                }
            } else {
                String error = "数组中必须用整数来初始化" + "\n";
                error(error);
                jumpToNextStatement();
                return new TokenTree(ConstValues.ERROR + "数组中必须为整数来初始化\"]\"");
            }
        } else {
            if (currentToken != null && currentToken.getKind().equals("标识符")) {
                TokenTree idNode = new TokenTree("标识符", currentToken.getContent(),
                        currentToken.getLine());
                root.add(idNode);
                nextToken();
                // 处理array的情况
                if (currentToken != null
                        && currentToken.getContent().equals(ConstValues.LBRACKET)) {
                    String error = " 数组声明语句出错，数组大小应在关键字后面" + "\n";
                    error(error);
                    root.add(new TokenTree(ConstValues.ERROR + "数组应在关键字后申明大小"));
                    jumpToNextStatement();
                } else if (currentToken != null
                        && !currentToken.getContent().equals(ConstValues.ASSIGN)
                        && !currentToken.getContent().equals(ConstValues.SEMICOLON)
                        && !currentToken.getContent().equals(ConstValues.COMMA)) {
                    String error = " 声明语句出错,标识符后出现不正确的token" + "\n";
                    error(error);
                    root
                            .add(new TokenTree(ConstValues.ERROR
                                    + "声明语句出错,标识符后出现不正确的token"));
                    nextToken();
                }
            } else { // 报错
                String error = " 声明语句中标识符出错" + "\n";
                error(error);
                root.add(new TokenTree(ConstValues.ERROR + "声明语句中标识符出错"));
                nextToken();
            }
            // 匹配赋值符号=
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.ASSIGN)) {
                TokenTree assignNode = new TokenTree("分隔符", ConstValues.ASSIGN,
                        currentToken.getLine());
                root.add(assignNode);
                nextToken();
                assignNode.add(condition());
            }
        }
        return root;
    }

    /**
     * for语句
     *
     * @return TokenTree
     */
    private final TokenTree forStatement() {
        // 是否有大括号,默认为true
        boolean hasBrace = true;
        // if函数返回结点的根结点
        TokenTree forNode = new TokenTree("关键字", "for", currentToken.getLine());
        nextToken();
        // 匹配左括号
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) {
            nextToken();
        } else { // 报错
            String error = " for循环语句缺少左括号\"(\"" + "\n";
            error(error);
            forNode.add(new TokenTree(ConstValues.ERROR + "for循环语句缺少左括号\"(\""));
        }
        // initialization
        TokenTree initializationNode = new TokenTree("initialization",
                "Initialization", currentToken.getLine());
        initializationNode.add(assignStatement(true));
        forNode.add(initializationNode);
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        } else {
            String error = " for循环语句缺少分号\";\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "for循环语句缺少分号\";\"");
        }
        // condition
        TokenTree conditionNode = new TokenTree("condition", "Condition",
                currentToken.getLine());
        conditionNode.add(condition());
        forNode.add(conditionNode);
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        } else {
            String error = " for循环语句缺少分号\";\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "for循环语句缺少分号\";\"");
        }
        // change
        TokenTree changeNode = new TokenTree("change", "Change", currentToken
                .getLine());
        changeNode.add(assignStatement(true));
        forNode.add(changeNode);
        // 匹配右括号)
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RPAREN)) {
            nextToken();
        } else { // 报错
            String error = " if条件语句缺少右括号\")\"" + "\n";
            error(error);
            forNode.add(new TokenTree(ConstValues.ERROR + "if条件语句缺少右括号\")\""));
        }
        // 匹配左大括号{
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LBRACE)) {
            nextToken();
        } else {
            hasBrace = false;
        }
        // statement
        TokenTree statementNode = new TokenTree("statement", "Statements",
                currentToken.getLine());
        forNode.add(statementNode);
        if (hasBrace) {
            while (currentToken != null) {
                if (!currentToken.getContent().equals(ConstValues.RBRACE))
                    statementNode.add(statement());
                else if (statementNode.getChildCount() == 0) {
                    forNode.remove(forNode.getChildCount() - 1);
                    statementNode.setContent("EmptyStm");
                    forNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.RBRACE)) {
                nextToken();
            } else { // 报错
                String error = " if条件语句缺少右大括号\"}\"" + "\n";
                error(error);
                forNode.add(new TokenTree(ConstValues.ERROR + "if条件语句缺少右大括号\"}\""));
            }
        } else {
            statementNode.add(statement());
        }
        return forNode;
    }

    /**
     * if语句
     *
     * @return TokenTree
     */
    private final TokenTree ifStatement() {
        // if语句是否有大括号,默认为true
        boolean hasIfBrace = true;
        // else语句是否有大括号,默认为true
        boolean hasElseBrace = true;
        // if函数返回结点的根结点
        TokenTree ifNode = new TokenTree("关键字", "if", currentToken.getLine());
        nextToken();
        // 匹配左括号(
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) {
            nextToken();
        } else { // 报错
            String error = " if条件语句缺少左括号\"(\"" + "\n";
            error(error);
            ifNode.add(new TokenTree(ConstValues.ERROR + "if条件语句缺少左括号\"(\""));
        }
        // condition
        TokenTree conditionNode = new TokenTree("condition", "Condition",
                currentToken.getLine());
        ifNode.add(conditionNode);
        conditionNode.add(condition());
        // 匹配右括号)
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RPAREN)) {
            nextToken();
        } else { // 报错
            String error = " if条件语句缺少右括号\")\"" + "\n";
            error(error);
            ifNode.add(new TokenTree(ConstValues.ERROR + "if条件语句缺少右括号\")\""));
        }
        // 匹配左大括号{
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LBRACE)) {
            nextToken();
        } else {
            hasIfBrace = false;
        }
        // statement
        TokenTree statementNode = new TokenTree("statement", "Statements",
                currentToken.getLine());
        ifNode.add(statementNode);
        if (hasIfBrace) {
            while (currentToken != null) {
                if (!currentToken.getContent().equals(ConstValues.RBRACE))
                    statementNode.add(statement());
                else if (statementNode.getChildCount() == 0) {
                    ifNode.remove(ifNode.getChildCount() - 1);
                    statementNode.setContent("EmptyStm");
                    ifNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.RBRACE)) {
                nextToken();
            } else { // 报错
                String error = " if条件语句缺少右大括号\"}\"" + "\n";
                error(error);
                ifNode.add(new TokenTree(ConstValues.ERROR + "if条件语句缺少右大括号\"}\""));
            }
        } else {
            if (currentToken != null)
                statementNode.add(statement());
        }
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.ELSE)) {
            TokenTree elseNode = new TokenTree("关键字", ConstValues.ELSE, currentToken
                    .getLine());
            ifNode.add(elseNode);
            nextToken();
            // 匹配左大括号{
            if (currentToken.getContent().equals(ConstValues.LBRACE)) {
                nextToken();
            } else {
                hasElseBrace = false;
            }
            if (hasElseBrace) {
                // statement
                while (currentToken != null
                        && !currentToken.getContent().equals(ConstValues.RBRACE)) {
                    elseNode.add(statement());
                }
                // 匹配右大括号}
                if (currentToken != null
                        && currentToken.getContent().equals(ConstValues.RBRACE)) {
                    nextToken();
                } else { // 报错
                    String error = " else语句缺少右大括号\"}\"" + "\n";
                    error(error);
                    elseNode.add(new TokenTree(ConstValues.ERROR
                            + "else语句缺少右大括号\"}\""));
                }
            } else {
                if (currentToken != null)
                    elseNode.add(statement());
            }
        }
        return ifNode;
    }

    /**
     * while语句
     *
     * @return TokenTree
     */
    private final TokenTree whileStatement() {
        // 是否有大括号,默认为true
        boolean hasBrace = true;
        // while函数返回结点的根结点
        TokenTree whileNode = new TokenTree("关键字", ConstValues.WHILE, currentToken
                .getLine());
        nextToken();
        // 匹配左括号(
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) {
            nextToken();
        } else { // 报错
            String error = " while循环缺少左括号\"(\"" + "\n";
            error(error);
            whileNode.add(new TokenTree(ConstValues.ERROR + "while循环缺少左括号\"(\""));
        }
        // condition
        TokenTree conditionNode = new TokenTree("condition", "Condition",
                currentToken.getLine());
        whileNode.add(conditionNode);
        conditionNode.add(condition());
        // 匹配右括号)
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RPAREN)) {
            nextToken();
        } else { // 报错
            String error = " while循环缺少右括号\")\"" + "\n";
            error(error);
            whileNode.add(new TokenTree(ConstValues.ERROR + "while循环缺少右括号\")\""));
        }
        // 匹配左大括号{
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LBRACE)) {
            nextToken();
        } else {
            hasBrace = false;
        }
        // statement
        TokenTree statementNode = new TokenTree("statement", "Statements",
                currentToken.getLine());
        whileNode.add(statementNode);
        if (hasBrace) {
            while (currentToken != null
                    && !currentToken.getContent().equals(ConstValues.RBRACE)) {
                if (!currentToken.getContent().equals(ConstValues.RBRACE))
                    statementNode.add(statement());
                else if (statementNode.getChildCount() == 0) {
                    whileNode.remove(whileNode.getChildCount() - 1);
                    statementNode.setContent("EmptyStm");
                    whileNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.RBRACE)) {
                nextToken();
            } else { // 报错
                String error = " while循环缺少右大括号\"}\"" + "\n";
                error(error);
                whileNode.add(new TokenTree(ConstValues.ERROR + "while循环缺少右大括号\"}\""));
            }
        } else {
            if (currentToken != null)
                statementNode.add(statement());
        }
        return whileNode;
    }

    /**
     * read语句
     *
     * @return TokenTree
     */
    private final TokenTree readStatement() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        nextToken();
        // 匹配左括号(
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) {
            nextToken();
        } else {
            String error = " read语句缺少左括号\"(\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "read语句缺少左括号\"(\"");
        }
        // 匹配标识符
        if (currentToken != null && currentToken.getKind().equals("标识符")) {
            tempNode = new TokenTree("标识符", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
            // 判断是否是为数组赋值
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.LBRACKET)) {
                tempNode.add(array());
            }
        } else {
            String error = " read语句左括号后不是标识符" + "\n";
            error(error);
            nextToken();
            return new TokenTree(ConstValues.ERROR + "read语句左括号后不是标识符");
        }
        // 匹配右括号)
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RPAREN)) {
            nextToken();
        } else {
            String error = " read语句缺少右括号\")\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "read语句缺少右括号\")\"");
        }
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        } else {
            String error = " read语句缺少分号\";\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "read语句缺少分号\";\"");
        }
        return tempNode;
    }

    /**
     * write语句
     *
     * @return TokenTree
     */
    private final TokenTree writeStatement() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        nextToken();
        // 匹配左括号(
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) {
            nextToken();
        } else {
            String error = " write语句缺少左括号\"(\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "write语句缺少左括号\"(\"");
        }
        // 调用expression函数匹配表达式
        tempNode = expression();
        // 匹配右括号)
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RPAREN)) {
            nextToken();
        } else {
            String error = " write语句缺少右括号\")\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "write语句缺少右括号\")\"");
        }
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        } else {
            String error = " write语句缺少分号\";\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "write语句缺少分号\";\"");
        }
        return tempNode;
    }

    /**
     * 赋值语句
     *
     * @param isFor 是否是在for循环中调用
     * @return TokenTree
     */
    private final TokenTree assignStatement(boolean isFor) {
        // assign函数返回结点的根结点
        TokenTree assignNode = new TokenTree("运算符", ConstValues.ASSIGN, currentToken
                .getLine());
        TokenTree idNode = new TokenTree("标识符", currentToken.getContent(),
                currentToken.getLine());
        assignNode.add(idNode);
        nextToken();
        // 判断是否是为数组赋值
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LBRACKET)) {
            idNode.add(array());
        }
        // 匹配赋值符号=
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.ASSIGN)) {
            nextToken();
        } else { // 报错
            String error = " 赋值语句缺少\"=\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "赋值语句缺少\"=\"");
        }
        // expression
        assignNode.add(condition());
        // 如果不是在for循环语句中调用声明语句,则匹配分号
        if (!isFor) {
            // 匹配分号;
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
                nextToken();
            } else { // 报错
                String error = " 赋值语句缺少分号\";\"" + "\n";
                error(error);
                assignNode.add(new TokenTree(ConstValues.ERROR + "赋值语句缺少分号\";\""));
            }
        }
        return assignNode;
    }

    /**
     * 声明语句
     *
     * @return TokenTree
     */
    private final TokenTree declareStatement() {
        TokenTree declareNode = new TokenTree("关键字", currentToken.getContent(),
                currentToken.getLine());
        nextToken();
        // declareProcess
        declareNode = declareProcess(declareNode);
        // 处理同时声明多个变量的情况
        String next = null;
        while (currentToken != null) {
            next = currentToken.getContent();
            if (next.equals(ConstValues.COMMA)) {
                nextToken();
                declareNode = declareProcess(declareNode);
            } else {
                break;
            }
            if (currentToken != null)
                next = currentToken.getContent();
        }
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.SEMICOLON)) {
            nextToken();
        } else { // 报错
            String error = " 声明语句缺少分号\";\"" + "\n";
            error(error);
            declareNode.add(new TokenTree(ConstValues.ERROR + "声明语句缺少分号\";\""));
        }
        return declareNode;
    }


    /**
     * @return TokenTree
     */
    private final TokenTree condition() {
        // 记录expression生成的结点
        TokenTree tempNode = expression();
        // 如果条件判断为比较表达式
        if (currentToken != null
                && (currentToken.getContent().equals(ConstValues.EQUAL)
                || currentToken.getContent().equals(ConstValues.NOTEQUAL)
                || currentToken.getContent().equals(ConstValues.LT) || currentToken
                .getContent().equals(ConstValues.GT))) {
            TokenTree comparisonNode = comparisonOperation();
            comparisonNode.add(tempNode);
            comparisonNode.add(expression());
            return comparisonNode;
        }
        // 如果条件判断为bool变量
        return tempNode;
    }

    /**
     * @return TokenTree
     */
    private final TokenTree expression() {
        // 记录term生成的结点
        TokenTree tempNode = term();

        // 如果下一个token为加号或减号
        while (currentToken != null
                && (currentToken.getContent().equals(ConstValues.PLUS) || currentToken
                .getContent().equals(ConstValues.MINUS))) {
            // addOperation
            TokenTree addNode = addOperation();
            addNode.add(tempNode);
            tempNode = addNode;
            tempNode.add(term());
        }
        return tempNode;
    }

    /**
     * @return TokenTree
     */
    private final TokenTree term() {
        // 记录factor生成的结点
        TokenTree tempNode = factor();

        // 如果下一个token为乘号或除号
        while (currentToken != null
                && (currentToken.getContent().equals(ConstValues.TIMES) || currentToken
                .getContent().equals(ConstValues.DIVIDE))) {
            // mulOperation
            TokenTree mulNode = mulOperation();
            mulNode.add(tempNode);
            tempNode = mulNode;
            tempNode.add(factor());
        }
        return tempNode;
    }

    /**
     * @return TokenTree
     */
    private final TokenTree factor() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        if (currentToken != null && currentToken.getKind().equals("整数")) {
            tempNode = new TokenTree("整数", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null && currentToken.getKind().equals("实数")) {
            tempNode = new TokenTree("实数", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.TRUE)) {
            tempNode = new TokenTree("布尔值", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.FALSE)) {
            tempNode = new TokenTree("布尔值", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null && currentToken.getKind().equals("标识符")) {
            tempNode = new TokenTree("标识符", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.LBRACKET)) {
                tempNode.add(array());
            }
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LPAREN)) { // 匹配左括号(
            nextToken();
            tempNode = expression();
            // 匹配右括号)
            if (currentToken != null
                    && currentToken.getContent().equals(ConstValues.RPAREN)) {
                nextToken();
            } else { // 报错
                String error = " 算式因子缺少右括号\")\"" + "\n";
                error(error);
                return new TokenTree(ConstValues.ERROR + "算式因子缺少右括号\")\"");
            }
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.DQ)) { // 匹配双引号
            nextToken();
            tempNode = new TokenTree("字符串", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
            // 匹配另外一个双引号
            nextToken();
        } else { // 报错
            String error = " 算式因子存在错误" + "\n";
            error(error);
            if (currentToken != null
                    && !currentToken.getContent().equals(ConstValues.SEMICOLON)) {
                nextToken();
            }
            return new TokenTree(ConstValues.ERROR + "算式因子存在错误");
        }
        return tempNode;
    }

    /**
     * array操作
     *
     * @return TokenTree
     */
    private final TokenTree array() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LBRACKET)) {
            nextToken();
        } else {
            String error = " 缺少左中括号\"[\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "缺少左中括号\"[\"");
        }
        // 调用expression函数匹配表达式
        tempNode = expression();
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.RBRACKET)) {
            nextToken();
        } else { // 报错
            String error = " 缺少右中括号\"]\"" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "缺少右中括号\"]\"");
        }
        return tempNode;
    }

    /**
     * add语句
     *
     * @return TokenTree
     */
    private final TokenTree addOperation() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.PLUS)) {
            tempNode = new TokenTree("运算符", ConstValues.PLUS, currentToken
                    .getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.MINUS)) {
            tempNode = new TokenTree("运算符", ConstValues.MINUS, currentToken
                    .getLine());
            nextToken();
        } else { // 报错
            String error = " 加减符号出错" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "加减符号出错");
        }
        return tempNode;
    }

    /**
     * mul语句
     *
     * @return TokenTree
     */
    private final TokenTree mulOperation() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.TIMES)) {
            tempNode = new TokenTree("运算符", ConstValues.TIMES, currentToken
                    .getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.DIVIDE)) {
            tempNode = new TokenTree("运算符", ConstValues.DIVIDE, currentToken
                    .getLine());
            nextToken();
        } else { // 报错
            String error = " 乘除符号出错" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "乘除符号出错");
        }
        return tempNode;
    }

    /**
     * 比较
     * comparison语句
     *
     * @return TokenTree
     */
    private final TokenTree comparisonOperation() {
        // 保存要返回的结点
        TokenTree tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstValues.LT)) {
            tempNode = new TokenTree("运算符", ConstValues.LT, currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.GT)) {
            tempNode = new TokenTree("运算符", ConstValues.GT, currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.EQUAL)) {
            tempNode = new TokenTree("运算符", ConstValues.EQUAL, currentToken
                    .getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstValues.NOTEQUAL)) {
            tempNode = new TokenTree("运算符", ConstValues.NOTEQUAL, currentToken
                    .getLine());
            nextToken();
        } else { // 报错
            String error = " 比较运算符出错" + "\n";
            error(error);
            return new TokenTree(ConstValues.ERROR + "比较运算符出错");
        }
        return tempNode;
    }


}