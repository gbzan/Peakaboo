package swidget.dialogues.fileio;


/*
 * @(#)ExampleFileFilter.java   1.16 04/07/26
 * 
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)ExampleFileFilter.java   1.16 04/07/26
 */

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out all files except for those type extensions that
 * it knows about.
 * 
 * Extensions are of the type ".foo", which is typically found on Windows and Unix boxes, but not on
 * Macinthosh. Case is ignored.
 * 
 * Example - create a new filter that filerts out all files but gif and jpg image files:
 * 
 * JFileChooser chooser = new JFileChooser(); ExampleFileFilter filter = new ExampleFileFilter( new
 * String{"gif", "jpg"}, "JPEG & GIF Images") chooser.addChoosableFileFilter(filter);
 * chooser.showOpenDialog(this);
 * 
 * @version 1.16 07/26/04
 * @author Jeff Dinkins
 */
public class SimpleFileFilter extends FileFilter
{

	// private static String TYPE_UNKNOWN = "Type Unknown";
	// private static String HIDDEN_FILE = "Hidden File";

	private Hashtable<String, FileFilter>	filters						= null;
	private String							description					= null;
	private String							fullDescription				= null;
	private boolean							useExtensionsInDescription	= true;



	private SimpleFileFilter()
	{
		this.filters = new Hashtable<String, FileFilter>();
	}




	/**
	 * Creates a file filter that accepts the given file type. 
	 * 
	 * Note that the "." before the extension is not needed. If provided, it will be ignored.
	 */
	public SimpleFileFilter(SimpleFileExtension extension)
	{
		this();
		
		setDescription(extension.getName());
		for (String e : extension.getExtensions()) {
			addExtension(e);
		}
	}



	/**
	 * Return true if this file should be shown in the directory pane, false if it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	 * @see #getExtension
	 * @see FileFilter#accepts
	 */
	@Override
	public boolean accept(File f)
	{
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Return the extension portion of the file's name .
	 * 
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public String getExtension(File f)
	{
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}

		}
		return null;
	}


	/**
	 * Adds a filetype "dot" extension to filter against.
	 * 
	 * For example: the following code will create a filter that filters out all files except those that end
	 * in ".jpg" and ".tif":
	 * 
	 * ExampleFileFilter filter = new ExampleFileFilter(); filter.addExtension("jpg");
	 * filter.addExtension("tif");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	public void addExtension(String extension)
	{
		if (filters == null) {
			filters = new Hashtable<String, FileFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}


	/**
	 * Returns the human readable description of this filter. For example:
	 * "JPEG and GIF Image Files (*.jpg, *.gif)"
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 * @see FileFilter#getDescription
	 */
	@Override
	public String getDescription()
	{
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration<String> extensions = filters.keys();
				if (extensions != null) {
					if (extensions.hasMoreElements()) {
						fullDescription += "." + extensions.nextElement();
					}
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + extensions.nextElement();
					}
				}
				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}


	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 */
	public void setDescription(String description)
	{
		this.description = description;
		fullDescription = null;
	}


	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in the human readable
	 * description.
	 * 
	 * Only relevent if a description was provided in the constructor or using setDescription();
	 * 
	 * @param b 
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see isExtensionListInDescription
	 */
	public void setExtensionListInDescription(boolean b)
	{
		useExtensionsInDescription = b;
		fullDescription = null;
	}


	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see setExtensionListInDescription
	 */
	public boolean isExtensionListInDescription()
	{
		return useExtensionsInDescription;
	}
}
