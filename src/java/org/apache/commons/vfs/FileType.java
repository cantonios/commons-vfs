/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/07/05 04:08:17 $
 */
package org.apache.commons.vfs;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;

/**
 * An enumeration that represents a file's type.
 */
public final class FileType
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( FileType.class );

    /**
     * A folder, which can contain other files, but does not have any data
     * content.
     */
    public static final FileType FOLDER = new FileType( REZ.getString( "folder.name" ) );

    /**
     * A regular file, which has data content, but cannot contain other files.
     */
    public static final FileType FILE = new FileType( REZ.getString( "file.name" ) );

    private final String m_name;

    private FileType( final String name )
    {
        m_name = name;
    }

    /** Returns the name of the type. */
    public String toString()
    {
        return m_name;
    }

    /** Returns the name of the type. */
    public String getName()
    {
        return m_name;
    }
}