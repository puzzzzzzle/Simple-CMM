package zhangtao.iss2015.gui;

public interface GUIOuterController {
    void showErrorDialog(String title, String head, String body);
    String inputTextDialog(String title, String head, String body);
    void writeToConsole(String s);
}
