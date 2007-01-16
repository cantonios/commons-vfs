/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.mime;

import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.FileObjectUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * A part of a MIME message.
 *
 * @author <a href="mailto:imario@apache.org">imario@apache.org</a>
 * @version $Revision$ $Date$
 */
public class MimeFileObject
	extends AbstractFileObject
	implements FileObject
{
	private Part part;
	private Map attributeMap;

	protected MimeFileObject(final FileName name,
							final Part part,
							final AbstractFileSystem fileSystem) throws FileSystemException
	{
		super(name, fileSystem);
		setPart(part);
	}
	/**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
		if (part == null)
		{
			if (!getName().equals(getFileSystem().getRootName()))
			{
				MimeFileObject foParent = (MimeFileObject) FileObjectUtils.getAbstractFileObject(getParent());
				setPart(foParent.findPart(getName().getBaseName()));
				return;
			}

			setPart(((MimeFileSystem) getFileSystem()).createCommunicationLink());
		}
	}

	private Part findPart(String partName) throws Exception
	{
		if (getType() == FileType.IMAGINARY)
		{
			// not existent
			return null;
		}

		if (isMultipart())
		{
			Multipart multipart = (Multipart)  part.getContent();
			if (partName.startsWith(MimeFileSystem.NULL_BP_NAME))
			{
				int partNumber = Integer.parseInt(partName.substring(MimeFileSystem.NULL_BP_NAME.length()), 10);
				if (partNumber < 0 || partNumber+1 > multipart.getCount())
				{
					// non existent
					return null;
				}

				return multipart.getBodyPart(partNumber);
			}

			for (int i = 0; i<multipart.getCount(); i++)
			{
				Part childPart = multipart.getBodyPart(i);
				if (partName.equals(childPart.getFileName()))
				{
					return childPart;
				}
			}
		}

		if (partName.equals(MimeFileSystem.CONTENT_NAME))
		{
			return (Part) part.getContent();
		}

		return null;
	}

	protected void doDetach() throws Exception
    {
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
		if (part == null)
		{
			return FileType.IMAGINARY;
		}

		/*
		if (!isMultipart())
		{
			// we cant have children ...
			return FileType.FILE;
		}
		*/

		// we have both
		return FileType.FILE_OR_FOLDER;
    }

	protected String[] doListChildren() throws Exception
	{
		return null;
	}

	/**
	 * Lists the children of the file.  Is only called if {@link #doGetType}
	 * returns {@link org.apache.commons.vfs.FileType#FOLDER}.
	 */
	protected FileObject[] doListChildrenResolved() throws Exception
	{
		if (part == null)
		{
			return null;
		}

		List vfs = new ArrayList();
		if (isMultipart())
		{
			Object container = part.getContent();
			if (container instanceof Multipart)
			{
				Multipart multipart = (Multipart) container;
				vfs = new ArrayList(multipart.getCount());

				for (int i = 0; i<multipart.getCount(); i++)
				{
					Part part = multipart.getBodyPart(i);

					String filename = UriParser.encode(part.getFileName());
					if (filename == null)
					{
						filename = MimeFileSystem.NULL_BP_NAME + i;
					}

					MimeFileObject fo = (MimeFileObject) FileObjectUtils.getAbstractFileObject(getFileSystem().resolveFile(
						getFileSystem().getFileSystemManager().resolveName(
							getName(),
							filename,
							NameScope.CHILD)));
					fo.setPart(part);
					vfs.add(fo);
				}
			}
		}
		else
		{
			Object content = part.getContent();
			if (content instanceof Part)
			{
				MimeFileObject fo = (MimeFileObject) FileObjectUtils.getAbstractFileObject(getFileSystem().resolveFile(
					getFileSystem().getFileSystemManager().resolveName(
						getName(),
						MimeFileSystem.CONTENT_NAME,
						NameScope.CHILD)));
				fo.setPart((Part) content);
				vfs.add(fo);
			}
		}

		return (MimeFileObject[]) vfs.toArray(new MimeFileObject[vfs.size()]);
	}

	private void setPart(Part part)
	{
		this.part = part;
		this.attributeMap = null;
	}

	/**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
		return part.getSize();
	}

    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
		Message mm = getMessage();
		if (mm == null)
		{
			return -1;
		}
		if (mm.getSentDate() != null)
		{
			return mm.getSentDate().getTime();
		}
		if (mm.getReceivedDate() != null)
		{
			mm.getReceivedDate();
		}
		return 0;
	}

	private Message getMessage() throws FileSystemException
	{
		if (part instanceof Message)
		{
			return (Message) part;
		}

		return ((MimeFileObject) FileObjectUtils.getAbstractFileObject(getParent())).getMessage();
	}

	/**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
		if (isMultipart())
		{
			// deliver the preamble as the only content

			String preamble = ((MimeMultipart) part.getContent()).getPreamble();
			return new ByteArrayInputStream(preamble.getBytes(MimeFileSystem.PREAMBLE_CHARSET));
		}

		return part.getInputStream();
	}

	boolean isMultipart() throws MessagingException
	{
		return part.getContentType() != null && part.getContentType().startsWith("multipart/");
	}

	protected FileContentInfoFactory getFileContentInfoFactory()
	{
		return new MimeFileContentInfoFactory();
	}

	protected Part getPart()
	{
		return part;
	}

	/**
	 * Returns all headers of this part.<br />
	 * The map key is a java.lang.String and the value is a:<br />
	 * <ul>
	 * <li>java.lang.Strings for single entries</li>
	 * or a
	 * <li>java.utils.List of java.lang.Strings for entries with multiple values</li>
	 * </ul>
	 */
	protected Map doGetAttributes() throws Exception
	{
		if (attributeMap == null)
		{
			if (part != null)
			{
				attributeMap = new MimeAttributesMap(part);
			}
			else
			{
				attributeMap = Collections.EMPTY_MAP;
			}
		}

		return attributeMap;
	}

	protected Enumeration getAllHeaders() throws MessagingException
	{
		return part.getAllHeaders();
	}
}