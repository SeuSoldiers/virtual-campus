package seu.virtualcampus.ui.aichat;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Text;

/**
 * Markdown 渲染器，将 Markdown 文本渲染为 JavaFX 节点。
 * <p>
 * 支持标题、段落、加粗、斜体、代码块、无序列表等常见 Markdown 语法，
 * 并根据不同类型节点设置相应的字体、样式和布局。
 * </p>
 */
public class MarkdownFXRenderer {

    /**
     * Flexmark 解析器，用于将 Markdown 文本解析为 AST。
     */
    private final Parser parser;

    /**
     * 构造方法，初始化 Markdown 解析器。
     */
    public MarkdownFXRenderer() {
        parser = Parser.builder().build();
    }

    /**
     * 渲染 Markdown 文本为 JavaFX 节点。
     *
     * @param markdown Markdown 源文本
     * @param maxWidth 渲染区域最大宽度
     * @return 渲染后的 JavaFX 节点（VBox）
     */
    public javafx.scene.Node render(String markdown, double maxWidth) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        VBox container = new VBox(4);  // 行间距 4
        renderChildren(document, container, maxWidth);
        return container;
    }

    /**
     * 渲染 Markdown AST 的所有块级子节点。
     *
     * @param parent    父节点
     * @param container VBox 容器
     * @param maxWidth  最大宽度
     */
    private void renderChildren(Node parent, VBox container, double maxWidth) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Paragraph) {
                TextFlow tf = new TextFlow();
                tf.setPrefWidth(maxWidth);
                renderInlines(child, tf, maxWidth);
                container.getChildren().add(tf);
            } else if (child instanceof Heading) {
                Heading h = (Heading) child;
                TextFlow tf = new TextFlow();
                tf.setPrefWidth(maxWidth);
                Text t = new Text(h.getText().toString());
                // 根据 heading level 设置字体大小粗细
                switch (h.getLevel()) {
                    case 1 -> {
                        t.setFont(Font.font("System", FontWeight.BOLD, 24));
                    }
                    case 2 -> {
                        t.setFont(Font.font("System", FontWeight.BOLD, 20));
                    }
                    case 3 -> {
                        t.setFont(Font.font("System", FontWeight.BOLD, 16));
                    }
                    default -> {
                        t.setFont(Font.font("System", FontWeight.BOLD, 14));
                    }
                }
                tf.getChildren().add(t);
                container.getChildren().add(tf);
            } else if (child instanceof FencedCodeBlock) {
                FencedCodeBlock cb = (FencedCodeBlock) child;
                String code = cb.getContentChars().toString();
                Text t = new Text(code);
                t.setFont(Font.font("Monospaced", 12));
                // 加背景样式，可以包在 TextFlow 或者用 HBox 来做背景
                TextFlow tf = new TextFlow(t);
                tf.setStyle("-fx-background-color: #f6f8fa; -fx-padding: 8;");
                tf.setPrefWidth(maxWidth);
                container.getChildren().add(tf);
            } else if (child instanceof BulletList) {
                for (Node itemNode = child.getFirstChild(); itemNode != null; itemNode = itemNode.getNext()) {
                    if (itemNode instanceof BulletListItem) {
                        HBox hbox = new HBox(5);
                        // 前缀 bullet 点
                        Text bullet = new Text("• ");
                        bullet.setFont(Font.font("System", 12));
                        hbox.getChildren().add(bullet);
                        // 内容
                        TextFlow tf = new TextFlow();
                        tf.setPrefWidth(maxWidth - 20); // 留空间给 bullet
                        renderInlines(itemNode, tf, maxWidth - 20);
                        hbox.getChildren().add(tf);
                        container.getChildren().add(hbox);
                    }
                }
            } else {
                // 其他块类型，比如 block quote, images, list numbered 等
                // 简单处理为文本
                TextFlow tf = new TextFlow();
                tf.setPrefWidth(maxWidth);
                Text t = new Text(child.getChars().toString());
                tf.getChildren().add(t);
                container.getChildren().add(tf);
            }
        }
    }

    /**
     * 渲染 Markdown AST 的所有内联子节点。
     *
     * @param parent   父节点
     * @param tf       TextFlow 容器
     * @param maxWidth 最大宽度
     */
    private void renderInlines(Node parent, TextFlow tf, double maxWidth) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof com.vladsch.flexmark.ast.Text) {
                BasedSequence bs = ((com.vladsch.flexmark.ast.Text) child).getChars();
                Text t = new Text(bs.toString());
                t.setFont(Font.font("System", 12));
                tf.getChildren().add(t);
            } else if (child instanceof Emphasis) {
                // 斜体
                Emphasis e = (Emphasis) child;
                String content = e.getText().toString();
                Text t = new Text(content);
                t.setFont(Font.font("System", FontPosture.ITALIC, 12));
                tf.getChildren().add(t);
            } else if (child instanceof StrongEmphasis) {
                StrongEmphasis se = (StrongEmphasis) child;
                String content = se.getText().toString();
                Text t = new Text(content);
                t.setFont(Font.font("System", FontWeight.BOLD, 12));
                tf.getChildren().add(t);
            } else if (child instanceof Code) {
                Code c = (Code) child;
                String content = c.getText().toString();
                Text t = new Text(content);
                t.setFont(Font.font("Monospaced", 12));
                t.setFill(Color.DARKRED);
                tf.getChildren().add(t);
            } else if (child instanceof SoftLineBreak || child instanceof HardLineBreak) {
                Text nl = new Text("\n");
                tf.getChildren().add(nl);
            } else {
                // 更复杂或嵌套的类型，递归
                renderInlines(child, tf, maxWidth);
            }
        }
    }
}
