/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.smb;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.FileSystem;
import org.apache.commons.vfs.provider.FileSystemProviderContext;

/**
 * A SMB file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:19 $
 */
public final class SmbFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    public SmbFileSystem( final FileSystemProviderContext context,
                          final FileName rootName )
    {
        super( context, rootName );
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( final FileName name ) throws FileSystemException
    {
        final String fileName = name.getURI();
        return new SmbFileObject( fileName, name, this );
    }
}