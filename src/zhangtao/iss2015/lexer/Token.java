package zhangtao.iss2015.lexer;

/**
 * 识别Token类
 */
public class Token {
    //类型
    private String kind;
    //内容
    private String content;
    //行
    private int line;
    //所在列
    private int column;

    public Token(int l, int c, String k, String con) {
        this.line = l;
        this.column = c;
        this.kind = k;
        this.content = con;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}