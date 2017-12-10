package zhangtao.iss2015.semantic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

/**
 * 存放符号表项
 */
public class SymbolTable {
	private Vector<SymbolTableElement> tableElementVector = new Vector<>();
	private static final String TEMP_PREFIX = "*temp";

	private static SymbolTable symbolTable = new SymbolTable();
	private static LinkedList<Symbol> tempNames;

	private ArrayList<Symbol> symbolList;
	
	/**
	 * 根据索引查找符号表项
	 * @param index 提供的索引
	 * @return 返回SymbolTableElement对象
	 */
	public SymbolTableElement get(int index) {
		return tableElementVector.get(index);
	}

	/**
	 * 根据SymbolTableElement对象的名字对所有作用域查找
	 * @param name SymbolTableElement名字
	 * @param level SymbolTableElement作用域
	 * @return 如果存在,则返回SymbolTableElement对象;否则返回null
	 */
	public SymbolTableElement getAllLevel(String name, int level) {
		while (level > -1) {
			for (SymbolTableElement element : tableElementVector) {
				if (element.getName().equals(name) && element.getLevel() == level) {
					return element;
				}
			}
			level--;
		}
		return null;
	}

	/**
	 * 根据名字对当前作用域查找
	 * @param name 名字
	 * @param level 作用域
	 * @return 如果存在,则返回;否则返回null
	 */
	public SymbolTableElement getCurrentLevel(String name, int level) {
		for (SymbolTableElement element : tableElementVector) {
			if (element.getName().equals(name) && element.getLevel() == level) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 向symbolTable中添加对象
	 * @param element 要添加的元素
	 * @return
	 */
	public boolean add(SymbolTableElement element) {
		return tableElementVector.add(element);
	}


	/**
	 * 移除指定索引处的元素
	 * @param index
	 */
	public void remove(int index) {
		tableElementVector.remove(index);
	}

	/**
	 * 清空symbolTable中的元素
	 */
	public void removeAll() {
		tableElementVector.clear();
	}

	/**
	 * 当level减小时更新符号表,去除无用的元素
	 */
	public void update(int level) {
		for (int i = 0; i < size(); i++) {
			if (get(i).getLevel() > level) {
				remove(i);
			}
		}
	}

	/**
	 * 计算元素个数
	 * @return 返回对象中元素的个数
	 */
	public int size() {
		return tableElementVector.size();
	}


	// getter
	public static SymbolTable getSymbolTable() {
		return symbolTable;
	}

    /**
     * 创建新表
     */
	public void newTable() {
		symbolList = new ArrayList<>();
		tempNames = new LinkedList<>();
	}

	/**
	 * 删除表
	 */
	public void deleteTable() {
		if (symbolList != null) {
			symbolList.clear();
			symbolList = null;
		}
		if (tempNames != null) {
			tempNames.clear();
			tempNames = null;
		}
	}

	public void register(Symbol symbol) {
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).getName().equals(symbol.getName())) {
				if (symbolList.get(i).getLevel() < symbol.getLevel()) {
					symbol.setNext(symbolList.get(i));
					symbolList.set(i, symbol);
					return;
				} else {
				}
			}
		}
		symbolList.add(symbol);
	}

	public void deregister(int level) {
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).getLevel() == level) {
				symbolList.set(i, symbolList.get(i).getNext());
			}
		}
		for (int i = symbolList.size() - 1; i >= 0; i--) {
			if (symbolList.get(i) == null) {
				symbolList.remove(i);
			}
		}
	}
	/**
	 * 返回Symbol中的类型
	 */
	public int getSymbolType(String name) {
		return getSymbol(name).getType();
	}

    /**
     * getter
     * @param name
     * @return
     */
	private Symbol getSymbol(String name) {
		for (Symbol s : symbolList) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		for (Symbol s : tempNames) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		if (name.startsWith(TEMP_PREFIX)) {
			Symbol s = new Symbol(name, Symbol.TEMP, -1);
			tempNames.add(s);
			return s;
		}
		return null;
	}

	/**
	 * 获取一个没有使用的临时符号名
	 */
	public Symbol getTempSymbol() {
		String temp = null;
		for (int i = 1;; i++) {
			temp = TEMP_PREFIX + i;
			boolean exist = false;
			for (Symbol s : tempNames) {
				if (s.getName().equals(temp)) {
					exist = true;
					break;
				}
			}
			for (Symbol s : symbolList) {
				if (s.getName().equals(temp)) {
					exist = true;
					break;
				}
			}
			if (exist) {
				continue;
			}
			Symbol s = new Symbol(temp, Symbol.TEMP, -1);
			tempNames.add(s);
			return s;
		}
	}

	/**
	 * 清空临时符号名
	 */
	public void clearTempNames() {
		tempNames.clear();
	}
}
