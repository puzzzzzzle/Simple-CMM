package zhangtao.iss2015.fourcode;

/**
 * 四元式
 * assign 元素 null 目标
 * assign 值 null 目标
 * int/double null 元素个数/null 变量名 给整数/浮点数赋值
 * read/write null null 元素 读取/写出元素值
 * jmp 条件  null 目标  条件为假时跳转到目标
 * jmp null null 目标  无条件跳转到目标
 * in null null null 进语句块
 * out null null null 出语句块
 * +  值  值  目标
 * -  值  值  目标
 * *  值  值  目标
 * /  值  值  目标
 */
public class FourCodeItem {
    //四元式数据
    private String first;
    private String second;
    private String third;
    private String forth;


    /**
     * 定义操作项
     */
    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String IN = "in";
    public static final String JMP = "jmp";
    public static final String OUT = "out";
    public static final String INT = "int";
    public static final String REAL = "double";
    public static final String ASSIGN = "assign";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String MUL = "*";
    public static final String DIV = "/";
    public static final String GT = ">";
    public static final String LT = "<";
    public static final String GET = ">=";
    public static final String LET = "<=";
    public static final String EQ = "==";
    public static final String NEQ = "!=";



    /**
     * 四元式结构
     *
     * @param first
     * @param second
     * @param third
     * @param forth
     */
    public FourCodeItem(String first, String second, String third, String forth) {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
        this.forth = forth;
    }

    /**
     * 获取四元式
     */
    public void setForth(String forth) {
        this.forth = forth;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s, %s)", first, second, third, forth);
    }
}