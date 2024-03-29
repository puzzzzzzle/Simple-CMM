package zhangtao.iss2015.fourcode;


import zhangtao.iss2015.lexer.Lexer;
import zhangtao.iss2015.lexer.Token;
import zhangtao.iss2015.praser.CMMParser;
import zhangtao.iss2015.semantic.SymbolTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
/**
 * 生成四元式
 */
public class FourCode {
    private static int mLevel;
    private static int mLine;
    private static LinkedList<FourCodeItem> codes;
    private static SymbolTable symbolTable;

    private void interpret(TokenList node) {
        while (true) {
            switch (node.getType()) {
                case TokenList.IF_STMT:
                    interpretIfStmt(node);
                    break;
                case TokenList.WHILE_STMT:
                {
                    int jmpline = mLine + 1;
                    FourCodeItem falsejmp = new FourCodeItem(FourCodeItem.JMP, interpretExp(node.getLeft()), null, null);
                    codes.add(falsejmp);
                    mLine++;
                    codes.add(new FourCodeItem(FourCodeItem.IN, null, null, null));
                    mLine++;
                    mLevel++;
                    interpret(node.getMiddle());
                    SymbolTable.getSymbolTable().deregister(mLevel);
                    mLevel--;
                    codes.add(new FourCodeItem(FourCodeItem.OUT, null, null, null));
                    mLine++;
                    codes.add(new FourCodeItem(FourCodeItem.JMP, null, null, jmpline + ""));
                    mLine++;
                    falsejmp.setForth(String.valueOf(mLine + 1));
                    break;
                }
                case TokenList.READ_STMT:
                {
                    String varname = null;
                    int type = symbolTable.getSymbolType(node.getLeft().getValue());
                    switch (type) {
                        case Symbol.SINGLE_INT:
                        case Symbol.SINGLE_REAL:
                            codes.add(new FourCodeItem(FourCodeItem.READ, null, null, node.getLeft().getValue()));
                            mLine++;
                            break;
                        case Symbol.ARRAY_INT:
                        case Symbol.ARRAY_REAL:
                            codes.add(new FourCodeItem(FourCodeItem.READ, null, null, node.getLeft().getValue() + "[" + interpretExp(node.getLeft().getLeft()) + "]"));
                            mLine++;
                            break;
                        case Symbol.TEMP:
                        default:

                    }
                    break;
                }
                case TokenList.WRITE_STMT:
                    codes.add(new FourCodeItem(FourCodeItem.WRITE, null, null, interpretExp(node.getLeft())));
                    mLine++;
                    break;
                case TokenList.DECLARE_STMT:
                {
                    SymbolTable table = SymbolTable.getSymbolTable();
                    TokenList var = node.getLeft();
                    if (var.getLeft() == null) {//单值
                        String value = null;
                        if (node.getMiddle() != null) {
                            value = interpretExp(node.getMiddle());
                        }
                        if (var.getDataType() == Token.INT) {
                            codes.add(new FourCodeItem(FourCodeItem.INT, value, null, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_INT, mLevel);
                            table.register(symbol);
                        } else if (var.getDataType() == Token.DOUBLE) {
                            codes.add(new FourCodeItem(FourCodeItem.REAL, value, null, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_REAL, mLevel);
                            table.register(symbol);
                        }
                    } else {
                        String len = interpretExp(var.getLeft());
                        if (var.getDataType() == Token.INT) {
                            codes.add(new FourCodeItem(FourCodeItem.INT, null, len, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_INT, mLevel);
                            table.register(symbol);
                        } else {
                            codes.add(new FourCodeItem(FourCodeItem.REAL, null, len, var.getValue()));
                            mLine++;
                            Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_REAL, mLevel);
                            table.register(symbol);
                        }
                    }

                    break;
                }
                case TokenList.ASSIGN_STMT:
                {
                    String value = interpretExp(node.getMiddle());

                    TokenList var = node.getLeft();
                    if (var.getLeft() == null) {//单值
                        codes.add(new FourCodeItem(FourCodeItem.ASSIGN, value, null, var.getValue()));
                        mLine++;
                    } else {
                        String index = interpretExp(var.getLeft());
                        codes.add(new FourCodeItem(FourCodeItem.ASSIGN, value, null, var.getValue() + "[" + index + "]"));
                        mLine++;
                    }
                    break;
                }
                default:
                    break;
            }
            symbolTable.clearTempNames();
            if (node.getNext() != null) {
                node = node.getNext();
            } else {
                break;
            }
        }
    }

    private void interpretIfStmt(TokenList node){
        if (node.getType() == TokenList.IF_STMT) {
            /**
             * 条件跳转 jmp 条件  null 目标  条件为假时跳转
             */
            FourCodeItem falsejmp = new FourCodeItem(FourCodeItem.JMP, interpretExp(node.getLeft()), null, null);
            codes.add(falsejmp);
            mLine++;
            codes.add(new FourCodeItem(FourCodeItem.IN, null, null, null));
            mLine++;
            mLevel++;
            interpret(node.getMiddle());
            SymbolTable.getSymbolTable().deregister(mLevel);
            mLevel--;
            codes.add(new FourCodeItem(FourCodeItem.OUT, null, null, null));
            mLine++;
            if (node.getRight() != null) {
                FourCodeItem outjump = new FourCodeItem(FourCodeItem.JMP, null, null, null);
                codes.add(outjump);
                mLine++;
                falsejmp.setForth(String.valueOf(mLine + 1));
                codes.add(new FourCodeItem(FourCodeItem.IN, null, null, null));
                mLine++;
                mLevel++;
                interpret(node.getRight());
                codes.add(new FourCodeItem(FourCodeItem.OUT, null, null, null));
                mLine++;
                SymbolTable.getSymbolTable().deregister(mLevel);
                mLevel--;
                outjump.setForth(String.valueOf(mLine + 1));
            } else {
                falsejmp.setForth(String.valueOf(mLine + 1));
            }
        }
    }

    private String interpretExp(TokenList node) {
        if (node.getType() == TokenList.EXP) {
            switch (node.getDataType()) {
                case Token.LOGIC_EXP:
                    return interpretLogicExp(node);
                case Token.ADDTIVE_EXP:
                    return interpretAddtiveExp(node);
                case Token.TERM_EXP:
                    return interpretTermExp(node);
                default:
            }
        } else if (node.getType() == TokenList.FACTOR) {
            if (node.getDataType() == Token.MINUS) {
                String temp = symbolTable.getTempSymbol().getName();
                codes.add(new FourCodeItem(FourCodeItem.MINUS, interpretExp(node.getLeft()), null, temp));
                mLine++;
                return temp;
            } else {
                return interpretExp(node.getLeft());
            }
        } else if (node.getType() == TokenList.VAR) {
            if (node.getLeft() == null) {//单值
                if (symbolTable.getSymbolType(node.getValue()) == Symbol.SINGLE_INT || symbolTable.getSymbolType(node.getValue()) == Symbol.SINGLE_REAL) {
                    return node.getValue();
                }
            } else {
                if (symbolTable.getSymbolType(node.getValue()) == Symbol.ARRAY_INT || symbolTable.getSymbolType(node.getValue()) == Symbol.ARRAY_REAL) {
                    String temp = symbolTable.getTempSymbol().getName();
                    String index = interpretExp(node.getLeft());
                    codes.add(new FourCodeItem(FourCodeItem.ASSIGN, node.getValue() + "[" + index + "]", null, temp));
                    mLine++;
                    return temp;
                }
            }
        } else if (node.getType() == TokenList.LITREAL) {
            return node.getValue();
        }
        return null;
    }

    private String interpretLogicExp(TokenList node) {
        String temp = symbolTable.getTempSymbol().getName();
        switch (node.getMiddle().getDataType()) {
            case Token.GT:
                codes.add(new FourCodeItem(FourCodeItem.GT, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.GET:
                codes.add(new FourCodeItem(FourCodeItem.GET, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.LT:
                codes.add(new FourCodeItem(FourCodeItem.LT, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.LET:
                codes.add(new FourCodeItem(FourCodeItem.LET, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.EQ:
                codes.add(new FourCodeItem(FourCodeItem.EQ, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            case Token.NEQ:
                codes.add(new FourCodeItem(FourCodeItem.NEQ, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));
                break;
            default:
        }
        mLine++;
        return temp;
    }

    private String interpretAddtiveExp(TokenList node){
        String temp = symbolTable.getTempSymbol().getName();
        switch (node.getMiddle().getDataType()) {
            case Token.PLUS:
                codes.add(new FourCodeItem(FourCodeItem.PLUS, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));

                break;
            case Token.MINUS:
                codes.add(new FourCodeItem(FourCodeItem.MINUS, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp));

                break;
            default:
        }
        mLine++;
        return temp;
    }

    /**
     * 修正存储结构带来的整数乘除法从右往左的计算错误
     * 注意term的Tree left一定是factor
     */
    private String interpretTermExp(TokenList node){
        String opcode = getOpcode(node.getMiddle().getDataType());
        String temp1 = symbolTable.getTempSymbol().getName();
        if (node.getRight().getType() == TokenList.FACTOR) {
            codes.add(new FourCodeItem(opcode, interpretExp(node.getLeft()), interpretExp(node.getRight()), temp1));
            mLine++;
        } else {
            codes.add(new FourCodeItem(opcode, interpretExp(node.getLeft()), interpretExp(node.getRight().getLeft()), temp1));
            mLine++;
            node = node.getRight();
            String temp2 = null;
            while (node.getRight() != null && node.getRight().getType() != TokenList.FACTOR) {
                opcode = getOpcode(node.getMiddle().getDataType());
                temp2 = symbolTable.getTempSymbol().getName();
                codes.add(new FourCodeItem(opcode, temp1, interpretExp(node.getRight().getLeft()), temp2));
                mLine++;
                node = node.getRight();
                temp1 = temp2;
            }
            opcode = getOpcode(node.getMiddle().getDataType());
            temp2 = symbolTable.getTempSymbol().getName();
            codes.add(new FourCodeItem(opcode, temp1, interpretExp(node.getRight()), temp2));
            mLine++;
            temp1 = temp2;
        }
        return temp1;
    }

    private String getOpcode(int op) {
        if (op == Token.MUL) {
            return FourCodeItem.MUL;
        } else {
            return FourCodeItem.DIV;
        }
    }

    public static LinkedList<Token> getTokenList(String filestr) throws IOException {
        StringReader sr = new StringReader(filestr);
        BufferedReader br = new BufferedReader(sr);
        LinkedList<Token> tokenList = Lexer.getTokenList(br);
        br.close();
        sr.close();
        return tokenList;
    }
    public static LinkedList<FourCodeItem> generateCode(String text) {
        mLine = -1;//代码编号从0开始
        mLevel = 0;
        codes = new LinkedList<FourCodeItem>();
        try {
            LinkedList<TokenList> nodeList = getNodeList(getTokenList(text));
            symbolTable = SymbolTable.getSymbolTable();
            symbolTable.newTable();
            FourCode generator = new FourCode();
            for (TokenList node : nodeList) {
                generator.interpret(node);
            }
            symbolTable.deleteTable();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return codes;
    }
    public static LinkedList<TokenList> getNodeList(LinkedList<Token> tokenList){
        LinkedList<TokenList> nodeList = CMMParser.getTreeNodeList(tokenList);
        return nodeList;
    }
}
