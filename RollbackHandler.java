package me.shooyudev.gladiator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

public class RollbackHandler {
	
	private static RollbackHandler rollbackHandler = new RollbackHandler();
	private Main main = Main.getPlugin(Main.class);

	private RollbackHandler() {
	}

	public static RollbackHandler getRollbackHandler() {
		return rollbackHandler;
	}
	
	public void register(String worldName) {
		String rootDirectory = main.getServer().getWorldContainer().getAbsolutePath();

		File srcFolder = new File(main.getDataFolder() + "/" + worldName);
		File destFolder = new File(rootDirectory + "/" + worldName);
		
		try {
			copyFolder(srcFolder, destFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new BukkitRunnable() {
			
			@Override
			public void run() {
				WorldCreator worldCreator = new WorldCreator(worldName);
				World map = worldCreator.createWorld();
				worldCreator.generateStructures(false);
				map.setAutoSave(false);
				map.setKeepSpawnInMemory(false);
				map.setAnimalSpawnLimit(1);
				map.setTime(0);
				map.setStorm(false);
				
			}
		}.runTaskLater(main, 20);
	}
	
	public void rollback(World world) {
		Bukkit.unloadWorld(world, false);
		String worldName = world.getName();
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try {
					rollback(worldName);
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("§cErro ao tentar criar o mapa.");
				}
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						WorldCreator worldCreator = new WorldCreator(worldName);
						World map = worldCreator.createWorld();
						worldCreator.generateStructures(false);
						map.setAutoSave(false);
						map.setKeepSpawnInMemory(false);
						map.setAnimalSpawnLimit(1);
						map.setTime(0);
						map.setStorm(false);
					}
				};
			}
		}.runTaskLater(main, 20);
	}

	public void rollback(String worldName) {
		String rootDirectory = main.getServer().getWorldContainer().getAbsolutePath();

		File srcFolder = new File(main.getDataFolder() + "/" + worldName);
		File destFolder = new File(rootDirectory + "/" + worldName);

		delete(destFolder);
		try {
			copyFolder(srcFolder, destFolder);
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	private void delete(File delete) {
		if (delete.isDirectory()) {
			String[] files = delete.list();

			if (files != null) {
				for (String file : files) {
					File toDelete = new File(file);
					delete(toDelete);
				}
			}
		} else {
			delete.delete();
		}
	}

	private void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			if (files != null) {
				for (String file : files) {
					// construct the src and dest file structure
					File srcFile = new File(src, file);
					File destFile = new File(dest, file);
					// recursive copy
					copyFolder(srcFile, destFile);
				}
			}
		} else {
			// if file, then copy it
			// Use byte stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

}
