package zhangtao.iss2015.semantic;

/**
 * list中元素的symbol自身组成链表,在同名不同层的符号出现时使用,此外在linkedlist中存储不同名的符号
 */
public class Symbol {
    public static final int TEMP = -1;
    public static final int SINGLE_INT = 0;
    public static final int SINGLE_REAL = 1;
    public static final int ARRAY_INT = 2;
    public static final int ARRAY_REAL = 3;
    /**
     * 供value使用
     */
    public static final int TRUE = 4;
    public static final int FALSE = 5;

    private String name;
    private int type;
    private Value value;
    private int level;
    private Symbol next;

    /**
     * 当type是ARRAY_*时，通过调用value的initArray方法来初始化数组
     */
    public Symbol(String name, int type, int level) {
        this.name

                = name;
        this.type = type;
        this.level = level;
        this.next = null;
        this.value = new Value(type);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 获取下一个同名Symbol
     */
    public Symbol getNext() {
        return next;
    }

    /**
     * 设置下一个同名symbol
     */
    public void setNext(Symbol next) {
        this.next = next;
    }
}
