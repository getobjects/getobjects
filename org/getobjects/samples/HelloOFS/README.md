HelloOFS
========

Very simple OFS demo with no custom Java code. OFS stands for Object File
System, it is kinda the reverse of ZODB, it contructs Go objects out of the
filesystem.
Meaning you can drop files into a htdocs like directory and OFS will serve
them as a web application.

Important: When running this app the current directory needs to be this samples
folder. In Eclipse you can set the cwd in the run configuration to:

    ${workspace_loc:GETobjects/org/getobjects/samples/HelloOFS}


****Default Method 'index'

The lookup of OFS object in the 'web' folder. The default method in OFS is
called 'index'. Hence 'index.html' will be invoked if you access
http://localhost:8181/ w/o specifying any path.


****Frames

You'll notice that index.html is just an HTML fragment, but the content
delivered to the browser is a proper HTML file. This is because the
OFSApplication automatically wraps components in a 'Frame' component - if there
is one.

How/when does this happen? The result of the OFS lookup is the OFSComponentFile,
which gets invoked (it is an IGoCallable) and returns a WOComponent.
Next Go will look for a 'renderer' to render this result, and unless overidden,
OFSApplication will act as that (more precisely as a render factory).
It'll look for an OFS object called 'Frame'. If it exists it'll wrap the
component returned into this frame component.

FIXME: is this true? Doesn't look like:
Another thing which is special about Frame's is that they are *not* published
to the web. I.e. you cannot access http://localhost:8181/Frame.html or
Frame.goframe.


****File Extensions

Remember that the file extensions used on the Web are separate from the file
extensions used within OFS itself! Eg if you access /view.html, it might
actually load view.wo (or view.gif fwiw). And you can access the same component
via view.xml (and that component could adapt).
During lookup the extensions are cut off from the URL path. And during OFS
reconstruction the filesystem extensions are used to determine the class hosting
the OFS object.

Files ending in .html are loaded as WOComponent templates, not raw HTML files
(the extension is mapped to OFSComponentFile).

To deliver a raw HTML file w/o any processing, use '.rawhtml' as the extension
(mapped to OFSHtmlFile which is an OFSResourceFile). Again, you can still access
such as 'xyz.html' on the web!


****Hierarchies

HelloOFS is the root of the OFS database.

The 'web' directory becomes the root of the stuff exposed online. That is a
URL like http://localhost:8181/index.html will actually become 'web/index.html'
in OFS.

What this means is that you can put stuff above 'web' and it won't (directly)
be visible on the web. Note that it still can be acquired!
The top-level is a good place where you can put a Frame.goframe.
In case it isn't obvious, you can also additional folders at the top. Like
'HelloOFS/debug/info.wo'.


****Acquisition

FIXME: The GoObjectRequestHandler currently disables acquisition?!
Eg in our example the 'index' should also be inherited by the 'css' and 'images'
folders, but that does not work.

NOTE: There is a hack in run.java to enable acquisition.


****Reusable Components

Similar to the Frame which is automagically wrapped around html pages, the
html pages (which are just WOComponents) can embed other components explicitly.

ContextInfo.goframe, DBInfo.goframe and DBFolderInfo.goframe are examples of
such reusable components. They can be embedded by any component/page in the
hierarchy below by a simple:

  <#DBInfo />

Those become regular WOComponent's, so you can use all features of those. Which
includes bi-directional parameters (like <#DBInfo color=green/>) or embedded
content (like <#DBInfo>Some Content</#DBInfo>).


****Database Access

The demo also includes demos on how to connect to a database using Go's
EOAccess framework. The key filesystem objects here are:

  .godb - Go database, to configure DB connection parameters/model
  .gods - Go data source, represents a table.
  .godo - Go database object, a single object (like /persons/12345/)

Via the config subsystem those objects can be further configured. Eg you can
explicitly set the EOEntity (table) of a datasource via the EOEntity directive.
Or you can apply default qualifiers and sort orderings via the EOQualifier
directive. 
In the example the config subsystem is only used to configure the JDBC URL of
the PostgreSQL adaptor. The SQLite example works w/o any configuration (just by
following fallback conventions).


****Configuration

Configuration of OFS controllers can be done using config.htaccess files. Only
used for JDBC in this demo:

  config.htaccess
  EOAdaptorURL jdbc:postgresql://127.0.0.1/OGo?user=OGo&password=OGo

The configuration system works just like Apache .htaccess files and supports
many of the Apache directives (eg LocationMatch, AliasMatch, etc).

Note: You cannot name the files just .htaccess because that would lead to an
      empty OFS name (remember, OFS always splits the filename into name and
      extension)
