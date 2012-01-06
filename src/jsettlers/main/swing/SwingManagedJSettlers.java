package jsettlers.main.swing;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import go.graphics.swing.AreaContainer;
import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.resources.ResourceManager;
import jsettlers.graphics.ISettlersGameDisplay;
import jsettlers.graphics.JOGLPanel;
import jsettlers.graphics.JoglLibraryPathInitializer;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.sound.SoundManager;
import jsettlers.main.ManagedJSettlers;

import javax.swing.JFrame;

public class SwingManagedJSettlers {

	static { // sets the native library path for the system dependent jogl libs
		JoglLibraryPathInitializer.initLibraryPath();

		ImageProvider provider = ImageProvider.getInstance();
		provider.addLookupPath(new File(
		        "/home/michael/.wine/drive_c/BlueByte/S3AmazonenDemo/GFX"));
		provider.addLookupPath(new File("D:/Games/Siedler3/GFX"));
		provider.addLookupPath(new File("C:/Program Files/siedler 3/GFX"));
	
		SoundManager.addLookupPath(new File(
		        "/home/michael/.wine/drive_c/BlueByte/S3AmazonenDemo/Snd"));
		SoundManager.addLookupPath(new File("D:/Games/Siedler3/Snd"));
		SoundManager.addLookupPath(new File("C:/Program Files/siedler 3/Snd"));
	}

	/**
	 * @param args
	 *            args can have no entries or <br>
	 *            args[0] must be "host" or "client"
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) {
		ResourceManager.setProvider(new ResourceProvider());
		ManagedJSettlers game = new ManagedJSettlers();
		game.start(getGui());

		// NetworkTimer.loadLogging("logs/2011_11_02-11_39_44.log");
		// NetworkTimer.activateLogging("logs");
	}

	private static ISettlersGameDisplay getGui() {
		JOGLPanel content = new JOGLPanel(new SwingSoundPlayer());
		JFrame jsettlersWnd = new JFrame("jsettlers");
		AreaContainer panel = new AreaContainer(content.getArea());
		panel.setPreferredSize(new Dimension(640, 480));
		jsettlersWnd.add(panel);
		panel.requestFocusInWindow();

		jsettlersWnd.pack();
		jsettlersWnd.setSize(1200, 800);
		jsettlersWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jsettlersWnd.setVisible(true);
		jsettlersWnd.setLocationRelativeTo(null);
		return content;
    }
}
