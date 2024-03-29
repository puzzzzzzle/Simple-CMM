package zhangtao.iss2015.semantic;

import zhangtao.iss2015.gui.GUIOuterController;
import zhangtao.iss2015.lexer.ConstValues;
import zhangtao.iss2015.praser.TokenTreeNode;
import java.math.BigDecimal;


/**
 * CMM语义分析器
 */
public class Semantic {
    public Semantic(TokenTreeNode root, GUIOuterController controller) {
        this.root = root;
        this.controller=controller;
    }

    // GUI 中传进来的来的UI控制器
    private GUIOuterController controller = null;

    // 语义分析信息
    // 符号表
	private SymbolTable table = new SymbolTable();
	// 抽象语法树
	private TokenTreeNode root;
	// 错误信息
	private String errorInfo = "";
	// 错误个数
	private int errorNum = 0;
	// 标识符作用域
	private int level = 0;

	/**
	 * 进程运行时执行的方法
	 */
	public void start() {
		//todo:run
		table.removeAll();
		statement(root);
	}
	/**
	 * 语义分析
	 * @param root
	 */
	private void statement(TokenTreeNode root) {
		for (int i = 0; i < root.getChildCount(); i++) {
			TokenTreeNode currentNode = root.getChildAt(i);
			String content = currentNode.getContent();
			if (content.equals(ConstValues.INT) || content.equals(ConstValues.DOUBLE)
					|| content.equals(ConstValues.BOOL)
					|| content.equals(ConstValues.STRING)) {
				processDeclare(currentNode);
			} else if (content.equals(ConstValues.ASSIGN)) {
				processAssign(currentNode);
			}
			else if (content.equals(ConstValues.IF)) {
				// 进入if语句，改变作用域
				level++;
				processIf(currentNode);
				//输出if语句，改变作用域并更新符号表
				level--;
				table.update(level);
			} else if (content.equals(ConstValues.WHILE)) {
				// 进入while语句，改变作用域
				level++;
				processWhile(currentNode);
				//输出while语句，改变作用域并更新符号表
				level--;
				table.update(level);
			} else if (content.equals(ConstValues.READ)) {
				processRead(currentNode.getChildAt(0));
			} else if (content.equals(ConstValues.WRITE)) {
				processWrite(currentNode.getChildAt(0));
			}
		}
	}
    /**
     * 语义分析错误处理方法
     * @param error 错误信息
     * @param line 错误位置
     */
    public void error(String error, int line) {
        errorNum++;
        String s = ConstValues.ERROR + "第 " + line + " 行：" + error + "\n";
        errorInfo += s;
    }
    /**
     * 识别整数
     * @param input
     * @return
     */
    private static boolean matchInteger(String input) {
        if (input.matches("^-?\\d+$") && !input.matches("^-?0+\\d+$"))
            return true;
        else
            return false;
    }

    /**
     * 识别浮点数
     * @param input
     * @return
     */
    private static boolean matchReal(String input) {
        if (input.matches("^(-?\\d+)(\\.\\d+)+$")
                && !input.matches("^(-?0{2,}+)(\\.\\d+)+$"))
            return true;
        else
            return false;
    }


    /**
     * 读取用户输入
     * @return 返回用户输入内容的字符串形式
     */
    public synchronized String readInput(){
        //调用Controller
        String result = controller.inputTextDialog("输入","输入","输入");
        return result;
    }
	/**
	 * 分析declare语句
	 * @param root
	 */
	private void processDeclare(TokenTreeNode root) {
		// 结点显示的内容,即声明变量的类型int real bool string
		String content = root.getContent();
		int index = 0;
		while (index < root.getChildCount()) {
			TokenTreeNode temp = root.getChildAt(index);
			// 变量值
			String name = temp.getContent();
			// 判断变量是否已经被声明
			if (table.getCurrentLevel(name, level) == null) {
				if (temp.getChildCount() == 0) {
					Element element = new Element(temp
							.getContent(), content, temp.getLineNum(), level);
					index++;
					// 判断变量是否在声明时被初始化
					if (index < root.getChildCount()
							&& root.getChildAt(index).getContent().equals(
									ConstValues.ASSIGN)) {
						// 获得变量的初始子节点
						TokenTreeNode valueNode = root.getChildAt(index).getChildAt(
								0);
						String value = valueNode.getContent();
						if (content.equals(ConstValues.INT)) { // 声明int型变量
							if (matchInteger(value)) {
								element.setIntValue(value);
								element.setRealValue(String.valueOf(Double
										.parseDouble(value)));
							} else if (matchReal(value)) {
								String error = "无法将浮点数赋值给整型变量";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "无法把" + value + "赋值给整型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("字符串")) {
								String error = "无法将字符串赋值给整型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("标识符")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.INT)) {
										element.setIntValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getIntValue());
										element.setRealValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getRealValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.DOUBLE)) {
										String error = "无法将浮点型变量赋值给整型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.BOOL)) {
										String error = "无法将布尔型变量赋值给整型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.STRING)) {
										String error = "无法将字符串变量赋值给整型变量";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstValues.PLUS)
									|| value.equals(ConstValues.MINUS)
									|| value.equals(ConstValues.TIMES)
									|| value.equals(ConstValues.DIVIDE)) {
								String result = processExpression(valueNode);
								if (result != null) {
									if (matchInteger(result)) {
										element.setIntValue(result);
										element.setRealValue(String
												.valueOf(Double
														.parseDouble(result)));
									} else if (matchReal(result)) {
										String error = "无法将浮点数赋值给整型变量";
										error(error, valueNode.getLineNum());
										return;
									} else {
										return;
									}
								} else {
									return;
								}
							}
						} else if (content.equals(ConstValues.DOUBLE)) { // 声明double型变量
							if (matchInteger(value)) {
								element.setRealValue(String.valueOf(Double
										.parseDouble(value)));
							} else if (matchReal(value)) {
								element.setRealValue(value);
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "无法将" + value + "赋值给浮点型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("字符串")) {
								String error = "无法将字符串给浮点型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("标识符")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.INT)
											|| table.getAllLevel(
													valueNode.getContent(),
													level).getKind().equals(
													ConstValues.DOUBLE)) {
										element.setRealValue(table.getAllLevel(
												valueNode.getContent(), level)
												.getRealValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.BOOL)) {
										String error = "无法将布尔型变量赋值给浮点型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.STRING)) {
										String error = "无法将字符串变量赋值给浮点型变量";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstValues.PLUS)
									|| value.equals(ConstValues.MINUS)
									|| value.equals(ConstValues.TIMES)
									|| value.equals(ConstValues.DIVIDE)) {
								String result = processExpression(valueNode);
								if (result != null) {
									if (matchInteger(result)) {
										element.setRealValue(String
												.valueOf(Double
														.parseDouble(result)));
									} else if (matchReal(result)) {
										element.setRealValue(result);
									}
								} else {
									return;
								}
							}
						} else if (content.equals(ConstValues.STRING)) { // 声明string型变量
							if (matchInteger(value)) {
								String error = "无法将整数赋值给字符串型变量";
								error(error, valueNode.getLineNum());
							} else if (matchReal(value)) {
								String error = "无法将浮点数赋值给字符串型变量";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								String error = "无法将" + value + "赋值给字符串型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("字符串")) {
								element.setStringValue(value);
							} else if (valueNode.getNodeKind().equals("标识符")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.INT)) {
										String error = "无法将整数赋值给字符串型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.DOUBLE)) {
										String error = "无法将浮点数赋值给字符串型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.BOOL)) {
										String error = "无法将布尔型变量赋值给字符串型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.STRING)) {
										element.setStringValue(value);
									}
								} else {
									return;
								}
							} else if (value.equals(ConstValues.PLUS)
									|| value.equals(ConstValues.MINUS)
									|| value.equals(ConstValues.TIMES)
									|| value.equals(ConstValues.DIVIDE)) {
								String error = "无法将算术表达式赋值给字符串型变量";
								error(error, valueNode.getLineNum());
							}
						} else { // 声明bool型变量
							if (matchInteger(value)) {
								// 如果是0或负数则记为false,其他记为true
								int i = Integer.parseInt(value);
								if (i <= 0)
									element.setStringValue("false");
								else
									element.setStringValue("true");
							} else if (matchReal(value)) {
								String error = "无法将浮点数赋值给布尔型变量";
								error(error, valueNode.getLineNum());
							} else if (value.equals("true")
									|| value.equals("false")) {
								element.setStringValue(value);
							} else if (valueNode.getNodeKind().equals("字符串")) {
								String error = "无法将字符串给布尔型变量";
								error(error, valueNode.getLineNum());
							} else if (valueNode.getNodeKind().equals("标识符")) {
								if (checkID(valueNode, level)) {
									if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.INT)) {
										int i = Integer.parseInt(table
												.getAllLevel(
														valueNode.getContent(),
														level).getIntValue());
										if (i <= 0)
											element.setStringValue("false");
										else
											element.setStringValue("true");
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.DOUBLE)) {
										String error = "无法将浮点型变量赋值给布尔型变量";
										error(error, valueNode.getLineNum());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.BOOL)) {
										element
												.setStringValue(table
														.getAllLevel(
																valueNode
																		.getContent(),
																level)
														.getStringValue());
									} else if (table.getAllLevel(
											valueNode.getContent(), level)
											.getKind().equals(ConstValues.STRING)) {
										String error = "无法将字符串变量赋值给布尔型变量";
										error(error, valueNode.getLineNum());
									}
								} else {
									return;
								}
							} else if (value.equals(ConstValues.EQUAL)
									|| value.equals(ConstValues.NOTEQUAL)
									|| value.equals(ConstValues.LT)
									|| value.equals(ConstValues.GT)) {
								boolean result = processCondition(valueNode);
								if (result) {
									element.setStringValue("true");
								} else {
									element.setStringValue("false");
								}
							}
						}
						index++;
					}
					table.add(element);
				} else { // 声明数组
					Element element = new Element(temp
							.getContent(), content, temp.getLineNum(), level);
					String sizeValue = temp.getChildAt(0).getContent();
					if (matchInteger(sizeValue)) {
						int i = Integer.parseInt(sizeValue);
						if (i < 1) {
							String error = "数组大小必须大于0";
							error(error, root.getLineNum());
							return;
						}
					} else if (temp.getChildAt(0).getNodeKind().equals("标识符")) {
						if (checkID(root, level)) {
							Element tempElement = table.getAllLevel(
									root.getContent(), level);
							if (tempElement.getKind().equals(ConstValues.INT)) {
								int i = Integer.parseInt(tempElement
										.getIntValue());
								if (i < 1) {
									String error = "数组大小必须大于0";
									error(error, root.getLineNum());
									return;
								} else {
									sizeValue = tempElement.getIntValue();
								}
							} else {
								String error = "类型不匹配,数组大小必须为整数类型";
								error(error, root.getLineNum());
								return;
							}
						} else {
							return;
						}
					} else if (sizeValue.equals(ConstValues.PLUS)
							|| sizeValue.equals(ConstValues.MINUS)
							|| sizeValue.equals(ConstValues.TIMES)
							|| sizeValue.equals(ConstValues.DIVIDE)) {
						sizeValue = processExpression(temp.getChildAt(0));
						if (sizeValue != null) {
							if (matchInteger(sizeValue)) {
								int i = Integer.parseInt(sizeValue);
								if (i < 1) {
									String error = "数组大小必须大于0";
									error(error, root.getLineNum());
									return;
								}
							} else {
								String error = "类型不匹配,数组大小必须为整数类型";
								error(error, root.getLineNum());
								return;
							}
						} else {
							return;
						}
					}
					element.setArrayElementsNum(Integer.parseInt(sizeValue));
					table.add(element);
					index++;
					for (int j = 0; j < Integer.parseInt(sizeValue); j++) {
						String s = temp.getContent() + "@" + j;
						Element ste = new Element(s,
								content, temp.getLineNum(), level);
						table.add(ste);
					}
				}
			} else { // 报错
				String error = "变量" + name + "已被声明,请重命名该变量";
				error(error, temp.getLineNum());
				return;
			}
		}
	}

	/**
	 * assign语句
	 * @param root
	 */
	private void processAssign(TokenTreeNode root) {
		// 赋值语句左半部分
		TokenTreeNode node1 = root.getChildAt(0);
		// 赋值语句左半部分标识符
		String node1Value = node1.getContent();
		if (table.getAllLevel(node1Value, level) != null) {
			if (node1.getChildCount() != 0) {
				String s = processArray(node1.getChildAt(0), table.getAllLevel(
						node1Value, level).getArrayElementsNum());
				if (s != null)
					node1Value += "@" + s;
				else
					return;
			}
		} else {
			String error = "变量" + node1Value + "在使用前未声明";
			error(error, node1.getLineNum());
			return;
		}
		// 赋值语句左半部分标识符类型
		String node1Kind = table.getAllLevel(node1Value, level).getKind();
		// 赋值语句右半部分
		TokenTreeNode node2 = root.getChildAt(1);
		String node2Kind = node2.getNodeKind();
		String node2Value = node2.getContent();
		// 赋值语句右半部分的值
		String value = "";
		if (node2Kind.equals("整数")) { // 整数
			value = node2Value;
			node2Kind = "int";
		} else if (node2Kind.equals("实数")) { // 实数
			value = node2Value;
			node2Kind = "real";
		} else if (node2Kind.equals("字符串")) { // 字符串
			value = node2Value;
			node2Kind = "string";
		} else if (node2Kind.equals("布尔值")) { // true和false
			value = node2Value;
			node2Kind = "bool";
		} else if (node2Kind.equals("标识符")) { // 标识符
			if (checkID(node2, level)) {
				if (node2.getChildCount() != 0) {
					String s = processArray(node2.getChildAt(0), table.getAllLevel(
							node2Value, level).getArrayElementsNum());
					if (s != null)
						node2Value += "@" + s;
					else
						return;
				}
				Element temp = table.getAllLevel(node2Value, level);
				if (temp.getKind().equals(ConstValues.INT)) {
					value = temp.getIntValue();
				} else if (temp.getKind().equals(ConstValues.DOUBLE)) {
					value = temp.getRealValue();
				} else if (temp.getKind().equals(ConstValues.BOOL)
						|| temp.getKind().equals(ConstValues.STRING)) {
					value = temp.getStringValue();
				}
				node2Kind = table.getAllLevel(node2Value, level).getKind();
			} else {
				return;
			}
		} else if (node2Value.equals(ConstValues.PLUS)
				|| node2Value.equals(ConstValues.MINUS)
				|| node2Value.equals(ConstValues.TIMES)
				|| node2Value.equals(ConstValues.DIVIDE)) { // 表达式
			String result = processExpression(node2);
			if (result != null) {
				if (matchInteger(result))
					node2Kind = "int";
				else if (matchReal(result))
					node2Kind = "real";
				value = result;
			} else {
				return;
			}
		} else if (node2Value.equals(ConstValues.EQUAL)
				|| node2Value.equals(ConstValues.NOTEQUAL)
				|| node2Value.equals(ConstValues.LT)
				|| node2Value.equals(ConstValues.GT)) { // 逻辑表达式
			boolean result = processCondition(node2);
			node2Kind = "bool";
			value = String.valueOf(result);
		}
		if (node1Kind.equals(ConstValues.INT)) {
			if (node2Kind.equals(ConstValues.INT)) {
				table.getAllLevel(node1Value, level).setIntValue(value);
				table.getAllLevel(node1Value, level).setRealValue(
						String.valueOf(Double.parseDouble(value)));
			} else if (node2Kind.equals(ConstValues.DOUBLE)) {
				String error = "无法将浮点数赋值给整型变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.BOOL)) {
				String error = "无法将布尔值赋值给整型变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.STRING)) {
				String error = "无法将字符串给整型变量";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstValues.DOUBLE)) {
			if (node2Kind.equals(ConstValues.INT)) {
				table.getAllLevel(node1Value, level).setRealValue(
						String.valueOf(Double.parseDouble(value)));
			} else if (node2Kind.equals(ConstValues.DOUBLE)) {
				table.getAllLevel(node1Value, level).setRealValue(value);
			} else if (node2Kind.equals(ConstValues.BOOL)) {
				String error = "无法将布尔值赋值给浮点型变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.STRING)) {
				String error = "无法将字符串给浮点型变量";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstValues.BOOL)) {
			if (node2Kind.equals(ConstValues.INT)) {
				int i = Integer.parseInt(node2Value);
				if (i <= 0)
					table.getAllLevel(node1Value, level).setStringValue("false");
				else
					table.getAllLevel(node1Value, level).setStringValue("true");
			} else if (node2Kind.equals(ConstValues.DOUBLE)) {
				String error = "无法将浮点数赋值给布尔型变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.BOOL)) {
				table.getAllLevel(node1Value, level).setStringValue(value);
			} else if (node2Kind.equals(ConstValues.STRING)) {
				String error = "无法将字符串赋值给布尔型变量";
				error(error, node1.getLineNum());
				return;
			}
		} else if (node1Kind.equals(ConstValues.STRING)) {
			if (node2Kind.equals(ConstValues.INT)) {
				String error = "无法将整数赋值给字符串变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.DOUBLE)) {
				String error = "无法将浮点数赋值给字符串变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.BOOL)) {
				String error = "无法将布尔变量赋值给字符串变量";
				error(error, node1.getLineNum());
				return;
			} else if (node2Kind.equals(ConstValues.STRING)) {
				table.getAllLevel(node1Value, level).setStringValue(value);
			}
		}
	}
	/**
	 * if语句
	 * @param root  语法树中if语句结点
	 */
	private void processIf(TokenTreeNode root) {
		int count = root.getChildCount();
		// 根结点Condition
		TokenTreeNode conditionNode = root.getChildAt(0);
		// 根结点Statements
		TokenTreeNode statementNode = root.getChildAt(1);
		// 条件为真
		if (processCondition(conditionNode.getChildAt(0))) {
			statement(statementNode);
		} else if (count == 3) { // 条件为假且有else语句
			TokenTreeNode elseNode = root.getChildAt(2);
			level++;
			statement(elseNode);
			level--;
			table.update(level);
		} else { // 条件为假同时没有else语句
			return;
		}
	}

	/**
	 * while语句
	 * @param root
	 */
	private void processWhile(TokenTreeNode root) {
		// 根结点Condition
		TokenTreeNode conditionNode = root.getChildAt(0);
		// 根结点Statements
		TokenTreeNode statementNode = root.getChildAt(1);
		while (processCondition(conditionNode.getChildAt(0))) {
			statement(statementNode);
			level--;
			table.update(level);
			level++;
		}
	}

	/**
	 * read语句
	 * @param root
	 */
	private void processRead(TokenTreeNode root) {
		// 要读取的变量的名称
		String idName = root.getContent();
		// 查找变量
		Element element = table.getAllLevel(idName, level);
		// 判断变量是否已经声明
		if (element != null) {
			if (root.getChildCount() != 0) {
				String s = processArray(root.getChildAt(0), element
						.getArrayElementsNum());
				if (s != null) {
					idName += "@" + s;
				} else {
					return;
				}
			}
			String value = readInput();
			if (element.getKind().equals(ConstValues.INT)) {
				if (matchInteger(value)) {
					table.getAllLevel(idName, level).setIntValue(value);
					table.getAllLevel(idName, level).setRealValue(
							String.valueOf(Double.parseDouble(value)));
				} else { // 报错
					String error = "无法将\"" + value + "\"赋值给变量" + idName;
					controller.showErrorDialog("ERROR","输入错误",error);
				}
			} else if (element.getKind().equals(ConstValues.DOUBLE)) {
				if (matchReal(value)) {
					table.getAllLevel(idName, level).setRealValue(value);
				} else if (matchInteger(value)) {
					table.getAllLevel(idName, level).setRealValue(
							String.valueOf(Double.parseDouble(value)));
				} else { // 报错
					String error = "无法将\"" + value + "\"赋值给变量" + idName;
					controller.showErrorDialog("ERROR","输入错误",error);
				}
			} else if (element.getKind().equals(ConstValues.BOOL)) {
				if (value.equals("true")) {
					table.getAllLevel(idName, level).setStringValue("true");
				} else if (value.equals("false")) {
					table.getAllLevel(idName, level).setStringValue("false");
				} else if(value.equals("0")) { // 报错
					table.getAllLevel(idName, level).setStringValue("false");
					String error = "无法将\"" + value + "\"赋值给变量" + idName;
				}else {
					table.getAllLevel(idName, level).setStringValue("true");
				}
			} else if (element.getKind().equals(ConstValues.STRING)) {
				table.getAllLevel(idName, level).setStringValue(value);
			}
		} else { // 报错
			String error = "变量" + idName + "在使用前未声明";
			error(error, root.getLineNum());
		}
	}
	/**
	 * write语句
	 * @param root
	 */
	private void processWrite(TokenTreeNode root) {
		//todo:write
		// 结点显示的内容
		String content = root.getContent();
		// 结点的类型
		String kind = root.getNodeKind();
		if (kind.equals("整数") || kind.equals("实数")) { // 常量
            controller.writeToConsole(content);
		} else if (kind.equals("字符串")) { // 字符串
            controller.writeToConsole(content);
		} else if (kind.equals("标识符")) { // 标识符
			if (checkID(root, level)) {
				if (root.getChildCount() != 0) {
					String s = processArray(root.getChildAt(0), table.getAllLevel(
							content, level).getArrayElementsNum());
					if (s != null)
						content += "@" + s;
					else
						return;
				}
				Element temp = table.getAllLevel(content, level);
				if (temp.getKind().equals(ConstValues.INT)) {
                    controller.writeToConsole(temp.getIntValue());
				} else if (temp.getKind().equals(ConstValues.DOUBLE)) {
                    controller.writeToConsole(temp.getRealValue());
				} else {
                    controller.writeToConsole(temp.getStringValue());
				}
			} else {
				return;
			}
		} else if (content.equals(ConstValues.PLUS)
				|| content.equals(ConstValues.MINUS)
				|| content.equals(ConstValues.TIMES)
				|| content.equals(ConstValues.DIVIDE)) { // 表达式
			String value = processExpression(root);
			if (value != null) {
                controller.writeToConsole(value);
			}
		}
	}

	/**
	 * if和while语句的条件
	 * @param root 根节点
	 * @return 返回计算结果
	 */
	private boolean processCondition(TokenTreeNode root) {
		// > < <> == true false 布尔变量
		String content = root.getContent();
		if (content.equals(ConstValues.TRUE)) {
			return true;
		} else if (content.equals(ConstValues.FALSE)) {
			return false;
		} else if (root.getNodeKind().equals("标识符")) {
			if (checkID(root, level)) {
				if (root.getChildCount() != 0) {
					String s = processArray(root.getChildAt(0), table.getAllLevel(
							content, level).getArrayElementsNum());
					if (s != null)
						content += "@" + s;
					else
						return false;
				}
				Element temp = table.getAllLevel(content, level);
				if (temp.getKind().equals(ConstValues.BOOL)) {
					if(temp.getStringValue().equals(ConstValues.TRUE))
						return true;
					else
						return false;
				} else { // 报错
					String error = "无法将变量" + content + "作为判断条件";
					error(error, root.getLineNum());
				}
			} else {
				return false;
			}
		} else if (content.equals(ConstValues.EQUAL)
				|| content.equals(ConstValues.NOTEQUAL)
				|| content.equals(ConstValues.LT) || content.equals(ConstValues.GT)) {
			// 存放两个待比较对象的�?
			String[] results = new String[2];
			for (int i = 0; i < root.getChildCount(); i++) {
				String kind = root.getChildAt(i).getNodeKind();
				String tempContent = root.getChildAt(i).getContent();
				if (kind.equals("整数") || kind.equals("实数")) { // 常量
					results[i] = tempContent;
				} else if (kind.equals("标识符")) { // 标识符
					if (checkID(root.getChildAt(i), level)) {
						if (root.getChildAt(i).getChildCount() != 0) {
							String s = processArray(root.getChildAt(i)
									.getChildAt(0), table.getAllLevel(
									tempContent, level).getArrayElementsNum());
							if (s != null)
								tempContent += "@" + s;
							else
								return false;
						}
						Element temp = table.getAllLevel(
								tempContent, level);
						if (temp.getKind().equals(ConstValues.INT)) {
							results[i] = temp.getIntValue();
						} else {
							results[i] = temp.getRealValue();
						}
					} else {
						return false;
					}
				} else if (tempContent.equals(ConstValues.PLUS)
						|| tempContent.equals(ConstValues.MINUS)
						|| tempContent.equals(ConstValues.TIMES)
						|| tempContent.equals(ConstValues.DIVIDE)) { // 表达式
					String result = processExpression(root.getChildAt(i));
					if (result != null)
						results[i] = result;
					else
						return false;
				}
			}
			if (!results[0].equals("") && !results[1].equals("")) {
				double element1 = Double.parseDouble(results[0]);
				double element2 = Double.parseDouble(results[1]);
				if (content.equals(ConstValues.GT)) { // >
					if (element1 > element2)
						return true;
				} else if (content.equals(ConstValues.LT)) { // <
					if (element1 < element2)
						return true;
				} else if (content.equals(ConstValues.EQUAL)) { // ==
					if (element1 == element2)
						return true;
				} else { // <>
					if (element1 != element2)
						return true;
				}
			}
		}
		// 语义分析出错或语义分析条件结果为假返回false
		return false;
	}

	/**
	 * 表达式
	 * @param root
	 * @return
	 */
	private String processExpression(TokenTreeNode root) {
		boolean isInt = true;
		// + -
		String content = root.getContent();
		// 存放两个运算对象的结果
		String[] results = new String[2];
		for (int i = 0; i < root.getChildCount(); i++) {
			TokenTreeNode tempNode = root.getChildAt(i);
			String kind = tempNode.getNodeKind();
			String tempContent = tempNode.getContent();
			if (kind.equals("整数")) { // 整数
				results[i] = tempContent;
			} else if (kind.equals("实数")) { // 实数
				results[i] = tempContent;
				isInt = false;
			} else if (kind.equals("标识符")) { // 标识符
				if (checkID(tempNode, level)) {
					if (tempNode.getChildCount() != 0) {
						String s = processArray(tempNode.getChildAt(0), table
								.getAllLevel(tempContent, level)
								.getArrayElementsNum());
						if (s != null)
							tempContent += "@" + s;
						else
							return null;
					}
					Element temp = table.getAllLevel(tempNode
							.getContent(), level);
					if (temp.getKind().equals(ConstValues.INT)) {
						results[i] = temp.getIntValue();
					} else if (temp.getKind().equals(ConstValues.DOUBLE)) {
						results[i] = temp.getRealValue();
						isInt = false;
					}
				} else {
					return null;
				}
			} else if (tempContent.equals(ConstValues.PLUS)
					|| tempContent.equals(ConstValues.MINUS)
					|| tempContent.equals(ConstValues.TIMES)
					|| tempContent.equals(ConstValues.DIVIDE)) { // 表达式
				String result = processExpression(root.getChildAt(i));
				if (result != null) {
					results[i] = result;
					if (matchReal(result))
						isInt = false;
				} else
					return null;
			}
		}
		if (isInt) {
			int e1 = Integer.parseInt(results[0]);
			int e2 = Integer.parseInt(results[1]);
			if (content.equals(ConstValues.PLUS))
				return String.valueOf(e1 + e2);
			else if (content.equals(ConstValues.MINUS))
				return String.valueOf(e1 - e2);
			else if (content.equals(ConstValues.TIMES))
				return String.valueOf(e1 * e2);
			else
				return String.valueOf(e1 / e2);
		} else {
			double e1 = Double.parseDouble(results[0]);
			double e2 = Double.parseDouble(results[1]);
			BigDecimal bd1 = new BigDecimal(e1);
			BigDecimal bd2 = new BigDecimal(e2);
			if (content.equals(ConstValues.PLUS))
				return String.valueOf(bd1.add(bd2).floatValue());
			else if (content.equals(ConstValues.MINUS))
				return String.valueOf(bd1.subtract(bd2).floatValue());
			else if (content.equals(ConstValues.TIMES))
				return String.valueOf(bd1.multiply(bd2).floatValue());
			else
				return String.valueOf(bd1.divide(bd2, 3,
						BigDecimal.ROUND_HALF_UP).floatValue());
		}
	}

	/**
	 * array
	 * @param root
	 * @param arraySize  数组大小
	 * @return
	 */
	private String processArray(TokenTreeNode root, int arraySize) {
		if (root.getNodeKind().equals("整数")) {
			int i = Integer.parseInt(root.getContent());
			if (i > -1 && i < arraySize) {
				return root.getContent();
			} else if (i < 0) {
				String error = "数组下标无法为负值";
				error(error, root.getLineNum());
				return null;
			} else {
				String error = "数组下标越界";
				error(error, root.getLineNum());
				return null;
			}
		} else if (root.getNodeKind().equals("标识符")) {
			// 审查标识符
			if (checkID(root, level)) {
				Element temp = table.getAllLevel(root.getContent(),
						level);
				if (temp.getKind().equals(ConstValues.INT)) {
					int i = Integer.parseInt(temp.getIntValue());
					if (i > -1 && i < arraySize) {
						return temp.getIntValue();
					} else if (i < 0) {
						String error = "数组下标无法为负值";
						error(error, root.getLineNum());
						return null;
					} else {
						String error = "数组下标越界";
						error(error, root.getLineNum());
						return null;
					}
				} else {
					String error = "类型不匹配,数组索引号必须为整数类型";
					error(error, root.getLineNum());
					return null;
				}
			} else {
				return null;
			}
		} else if (root.getContent().equals(ConstValues.PLUS)
				|| root.getContent().equals(ConstValues.MINUS)
				|| root.getContent().equals(ConstValues.TIMES)
				|| root.getContent().equals(ConstValues.DIVIDE)) { // 表达式
			String result = processExpression(root);
			if (result != null) {
				if (matchInteger(result)) {
					int i = Integer.parseInt(result);
					if (i > -1 && i < arraySize) {
						return result;
					} else if (i < 0) {
						String error = "数组下标无法为负值";
						error(error, root.getLineNum());
						return null;
					} else {
						String error = "数组下标越界";
						error(error, root.getLineNum());
						return null;
					}
				} else {
					String error = "类型不匹配,数组索引号必须为整数类型";
					error(error, root.getLineNum());
					return null;
				}
			} else
				return null;
		}
		return null;
	}

	/**
	 * 核查字符串是否声明和初始化
	 * @param root
	 * @param level
	 * @return
	 */
	private boolean checkID(TokenTreeNode root, int level) {
		// 标识符名称
		String idName = root.getContent();
		// 标识符未声明
		if (table.getAllLevel(idName, level) == null) {
			String error = "变量" + idName + "在使用前未声明";
			error(error, root.getLineNum());
			return false;
		} else {
			if (root.getChildCount() != 0) {
				String tempString = processArray(root.getChildAt(0), table
						.getAllLevel(idName, level).getArrayElementsNum());
				if (tempString != null)
					idName += "@" + tempString;
				else
					return false;
			}
			Element temp = table.getAllLevel(idName, level);
			// 变量未初始化
			if (temp.getIntValue().equals("") && temp.getRealValue().equals("")
					&& temp.getStringValue().equals("")) {
				String error = "变量" + idName + "在使用前未初始化";
				error(error, root.getLineNum());
				return false;
			} else {
				return true;
			}
		}
	}
	public String getErrorInfo() {
		return errorInfo;
	}
	public int getErrorNum() {
		return errorNum;
	}
}