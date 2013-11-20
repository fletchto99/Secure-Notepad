package me.matt.notepad.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SecureFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		return f.getName().toLowerCase().endsWith(".stxt");
	}

	@Override
	public String getDescription() {
		return "Secure Text Files";
	}

}
