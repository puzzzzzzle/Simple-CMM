package zhangtao.iss2015.fourcode;


import zhangtao.iss2015.lexer.Token;

/**
 * 四元式解析支持
 * token 列表
 */
public class TokenList {

    /**
     * 语句块使用链表存储,使用NULL类型的Tree作为头部
     * 不要使用NULL的节点存储信息,仅使用next指向下一个Tree
     */
    public static final int NULL = 0;
    /**
     * if语句
     * left存放exp表达式
     * middle存放if条件正确时的Tree
     * 若存在else则在right存放的Tree
     */
    public static final int IF_STMT = 1;
    /**
     * left存放exp表达式
     * middle存放循环体
     */
    public static final int WHILE_STMT = 2;
    /**
     * left存放var
     */
    public static final int READ_STMT = 3;
    /**
     * left存放exp
     */
    public static final int WRITE_STMT = 4;
    /**
     * 声明语句left中存放var节点
     * 若赋值,则exp存放中middle中
     */
    public static final int DECLARE_STMT = 5;
    /**
     * 赋值语句
     * left存放var
     * middle存放exp
     */
    public static final int ASSIGN_STMT = 6;
    /**
     * 复合表达式
     * 复合表达式则形如left middle right
     * 此时datatype为可能为LOGIC_EXP\ADDTIVE_EXP\TERM_EXP
     * value==null
     */
    public static final int EXP = 7;
    /**
     * 变量
     * datatype存放类型Token.INT和 DOUBLE value存放变量名
     * left在声明语句中变量的left值代表变量长度exp
     * 在其他的调用中变量的left代表变量索引值exp
     * exp若为null,则说明是单个的变量,不是数组
     * 不存储值
     */
    public static final int VAR = 8;
    /**
     * 运算符
     * 在datatype中存储操作符类型
     */
    public static final int OP = 9;

    /**
     * 因子
     * 有符号datatype存储TOKEN.PLUS/MINUS
     * left中存放一个Tree
     * 若Tree为var,代表一个变量/数组元素
     * 若Tree为exp,则是一个表达式因子
     * 若是LITREAL,则value中存放字面值的字符形式
     * EXP为因子时,mDataType存储符号PLUS/MINUS
     */
    public static final int FACTOR = 10;

    /**
     * 字面值
     * value中存放字面值,无符号
     * datatype存放类型,在TOKEN中
     */
    public static final int LITREAL = 11;

    private int type;
    private TokenList mLeft;
    private TokenList mMiddle;
    private TokenList mRight;
    private int mDataType;
    private String value;
    //若为代码块中的代码,则mNext指向其后面的一条语句

    private TokenList mNext;

    public TokenList(int type) {
        super();
        this.type = type;
        switch (this.type) {
            case FACTOR:
            case LITREAL:
                this.mDataType = Token.PLUS;
                break;
            default:
                break;
        }
    }
    // getter and setter
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public TokenList getLeft() {
        return mLeft;
    }
    public void setLeft(TokenList mLeft) {
        this.mLeft = mLeft;
    }
    public TokenList getMiddle() {
        return mMiddle;
    }
    public void setMiddle(TokenList mMiddle) {
        this.mMiddle = mMiddle;
    }
    public TokenList getRight() {
        return mRight;
    }
    public void setRight(TokenList mRight) {
        this.mRight = mRight;
    }
    public TokenList getNext() {
        return mNext;
    }
    public void setNext(TokenList mNext) {
        this.mNext = mNext;
    }
    public int getDataType() {
        return mDataType;
    }
    public void setDataType(int type) {
        this.mDataType = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        switch (this.type) {
            case IF_STMT: return "IF_STMT";
            case WHILE_STMT: return "WHILE_STMT";
            case READ_STMT: return "READ_STMT";
            case WRITE_STMT: return "WRITE_STMT";
            case DECLARE_STMT: return "DECLARE_STMT";
            case ASSIGN_STMT: return "ASSIGN_STMT";
            case EXP:// return "EXP";
            case VAR: return this.value;// return "VAR";
            case OP: return new Token(this.mDataType).toString();// return "OP";
            case NULL:return "";// return "BLOCK HEADER";
            default: return "UNKNOWN";
        }
    }
}
