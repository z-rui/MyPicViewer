import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

abstract class CommandsViewFrame extends JFrame implements ActionListener {
	final JScrollPane scrollpane = new JScrollPane();
	final JPanel toolbar = new JPanel();

	public CommandsViewFrame(String title, int width, int height, String [] commands, String commandPosition) {
		Container cp = this.getContentPane();

		// 工具栏
		for (int i = 0; i < commands.length; i++) {
			JButton btn = new JButton(commands[i]);
			btn.addActionListener(this);
			this.toolbar.add(btn);
		}
		cp.add(this.toolbar, commandPosition);

		// 工作区
		cp.add(this.scrollpane, BorderLayout.CENTER);

		// 窗口属性
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(width, height);
		setTitle(title);
	}

	public void setView(JComponent view) {
		this.scrollpane.setViewportView(view);
	}

	public void setCommandsEnabled(boolean [] enabled) {
		for (int i = 0; i < enabled.length; i++) {
			this.toolbar.getComponent(i).setEnabled(enabled[i]);
		}
	}
}

final class MyFileChooser extends JFileChooser {
	final MyFilterWrapper filter;

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

	void traverse(File [] files, ArrayList<String> paths) {
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				traverse(f.listFiles(this.filter), paths);
			} else {
				paths.add(f.getAbsolutePath());
			}
		}
	}
}

final class MyFilterWrapper extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
	final FileNameExtensionFilter filter;

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

final class MyPicViewer extends CommandsViewFrame implements ActionListener {
	final static String [] commands = { "打开", "关闭", "放大", "缩小", "上一个", "下一个", };

	String [] pictureList;
	int pictureIndex = -1;

	public static void main(String [] args) {
		new MyPicViewer().setVisible(true);
	}

	public MyPicViewer() {
		super("图片查看器", 800, 600, commands, BorderLayout.NORTH);
		setCommandsEnabled(new boolean [] {true, false, false, false, false, false});
	}

	void showPicture(String filename) {
		if (filename != null) {
			setView(new JLabel(new ImageIcon(filename)));
		} else {
			setView(new JLabel());
		}
	}

	public void actionPerformed(ActionEvent e) { 
		String command = e.getActionCommand();

		if (command.equals("打开")) {
			choosePictures();
		} else if (command.equals("上一个")) {
			this.pictureIndex--;
		} else if (command.equals("下一个")) {
			this.pictureIndex++;
		}
		showCurrentPicture();
	}

	void choosePictures() {
		MyFileChooser fc = new MyFileChooser(new MyFilterWrapper("JPG图片文件", "jpg"));
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			this.pictureList = fc.getAbsolutePathsRecursively();
			this.pictureIndex = (this.pictureList.length > 0) ? 0 : -1;
		}
	}

	void showCurrentPicture() {
		showPicture((pictureIndex >= 0) ? pictureList[pictureIndex] : null);
		setCommandsEnabled(new boolean [] {true, false, pictureIndex >= 0, pictureIndex >= 0, pictureIndex > 0, pictureIndex + 1 < pictureList.length});
	}
}
