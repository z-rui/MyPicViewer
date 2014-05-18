import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

abstract class CommandsViewFrame extends JFrame implements ActionListener {
	private final JScrollPane scrollpane = new JScrollPane();
	private final JToolBar toolbar = new JToolBar();
	private final JLabel status = new JLabel();

	public CommandsViewFrame(String title, int width, int height, String [] commands, String [] icons) {
		Container cp = this.getContentPane();

		// 工具栏
		for (int i = 0; i < commands.length; i++) {
			JButton btn = new JButton(commands[i], new ImageIcon(icons[i]));
			btn.addActionListener(this);
			this.toolbar.add(btn);
		}
		cp.add(this.toolbar, BorderLayout.NORTH);

		// 工作区
		cp.add(this.scrollpane, BorderLayout.CENTER);

		// 状态栏
		cp.add(this.status, BorderLayout.SOUTH);

		// 窗口属性
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(width, height);
		setTitle(title);
	}

	public void setView(JComponent view) {
		this.scrollpane.setViewportView(view);
	}

	public void setCommandsEnabled(boolean... enabled) {
		for (int i = 0; i < enabled.length; i++) {
			this.toolbar.getComponent(i).setEnabled(enabled[i]);
		}
	}

	public void setStatus(String status) {
		this.status.setText(status);
	}
}

final class MyFileChooser extends JFileChooser {
	private final MyFilterWrapper filter;

	public MyFileChooser(MyFilterWrapper filter) {
		this.filter = filter;
		// 扩展名过滤
		setFileFilter(filter);

		// 文件选择属性设置
		setMultiSelectionEnabled(true);
		setAcceptAllFileFilterUsed(false);
		setFileSelectionMode(FILES_AND_DIRECTORIES);
	}

	public String [] getAbsolutePathsRecursively() {
		ArrayList<String> paths = new ArrayList<String>();
		File [] files = getSelectedFiles();
		traverse(files, paths);
		return paths.toArray(new String [] {});
	}

	private void traverse(File [] files, ArrayList<String> paths) {
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				traverse(f.listFiles(this.filter), paths);
			} else if (f.isFile() && this.filter.accept(f)) {
				paths.add(f.getAbsolutePath());
			}
		}
	}
}

final class MyFilterWrapper extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
	private final FileNameExtensionFilter filter;

	public MyFilterWrapper(String description, String... extensions) {
		this.filter = new FileNameExtensionFilter(description, extensions);
	}

	public boolean accept(File f) {
		return this.filter.accept(f);
	}

	public String getDescription() {
		return this.filter.getDescription();
	}
}

class Picture extends JLabel {
	public Picture() {
		super(null, null, CENTER);
	}

	public void load(String filename) {
		this.setIcon(new ImageIcon(filename));
	}

	public void unload() {
		this.setIcon(null);
	}

	public void zoom(float factor) {
	}
}

class ScrollablePicture extends Picture {
	private MouseMotionListener dragListener;
	private Point oldCursorPos;

	public ScrollablePicture() {
		dragListener = new MouseMotionAdapter () {
			public void mouseDragged(MouseEvent e) {
				dragTo(e.getLocationOnScreen());
			}
		};
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				startDragging(e.getLocationOnScreen());
			}
			public void mouseReleased(MouseEvent e) {
				stopDragging();
			}
		});
	}

	public void load(String filename) {
		super.load(filename);
		scrollRectToVisible(new Rectangle()); // 滚动到左上角位置
	}

	private void startDragging(Point cursorPos) {
		oldCursorPos = cursorPos;
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
		addMouseMotionListener(dragListener);
	}

	private void stopDragging() {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		removeMouseMotionListener(dragListener);
	}

	private void dragTo(Point newCursorPos) {
		int dx = newCursorPos.x - oldCursorPos.x;
		int dy = newCursorPos.y - oldCursorPos.y;
		Rectangle visibleRect = getVisibleRect();
		visibleRect.translate(-dx, -dy);
		scrollRectToVisible(visibleRect);
		oldCursorPos = newCursorPos;
	}
}

final class MyPicViewer extends CommandsViewFrame {
	final static String [] commands = {"打开/查找", "放大", "缩小", "上一幅", "下一幅", "退出"};
	final static String [] icons = {"icons/document-open.png", "icons/list-add.png", "icons/list-remove.png", "icons/go-previous.png", "icons/go-next.png", "icons/system-log-out.png"};

	private String [] pictureList = {};
	private int pictureIndex = -1;

	private Picture view = new ScrollablePicture();

	public static void main(String [] args) {
		new MyPicViewer();
	}

	public MyPicViewer() {
		super("图片查看器", 800, 600, commands, icons);
		setCommandsEnabled(true, false, false, false, false, true);
		setView(this.view);
		showCurrentPicture();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) { 
		String command = e.getActionCommand();

		if (command.equals("打开/查找")) {
			choosePictures();
			showCurrentPicture();
		} else if (command.equals("放大")) {
			this.view.zoom(+0.1f);
		} else if (command.equals("缩小")) {
			this.view.zoom(-0.1f);
		} else if (command.equals("上一幅")) {
			this.pictureIndex--;
			showCurrentPicture();
		} else if (command.equals("下一幅")) {
			this.pictureIndex++;
			showCurrentPicture();
		} else if (command.equals("退出")) {
			dispose();
		}
	}

	private void choosePictures() {
		MyFileChooser fc = new MyFileChooser(new MyFilterWrapper("图片文件", "jpg", "png", "gif"));
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			this.pictureList = fc.getAbsolutePathsRecursively();
			this.pictureIndex = (this.pictureList.length > 0) ? 0 : -1;
		}
	}

	private void showCurrentPicture() {
		int i = this.pictureIndex;
		if (i >= 0) {
			String filename = this.pictureList[i];
			this.view.load(filename);
			setStatus(String.format("[%d/%d] %s", i + 1, this.pictureList.length, filename));
		} else {
			this.view.unload();
			setStatus("没有加载图片");
		}
		setCommandsEnabled(true, i >= 0, i >= 0, i > 0, i + 1 < this.pictureList.length, true);
	}
}
