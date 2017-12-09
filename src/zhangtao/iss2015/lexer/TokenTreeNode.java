package zhangtao.iss2015.lexer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 节点树
 */
public class TokenTreeNode extends DefaultMutableTreeNode {
    //当前结点类型
    private String nodeKind;
    // 当前结点内容
    private String content;
    //当前结点所在行号
    private int lineNum;
    //如果是数组的话，它的大小,或操作符中，操作单元的位置
    private int arraySize=-1;

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }



    public TokenTreeNode() {
        super();
        nodeKind = "";
        content = "";
    }

    public TokenTreeNode(String content) {
        super(content);
        this.content = content;
        nodeKind = "";
    }

    public TokenTreeNode(String kind, String content) {
        super(content);
        this.content = content;
        nodeKind = kind;
    }

    public TokenTreeNode(String kind, String content, int lineNum) {
        super(content);
        this.content = content;
        this.lineNum = lineNum;
        nodeKind = kind;
    }

    public String getNodeKind() {
        return nodeKind;
    }

    public void setNodeKind(String nodeKind) {
        this.nodeKind = nodeKind;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        setUserObject(content);
    }

    /**
     * 为该结点添加孩子结点
     *
     * @param childNode
     *            要添加的孩子结点
     */
    public void add(TokenTreeNode childNode) {
        super.add(childNode);
    }

    public TokenTreeNode getChildAt(int index) {
        return (TokenTreeNode) super.getChildAt(index);
    }

}
