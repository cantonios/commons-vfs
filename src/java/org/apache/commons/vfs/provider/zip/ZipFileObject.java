/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.zip;

import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A file in a Zip file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:19 $
 */
final class ZipFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final ZipEntry m_entry;
    private final ZipFile m_file;
    private final FileType m_type;
    private final HashSet m_children = new HashSet();

    public ZipFileObject( FileName name,
                          ZipEntry entry,
                          ZipFile zipFile,
                          ZipFileSystem fs )
    {
        super( name, fs );
        m_type = FileType.FILE;
        m_entry = entry;
        m_file = zipFile;
    }

    public ZipFileObject( final FileName name,
                          final boolean exists,
                          final ZipFileSystem fs )
    {
        super( name, fs );
        m_entry = null;
        m_file = null;
        if( exists )
        {
            m_type = FileType.FOLDER;
        }
        else
        {
            m_type = null;
        }
    }

    /**
     * Attaches a child
     */
    public void attachChild( FileName childName )
    {
        m_children.add( childName.getBaseName() );
    }

    /**
     * Returns true if this file is read-only.
     */
    protected boolean isReadOnly()
    {
        return true;
    }

    /**
     * Returns the file's type.
     */
    protected FileType doGetType()
    {
        return m_type;
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren()
    {
        return (String[])m_children.toArray( new String[ m_children.size() ] );
    }

    /**
     * Returns the size of the file content (in bytes).  Is only called if
     * {@link #doGetType} returns {@link FileType#FILE}.
     */
    protected long doGetContentSize()
    {
        return m_entry.getSize();
    }

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if  {@link #doGetType} returns {@link FileType#FILE}.  The input stream
     * returned by this method is guaranteed to be closed before this
     * method is called again.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return m_file.getInputStream( m_entry );
    }
}